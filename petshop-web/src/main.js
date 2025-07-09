import { createApp } from 'vue'
import { createPinia } from 'pinia'
import './style.css'
import App from './App.vue'
import router from './router'
import 'element-plus/dist/index.css'
import { useAuthStore } from '@/stores/auth' // 👈 导入 Store

// 1. 创建 Pinia 实例
const pinia = createPinia()

// 2. 创建应用实例
const app = createApp(App)

// 3. 先注册 Pinia（确保状态管理在路由之前可用）
app.use(pinia)

// 4. 注册路由（路由守卫可能依赖 Pinia）
app.use(router)

// 5. 挂载到 DOM
app.mount('#app')

// 初始化认证状态
// 初始化 Store
const authStore = useAuthStore()
authStore.initialize().catch(error => {
    console.error('初始化失败:', error)
})


