<template>
  <el-card class="seller-chat-window">
    <template #header>
      <div class="chat-header">
        <el-button icon="ArrowLeft" @click="router.go(-1)">返回</el-button>
        <span>与 {{ buyerInfo.buyerName }} 的对话（{{ buyerInfo.petName }}）</span>
      </div>
    </template>

    <!-- 弹性容器包裹 -->
    <div class="chat-body">
      <div class="messages-container">
        <div
            v-for="msg in messages"
            :key="msg.id"
            :class="['message-item', { 'seller-message': msg.isMe }]"
        >
          <div class="message-content">{{ msg.content }}</div>
          <div class="message-time">{{ formatTime(msg.createdAt) }}</div>
        </div>
      </div>

      <!-- 固定输入区域 -->
      <div class="input-area">
        <el-input
            v-model="newMessage"
            type="textarea"
            :rows="2"
            placeholder="输入回复..."
            resize="none"
            @keyup.enter.native="sendMessage"
        />
        <div class="action-bar">
          <el-button type="primary" @click="sendMessage">发送</el-button>
          <el-button @click="markAsRead">标记已读</el-button>
        </div>
      </div>
    </div>
  </el-card>
</template>


<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getChatHistory, markMessagesRead } from '@/api/chatApi.js'
import chatClient from '@/utils/websocket'
import {ElMessage} from "element-plus";
import {jwtDecode} from "jwt-decode";
import router from "@/router/index.js";

const route = useRoute()
const buyerId = parseInt(route.params.buyerId)

// 组件状态
const messages = ref([])
const newMessage = ref('')
const buyerInfo = ref({
  buyerName: '买家',
  petName: '未知宠物'
})
// ✅ 用户ID解析函数
const getCurrentUserId = () => {
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.error('请先登录')
    router.push('/login')
    return null
  }

  try {
    const decoded = jwtDecode(token)
    const userId = decoded?.sub || decoded?.userId

    if (!userId || isNaN(userId)) {
      ElMessage.error('用户身份异常')
      return null
    }

    return Number(userId)
  } catch (error) {
    ElMessage.error('登录凭证无效')
    return null
  }
}

// 初始化聊天
const initChat = async () => {

  // 连接WebSocket
  await chatClient.connect(localStorage.getItem('token'), handleNewMessage)
  try {
    // 加载历史消息
    const { data } = await getChatHistory({
      sellerId:buyerId

    })
    messages.value = data.map(msg => ({
      ...msg,
      isMe: msg.senderId === getCurrentUserId()
    }))



    // 自动标记为已读
    await markAsRead()
  } catch (error) {
    ElMessage.error('初始化聊天失败')
  }
}

// 处理新消息
const handleNewMessage = (message) => {
  messages.value.push({
    ...message,
    isMe: message.senderId === getCurrentUserId()
  })
}

// 发送消息
const sendMessage = async () => {
  if (!newMessage.value.trim()) return

  try {
    chatClient.sendMessage({
      receiverId: buyerId,
      content: newMessage.value.trim()
    })

    // ✅ 重新加载消息（不清空页面状态）
    await initChat();
    newMessage.value = '';

  } catch (error) {
    ElMessage.error('消息发送失败')
  }
}

// 标记已读
const markAsRead = async () => {
  try {
    await markMessagesRead({
      sellerId: buyerId
    })
  } catch (error) {
    console.error('标记已读失败:', error)
  }
}


const formatTime = (isoTime) => {
  return new Date(isoTime).toLocaleString()
}

onMounted(initChat)
onUnmounted(() => chatClient.disconnect())
</script>

<style scoped>
.seller-chat-window {
  flex: 1;
  height: 100vh;
  border-radius: 0;

  .chat-header {
    display: flex;
    align-items: center;
    gap: 15px;
  }
}

.messages-container {
  height: calc(100vh - 200px);
  overflow-y: auto;
  padding: 15px;
  background: #f8f9fa;
}

.message-item {
  margin: 10px 0;
  padding: 10px 15px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  max-width: 75%;

  &.seller-message {
    margin-left: auto;
    background: #409eff;
    color: white;

    .message-time {
      color: rgba(255,255,255,0.8);
    }
  }
}

.message-content {
  font-size: 14px;
}

.message-time {
  font-size: 12px;
  color: #666;
  margin-top: 5px;
}

.input-area {
  margin-top: 20px;

  .action-bar {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    margin-top: 10px;
  }
}
</style>