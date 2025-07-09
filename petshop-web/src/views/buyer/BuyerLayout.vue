<!-- src/views/buyer/BuyerLayout.vue -->
<template>
  <div class="buyer-layout">
    <!-- 侧边导航 -->
    <el-menu
        :router="true"
        :default-active="$route.path"
        class="side-nav"
    >
      <el-menu-item index="/buyer/orders">
        <el-icon><Document /></el-icon>
        <span>订单管理</span>
      </el-menu-item>
    </el-menu>

    <!-- 主内容区 -->
    <div class="main-content">
      <router-view />
    </div>
  </div>
</template>

<script setup>
import { Document } from "@element-plus/icons-vue"
</script>

<style scoped>
.buyer-layout {
  display: flex;
  height: 100vh;
}
.side-nav {
  width: 200px;
  border-right: 1px solid #eee;
}
.main-content {
  flex: 1;
  padding: 20px;
}
</style>