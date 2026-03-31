<template>
  <div class="dashboard">
    <div class="page-header">
      <div class="page-title">
        <h1>数据概览</h1>
        <p>实时监控游戏运营数据</p>
      </div>
      <div class="page-actions">
        <el-button @click="refreshData" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>
    </div>
    
    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-header">
          <div class="stat-icon icon-users">👥</div>
          <div class="stat-trend up">
            <span>↑ 12%</span>
          </div>
        </div>
        <div class="stat-value">{{ formatNumber(stats.totalUsers) }}</div>
        <div class="stat-label">总用户数</div>
      </div>
      
      <div class="stat-card">
        <div class="stat-header">
          <div class="stat-icon icon-games">🎮</div>
          <div class="stat-trend up">
            <span>↑ 8%</span>
          </div>
        </div>
        <div class="stat-value">{{ formatNumber(gameStats.totalGames) }}</div>
        <div class="stat-label">总游戏次数</div>
      </div>
      
      <div class="stat-card">
        <div class="stat-header">
          <div class="stat-icon icon-score">🏆</div>
        </div>
        <div class="stat-value">{{ formatNumber(gameStats.highestScore) }}</div>
        <div class="stat-label">最高分记录</div>
      </div>
      
      <div class="stat-card">
        <div class="stat-header">
          <div class="stat-icon icon-orders">📦</div>
          <div class="stat-trend up">
            <span>↑ 15%</span>
          </div>
        </div>
        <div class="stat-value">{{ formatNumber(orderStats.totalOrders) }}</div>
        <div class="stat-label">总订单数</div>
      </div>
    </div>
    
    <!-- 今日数据 - 横向4卡片 -->
    <div class="today-cards">
      <div class="today-card">
        <div class="today-card-icon">🎮</div>
        <div class="today-card-content">
          <span class="today-card-value">{{ gameStats.todayGames || 0 }}</span>
          <span class="today-card-label">今日游戏</span>
        </div>
      </div>
      <div class="today-card">
        <div class="today-card-icon">📦</div>
        <div class="today-card-content">
          <span class="today-card-value">{{ orderStats.todayOrders || 0 }}</span>
          <span class="today-card-label">今日订单</span>
        </div>
      </div>
      <div class="today-card">
        <div class="today-card-icon">✅</div>
        <div class="today-card-content">
          <span class="today-card-value">{{ orderStats.paidOrders || 0 }}</span>
          <span class="today-card-label">已支付订单</span>
        </div>
      </div>
      <div class="today-card">
        <div class="today-card-icon">👤</div>
        <div class="today-card-content">
          <span class="today-card-value">{{ stats.todayUsers || 0 }}</span>
          <span class="today-card-label">新增用户</span>
        </div>
      </div>
    </div>
    
    <!-- 最近游戏记录 - 全宽 -->
    <div class="data-card recent-games-card">
      <div class="card-header">
        <span class="card-title">🎯 最近游戏记录</span>
        <router-link to="/games" class="view-all">查看全部 →</router-link>
      </div>
      <div class="card-body" style="padding: 0;">
        <el-table :data="recentGames.slice(0, 5)" size="default" class="game-table">
          <el-table-column prop="nickname" label="玩家" min-width="180">
            <template #default="{ row }">
              <div class="player-cell">
                <div class="player-avatar">{{ row.nickname?.charAt(0) || '?' }}</div>
                <span>{{ row.nickname || '匿名' }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="score" label="得分" align="center" min-width="120">
            <template #default="{ row }">
              <span class="score-value">{{ row.score?.toLocaleString() }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="level" label="关卡" align="center" min-width="100">
            <template #default="{ row }">
              <el-tag size="small">Lv.{{ row.level }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="duration" label="时长" align="center" min-width="100">
            <template #default="{ row }">
              {{ formatDuration(row.duration) }}
            </template>
          </el-table-column>
          <el-table-column prop="playedAt" label="时间" align="center" min-width="140">
            <template #default="{ row }">
              {{ formatTime(row.playedAt) }}
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { userApi, gameApi, orderApi } from '../api'

const loading = ref(false)
const stats = ref({ totalUsers: 0, todayUsers: 0 })
const gameStats = ref({ totalGames: 0, highestScore: 0, todayGames: 0 })
const orderStats = ref({ totalOrders: 0, paidOrders: 0, todayOrders: 0 })
const recentGames = ref([])

function formatNumber(num) {
  if (!num) return '0'
  return num.toLocaleString()
}

function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
}

function formatDuration(seconds) {
  if (!seconds) return '0s'
  if (seconds < 60) return `${seconds}s`
  return `${Math.floor(seconds / 60)}m ${seconds % 60}s`
}

async function refreshData() {
  loading.value = true
  try {
    const [usersRes, gameStatsRes, orderStatsRes, gamesRes] = await Promise.all([
      userApi.list({ pageNum: 1, pageSize: 1 }),
      gameApi.statistics(),
      orderApi.statistics(),
      gameApi.records({ pageNum: 1, pageSize: 8 })
    ])
    
    stats.value = { totalUsers: usersRes.data.total, todayUsers: 12 }
    gameStats.value = gameStatsRes.data
    orderStats.value = orderStatsRes.data
    recentGames.value = gamesRes.data.list || []
    ElMessage.success('数据刷新成功')
  } catch (e) {
    console.error(e)
    ElMessage.error('数据刷新失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => refreshData())
</script>

<style lang="scss" scoped>
// 今日数据 - 横向4卡片
.today-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.today-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.3s ease;
  
  &:hover {
    border-color: rgba(255, 215, 0, 0.3);
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
  }
  
  .today-card-icon {
    width: 48px;
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    background: linear-gradient(135deg, rgba(255, 215, 0, 0.15) 0%, rgba(255, 140, 0, 0.08) 100%);
    border-radius: 12px;
    flex-shrink: 0;
  }
  
  .today-card-content {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }
  
  .today-card-value {
    font-size: 28px;
    font-weight: 700;
    background: linear-gradient(135deg, var(--primary) 0%, #FF8C00 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    line-height: 1.2;
  }
  
  .today-card-label {
    font-size: 13px;
    color: var(--text-secondary);
    font-weight: 500;
  }
}

// 最近游戏记录卡片
.recent-games-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
  
  .card-body {
    flex: 1;
    overflow: hidden;
  }
}

.view-all {
  font-size: 13px;
  color: var(--text-secondary);
  text-decoration: none;
  transition: all 0.2s;
  padding: 6px 12px;
  border-radius: 6px;
  
  &:hover {
    color: var(--primary);
    background: rgba(255, 215, 0, 0.1);
  }
}

// 优化表格样式
:deep(.el-table) {
  --el-table-border-color: rgba(255, 255, 255, 0.06);
  --el-table-row-hover-bg-color: rgba(255, 215, 0, 0.05);
  
  .el-table__header th {
    background: rgba(255, 255, 255, 0.02) !important;
    font-weight: 600;
    color: var(--text-secondary);
    font-size: 12px;
    padding: 10px 0;
    height: auto;
  }
  
  .el-table__row td {
    padding: 10px 0;
    border-bottom: 1px solid rgba(255, 255, 255, 0.04);
    
    .cell {
      padding: 0 8px;
    }
  }
  
  .el-tag {
    background: linear-gradient(135deg, rgba(0, 212, 255, 0.15) 0%, rgba(0, 150, 200, 0.1) 100%);
    border: 1px solid rgba(0, 212, 255, 0.3);
    color: #00d4ff;
    font-weight: 600;
    border-radius: 6px;
    padding: 2px 8px;
    font-size: 12px;
  }
}

// 游戏记录表格
:deep(.game-table) {
  .el-table__header-wrapper,
  .el-table__body-wrapper {
    overflow-x: hidden;
  }
}

.player-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  
  .player-avatar {
    width: 32px;
    height: 32px;
    border-radius: 8px;
    background: linear-gradient(135deg, var(--primary) 0%, #FF8C00 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    font-weight: 700;
    color: #000;
    box-shadow: 0 3px 8px rgba(255, 215, 0, 0.25);
  }
}

.score-value {
  font-weight: 700;
  font-size: 14px;
  background: linear-gradient(135deg, var(--primary) 0%, #FF8C00 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
</style>
