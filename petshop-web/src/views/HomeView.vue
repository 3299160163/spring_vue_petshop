<template>
  <div class="home-container">

    <!-- 轮播图 -->
    <el-carousel :interval="5000" height="400px" class="carousel">
      <el-carousel-item v-for="(item, index) in carouselItems" :key="index">
        <img :src="item.image" :alt="item.title" class="carousel-image">
        <h3 class="carousel-title">{{ item.title }}</h3>
      </el-carousel-item>
    </el-carousel>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-select
          v-model="filterCategory"
          placeholder="所有类别"
          clearable
          @change="handleFilterChange"
      >
        <el-option
            v-for="category in categories"
            :key="category.value"
            :label="category.label"
            :value="category.value"
        />
      </el-select>

      <el-input
          v-model="searchText"
          placeholder="搜索宠物名称"
          clearable
          @keyup.enter="handleSearch"
          style="width: 300px; margin-left: 20px"
      />
    </div>

    <!-- 顶部分页 -->
    <div class="pagination-wrapper" v-if="totalPages > 1">
      <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="totalItems"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
      />
    </div>

    <!-- 错误提示 -->
    <el-alert
        v-if="error"
        :title="error"
        type="error"
        show-icon
        class="error-alert"
    />

    <!-- 宠物列表 -->
    <div class="pet-list">
      <div v-if="isLoading">加载中...</div>
      <div v-if="error" class="error">{{ error }}</div>
      <el-row v-if="!isLoading && !error" :gutter="20">
        <el-col
            v-for="pet in pets"
            :key="pet.id"
            :xs="24"
            :sm="12"
            :md="8"
            :lg="6"
        >
          <!-- 正确绑定当前循环的 pet -->
          <PetCard :pet="pet" class="pet-card" />
        </el-col>
      </el-row>
      <!-- 空数据提示 -->
      <div v-else-if="!isLoading" class="empty">暂无数据</div>
    </div>

    <!-- 底部分页 -->
    <div class="pagination-wrapper" v-if="totalPages > 1">
      <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="totalItems"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>


// 轮播图数据（可保持静态）
import petApi from "@/api/petApi.js";
import { ref } from 'vue';
import { useRouter } from 'vue-router';
const router = useRouter();

const carouselItems = ref([
  {
    title: '热门宠物推荐',
    image: '/images/c1.jpg'
  },
  {
    title: '新到萌宠',
    image: '/images/d1.jpg'
  }
])

// 响应式数据
const pets = ref([])
const filterCategory = ref('')
const searchText = ref('')
const isLoading = ref(false)
const error = ref(null)

// 分类选项（需与后端数据一致）
const categories = ref([
  { value: 'dog', label: '狗狗' },   // 改为小写保持前后端一致
  { value: 'cat', label: '猫咪' },
  { value: 'bird', label: '鸟类' }
])

// 生命周期钩子 - 初始化数据
onMounted(async () => {
  await fetchPets()
})

// 修改后的获取宠物方法
// 在组件中添加分页相关的响应式变量
const currentPage = ref(1)     // 当前页码（从1开始）
const pageSize = ref(10)       // 每页数量
const totalItems = ref(0)      // 总数据量
const totalPages = ref(1)      // 总页数

// 分页事件处理
const handlePageChange = (newPage) => {
  currentPage.value = newPage
}

const handleSizeChange = (newSize) => {
  pageSize.value = newSize
  currentPage.value = 1
}

const fetchPets = async () => {
  try {
    isLoading.value = true
    error.value = null

    //组合分页参数
    const params = {
      page: currentPage.value,  // 添加页码参数
      size: pageSize.value,     // 添加每页数量
      category: filterCategory.value,
      name: searchText.value.trim()
    }

    // 调用API并解构响应
    const response = await petApi.getPets(params)
    console.log('API原始数据:', response); // 调试关键点
    // 数据映射（假设后端返回 _id）
    pets.value = response.records.map(pet => ({
      id: pet.id, // 明确映射
      name: pet.name,
      age: pet.age
    }));

    // 更新数据和分页信息
    pets.value = response.records   // 使用分页对象中的records数组
    totalItems.value = response.total
    totalPages.value = response.pages

    // 自动校正页码（防止超出最大页数）
    currentPage.value = Math.min(currentPage.value, response.pages || 1)

  } catch (err) {
    error.value = `加载失败: ${err.message}`
    console.error('API Error:', err)

    // 失败时重置数据
    pets.value = []
    totalItems.value = 0
    currentPage.value = 1

  } finally {
    isLoading.value = false
  }
}

// 添加分页变化监听
watch([currentPage, pageSize], () => {
  fetchPets()
})

// 明确定义事件处理方法
const handleFilterChange = () => {
  console.log('分类筛选变化:', filterCategory.value)
  currentPage.value = 1
  fetchPets()
}

// 修改搜索功能（重置页码）
const handleSearch = () => {
  currentPage.value = 1
  fetchPets()
}

</script>


<style scoped>
.home-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.carousel {
  margin-bottom: 40px;
  border-radius: 8px;
  overflow: hidden;
}

.carousel-image {
  width: 100%;
  height: 400px;
  object-fit: cover;
}



.filter-bar {
  display: flex;
  justify-content: center;
  margin-bottom: 30px;
}


.error-alert {
  margin: 20px auto;
  max-width: 600px;
}

.pet-list {
  margin-top: 20px;
}

</style>
