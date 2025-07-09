<template>
  <div class="user-role-management">
    <!-- 用户列表 -->
    <el-card class="user-list">
      <el-table :data="userList" border>
        <el-table-column prop="id" label="用户ID" width="120" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button
                type="primary"
                size="small"
                @click="showRoleDialog(row)"
            >
              管理角色
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 角色管理对话框 -->
    <el-dialog
        v-model="roleDialogVisible"
        title="角色管理"
        width="600px"
    >
      <el-card>
        <!-- 当前角色列表 -->
        <div class="current-roles">
          <h4>当前角色：</h4>
          <el-tag
              v-for="role in currentRoles"
              :key="role.code"
              class="role-tag"
              closable
              @close="handleRemoveRole(role.code)"
          >
            {{ role.name }}
          </el-tag>
        </div>

        <!-- 分配新角色 -->
        <div class="assign-role">
          <el-select
              v-model="selectedRole"
              placeholder="选择新角色"
              filterable
          >
            <el-option
                v-for="role in allRoles"
                :key="role.code"
                :label="role.name"
                :value="role.code"
            />
          </el-select>
          <el-button
              type="primary"
              @click="handleAssignRole"
          >
            分配角色
          </el-button>
        </div>

        <!-- 更换角色 -->
        <div class="change-role">
          <el-select v-model="oldRole" placeholder="原角色">
            <el-option
                v-for="role in currentRoles"
                :key="role.code"
                :label="role.name"
                :value="role.code"
            />
          </el-select>
          <el-select v-model="newRole" placeholder="新角色">
            <el-option
                v-for="role in allRoles"
                :key="role.code"
                :label="role.name"
                :value="role.code"
            />
          </el-select>
          <el-button
              type="warning"
              @click="handleChangeRole"
          >
            更换角色
          </el-button>
        </div>
      </el-card>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getUserRoles,
  assignRole,
  deleteRole,
  changeRole
} from '@/api/admin/role.js'

// 模拟数据（实际应从接口获取）
const userList = ref([
  { id: 1, username: 'admin' },
  { id: 2, username: 'user1' }
])

const allRoles = ref([
  { code: 'ADMIN', name: '管理员' },
  { code: 'EDITOR', name: '编辑' },
  { code: 'USER', name: '普通用户' }
])

// 对话框状态
const roleDialogVisible = ref(false)
const currentUser = ref(null)
const currentRoles = ref([])

// 表单数据
const selectedRole = ref('')
const oldRole = ref('')
const newRole = ref('')

// 打开角色管理对话框
const showRoleDialog = async (user) => {
  currentUser.value = user
  try {
    const res = await getUserRoles(user.id)
    currentRoles.value = res.data
    roleDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取角色失败: ' + error.message)
  }
}

// 分配角色
const handleAssignRole = async () => {
  if (!selectedRole.value) {
    return ElMessage.warning('请选择角色')
  }

  try {
    await assignRole({
      userId: currentUser.value.id,
      roleCode: selectedRole.value
    })
    ElMessage.success('角色分配成功')
    await refreshRoles()
  } catch (error) {
    ElMessage.error('分配失败: ' + error.message)
  }
}

// 删除角色
const handleRemoveRole = (roleCode) => {
  ElMessageBox.confirm('确认删除该角色？', '警告', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteRole({
        userId: currentUser.value.id,
        roleCode
      })
      ElMessage.success('角色已移除')
      await refreshRoles()
    } catch (error) {
      ElMessage.error('删除失败: ' + error.message)
    }
  })
}

// 更换角色
const handleChangeRole = async () => {
  if (!oldRole.value || !newRole.value) {
    return ElMessage.warning('请选择原角色和新角色')
  }

  try {
    await changeRole({
      userId: currentUser.value.id,
      oldRoleCode: oldRole.value,
      newRoleCode: newRole.value
    })
    ElMessage.success('角色更换成功')
    await refreshRoles()
  } catch (error) {
    ElMessage.error('更换失败: ' + error.message)
  }
}

// 刷新角色列表
const refreshRoles = async () => {
  const res = await getUserRoles(currentUser.value.id)
  currentRoles.value = res.data
}
</script>

<style scoped>
.user-role-management {
  padding: 20px;
}

.role-tag {
  margin-right: 10px;
  margin-bottom: 10px;
}

.assign-role, .change-role {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}

.el-select {
  flex: 1;
}
</style>