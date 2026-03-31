<template>
  <div class="games-page table-page">
    <div class="page-header">
      <h2>游戏记录</h2>
    </div>
    
    <div class="stat-cards">
      <el-card class="stat-card">
        <div class="stat-title">总游戏次数</div>
        <div class="stat-value">{{ stats.totalGames || 0 }}</div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-title">最高分</div>
        <div class="stat-value">{{ stats.highestScore || 0 }}</div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-title">今日游戏</div>
        <div class="stat-value">{{ stats.todayGames || 0 }}</div>
      </el-card>
    </div>
    
    <el-card class="table-card">
      <div class="table-wrapper">
        <el-table :data="list" v-loading="loading" class="full-width-table">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="nickname" label="玩家" min-width="120">
            <template #default="{ row }">
              {{ row.nickname || '匿名' }}
            </template>
          </el-table-column>
          <el-table-column prop="score" label="得分" width="100" sortable />
          <el-table-column prop="level" label="关卡" width="80" />
          <el-table-column prop="duration" label="时长(秒)" width="100" />
          <el-table-column prop="playedAt" label="游戏时间" min-width="160">
            <template #default="{ row }">
              {{ formatTime(row.playedAt) }}
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
import { gameApi } from '../api'

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

function onSizeChange() {
  pageNum.value = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const [recordsRes, statsRes] = await Promise.all([
      gameApi.records({ pageNum: pageNum.value, pageSize: pageSize.value }),
      gameApi.statistics()
    ])
    list.value = recordsRes.data.list || []
    total.value = recordsRes.data.total || 0
    stats.value = statsRes.data
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => loadData())
</script>

