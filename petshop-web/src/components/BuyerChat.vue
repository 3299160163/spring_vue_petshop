<template>
  <div class="chat-container">
    <el-card class="chat-main">
      <template #header>
        <div class="chat-header">
          <el-button icon="ArrowLeft" @click="goBack">返回</el-button>
          <span>与卖家的对话（宠物ID: {{ petId }}）</span>
        </div>
      </template>

      <!-- 消息区域 -->
      <div class="messages-wrapper">
        <div
            v-for="msg in messages"
            :key="msg.id"
            :class="['message-item', { 'own-message': msg.isMe }]"
            @contextmenu.prevent="showContextMenu($event, msg.id)"
        >
          <div class="message-content">{{ msg.content }}</div>
          <div class="message-time">
            {{ formatTime(msg.createdAt) }}
          </div>

          <!-- 右键菜单 -->
          <div
              v-if="contextMenu.visible && contextMenu.messageId === msg.id"
              class="context-menu"
              :style="{ top: contextMenu.y + 'px', left: contextMenu.x + 'px' }"
          >
            <el-button
                type="danger"
                link
                @click.stop="handleDelete(msg.id)"
            >
              <el-icon><Delete /></el-icon>
              删除消息
            </el-button>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="input-area">
        <el-input
            v-model="newMessage"
            type="textarea"
            :rows="3"
            placeholder="输入消息..."
            @keyup.enter.native="send"
        />
        <el-button
            type="primary"
            class="send-btn"
            @click="send"
        >发送</el-button>
      </div>
    </el-card>
  </div>
</template>


<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, ArrowLeft } from '@element-plus/icons-vue'
import { jwtDecode } from 'jwt-decode'
import chatClient from '@/utils/websocket'
import instance from "@/utils/axios.js"

const router = useRouter()
const route = useRoute()

// 组件状态
const messages = ref([])
const newMessage = ref('')
const petId = ref(null)
const sellerId = ref(null)

// 右键菜单状态
const contextMenu = reactive({
  visible: false,
  x: 0,
  y: 0,
  messageId: null
})

// 路由处理
const getRouteParam = (param) => {
  return Array.isArray(param) ? param[0] : param
}

// 获取当前用户ID
const getCurrentUserId = () => {
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.error('请先登录')
    router.push('/login')
    return null
  }

  try {
    const decoded = jwtDecode(token)
    return Number(decoded?.sub || decoded?.userId)
  } catch (error) {
    ElMessage.error('登录凭证无效')
    return null
  }
}
// ✅ 定义返回方法
const goBack = () => {
  router.go(-1)
}
// 时间格式化
const formatTime = (isoTime) => {
  const date = new Date(isoTime)
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 自动滚动到底部
const scrollToBottom = async () => {
  await nextTick()
  const container = document.querySelector('.messages-wrapper')
  if (container) {
    container.scrollTop = container.scrollHeight
  }
}

// 初始化聊天
const initChat = async () => {
  try {
    petId.value = parseInt(getRouteParam(route.params.petId), 10)
    const buyerId = getCurrentUserId()

    if (!buyerId || isNaN(petId.value)) {
      ElMessage.error('参数错误')
      router.go(-1)
      return
    }

    // 获取宠物卖家信息
    const { data: petData } = await instance.get(`/pets/${petId.value}`)
    sellerId.value = petData.sellerId

    // 连接WebSocket
    await chatClient.connect(localStorage.getItem('token'), handleNewMessage)

    // 加载历史消息
    const { data } = await instance.get('/chat/messages', {
      params: { sellerId: sellerId.value, petId: petId.value }
    })

    messages.value = data.map(msg => ({
      ...msg,
      isMe: msg.senderId === buyerId
    }))

    await scrollToBottom()

  } catch (error) {
    ElMessage.error(`初始化失败: ${error.message}`)
  }
}

// 处理新消息
const handleNewMessage = (message) => {
  const buyerId = getCurrentUserId()
  messages.value.push({
    ...message,
    isMe: message.senderId === buyerId
  })
  scrollToBottom()
}

// 发送消息
const send = async () => {
  if (!newMessage.value.trim()) return

  try {
    const buyerId = getCurrentUserId()
    if (!buyerId) return

    await chatClient.sendMessage({
      receiverId: sellerId.value,
      petId: petId.value,
      content: newMessage.value.trim(),
      senderId: buyerId
    })

    newMessage.value = ''
    await scrollToBottom()

  } catch (error) {
    ElMessage.error('消息发送失败')
  }
}

// 右键菜单处理
const showContextMenu = (event, messageId) => {
  contextMenu.visible = true
  contextMenu.x = event.clientX
  contextMenu.y = event.clientY
  contextMenu.messageId = messageId
}

// 删除消息
const handleDelete = async (messageId) => {
  try {
    await ElMessageBox.confirm('确定要删除这条消息吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const currentUserId = getCurrentUserId()
    await instance.delete(`/chat/messages/${messageId}`, {
      params: { currentUserId }
    })

    messages.value = messages.value.filter(msg => msg.id !== messageId)
    ElMessage.success('删除成功')
    contextMenu.visible = false

  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

// 全局点击关闭菜单
const closeContextMenu = () => {
  contextMenu.visible = false
}

// 生命周期
onMounted(() => {
  initChat()
  document.addEventListener('click', closeContextMenu)
})

onUnmounted(() => {
  chatClient.disconnect()
  document.removeEventListener('click', closeContextMenu)
})
</script>


<style scoped>
.chat-container {
  height: 100vh;
  padding: 20px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0; /* ✅ 关键修复 */
}

.messages-wrapper {
  flex: 1;
  overflow-y: auto;
  min-height: 200px;
  height: 0; /* ✅ 强制高度计算 */
  padding: 10px;
  background: #f8f9fa;
  border-radius: 4px;
}

.input-area {
  position: sticky;
  bottom: 0;
  background: white;
  padding: 16px 0;
  z-index: 100;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.05);
}

.send-btn {
  margin-top: 10px;
  width: 100%;
}

.message-item {
  margin: 10px 0;
  padding: 12px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
  max-width: 85%;  /* ✅ 加宽消息容器 */
  min-width: 120px; /* 防止短消息变形 */
  word-break: break-word;
}

/* 新增关键样式（其余保持原样） */
.messages-wrapper {
  display: flex; /* ✅ 启用弹性布局 */
  flex-direction: column; /* ✅ 列方向排列 */
  gap: 12px; /* ✅ 消息间距 */
}

/* ✅ 修正样式穿透语法 */
:deep(.message-item.own-message) {
  align-self: flex-end; /* 替换margin-left实现更可靠右对齐 */
  background: #409eff;
  color: white;
  margin-left: unset; /* 清除旧属性影响 */

  .message-time {
    color: rgba(255,255,255,0.8);
  }
}

/* ✅ 卖家消息默认样式 */
.message-item {
  align-self: flex-start; /* 强制左对齐 */
  background: #fff;
}

.message-content {
  font-size: 14px;
  line-height: 1.5;
  white-space: pre-wrap; /* ✅ 保留换行符 */
}

.message-time {
  font-size: 12px;
  color: #666;
  margin-top: 8px;
  opacity: 0.8;
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 15px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eee;
}
</style>