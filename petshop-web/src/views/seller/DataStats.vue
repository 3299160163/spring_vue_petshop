<template>
  <div class="stats-container">
    <el-row :gutter="20">
      <!-- 今日销售额 -->
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span>今日销售额</span>
          </template>
          <div class="stat-item">
            <div class="value">¥ 8,500</div>
          </div>
        </el-card>
      </el-col>

      <!-- 本月订单数 -->
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span>本月订单</span>
          </template>
          <div class="stat-item">
            <div class="value">68 笔</div>
          </div>
        </el-card>
      </el-col>

      <!-- 进行中订单 -->
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span>进行中订单</span>
          </template>
          <div class="stat-item">
            <div class="value">12 笔</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.stat-item {
  text-align: center;
  padding: 20px 0;

  .value {
    font-size: 24px;
    color: #409EFF;
    margin-top: 10px;
  }
}
</style>