<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {buyerOrders, fetchOrders} from '@/api/orderApi.js'

const router = useRouter()

const orders = ref([])
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})
const loading = ref(false)

// 状态样式映射（键为中文状态）
const statusTypeMap = {
  '待处理': 'warning',
  '处理中': 'primary',
  '已完成': 'success',
  '已取消': 'danger'
}

const loadOrders = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.current,
      size: pagination.size
    }
    const res = await buyerOrders(params)
    orders.value = res.data.data
    pagination.total = res.data.total
  } catch (error) {
    ElMessage.error('数据加载失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

const handlePageChange = (val) => {
  pagination.current = val
  loadOrders()
}

// ✅ 使用命名路由（需在路由配置中定义 name）
const viewDetail = (orderNo) => {
  router.push({
    name: 'BuyerOrderDetail', // 路由配置中的 name
    params: { orderNo }       // 动态参数
  })
}

onMounted(() => {
  loadOrders()
})
</script>

<template>
  <div class="order-container">
    <el-skeleton :loading="loading" animated>
      <template #template>
        <el-skeleton-item variant="text" style="width: 30%" />
        <el-skeleton-item variant="text" />
        <el-skeleton-item variant="text" style="width: 20%" />
      </template>

      <el-table :data="orders" border style="width: 100%" v-loading="loading">
        <el-table-column prop="orderNo" label="订单号" width="200" />
        <el-table-column prop="petName" label="宠物名称" />

        <el-table-column label="金额" width="120">
          <template #default="{ row }">
            ¥{{ (row.amount || 0).toFixed(2) }}
          </template>
        </el-table-column>

        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag
                :type="statusTypeMap[row.status] || 'info'"
                size="small"
                effect="dark"
            >
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDetail(row.orderNo)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
          class="pagination"
          :current-page="pagination.current"
          :page-size="pagination.size"
          :total="pagination.total"
          @current-change="handlePageChange"
          layout="prev, pager, next"
      />
    </el-skeleton>
  </div>
</template>

<style scoped>
.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}

.el-table {
  margin-top: 20px;
}
</style>