// router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '@/views/LoginView.vue'
import HomeView from '@/views/HomeView.vue'
import { useAuthStore } from '@/stores/auth'
import RegisterView from "@/views/RegisterView.vue"; // 添加导入

const routes = [

    {
        path: '/',
        name: 'Home',
        component: HomeView,
        meta: { requiresAuth: true } // 👈 添加此行
    },
    {
        path: '/login',
        name: 'Login',
        component: LoginView,
        meta: { requiresAuth: false,hideNav: true} // 👈 明确标记无需认证  // 可选：在特定页面隐藏导航

    },
    {
        path: '/register',  // 访问路径
        name: 'Register',   // 路由名称
        component: RegisterView,
        meta: {
            requiresAuth: false,
            hideNav: true // ✅ 添加此行，隐藏导航栏
        }
    },


    // src/router/index.js
    {
        path: '/profile',
        name: 'UserProfile',
        component: () => import('@/views/UserProfile.vue'), // 检查路径是否正确
        meta: { requiresAuth: true } // 👈 需要登录
    },

    // 卖家中心路由组
    {
        path: '/seller',
        component: () => import('@/views/seller/SellerLayout.vue'),
        children: [
            // 默认重定向到商品管理
            {
                path: '',
                redirect: '/seller/products'
            },
            {
                path: 'products',
                name: 'SellerProducts',
                component: () => import('@/views/seller/ProductManagement.vue'),
                meta: { title: '商品管理' }
            },
            // 订单管理路由
            {
                path: 'orders',
                component: () => import('@/views/seller/OrderList.vue')
            },
            {
                path: '/orders/:orderNo', // 使用动态参数
                component: () => import('@/views/seller/OrderDetail.vue'),
                name: 'OrderDetail'
            },
            // 数据统计路由
            {
                path: 'stats',
                component: () => import('@/views/seller/DataStats.vue')
            },

            // ✅ 聊天相关路由
            {
                path: 'chat', // 主入口路径：/seller/chat
                component: () => import('@/views/seller/ChatList.vue'), // ChatList组件
                children: [
                    {
                        path: ':buyerId', // 嵌套路径：/seller/chat/123
                        component: () => import('@/views/seller/SellerChat.vue'),
                        props: true
                    }
                ]
            }
        ]
    },

    {
        path: '/pets/:id', // 动态参数 :id
        name: 'PetDetail',
        component: () => import('@/views/PetDetail.vue'), // 确保组件存在
        props: true // 启用 props 接收路由参数
    },
    {
        path: '/pets/:petId/order', // 从路径中捕获宠物ID
        name: 'CreateOrder',
        component: () => import('../components/OrderCreate.vue')
    },

    {
        path: '/chat/:sellerId/:petId',
        name: 'BuyerChat',
        component: () => import('@/components/BuyerChat.vue'),
        meta: { requiresAuth: true }
    },

    //买家
    {
        path: '/buyer',
        component: () => import('@/views/buyer/BuyerLayout.vue'),
        redirect: '/buyer/orders', // ✅ 新增重定向
        meta: { requiresAuth: true },
        children: [
            // 订单列表
            {
                path: 'orders',
                name: 'BuyerOrderList',
                component: () => import('@/views/buyer/OrderList.vue')
            },
            // 订单详情（动态参数）
            {
                path: 'orders/:orderNo',
                name: 'BuyerOrderDetail',
                component: () => import('@/views/buyer/OrderDetail.vue'),
                props: true // 启用 props 接收路由参数
            }
        ]
    },






    {
        path: '/admin',
        component: () => import('@/views/admin/AdminLayout.vue'),
        // 添加重定向规则
        redirect: '/admin/users', // 关键配置
        meta: {requiresAuth: true ,
            roles: ['ADMIN'] // 父级统一权限// 需要登录
        },
        children: [
            {
                path: 'users',
                name: 'UserManagement',
                component: () => import('@/views/admin/UserManagement.vue')
            },
            {
                path: 'roles',
                name: 'RoleManagement',
                component: () => import('@/views/admin/RoleManagement.vue')
            },
            {
                path: 'products',
                name: 'AdminProducts',
                component: () => import('@/views/admin/ProductManagement.vue'),
                meta: {
                    title: '商品管理',
                    // 可添加附加权限说明（继承父级ADMIN权限）
                    permissionDesc: '管理全平台商品'
                }
            },
            // 订单列表
            {
                path: 'orders',
                name: 'AdminOrders',
                component: () => import('@/views/admin/OrderList.vue'),
                meta: {
                    title: '订单管理'
                }
            },
            // 订单详情（动态路由）
            {
                path: 'orders/:orderNo', // 完整路径：/admin/orders/123456
                name: 'AdminOrderDetail',
                component: () => import('@/views/admin/OrderDetail.vue'),
                meta: {
                    title: '订单详情',
                },
                props: true // 启用路由参数传递
            }
        ]
    }

]


const router = createRouter({
    history: createWebHistory(),
    routes
})
// 全局前置守卫
// 全局前置守卫
router.beforeEach(async (to, from, next) => {
    const authStore = useAuthStore();

    try {
        // 【优化点1】用 initialize 替代原来的初始化（包含 token 有效性检查）
        await authStore.initialize();

        // 【可选项】调试用日志（如需保留）
        console.log('当前认证状态:', {
            isAuthenticated: authStore.isAuthenticated,
            user: authStore.user,
            roles: authStore.user?.roles || []
        });

        // 【核心修改】认证检查逻辑重构
        if (to.meta.requiresAuth) {
            // 【优化点2】改用 isAuthenticated 判断（基于 token + 用户数据）
            if (!authStore.isAuthenticated) {
                return next('/login');
            }

            // 【新增】角色权限验证 --------------------------
            if (to.meta.roles?.length) {
                const hasRole = to.meta.roles.some(role =>
                    authStore.hasRole(role)
                );

                if (!hasRole) {
                    return next('/forbidden'); // 需要提前定义 403 路由
                }
            }
        }

        next();
    } catch (error) {
        // 【新增】错误处理（如 token 过期等情况）
        console.error('路由守卫错误:', error);
        authStore.logout();
        next('/login');
    }
});

export default router;

// // 全局前置守卫
// router.beforeEach(async (to, from, next) => {
//     // 通过 Pinia 实例获取 Store
//     const authStore = useAuthStore()
//
//     try {
//         // 初始化认证状态
//         await authStore.initialize()
//
//         // 调试日志
//         console.log('[路由守卫] 认证状态:', authStore.isAuthenticated)
//         console.log('[路由守卫] 用户角色:', authStore.roles)
//
//         // 需要登录的页面
//         if (to.meta.requiresAuth) {
//             if (!authStore.isAuthenticated) {
//                 return next({
//                     path: '/login',
//                     query: { redirect: to.fullPath }
//                 })
//             }
//
//             // 需要特定角色
//             if (to.meta.requiredRoles) {
//                 const hasPermission = authStore.roles.some(role =>
//                     to.meta.requiredRoles.includes(role)
//                 )
//                 if (!hasPermission) return next('/403')
//             }
//         }
//
//         next()
//     } catch (error) {
//         console.error('路由守卫错误:', error)
//         next(false)
//     }
// })
//
// export default router