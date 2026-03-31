<template>
  <div class="page-container profile-page">
    <!-- 用户信息卡片 -->
    <div class="profile-card">
      <div class="profile-bg"></div>
      <div class="profile-content">
        <div class="avatar-wrapper">
          <div class="avatar">{{ user?.nickname?.charAt(0) || '?' }}</div>
          <div class="avatar-ring"></div>
        </div>
        <h2 class="nickname">{{ user?.nickname || '未设置昵称' }}</h2>
        <p class="username">@{{ user?.username }}</p>
      </div>
    </div>
    
    <!-- 统计数据 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">🏆</div>
        <div class="stat-info">
          <span class="stat-value">{{ bestScore.toLocaleString() }}</span>
          <span class="stat-label">最高分</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">🎮</div>
        <div class="stat-info">
          <span class="stat-value">{{ totalGames }}</span>
          <span class="stat-label">游戏次数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">🎒</div>
        <div class="stat-info">
          <span class="stat-value">{{ myItems.length }}</span>
          <span class="stat-label">道具数</span>
        </div>
      </div>
    </div>
    
    <!-- 最近游戏记录 -->
    <div class="section">
      <div class="section-header">
        <h3><span>📊</span> 最近游戏</h3>
      </div>
      <div class="records-list">
        <div v-for="record in records" :key="record.id" class="record-item">
          <div class="record-main">
            <span class="record-score">{{ record.score.toLocaleString() }}</span>
            <span class="record-label">分</span>
          </div>
          <div class="record-meta">
            <span class="record-level">Lv.{{ record.level }}</span>
            <span class="record-time">{{ formatTime(record.playedAt) }}</span>
          </div>
        </div>
        <div v-if="records.length === 0" class="empty-state">
          <p>暂无游戏记录</p>
          <router-link to="/game" class="btn btn-primary btn-sm">去玩一局</router-link>
        </div>
      </div>
    </div>
    
    <!-- 我的订单 -->
    <div class="section">
      <div class="section-header">
        <h3><span>📦</span> 我的订单</h3>
      </div>
      <div class="orders-list">
        <div v-for="order in orders" :key="order.id" class="order-item">
          <div class="order-main">
            <span class="order-no">{{ order.orderNo.slice(-8) }}</span>
            <span class="order-time">{{ formatTime(order.createdAt) }}</span>
          </div>
          <div class="order-right">
            <span class="order-amount">¥{{ order.amount }}</span>
            <span class="order-status" :class="getStatusClass(order.status)">
              {{ getStatusText(order.status) }}
            </span>
          </div>
        </div>
        <div v-if="orders.length === 0" class="empty-state">
          <p>暂无订单记录</p>
        </div>
      </div>
    </div>
    
    <!-- 退出登录 -->
    <button class="btn btn-ghost btn-block logout-btn" @click="handleLogout">
      <span>🚪</span> 退出登录
    </button>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { gameApi, itemApi, paymentApi } from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const user = computed(() => userStore.user)
const records = ref([])
const myItems = ref([])
const orders = ref([])
const bestScore = ref(0)
const totalGames = computed(() => records.value.length)

function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  
  return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
}

function getStatusClass(status) {
  return { 0: 'pending', 1: 'paid', 2: 'cancelled', 3: 'refunded' }[status] || ''
}

function getStatusText(status) {
  return { 0: '待支付', 1: '已支付', 2: '已取消', 3: '已退款' }[status] || '未知'
}

function handleLogout() {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}

onMounted(async () => {
  try {
    const [recordsRes, bestRes, itemsRes, ordersRes] = await Promise.all([
      gameApi.getMyRecords(10),
      gameApi.getMyBest(),
      itemApi.getMyItems(),
      paymentApi.getOrders(1, 10)
    ])
    
    records.value = recordsRes.data || []
    bestScore.value = bestRes.data?.score || 0
    myItems.value = itemsRes.data || []
    orders.value = ordersRes.data?.list || []
  } catch (e) {
    console.error(e)
  }
})
</script>

<style lang="scss" scoped>
.profile-page {
  // padding-bottom 由 Layout.vue 统一管理
}

