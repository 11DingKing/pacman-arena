<template>
  <div class="orders-page table-page">
    <div class="page-header">
      <h2>订单管理</h2>
    </div>
    
    <div class="stat-cards">
      <el-card class="stat-card">
        <div class="stat-title">总订单数</div>
        <div class="stat-value">{{ stats.totalOrders || 0 }}</div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-title">已支付订单</div>
        <div class="stat-value">{{ stats.paidOrders || 0 }}</div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-title">今日订单</div>
        <div class="stat-value">{{ stats.todayOrders || 0 }}</div>
      </el-card>
    </div>
    
    <el-card class="table-card">
      <div class="table-wrapper">
        <el-table :data="list" v-loading="loading" class="full-width-table" height="100%">
          <el-table-column prop="orderNo" label="订单号" min-width="160" />
          <el-table-column prop="nickname" label="用户" min-width="100" />
          <el-table-column prop="itemName" label="道具" min-width="100" />
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column prop="amount" label="金额" width="90">
            <template #default="{ row }">¥{{ row.amount }}</template>
          </el-table-column>
          <el-table-column prop="payType" label="支付方式" width="100" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" min-width="160">
            <template #default="{ row }">
              {{ formatTime(row.createdAt) }}
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadData"
          @size-change="onSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { orderApi } from '../api'

const list = ref([])
const stats = ref({})
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

function formatTime(time) {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

function getStatusType(status) {
  const map = { 0: 'warning', 1: 'success', 2: 'info', 3: 'danger' }
  return map[status] || 'info'
}

function getStatusText(status) {
  const map = { 0: '待支付', 1: '已支付', 2: '已取消', 3: '已退款' }
  return map[status] || '未知'
}

function onSizeChange() {
  pageNum.value = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const [ordersRes, statsRes] = await Promise.all([
      orderApi.list({ pageNum: pageNum.value, pageSize: pageSize.value }),
      orderApi.statistics()
    ])
    list.value = ordersRes.data.list || []
    total.value = ordersRes.data.total || 0
    stats.value = statsRes.data
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => loadData())
</script>

