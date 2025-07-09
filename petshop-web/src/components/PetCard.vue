<template>
  <el-card class="pet-card" shadow="hover">
    <div class="image-container">
      <img
          :src="props.pet.coverImage || '/images/a1.jpg'"
          :alt="props.pet.name"
          class="cover-image"
      >
      <el-tag
          :type="statusMap[props.pet.status] || 'info'"
          class="status-tag"
      >
        {{ props.pet.status || '未知状态' }}
      </el-tag>
    </div>

    <div class="content">
      <h3 class="name">{{ props.pet.name }}</h3>

      <div class="meta-info">
        <el-tag type="info" size="small">
          {{ props.pet.category }}
        </el-tag>

        <div class="price">
          <span class="currency">¥</span>
          {{ formattedPrice }}
        </div>
      </div>

      <div class="attributes">
        <div class="attribute-item">
          <el-icon :color="genderConfig.color">
            <component :is="genderConfig.icon" />
          </el-icon>
          <span>{{ genderConfig.text }}</span>
        </div>

        <div class="attribute-item">
          <el-icon color="#67C23A"><Calendar /></el-icon>
          <span>{{ props.pet.age }} 岁</span>
        </div>
      </div>

      <div class="action-buttons">
        <el-button
            type="primary"
            size="small"
            @click="handlePurchase"
            :disabled="props.pet.status !== 'AVAILABLE'"
        >
          <el-icon class="icon"><Wallet /></el-icon>
          立即支付
        </el-button>

        <!-- 修复商品详情按钮 -->
        <el-button
            type="info"
            size="small"
            @click="handleViewDetail(props.pet)"
        >
        <el-icon class="icon"><Document /></el-icon>
        商品详情
        </el-button>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { defineProps, computed } from 'vue'
import { Calendar, Wallet, Document } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import {ElMessage} from "element-plus";
const router = useRouter()


const props = defineProps({
  pet: {
    type: Object,
    required: true,
    validator: (pet) => {
      return 'id' in pet; // 强制校验 id 存在
    }
  }
});

// 新增处理函数
const handlePurchase = () => {
  router.push(`/pets/${props.pet.id}/order`)
}

// ✅ 确保传递有效的 pet.id
const handleViewDetail = (pet) => {

  if (!pet?.id || isNaN(pet.id)) {
    ElMessage.error('宠物ID无效')
    return
  }
  router.push(`/pets/${pet.id}`) // ✅ 有效ID跳转
}

const statusMap = {
  available: 'success',
  sold: 'danger',
  reserved: 'warning'
}

const formattedPrice = computed(() =>
    (props.pet.price || 0).toFixed(2)
)

const genderConfig = computed(() => {
  switch(props.pet.gender) {
    case 1:
      return { icon: 'Male', text: '雄性', color: '#409EFF' }
    default:
      return { icon: 'Female', text: '雌性', color: '#F56C6C' }
  }
})
</script>

<style scoped>
.pet-card {
  width: 300px;
  margin: 15px;
  transition: transform 0.3s;
}

.pet-card:hover {
  transform: translateY(-5px);
}

.image-container {
  position: relative;
  height: 200px;
  overflow: hidden;
  border-radius: 4px;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.status-tag {
  position: absolute;
  top: 10px;
  right: 10px;
}

.content {
  padding: 15px;
}

.meta-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 12px 0;
}

.price {
  font-size: 18px;
  color: #e6a23c;
  font-weight: bold;
}

.attributes {
  display: flex;
  gap: 15px;
  margin: 15px 0;
}

.attribute-item {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #606266;
}

.action-buttons {
  display: flex;
  gap: 10px;
  margin-top: 15px;
}

.icon {
  margin-right: 5px;
}
</style>