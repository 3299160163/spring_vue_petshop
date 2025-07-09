<template>
  <div class="pet-detail">
    <!-- 加载状态 -->
    <el-skeleton v-if="loading" :rows="6" animated />

    <!-- 错误提示 -->
    <el-alert
        v-else-if="error"
        type="error"
        :title="error"
        show-icon
        closable
    />

    <!-- 数据展示 -->
    <div v-else>
      <h1>{{ pet.name }}</h1>

      <el-descriptions
          title="基础信息"
          :column="2"
          border
      >
        <el-descriptions-item label="分类">
          <el-tag effect="plain">{{ pet.category }}</el-tag>
        </el-descriptions-item>

        <el-descriptions-item label="价格">
          <span class="price">¥{{ formattedPrice }}</span>
        </el-descriptions-item>

        <el-descriptions-item label="性别">
          <el-icon :color="genderColor">
            <component :is="genderIcon" />
          </el-icon>
          {{ genderText }}
        </el-descriptions-item>

        <el-descriptions-item label="年龄">
          {{ pet.age }} 岁
        </el-descriptions-item>

        <el-descriptions-item label="状态">
          <el-tag :type="statusMap[pet.status.toLowerCase()]">
            {{ pet.status }}
          </el-tag>
        </el-descriptions-item>

        <el-descriptions-item label="上架时间">
          {{ formattedCreateTime }}
        </el-descriptions-item>
      </el-descriptions>
      <!-- 新增返回按钮 -->
      <div class="action-buttons">
        <el-button
            type="primary"
            plain
            @click="handleGoBack"
        >
          <el-icon><ArrowLeft /></el-icon>
          返回列表
        </el-button>
        <!-- 新增聊天按钮 -->
        <el-button
            type="success"
            @click="handleChat"
        >
          <el-icon><ChatLineRound /></el-icon>
          联系卖家
        </el-button>
      </div>
    </div>
      </div>
</template>

<script setup>
import {computed, onMounted, ref, watch} from 'vue'
import {useRoute} from 'vue-router'
import {ElMessage} from 'element-plus'
import {ArrowLeft, ChatLineRound, Female, Male} from '@element-plus/icons-vue'
import petApi from '@/api/petApi'
import router from "@/router/index.js";

const route = useRoute()
const petId = computed(() => {
  const id = Number(route.params.id) // 强制转换为数字
  return isNaN(id) ? null : id // 无效ID返回null
})


// 加载数据时校验ID
onMounted(async () => {
  if (!petId.value) {
    ElMessage.error('无效的宠物ID')
    return router.push('/error')
  }
  try {
    const res = await petApi.getPetDetail(petId.value)
    // ...处理数据
  } catch (error) {
    // ...错误处理
  }
})

const handleGoBack = () => {
  // 方案1：返回上一页
  router.go(-1)
  // 方案2：直接跳转到列表页（根据实际路由配置）
  // router.push('/pets')
}


// 响应式数据
const loading = ref(false)
const error = ref(null)
const pet = ref({
  id: 0,
  name: '',
  category: '',
  price: 0,
  status: 'AVAILABLE',
  gender: 0,
  age: 1,
  sellerId: 0,
  createTime: null

})

// 状态映射
const statusMap = {
  available: 'success',
  sold: 'danger',
  reserved: 'warning'
}

// 性别配置计算
const genderConfig = computed(() => {
  return pet.value.gender === 1
      ? { icon: Male, text: '雄性', color: '#409EFF' }
      : { icon: Female, text: '雌性', color: '#F56C6C' }
})

const genderIcon = computed(() => genderConfig.value.icon)
const genderText = computed(() => genderConfig.value.text)
const genderColor = computed(() => genderConfig.value.color)

// 价格格式化
const formattedPrice = computed(() =>
    Number(pet.value.price).toFixed(2)
)

// 时间格式化
const formattedCreateTime = computed(() => {
  return pet.value.createTime
      ? new Date(pet.value.createTime).toLocaleString()
      : '未知时间'
})

// 获取数据
const fetchPetData = async (id) => {
  try {
    loading.value = true
    error.value = null
    pet.value = await petApi.getPetDetail(id)
  } catch (err) {
    error.value = err.message
    ElMessage.error(err.message)
  } finally {
    loading.value = false
  }
}

// 初始化加载
onMounted(() => {
  if (route.params.id) fetchPetData(route.params.id)
})

// 监听路由参数变化
watch(
    () => route.params.id,
    (newId) => newId && fetchPetData(newId)
)
//处理聊天按钮点击
const handleChat = () => {
  router.push({
    name: 'BuyerChat',
    params: {
      sellerId: pet.value.sellerId, // 假设宠物数据包含sellerId
      petId: pet.value.id
    }
  })
}
</script>

<style scoped>
.pet-detail {
  max-width: 1200px;
  margin: 2rem auto;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}

.price {
  color: #f56c6c;
  font-weight: bold;
  font-size: 1.4rem;
}


</style>