// src/stores/auth.js
import { defineStore } from 'pinia';
import axios from '@/utils/axios';
import router from '@/router';
import { jwtDecode } from 'jwt-decode'; // 引入 JWT 解码库

export const useAuthStore = defineStore('auth', {
    state: () => ({
        user: null,
        token: localStorage.getItem('token') || null
    }),
    getters: {
        isAuthenticated: (state) => !!state.token,
        // 优先从 Token 解析实时角色，其次用用户信息接口数据
        hasRole: (state) => (role) => {
            try {
                const decoded = state.token ? jwtDecode(state.token) : null;
                return decoded?.roles?.includes(role) || state.user?.roles?.includes(role);
            } catch (e) {
                return state.user?.roles?.includes(role);
            }
        }
    },
    actions: {
        // 设置 Token 并解析角色
        setToken(token) {
            this.token = token;
            localStorage.setItem('token', token);
            this.parseTokenRoles(); // 立即解析角色
        },
        // 解析 Token 中的角色信息
        parseTokenRoles() {
            if (!this.token) return;
            try {
                const decoded = jwtDecode(this.token);
                // 更新用户角色（保留其他用户信息）
                this.user = {
                    ...this.user,
                    roles: decoded.roles || []
                };
            } catch (error) {
                console.error('Token 解析失败:', error);
            }
        },
        // 清除 Token 和用户信息
        clearAuth() {
            this.token = null;
            this.user = null;
            localStorage.removeItem('token');
        },
        // 登录（整合 Token 角色解析）
        async login(credentials) {
            try {
                const response = await axios.post('/auth/login', credentials);
                const { token } = response.data.data;
                this.setToken(token); // 触发角色解析
                await this.fetchUserProfile(); // 获取用户基本信息
                return true;
            } catch (error) {
                throw new Error('登录失败: ' + (error.response?.data?.message || error.message));
            }
        },
        // 退出登录
        logout() {
            this.clearAuth();
            router.push('/login');
        },
        // 初始化应用状态（Token 优先）
        async initialize() {
            if (this.token) {
                try {
                    this.parseTokenRoles(); // 先解析 Token 角色
                    await this.fetchUserProfile(); // 再补全用户信息
                } catch (error) {
                    console.error('初始化失败:', error);
                    this.clearAuth();
                }
            }
        },
        // 获取用户基本信息（不依赖角色字段）
        async fetchUserProfile() {
            try {
                const response = await axios.get('/users/profile');
                const userData = response.data.data;
                if (!userData?.id) throw new Error('用户数据无效');

                // ✅ 新增：保存用户ID到 localStorage
                localStorage.setItem('userId', String(userData.id));

                // 合并数据：保留 Token 解析的角色，更新其他信息
                this.user = {
                    ...this.user, // 包含已解析的 roles
                    id: userData.id,
                    username: userData.username,
                    email: userData.email,
                    phone: userData.phone
                };
            } catch (error) {
                console.error('获取用户信息失败:', error);
                throw error;
            }
        }
    }
});
