<template>
  <div class="user-profile-container">
    <el-card class="profile-card">
      <!-- 头像上传 -->
      <div class="avatar-upload">
        <el-upload
            class="avatar-uploader"
            :action="uploadAvatarUrl"
            :name="avatar"
            :show-file-list="false"
            :headers="uploadHeaders"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
        >
          <el-avatar :size="120" :src="userInfo.avatar || defaultAvatar" />
          <div class="avatar-mask">
            <el-icon :size="30"><Edit /></el-icon>
          </div>
        </el-upload>
        <h2>{{ userInfo.username }}</h2>
      </div>

      <!-- 个人信息表单 -->
      <el-form
          :model="userForm"
          :rules="formRules"
          ref="profileForm"
          label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" />
        </el-form-item>

        <el-form-item label="电子邮箱" prop="email">
          <el-input v-model="userForm.email" />
        </el-form-item>

        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="userForm.phone" />
        </el-form-item>

        <el-form-item>
          <el-button
              type="primary"
              @click="submitForm"
              :loading="submitting"
          >
            保存修改
          </el-button>
          <!-- 新增修改密码按钮 -->
          <el-button
              type="danger"
              @click="showPasswordDialog = true"
              style="margin-left: 12px"
          >
            修改密码
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 注册时间展示 -->
      <el-descriptions :column="1" border>
        <el-descriptions-item label="注册时间">{{ formatDate(userInfo.createTime) }}</el-descriptions-item>
      </el-descriptions>

      <!-- 新增密码修改对话框 -->
      <el-dialog
          v-model="showPasswordDialog"
          title="修改密码"
          width="500px"
      >
        <el-form
            :model="passwordForm"
            :rules="passwordRules"
            ref="passwordFormRef"
            label-width="100px"
        >
          <el-form-item label="旧密码" prop="oldPassword">
            <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                show-password
                placeholder="请输入当前密码"
            />
          </el-form-item>

          <el-form-item label="新密码" prop="newPassword">
            <el-input
                v-model="passwordForm.newPassword"
                type="password"
                show-password
                placeholder="6-20位字符"
            />
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                show-password
                placeholder="请再次输入新密码"
            />
          </el-form-item>
        </el-form>

        <template #footer>
          <el-button @click="showPasswordDialog = false">取消</el-button>
          <el-button
              type="primary"
              @click="handlePasswordSubmit"
              :loading="changingPassword"
          >
            确认修改
          </el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Edit } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { getUserProfile, updateUserProfile,updatePassword } from '@/api/userApi.js'

// 默认头像
import defaultAvatarImg from '@/assets/new-image.jpg'
import {ElMessage} from "element-plus";
const defaultAvatar = ref(defaultAvatarImg)

// 用户数据
const userInfo = ref({
  id: null,
  username: '加载中...',
  email: '',
  phone: '',
  avatar: '',
  createTime: null
})
// 新增密码相关状态
const showPasswordDialog = ref(false)
const changingPassword = ref(false)
const passwordFormRef = ref(null)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})


// 表单数据
const userForm = reactive({
  username: '',
  email: '',
  phone: ''
})

// 上传配置
const uploadAvatarUrl = ref(`${import.meta.env.VITE_API_BASE}/api/users/upload-avatar`)
const uploadHeaders = reactive({
  Authorization: `Bearer ${useAuthStore().token}`
})

// 表单验证规则
const formRules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
})
// 密码验证规则
const validatePasswordConfirm = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}
// 加载用户数据
onMounted(async () => {
  try {
    const { data } = await getUserProfile()
    Object.assign(userInfo.value, data)
    Object.assign(userForm, {
      username: data.username,
      email: data.email,
      phone: data.phone
    })
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
})

// 头像上传处理
const beforeAvatarUpload = (file) => {
  const isImage = ['image/jpeg', 'image/png'].includes(file.type)
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传 JPG/PNG 格式!')
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB!')
  }
  return isImage && isLt2M
}

const handleAvatarSuccess = (response) => {
  if (response.code === 200) {
    userInfo.value.avatar = response.data; // ✅ 直接使用 data
    ElMessage.success('头像更新成功');
  } else {
    ElMessage.error(response.msg || '头像更新失败');
  }
};
const passwordRules = reactive({
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
})
// 提交表单
const handlePasswordSubmit = async () => {
  try {
    await passwordFormRef.value.validate();

    changingPassword.value = true;
    const res = await updatePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    });

    // 确认响应结构
    const { code, msg } = res.data; // 根据实际结构调整
    console.log('Response code:', code, 'msg:', msg);

    if (code === 200) {
      ElMessage.success('密码修改成功');
      showPasswordDialog.value = false;
      passwordFormRef.value?.resetFields();
    } else {
      ElMessage.error(msg || '密码修改失败');
    }
  } catch (error) {
    console.error('提交出错:', error);
    if (error?.response?.data?.code) {
      ElMessage.error(error.response.data.msg);
    } else if (!error.toString().includes('validation')) {
      ElMessage.error('修改密码失败，请稍后重试');
    }
  } finally {
    changingPassword.value = false;
  }
};

// 时间格式化
const formatDate = (timestamp) => {
  return new Date(timestamp).toLocaleString()
}
</script>

<style scoped>
.user-profile-container {
  max-width: 800px;
  margin: 20px auto;
  padding: 20px;
}

.profile-card {
  padding: 20px;
}

.avatar-upload {
  text-align: center;
  margin-bottom: 30px;
  position: relative;
}

.avatar-uploader {
  display: inline-block;
  position: relative;
  cursor: pointer;
}

.avatar-mask {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  opacity: 0;
  transition: opacity 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.avatar-uploader:hover .avatar-mask {
  opacity: 1;
}
</style>