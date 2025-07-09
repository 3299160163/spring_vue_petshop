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
// 修改状态配置（仅添加allowedActions字段）
const statusConfig = {
  '待处理': {
    type: 'warning',
    allowedActions: ['confirm', 'cancel'] // 允许确认和取消
  },
  '处理中': {
    type: 'primary',
    allowedActions: ['complete', 'cancel'] // 允许完成和取消
  },
  '已完成': {
    type: 'success',
    allowedActions: [] // 不允许操作
  },
  '已取消': {
    type: 'danger',
    allowedActions: []
  }
};
// 计算属性
const statusText = computed(() => orderData.value.status || '未知状态');
const statusTagType = computed(() =>
    statusConfig[orderData.value.status]?.type || 'info'
);
// 新增计算属性（放在statusTagType下方）
const showConfirmButton = computed(() =>
    statusConfig[orderData.value.status]?.allowedActions?.includes('confirm')
);
const showCompleteButton = computed(() =>
    statusConfig[orderData.value.status]?.allowedActions?.includes('complete')
);
const showCancelButton = computed(() =>
    statusConfig[orderData.value.status]?.allowedActions?.includes('cancel')
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
    // 确保 action 是 confirm/cancel/complete 之一
    const validActions = ['confirm', 'cancel', 'complete'];
    if (!validActions.includes(action.toLowerCase())) {
      ElMessage.error('无效的操作类型');
      return;
    }
    // 新增：检查当前状态是否允许此操作
    const allowedActions = statusConfig[orderData.value.status]?.allowedActions || [];
    if (!allowedActions.includes(action)) {
      ElMessage.error('当前状态不允许此操作');
      return;
    }
    await ElMessageBox.confirm(
        `确认${action === 'confirm' ? '确认' : action === 'cancel' ? '取消' : '完成'}订单？`,
        '提示',
        { type: 'warning' }
    );

    // 调用接口时传递正确的 action 参数
    await updateOrderStatus(route.params.orderNo, action.toLowerCase());
    ElMessage.success('操作成功');
    await fetchOrderDetail(); // 刷新数据
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
        <el-button
            v-if="orderData.status === '待处理'"
            @click="handleOrderAction('confirm')"
        >
          确认订单
        </el-button>
        <el-button
            v-if="showCompleteButton"
            type="success"
            @click="handleOrderAction('complete')"
        >完成订单</el-button>

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