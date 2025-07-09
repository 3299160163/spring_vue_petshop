<template>
  <div class="seller-chat-container">
    <el-card class="chat-list">
      <template #header>
        <div class="card-header">
          <span>买家咨询列表</span>
        </div>
      </template>

      <el-table
          :data="chatList"
          highlight-current-row
          @row-click="handleRowClick"
          style="width: 100%"
      >
        <el-table-column prop="buyerName" label="买家" width="120" />
        <el-table-column prop="lastMessage" label="最后消息" show-overflow-tooltip />
      </el-table>
    </el-card>

    <!-- 嵌套路由出口 -->
    <router-view />
  </div>
</template>

<script setup>
import { ref, onMounted,onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { fetchChatList } from '@/api/chatApi.js'
import {ElMessage} from "element-plus";

const router = useRouter()
const chatList = ref([])
let intervalId = null; // 存储定时器 ID

// 加载聊天列表
const loadChatList = async () => {
  try {
    const { data } = await fetchChatList()
    chatList.value = data
  }
  catch (error) {
    ElMessage.error('加载会话列表失败')
  }
}

// 处理行点击
const handleRowClick = (row) => {
  router.push(`/seller/chat/${row.buyerId}`)
}

onMounted(() => {
  loadChatList()
  // 保留列表刷新逻辑
  intervalId = setInterval(loadChatList, 30000); // 记录定时器 ID
})
onUnmounted(() => {
  if (intervalId) {
    clearInterval(intervalId); // 组件销毁时清除定时器
  }
});
</script>

<style scoped>
.seller-chat-container {
  display: flex;
  height: 100vh;

  .chat-list {
    width: 400px;
    border-radius: 0;
    border-right: 1px solid #ebeef5;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }
}
</style>