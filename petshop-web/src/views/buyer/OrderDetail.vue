<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getOrderDetail, updateOrderStatus } from '@/api/orderApi';
import { useAuthStore } from '@/stores/auth';
import dayjs from 'dayjs';
import { jwtDecode } from 'jwt-decode';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const orderData = ref({
  orderNo: '',
  status: '',       // 改为 status，对应接口的 "已取消" 等字符串
  amount: 0,
  buyerId: null,
  buyerName: '',
  sellerId: null,
  sellerName: '',
  petName: '',
  createTime: null
});
const loading = ref(true);

// 状态配置
const statusConfig = {
  '待处理': { type: 'warning' },
  '处理中': { type: 'primary' },
  '已完成': { type: 'success' },
  '已取消': { type: 'danger' }
};
// 计算属性
const statusText = computed(() => orderData.value.status || '未知状态');
const statusTagType = computed(() =>
    statusConfig[orderData.value.status]?.type || 'info'
);

// 格式化方法
const formatAmount = (value) => {
  const num = Number(value || 0);
  return isNaN(num) ? '0.00' : num.toFixed(2);
};
const formatTime = (time) => time ? dayjs(time).format('YYYY-MM-DD HH:mm') : '--';

// 获取订单详情
const fetchOrderDetail = async () => {
  try {
    const response = await getOrderDetail(route.params.orderNo);
    const { data } = response.data; // 正确解构嵌套data

    orderData.value = {
      ...data,
      createTime: data.createTime || new Date().toISOString()
    };
  } catch (error) {
    ElMessage.error('获取详情失败: ' + error.message);
    await router.push('/seller/orders');
  } finally {
    loading.value = false;
  }
};

// 订单操作处理
// 操作处理（修复后）
const handleOrderAction = async (action) => {
  try {
    // 仅保留取消操作相关逻辑
    if (action === 'cancel') {
      await ElMessageBox.confirm(
          '确认取消订单？',
          '提示',
          { type: 'warning' }
      );

      await updateOrderStatus(route.params.orderNo, 'cancel');
      ElMessage.success('订单已取消');
      await fetchOrderDetail();
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败: ' + error.message);
    }
  }
};

// 初始化加载
onMounted(async () => {
  await fetchOrderDetail();

  // 权限校验
  const token = authStore.token;
  const decoded = token ? jwtDecode(token) : null;
  const isAdmin = decoded?.roles?.includes('ADMIN');
  const isOwner = authStore.user?.id === orderData.value.buyerId ||
      authStore.user?.id === orderData.value.sellerId;

  if (!isAdmin && !isOwner) {
    ElMessage.error('无权限查看此订单');
    await router.push('/seller/orders');
  }
});
</script>

<template>
  <div class="order-detail">
    <el-skeleton :loading="loading" animated>
      <template v-if="orderData.orderNo">
        <h2>{{ orderData.orderNo }}</h2>
        <el-tag :type="statusTagType">{{ statusText }}</el-tag>
        <p>金额: ¥{{ formatAmount(orderData.amount) }}</p>
        <p>买家: {{ orderData.buyerName }}</p>
        <p>卖家: {{ orderData.sellerName }}</p>
        <p>宠物: {{ orderData.petName }}</p>
        <p>时间: {{ formatTime(orderData.createTime) }}</p>

        <!-- 确认订单按钮：仅当状态为“待处理”时显示 -->
<!--        <el-button-->
<!--            v-if="orderData.status === '待处理'"-->
<!--            @click="handleOrderAction('confirm')"-->
<!--        >-->
<!--          确认订单-->
<!--        </el-button>-->

        <!-- 取消订单按钮：仅当状态为“待处理”或“处理中”时显示 -->
        <el-button
            v-if="orderData.status === '待处理' || orderData.status === '处理中'"
            type="danger"
            @click="handleOrderAction('cancel')"
        >
          取消订单
        </el-button>
      </template>
      <template v-else>
        <el-empty description="订单不存在" />
      </template>
    </el-skeleton>
  </div>
</template>