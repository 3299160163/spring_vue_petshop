<template>
  <div class="order-create-container">
    <el-card class="form-card">
      <template #header>
        <div class="card-header">
          <span>创建宠物订单</span>
        </div>
      </template>

      <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="120px"
          label-position="top"
          status-icon
      >
        <!-- 省市区选择 -->
        <el-form-item label="收货地区" prop="area">
          <el-cascader
              v-model="form.area"
              :options="areaOptions"
              :props="cascaderProps"
              placeholder="请选择省/市/区"
              style="width: 100%"
              clearable
          />
        </el-form-item>

        <!-- 详细地址 -->
        <el-form-item label="详细地址" prop="detail">
          <el-input
              v-model.trim="form.detail"
              placeholder="街道、门牌号等详细信息"
              :maxlength="100"
              show-word-limit
              clearable
          />
        </el-form-item>

        <!-- 联系电话 -->
        <el-form-item label="联系电话" prop="phone">
          <el-input
              v-model.trim="form.phone"
              placeholder="请输入11位手机号码"
              maxlength="11"
              clearable
              @input="handlePhoneInput"
          >
            <template #prepend>
              <span class="phone-prefix">+86</span>
            </template>
          </el-input>
        </el-form-item>

        <!-- 提交按钮 -->
        <el-form-item>
          <el-button
              type="primary"
              :loading="isSubmitting"
              @click="handleSubmit"
          >
            立即下单
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createOrder } from '@/api/orderApi.js'
import { useAuthStore } from '@/stores/auth.js'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// 表单引用
const formRef = ref()
const isSubmitting = ref(false)

// 表单数据
const form = reactive({
  area: [],
  detail: '',
  phone: ''
})

// 省市区配置
const cascaderProps = {
  expandTrigger: 'hover',
  label: 'label',
  value: 'value',
  children: 'children'
}

// 省市区数据（示例数据）
const areaOptions = [
  {
    label: '北京市',
    value: '110000',
    children: [
      {
        label: '北京市辖区',
        value: '110100',
        children: [
          { label: '东城区', value: '110101' },
          { label: '西城区', value: '110102' },
          // 其他区数据...
        ]
      }
    ]
  },
  // 其他省份数据...
]

// 表单验证规则
const rules = reactive({
  area: [
    {
      required: true,
      validator: (_, value, callback) => {
        if (value?.length !== 3) {
          callback(new Error('请选择完整的省市区信息'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  detail: [
    { required: true, message: '请输入详细地址', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        const fullAddress = [...form.area.map(item => item.label), value].join(' ')
        if (fullAddress.length > 200) {
          callback(new Error('地址总长度不能超过200个字符'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '手机号格式不正确',
      trigger: 'blur'
    }
  ]
})

// 手机号输入处理
const handlePhoneInput = (value) => {
  form.phone = value.replace(/\D/g, '')
}

// 提交订单
const handleSubmit = async () => {
  if (!authStore.isAuthenticated || !authStore.user?.id) {
    ElMessage.warning('请先登录');
    return router.push('/login');
  }

  try {
    const valid = await formRef.value.validate();
    if (!valid) return;

    isSubmitting.value = true;

    // 构建请求参数
    const payload = {
      buyerId: authStore.user?.id,
      petId: Number(route.params.petId),
      address: [
        ...form.area.map(item => item.label), // 确保地区数据含 label
        form.detail
      ].join(' '),
      phone: form.phone
    };

    const { data } = await createOrder(payload);
    ElMessage.success('订单创建成功！');
    await router.push({ name: 'BuyerOrderList' });
  } catch (error) {
    handleApiError(error);
  } finally {
    isSubmitting.value = false;
  }
};

// 错误处理
const handleApiError = (error) => {
  if (error.response) {
    const { status, data } = error.response;
    switch (status) {
      case 400:
        ElMessage.warning(data.message || '请求参数错误');
        break;
      case 401:
        ElMessage.error('登录已过期，请重新登录');
        authStore.logout();
        router.push('/login');
        break;
      case 409:
        ElMessage.error(data.message || '宠物已被购买');
        break;
      default:
        ElMessage.error(`请求错误：${status}`);
    }
  } else {
    ElMessage.error('网络连接异常');
  }
};

// 初始化校验
onMounted(async () => {
  if (!route.params.petId || isNaN(route.params.petId)) {
    ElMessage.error('无效的宠物信息');
    return router.back();
  }

  try {
    // 初始化用户信息
    await authStore.initialize();

    // 调试：打印用户数据
    console.log('当前用户信息:', authStore.user);

    if (!authStore.user?.id) {
      ElMessage.error('用户信息未加载');
      await router.push('/login');
    }
  } catch (error) {
    console.error('初始化失败:', error);
  }
});
</script>

<style scoped>
.order-create-container {
  max-width: 800px;
  margin: 20px auto;
  padding: 20px;
}

.form-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.phone-prefix {
  color: #666;
  font-size: 0.9em;
}

.el-form-item {
  margin-bottom: 24px;
}

.el-button {
  width: 100%;
  padding: 12px 24px;
  font-size: 16px;
}
</style>