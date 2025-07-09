<template>
  <div class="admin-layout">
    <!-- 侧边导航栏 -->
    <el-aside width="220px" class="admin-sidebar">
      <div class="sidebar-header">
        <h2>后台管理</h2>
      </div>
      <el-menu
          router
          :default-active="$route.path"
          background-color="#001529"
          text-color="#fff"
          active-text-color="#ffd04b"
      >
        <!-- 用户管理菜单 -->
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>

        <!-- 角色管理菜单 -->
<!--        <el-menu-item index="/admin/roles">-->
<!--          <el-icon><Lock /></el-icon>-->
<!--          <span>角色管理</span>-->
<!--        </el-menu-item>-->

        <!-- 新增商品管理菜单 -->
        <el-menu-item index="/admin/products">
          <el-icon><Goods /></el-icon>
          <span>商品管理</span>
        </el-menu-item>

        <!-- 新增订单管理菜单 -->
        <el-menu-item index="/admin/orders">
          <el-icon><List /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <div class="content-wrapper">
      <router-view />
    </div>
  </div>
</template>

<script setup>
// 新增图标导入
import { User, Lock, Goods, List } from '@element-plus/icons-vue'
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  display: flex;
  overflow: hidden; /* 隐藏全局滚动条 */
  background: #f0f2f5;
}

.admin-sidebar {
  background-color: #001529;
  width: 220px;
  height: 100vh;
  display: flex;
  flex-direction: column;
  /* 禁用侧边栏整体滚动 */
  overflow: hidden;
}

/* 菜单容器添加滚动 */
.el-menu {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
}

.content-wrapper {
  flex: 1;
  padding: 24px;
  overflow-y: auto; /* 主内容区独立滚动 */
  background-color: #fff;
  margin: 16px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
}

/* 其他原有样式保持不变 */
.sidebar-header {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}

.sidebar-header h2 {
  color: #fff;
  font-size: 18px;
  margin: 0;
}

/* 滚动条美化 */
.el-menu::-webkit-scrollbar,
.content-wrapper::-webkit-scrollbar {
  width: 6px;
}

.el-menu::-webkit-scrollbar-thumb {
  background: rgba(255,255,255,0.3);
}

.content-wrapper::-webkit-scrollbar-thumb {
  background: rgba(0,0,0,0.3);
}
</style>