.profile-card {
  position: relative;
  border-radius: var(--radius-xl);
  overflow: hidden;
  margin-bottom: 20px;
  
  .profile-bg {
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, rgba(255, 215, 0, 0.2) 0%, rgba(168, 85, 247, 0.2) 100%);
    
    &::after {
      content: '';
      position: absolute;
      inset: 0;
      background: var(--bg-card);
      opacity: 0.9;
    }
  }
  
  .profile-content {
    position: relative;
    z-index: 1;
    padding: 30px 20px;
    text-align: center;
  }
  
  .avatar-wrapper {
    position: relative;
    display: inline-block;
    margin-bottom: 16px;
    
    .avatar {
      width: 80px;
      height: 80px;
      border-radius: 50%;
      background: linear-gradient(135deg, var(--primary-color) 0%, var(--accent-purple) 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 32px;
      font-weight: 700;
      position: relative;
      z-index: 1;
    }
    
    .avatar-ring {
      position: absolute;
      inset: -4px;
      border-radius: 50%;
      border: 2px solid var(--primary-color);
      animation: pulse-ring 2s ease-in-out infinite;
    }
  }
  
  .nickname {
    font-size: 22px;
    font-weight: 700;
    margin-bottom: 4px;
  }
  
  .username {
    font-size: 14px;
    color: var(--text-muted);
  }
}

@keyframes pulse-ring {
  0%, 100% { opacity: 0.5; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.05); }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 24px;
}

.stat-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 16px 12px;
  text-align: center;
  transition: all var(--transition-normal);
  
  &:hover {
    border-color: var(--border-glow);
  }
  
  .stat-icon {
    font-size: 24px;
    margin-bottom: 8px;
  }
  
  .stat-info {
    display: flex;
    flex-direction: column;
    
    .stat-value {
      font-size: 20px;
      font-weight: 700;
      color: var(--primary-color);
    }
    
    .stat-label {
      font-size: 11px;
      color: var(--text-muted);
      margin-top: 2px;
    }
  }
}

.section {
  margin-bottom: 24px;
}

.section-header {
  margin-bottom: 14px;
  
  h3 {
    font-size: 16px;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 8px;
    
    span {
      font-size: 18px;
    }
  }
}

.records-list {
  .record-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 14px 16px;
    background: var(--bg-card);
    border: 1px solid var(--border-color);
    border-radius: var(--radius-md);
    margin-bottom: 8px;
    
    .record-main {
      .record-score {
        font-size: 20px;
        font-weight: 700;
        color: var(--primary-color);
      }
      
      .record-label {
        font-size: 12px;
        color: var(--text-muted);
        margin-left: 4px;
      }
    }
    
    .record-meta {
      text-align: right;
      
      .record-level {
        display: block;
        font-size: 13px;
        color: var(--secondary-color);
        margin-bottom: 2px;
      }
      
      .record-time {
        font-size: 11px;
        color: var(--text-muted);
      }
    }
  }
}

.orders-list {
  .order-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 14px 16px;
    background: var(--bg-card);
    border: 1px solid var(--border-color);
    border-radius: var(--radius-md);
    margin-bottom: 8px;
    
    .order-main {
      .order-no {
        display: block;
        font-size: 13px;
        font-family: monospace;
        color: var(--text-secondary);
        margin-bottom: 2px;
      }
      
      .order-time {
        font-size: 11px;
        color: var(--text-muted);
      }
    }
    
    .order-right {
      text-align: right;
      
      .order-amount {
        display: block;
        font-size: 16px;
        font-weight: 600;
        color: var(--primary-color);
        margin-bottom: 4px;
      }
      
      .order-status {
        font-size: 11px;
        padding: 3px 8px;
        border-radius: var(--radius-full);
        
        &.pending { background: rgba(245, 158, 11, 0.15); color: var(--warning); }
        &.paid { background: rgba(16, 185, 129, 0.15); color: var(--success); }
        &.cancelled { background: rgba(107, 114, 128, 0.15); color: var(--text-muted); }
        &.refunded { background: rgba(239, 68, 68, 0.15); color: var(--danger); }
      }
    }
  }
}

.empty-state {
  text-align: center;
  padding: 30px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  
  p {
    color: var(--text-muted);
    margin-bottom: 12px;
  }
}

.logout-btn {
  margin-top: 20px;
  
  span {
    margin-right: 8px;
  }
}
</style>
