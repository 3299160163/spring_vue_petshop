<!-- src/App.vue -->
<template>
  <div class="app-container">
<!--    <NavBar v-if="showNavBar" />-->
    <NavBar v-if="showNav" />
    <div class="main-content">
      <router-view />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import NavBar from '@/components/NavBar.vue';

const route = useRoute();
const showNav = computed(() => !route.meta.hideNav)
// 根据路由路径决定是否显示 NavBar
// const showNavBar = computed(() => {
//   return route.path !== '/login'; // 登录页不显示
// });
</script>

<style>
/* 容器样式 */
.app-container {
  min-height: 100vh;    /* 确保占满整个视口高度 */
  display: flex;       /* 启用 Flex 布局 */
  flex-direction: column; /* 垂直排列 */
}

/* 主内容区域 */
.main-content {
  flex: 1;            /* 占据剩余空间 */
  padding: 20px;      /* 内容内边距 */
}
</style>