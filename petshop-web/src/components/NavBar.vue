<!-- src/components/NavBar.vue -->
<template>
  <div class="nav-container">
    <!-- 导航菜单 -->
    <el-menu
        mode="horizontal"
        :router="true"
        class="nav-menu"
        :default-active="activeMenu"
    >
      <el-menu-item index="/profile">
        <el-icon><User /></el-icon>
        <span>个人中心</span>
      </el-menu-item>
      <el-menu-item index="/">首页</el-menu-item>
      <el-menu-item index="/seller">卖家中心</el-menu-item>

      <!-- 买家中心（新增） -->
      <el-menu-item index="/buyer">
        <el-icon><ShoppingCart /></el-icon>
        <span>买家中心</span>
      </el-menu-item>

      <!-- 管理员模块显示控制 -->
      <el-menu-item
          v-if="showAdminModule"
          index="/admin"
      >
        <el-icon><Monitor /></el-icon>
        <span>管理员模块</span>
      </el-menu-item>
    </el-menu>

    <!-- 右侧功能区 -->
    <div class="right-actions">
      <el-button
          v-if="token"
          @click="handleLogout"
          type="danger"
      >
        退出登录
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { useRoute } from 'vue-router'
import {Monitor, ShoppingCart, User} from "@element-plus/icons-vue"
import { storeToRefs } from 'pinia'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const authStore = useAuthStore()

// 获取需要的响应式状态
const { token } = storeToRefs(authStore)

// 计算是否显示管理员模块
const showAdminModule = computed(() => {
  // 这里添加你的具体权限判断逻辑，例如：
  return authStore.hasRole('ADMIN') // 从 token 解析角色
})

// 动态高亮当前菜单
const activeMenu = ref('/')
watch(route, (newRoute) => {
  activeMenu.value = newRoute.matched[0]?.path || '/'
}, { immediate: true })

const handleLogout = () => {
  authStore.logout()
}
</script>

<style scoped>
/* 保持原有样式不变 */
.nav-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  background: #fff;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.nav-menu {
  flex: 1;
  border-bottom: none;
}

.el-menu--horizontal > .el-menu-item {
  height: 60px;
  line-height: 60px;
  margin: 0 15px;
  transition: all 0.3s;
}

.right-actions {
  margin-left: auto;
  padding-right: 20px;
}
</style>