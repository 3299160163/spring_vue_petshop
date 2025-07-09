// src/utils/axios.js
import axios from 'axios'
import { useAuthStore } from '@/stores/auth'

let authStore
const initializeStore = () => {
    if (!authStore) {
        authStore = useAuthStore()
    }
    return authStore
}
const instance = axios.create({
    baseURL:'/api',  // 代理到 vite 的 / → http://localhost:8080/api
    timeout: 15000,
    headers: {
        'Content-Type': 'application/json',

    }
})

// 请求拦截器（动态获取 Token）
// instance.interceptors.request.use(config => {
//     const store = initializeStore()
//     if (store.token) {
//         config.headers.Authorization = `Bearer ${store.token.trim()}`
//     }
//     return config
// })
instance.interceptors.request.use(config => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
})

// 响应拦截器
instance.interceptors.response.use(
    response => response,
    async error => {
        if (!error.response) {
            let message = '网络异常'
            if (error.code === 'ECONNABORTED') message = '请求超时'
            if (navigator.onLine === false) message = '网络连接已断开'

            return Promise.reject({
                code: 'NETWORK_ERROR',
                message,
                original: error
            })
        }

        const { status, config } = error.response
        const store = initializeStore()

        // 401 处理
        if (status === 401 && !isAuthEndpoint(config.url)) {
            store.clearToken()
            const router = await import('@/router')
            await router.default.push({
                path: '/login',
                query: {redirect: router.default.currentRoute.value.fullPath}
            })
        }

        // 统一错误格式
        const errorInfo = {
            code: error.response.data?.code || 'UNKNOWN_ERROR',
            message: error.response.data?.message || '请求失败',
            status: error.response.status,
            response: error.response
        }

        return Promise.reject(errorInfo)
    }
)

// 辅助函数：判断是否认证相关接口
function isAuthEndpoint(url) {
    return url.includes('/auth/login') || url.includes('/auth/refresh')
}



export default instance    //默认导出