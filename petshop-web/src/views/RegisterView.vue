<template>
  <div class="register-container">
    <div class="register-form">
      <h2 class="register-title">用户注册</h2>
      <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          label-width="100px"
          label-position="top"
      >
        <!-- 用户名 -->
        <el-form-item label="用户名" prop="username">
          <el-input
              v-model="registerForm.username"
              placeholder="请输入用户名"
              clearable
          />
        </el-form-item>

        <!-- 密码 -->
        <el-form-item label="密码" prop="password">
          <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码"
              show-password
              clearable
          />
        </el-form-item>

        <!-- 确认密码 -->
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              show-password
              clearable
          />
        </el-form-item>

        <!-- 邮箱 -->
        <el-form-item label="邮箱" prop="email">
          <el-input
              v-model="registerForm.email"
              placeholder="请输入邮箱"
              clearable
          />
        </el-form-item>

        <!-- 注册按钮 -->
        <el-form-item>
          <el-button
              type="primary"
              :loading="loading"
              class="register-btn"
              @click="handleRegister"
          >
            立即注册
          </el-button>
        </el-form-item>

        <!-- 已有账号登录 -->
        <div class="login-link">
          <span>已有账号？</span>
          <el-link type="primary" @click="$router.push('/login')">去登录</el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { createRouter } from 'vue-router'
import axios from "axios";

const router = useRouter()

// 表单引用
const registerFormRef = ref(null)

// 表单数据
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: ''
})

// 加载状态
const loading = ref(false)

// 表单验证规则
const registerRules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 16, message: '长度在 3 到 16 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 18, message: '长度在 6 到 18 个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
})

// 注册处理
const handleRegister = async () => {
  try {
    // 表单验证
    await registerFormRef.value.validate()

    loading.value = true

    // 发送注册请求
    const response = await axios.post('/api/auth/register', {
      username: registerForm.username,
      password: registerForm.password,
      email: registerForm.email
    })

    if (response.data.code === 200) {
      ElMessage.success('注册成功，请登录')
      await router.push('/login')
    } else {
      ElMessage.error(response.data.message || '注册失败')
    }
  } catch (error) {
    // 处理请求错误
    const errorMessage = error.response?.data?.message || '请求错误，请稍后重试'
    ElMessage.error(errorMessage)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.register-form {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.register-title {
  text-align: center;
  margin-bottom: 30px;
  color: #303133;
}

.register-btn {
  width: 100%;
  margin-top: 20px;
}

.login-link {
  text-align: center;
  margin-top: 20px;
  color: #909399;
}

.login-link span {
  margin-right: 8px;
}
</style>