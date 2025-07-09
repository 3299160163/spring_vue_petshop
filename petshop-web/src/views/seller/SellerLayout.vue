<template>
  <!-- 使用el-container包裹确保布局正确 -->
  <el-container class="seller-container">
    <!-- 头部 -->
    <el-header class="header" height="60px">
      <h1>卖家中心</h1>
    </el-header>

    <!-- 主体区域 -->
    <el-container>
      <!-- 侧边栏（启用路由模式） -->
      <el-aside width="200px" class="sidebar">
        <el-menu
            :default-active="$route.path"
            router
        >
          <el-menu-item index="/seller/products">
            <el-icon><icon-menu /></el-icon>
            <span>商品管理</span>
          </el-menu-item>
          <el-menu-item index="/seller/orders">
            <el-icon><icon-document /></el-icon>
            <span>订单管理</span>
          </el-menu-item>

          <!-- ✅ 新增聊天菜单项 -->
          <el-menu-item index="/seller/chat">
            <el-icon><icon-chat /></el-icon>
            <span>买家消息</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 内容区（路由视图容器） -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import {
  Menu as IconMenu,
  Document as IconDocument,
  DataAnalysis as IconDataAnalysis,
  ChatDotRound as IconChat
} from '@element-plus/icons-vue'
</script>

<style scoped>
.seller-container {
  height: 100vh;
}

.header {
  background-color: #409EFF;
  color: white;
  display: flex;
  align-items: center;
  border-bottom: 1px solid #eee;
}

.sidebar {
  background-color: #f5f5f5;
  border-right: 1px solid #e6e6e6;
}

.main-content {
  padding: 20px;
  background-color: white;
  height: calc(100vh - 60px);
  overflow-y: auto;
}

</style>