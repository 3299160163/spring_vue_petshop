<template>
  <div class="product-management">
    <h2>商品管理</h2>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-overlay">
      <el-icon class="is-loading"><Loading /></el-icon>
      数据加载中...
    </div>

    <!-- 错误提示 -->
    <el-alert
        v-if="error"
        :title="error"
        type="error"
        show-icon
        class="error-alert"
    />

    <!-- 上架按钮 -->
    <el-button type="primary" @click="showCreateDialog">上架新宠物</el-button>
    <el-button
        type="danger"
        :disabled="selectedPetIds.length === 0"
        @click="handleBatchDelete"
    >
      批量删除（{{ selectedPetIds.length }}）
    </el-button>

    <!-- 数据表格 -->
    <el-table
        :data="pets"
        border
        stripe
        v-loading="loading"
        empty-text="暂无商品数据"
        style="width: 100%"
        @selection-change="handleSelectionChange"
    row-key="id"
    >
    <!-- 新增多选列 -->
    <el-table-column
        type="selection"
        width="55"
        align="center"
    />


      <el-table-column label="封面图" width="120">
        <template #default="{ row }">
          <el-image
              v-if="row.coverImage"
              :src="row.coverImage"
              :preview-src-list="[row.coverImage]"
              style="width: 100px; height: 100px"
              fit="cover"
              hide-on-click-modal
          />
          <span v-else>无封面</span>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="名称" width="150" />
      <el-table-column prop="category" label="分类" width="120" />
      <el-table-column
          prop="price"
          label="价格"
          :formatter="formatPrice"
          width="120"
      />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag
              :type="row.status === 'AVAILABLE' ? 'success' : 'danger'"
              effect="dark"
          >
            {{ row.status === 'AVAILABLE' ? '可售' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="gender" label="性别" :formatter="formatGender" width="80" />
      <el-table-column prop="age" label="年龄" width="100" />
      <el-table-column prop="sellerId" label="卖家ID" width="120" />
      <el-table-column
          prop="createTime"
          label="创建时间"
          :formatter="formatDateTime"
          width="180"
      />

      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button
              type="primary"
              size="small"
              @click="handleEdit(row)"
              icon="Edit"
          >
            编辑
          </el-button>
          <el-button
              type="danger"
              size="small"
              @click="handleDelete(row.id)"
              icon="Delete"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>


    <!-- 上架对话框 -->
    <!-- 新增弹窗 -->
    <el-dialog
        v-model="createDialogVisible"
        title="上架新宠物"
        width="600px"
        @closed="resetCreateForm"
    >
      <el-form
          ref="createFormRef"
          :model="newPet"
          :rules="formRules"
          label-width="100px"
      >
        <!-- 复用编辑表单结构 -->
        <el-form-item label="宠物名称" prop="name">
          <el-input v-model="newPet.name" />
        </el-form-item>

        <el-form-item label="分类" prop="category">
          <el-select v-model="newPet.category">
            <el-option label="狗狗" value="DOG" />
            <el-option label="猫咪" value="CAT" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>

        <el-form-item label="价格" prop="price">
          <el-input-number
              v-model="newPet.price"
              :min="0"
              :precision="2"
          />
        </el-form-item>
        <!-- 新增：年龄 -->
        <el-form-item label="年龄" prop="age">
          <el-input-number
              v-model="newPet.age"
              :min="0"
              :max="30"
              controls-position="right"
              placeholder="请输入年龄"
          />
        </el-form-item>

        <!-- 新增：性别 -->
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="newPet.gender">
            <el-radio :label="0">雌性</el-radio>
            <el-radio :label="1">雄性</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 复用图片上传组件 -->
        <el-form-item label="封面图">
          <el-upload
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleFileChange"
              :before-upload="beforeUpload"
              accept="image/*"
          >
            <el-button type="primary">选择图片</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持JPG/PNG格式，大小不超过5MB
                <div v-if="previewImage" class="preview-wrapper">
                  <img :src="previewImage" class="preview-image" />
                  <el-icon class="remove-icon" @click="removeImage">
                    <CircleClose />
                  </el-icon>
                </div>
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button
            type="primary"
            :loading="submitting"
            @click="submitCreate"
        >
          立即上架
        </el-button>
      </template>
    </el-dialog>


    <!-- 新增：编辑对话框 -->
    <el-dialog
        v-model="editDialogVisible"
        title="编辑宠物信息"
        width="600px"
    >
      <el-form :model="editingPet" label-width="100px">
        <!-- 新增卖家ID字段（只读显示） -->
        <el-form-item label="卖家ID">
          <el-input
              v-model="editingPet.sellerId"
              disabled
              placeholder="系统自动关联"
          />
        </el-form-item>

        <el-form-item label="宠物名称">
          <el-input v-model="editingPet.name" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="editingPet.category">
            <el-option label="犬类" value="DOG" />
            <el-option label="猫类" value="CAT" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number
              v-model="editingPet.price"
              :min="0"
              :precision="2"
          />
        </el-form-item>
        <el-form-item label="封面图">
          <el-upload
              :show-file-list="false"
              :before-upload="handleBeforeUpload"
          >
            <el-button type="primary">更换图片</el-button>
          </el-upload>
          <el-image
              v-if="editingPet.coverImage"
              :src="editingPet.coverImage"
              style="width: 100px; margin-top: 10px"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 分页组件 -->
    <el-pagination
        class="pagination"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        :background="true"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="fetchPets"
        @size-change="fetchPets"
    />
  </div>
</template>

<script setup>
import {onMounted, ref,reactive } from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import { CircleClose } from '@element-plus/icons-vue'
import {Loading} from '@element-plus/icons-vue'
import instance from "@/utils/axios.js";
import {useAuthStore} from "@/stores/auth.js"; // 手动导入


// 响应式数据
const pets = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)
const error = ref(null)
const editDialogVisible = ref(false)
// 新增响应式数据
const selectedPetIds = ref([])

// 处理表格多选
const handleSelectionChange = (selection) => {
  selectedPetIds.value = selection.map(item => item.id)
}

// 批量删除处理
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
        `确定要删除选中的 ${selectedPetIds.value.length} 项商品吗？`,
        '警告',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }
    )

    const response = await instance.delete('/pets/batch', {
      params: { ids: selectedPetIds.value.join(',') }
    })

    if (response.data.code === 200) {
      ElMessage.success('删除成功')
      await fetchPets()
      selectedPetIds.value = []
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

// 定义文件对象
const imageFile = ref(null);

const editingPet = ref({
  name: '',
  category: 'DOG',
  price: 0,
  coverImage: ''
})

// 格式化方法
const formatPrice = (row) => {
  return `¥${row.price.toFixed(2)}`
}

const formatGender = (row) => {
  return row.gender === 1 ? '公' : '母'
}

const formatDateTime = (row) => {
  return new Date(row.createTime).toLocaleString()
}

// 数据获取
// fetchPets 方法优化版
const fetchPets = async () => {
  try {
    loading.value = true;
    const response = await instance.get('/pets', {
      params: { page: currentPage.value, size: pageSize.value }
    });

    // 严格数据验证
    if (
        !response.data?.records ||
        !Array.isArray(response.data.records) ||
        response.data.records.some(pet => typeof pet?.id !== 'number')
    ) {
      throw new Error('API返回数据格式异常');
    }

    // 保留响应式的数据赋值
    pets.value = response.data.records.map(pet => ({ ...pet }));
    total.value = response.data.total;

    // 调试输出
    console.log('处理后的宠物数据:', JSON.parse(JSON.stringify(pets.value)));

  } catch (err) {
    error.value = `数据加载失败: ${err.message}`;
    ElMessage.error(error.value);
  } finally {
    loading.value = false;
  }
};

// 初始化加载
onMounted(fetchPets)

// 打开编辑对话框
const handleEdit = (row) => {
  editingPet.value = {...row} // 深拷贝当前行数据
  editDialogVisible.value = true
}

// 图片上传处理
// 文件选择回调（获取文件对象）
const handleBeforeUpload = (file) => {
  imageFile.value = file; // 存储文件
  return false; // 阻止自动上传
};

// 提交编辑（使用 FormData）
const submitEdit = async () => {
  try {
    // 1. 创建 FormData 对象
    const formData = new FormData();

    // 2. 添加 JSON 数据（字段名需与后端 @RequestPart("pet") 匹配）
    formData.append('pet', new Blob([JSON.stringify(editingPet.value)], {
      type: 'application/json'
    }));

    // 3. 添加图片文件（如果有）
    if (imageFile.value) { // imageFile 是文件输入框绑定的变量
      formData.append('image', imageFile.value);
    }

    // 4. 发送请求
    // 发送请求（关键修正点：手动设置 multipart 类型）
    await instance.put(`/pets/${editingPet.value.id}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data' // 必须显式指定
      }
    });

    // 5. 更新本地数据
    const index = pets.value.findIndex(p => p.id === editingPet.value.id);
    if (index !== -1) {
      pets.value.splice(index, 1, editingPet.value);
    }

    ElMessage.success('修改成功');
    editDialogVisible.value = false;

  } catch (error) {
    ElMessage.error(`修改失败: ${error.response?.data?.message || error.message}`);
  }
};

// 删除（仅替换为 apiClient）
const handleDelete = async (id) => {
  try {
    await instance.delete(`/pets/${id}`) // 关键修改点
    ElMessage.success('删除成功')
    // 方案1: 明确忽略 Promise（推荐）
    await fetchPets() // 刷新数据
  } catch (err) {
    ElMessage.error(`删除失败: ${err.response?.data?.message || err.message}`)
  }
}



// 弹窗控制
const createDialogVisible = ref(false)
const createFormRef = ref(null)

// 表单数据（包含新增字段）
const newPet = reactive({
  name: '',
  category: 'DOG',
  price: 0,
  sellerId: 1, // 根据实际获取方式调整
  age: 0,      // 新增年龄
  gender: 0    // 新增性别
})

// 图片处理（复用编辑逻辑）
const previewImage = ref('')

// 提交状态
const submitting = ref(false)

// 表单校验规则（扩展新字段）
const formRules = reactive({
  name: [
    { required: true, message: '请输入宠物名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在2到20个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' },
    { type: 'number', min: 0, message: '价格不能小于0', trigger: 'blur' }
  ],
  age: [
    { required: true, message: '请输入年龄', trigger: 'blur' },
    { type: 'number', min: 0, max: 30, message: '年龄需在0-30之间', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ]
})

// 打开弹窗
const showCreateDialog = () => {
  createDialogVisible.value = true
}

// 图片处理（复用编辑逻辑）
const handleFileChange = (file) => {
  imageFile.value = file.raw
  previewImage.value = URL.createObjectURL(file.raw)
}

const removeImage = () => {
  imageFile.value = null
  previewImage.value = ''
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB!')
    return false
  }
  return true
}

// 重置表单（复用编辑逻辑）
const resetCreateForm = () => {
  createFormRef.value?.resetFields()
  imageFile.value = null
  previewImage.value = ''
}

// 提交创建（复用编辑的FormData逻辑）
const submitCreate = async () => {
  try {
    await createFormRef.value.validate()
    // 🔴 移除硬编码，动态获取用户ID
    const authStore = useAuthStore()
    const sellerId = authStore.user?.id
    if (!sellerId) {
      ElMessage.error('用户信息异常，请重新登录')
      return
    }
    const petData = {
      ...newPet,
      age: Number(newPet.age),    // 确保数字类型
      gender: Number(newPet.gender)
    }

    // 构建FormData（与编辑功能结构一致）
    const formData = new FormData()
    formData.append('pet', new Blob([JSON.stringify(petData)], {
      type: 'application/json'
    }))
    if (imageFile.value) {
      formData.append('image', imageFile.value)
    }

    // 发送POST请求（唯一不同点）
    const response = await instance.post('/pets', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    ElMessage.success('上架成功')
    createDialogVisible.value = false
    await fetchPets() // 刷新列表

  } catch (error) {
    ElMessage.error(`上架失败: ${error.response?.data?.message || error.message}`)
  } finally {
    submitting.value = false
  }
}

</script>

<style scoped>

.el-upload__tip {
  color: #666;
  font-size: 12px;
  margin-top: 8px;
}

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}

.loading-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 999;
}

.error-alert {
  margin-bottom: 20px;
}
</style>