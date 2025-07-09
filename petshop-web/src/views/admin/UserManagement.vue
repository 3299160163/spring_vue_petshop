<!-- @/views/admin/UserManagement.vue -->
<template>
  <div class="user-management">
    <!-- 操作工具栏 -->
    <div class="mb-4">
      <el-button type="primary" @click="handleCreate">新建用户</el-button>
    </div>

    <!-- 用户表格 -->
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="username" label="用户名" width="150" />
      <el-table-column prop="email" label="邮箱" width="220" />
      <el-table-column prop="roles" label="角色">
        <template #default="{ row }">
          <el-tag
              v-for="role in row.roles"
              :key="role"
              class="mr-2"
              type="primary"
          >
            {{ role }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button
              size="small"
              type="danger"
              @click="handleDelete(row.id)"
          >删除</el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="mt-4 flex justify-end">
      <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          @current-change="fetchUsers"
          layout="total, prev, pager, next"
      />
    </div>

    <!-- 用户表单对话框 -->
    <el-dialog
        v-model="dialogVisible"
        :title="formTitle"
        width="600px"
        @closed="resetForm"
    >
      <el-form
          :model="form"
          :rules="rules"
          ref="formRef"
          label-width="100px"
          label-position="right"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" />
        </el-form-item>

        <el-form-item
            label="密码"
            prop="password"
            v-if="formType === 'create'"
        >
          <el-input
              v-model="form.password"
              type="password"
              show-password
          />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>

        <el-form-item label="角色" prop="roles">
          <el-select
              v-model="form.roles"
              multiple
              placeholder="请选择角色"
              style="width: 100%"
          >
            <el-option
                v-for="role in allRoles"
                :key="role.value"
                :label="role.label"
                :value="role.value"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import instance from '@/utils/axios'

// 表格数据
const tableData = ref([])
const loading = ref(false)

// 分页配置
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 表单相关
const dialogVisible = ref(false)
const formType = ref('create') // create/edit
const formRef = ref(null)
const form = reactive({
  id: null,
  username: '',
  password: '',
  email: '',
  roles: []
})

// 角色选项（示例数据，需替换为实际接口获取）
const allRoles = ref([
  { value: 'ADMIN', label: '管理员' },
  { value: 'SELLER', label: '卖家' },
  { value: 'BUYER', label: '买家' }
])

// 表单验证规则
const rules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  roles: [
    { required: true, message: '请至少选择一个角色', trigger: 'change' }
  ]
})

// 对话框标题
const formTitle = computed(() =>
    formType.value === 'create' ? '新建用户' : '编辑用户'
)

// 生命周期
onMounted(() => {
  fetchUsers()
})

// 获取用户列表
const fetchUsers = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.current,
      size: pagination.size
    }
    const res = await instance.get('/admin/users', { params })
    tableData.value = res.data.data.records
    pagination.total = res.data.data.total
  } catch (error) {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 打开创建对话框
const handleCreate = () => {
  formType.value = 'create'
  dialogVisible.value = true
}

// 打开编辑对话框
const handleEdit = (row) => {
  formType.value = 'edit'
  Object.assign(form, {
    id: row.id,
    username: row.username,
    password: '', // 密码不显示
    email: row.email,
    roles: [...row.roles]
  })
  dialogVisible.value = true
}

// 提交表单
const submitForm = async () => {
  try {
    await formRef.value.validate()

    const payload = { ...form }
    if (formType.value === 'edit') {
      delete payload.password // 编辑时不传密码
    }

    const api = formType.value === 'create'
        ? await instance.post('/admin/users', payload)
        : await instance.put(`/admin/users/${form.id}`, payload)

    await api
    ElMessage.success('操作成功')
    dialogVisible.value = false
    await fetchUsers()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

// 删除用户
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确认删除该用户？', '警告', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await instance.delete(`/admin/users/${id}`)
    ElMessage.success('删除成功')
    await fetchUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 重置表单
const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(form, {
    id: null,
    username: '',
    password: '',
    email: '',
    roles: []
  })
}
</script>

<style scoped>
.user-management {
  padding: 20px;
  background: #fff;
  border-radius: 4px;
}

.el-tag {
  margin: 2px;
}
</style>