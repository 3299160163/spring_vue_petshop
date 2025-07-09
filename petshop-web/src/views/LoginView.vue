<template>
  <div class="login-container">
    <div class="login-card">
      <!-- 品牌展示 -->
      <div class="brand-header">
        <img src="@/assets/pet-logo.png" alt="宠物商店" class="logo">
        <h1 class="title">宠物交易平台</h1>
      </div>

      <!-- 使用Element Plus组件重构表单 -->
      <el-form
          ref="loginForm"
          :model="form"
          @submit.prevent="handleSubmit"
          class="login-form"
      >
        <!-- 用户名输入 -->
        <el-form-item>
          <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              prefix-icon="el-icon-user"
              size="large"
          />
        </el-form-item>

        <!-- 密码输入 -->
        <el-form-item>
          <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="el-icon-lock"
              size="large"
              show-password
          />
        </el-form-item>

        <!-- 登录按钮 -->
        <el-button
            type="primary"
            native-type="submit"
            :loading="loading"
            class="login-btn"
            size="large"
        >
          {{ loading ? '登录中...' : '立即登录' }}
        </el-button>

        <!-- 辅助链接 -->
        <div class="form-footer">
          <router-link to="/register" class="link-text">
            立即注册
          </router-link>
          <router-link to="/forgot-password" class="link-text">
            忘记密码？
          </router-link>
        </div>

        <!-- 错误提示 -->
        <el-alert
            v-if="error"
            :title="error"
            type="error"
            show-icon
            class="error-alert"
        />
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const form = ref({ username: '', password: '' })
const loading = ref(false)
const error = ref(null)

const handleSubmit = async () => {
  try {
    loading.value = true
    error.value = null

    const success = await authStore.login(form.value)
    if (success) {
      await router.push({ name: 'Home' })
    }
  } catch (err) {
    error.value = err.message || '登录失败，请检查网络连接'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  padding: 2rem;
}

.login-card {
  background: white;
  width: 100%;
  max-width: 420px;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.1);
  overflow: hidden;
  transition: transform 0.3s ease;

  &:hover {
    transform: translateY(-5px);
  }
}

.brand-header {
  background: #409EFF;
  padding: 2.5rem;
  text-align: center;
  color: white;

  .logo {
    width: 80px;
    margin-bottom: 1rem;
    filter: drop-shadow(0 2px 4px rgba(0,0,0,0.1));
  }

  .title {
    font-size: 1.8rem;
    margin: 0;
    letter-spacing: 1px;
  }
}

.login-form {
  padding: 2.5rem;

  ::v-deep .el-input__inner {
    border-radius: 12px;
    height: 48px;
    font-size: 1rem;
  }
}

.login-btn {
  width: 100%;
  border-radius: 12px;
  font-weight: 600;
  letter-spacing: 1px;
  transition: all 0.3s ease;

  &:hover {
    opacity: 0.9;
    transform: scale(0.98);
  }
}

.form-footer {
  margin-top: 1.5rem;
  display: flex;
  justify-content: space-between;

  .link-text {
    color: #606266;
    font-size: 0.9rem;
    transition: color 0.3s;

    &:hover {
      color: #409EFF;
    }
  }
}

.error-alert {
  margin-top: 1.5rem;
  border-radius: 8px;
}

@media (max-width: 480px) {
  .login-card {
    border-radius: 0;
    box-shadow: none;
  }

  .brand-header {
    padding: 1.5rem;

    .logo {
      width: 60px;
    }

    .title {
      font-size: 1.4rem;
    }
  }

  .login-form {
    padding: 1.5rem;
  }
}
</style>