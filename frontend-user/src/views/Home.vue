<template>
  <div class="page-container home">
    <div class="hero">
      <div class="hero-bg"></div>
      <div class="pacman-logo animate-float">
        <div class="pacman-character"></div>
      </div>
      <h1 class="title">PAC-MAN</h1>
      <p class="subtitle">经典街机游戏 · 全新体验</p>
      
      <div class="hero-stats">
        <div class="stat-item">
          <span class="stat-value">{{ formatNumber(totalPlayers) }}</span>
          <span class="stat-label">玩家</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-value">{{ formatNumber(totalGames) }}</span>
          <span class="stat-label">游戏局数</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-value">{{ formatNumber(highestScore) }}</span>
          <span class="stat-label">最高分</span>
        </div>
      </div>
    </div>
    
    <div class="action-section">
      <router-link to="/game" class="start-btn">
        <span class="btn-icon">🎮</span>
        <span class="btn-text">开始游戏</span>
        <span class="btn-arrow">→</span>
      </router-link>
    </div>
    
    <div class="section">
      <div class="section-header">
        <h3 class="section-title"><span class="title-icon">🏆</span>排行榜 TOP 5</h3>
        <router-link to="/ranking" class="view-all">查看全部</router-link>
      </div>
      
      <div class="ranking-preview">
        <div v-for="(item, index) in topRanking" :key="item.id" class="ranking-item" :style="{ animationDelay: index * 0.1 + 's' }">
          <div class="rank" :class="getRankClass(index)">
            {{ index + 1 }}
          </div>
          <div class="player-info">
            <div class="details">
              <div class="name">{{ item.nickname || '匿名玩家' }}</div>
              <div class="meta">关卡 {{ item.level }}</div>
            </div>
          </div>
          <div class="score">{{ formatNumber(item.score) }}</div>
        </div>
        <div v-if="topRanking.length === 0" class="empty-state">
          <div class="empty-icon">🎯</div>
          <p class="empty-text">暂无排行数据，快来创造记录！</p>
        </div>
      </div>
    </div>
    
    <div class="features">
      <div class="feature-card" v-for="feature in features" :key="feature.title">
        <div class="feature-icon">{{ feature.icon }}</div>
        <div class="feature-content">
          <h4>{{ feature.title }}</h4>
          <p>{{ feature.desc }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { gameApi } from '../api'

const topRanking = ref([])
const totalPlayers = ref(12580)
const totalGames = ref(89432)
const highestScore = ref(0)

const features = [
  { icon: '⚡', title: '经典玩法', desc: '原汁原味的街机体验' },
  { icon: '🎁', title: '道具系统', desc: '多种道具助你闯关' },
  { icon: '🏅', title: '全球排名', desc: '与全球玩家一较高下' },
  { icon: '💎', title: '成就系统', desc: '解锁专属成就徽章' }
]

function getRankClass(index) {
  return index === 0 ? 'gold' : index === 1 ? 'silver' : index === 2 ? 'bronze' : ''
}

function formatNumber(num) {
  if (!num) return '0'
  if (num >= 10000) return (num / 10000).toFixed(1) + 'w'
  if (num >= 1000) return (num / 1000).toFixed(1) + 'k'
  return num.toString()
}

onMounted(async () => {
  try {
    const res = await gameApi.getRanking(5)
    topRanking.value = res.data || []
    highestScore.value = topRanking.value[0]?.score || 0
  } catch (e) {
    console.error(e)
  }
})
</script>

<style lang="scss" scoped>
.home { padding-top: 0; }

.hero {
  position: relative;
  text-align: center;
  padding: 50px 0 40px;
  margin: -20px -16px 0;
  overflow: hidden;
  
  .hero-bg {
    position: absolute;
    inset: 0;
    background: radial-gradient(ellipse at 50% 0%, rgba(255, 215, 0, 0.15) 0%, transparent 60%),
                radial-gradient(ellipse at 20% 80%, rgba(0, 212, 255, 0.1) 0%, transparent 40%);
    z-index: 0;
  }
  > * { position: relative; z-index: 1; }
}

.pacman-logo {
  width: 100px;
  height: 100px;
  margin: 0 auto 20px;
  
  .pacman-character {
    width: 100%;
    height: 100%;
    background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
    border-radius: 50%;
    box-shadow: 0 0 40px rgba(255, 215, 0, 0.5), 0 0 80px rgba(255, 215, 0, 0.3);
    position: relative;
    
    &::after {
      content: '';
      position: absolute;
      top: 20%;
      left: 35%;
      width: 12px;
      height: 12px;
      background: #000;
      border-radius: 50%;
    }
  }
}

.title {
  font-size: 42px;
  font-weight: 800;
  letter-spacing: 6px;
  background: linear-gradient(135deg, #FFD700 0%, #FF8C00 50%, #FFD700 100%);
  background-size: 200% auto;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: shimmer 3s linear infinite;
}

@keyframes shimmer {
  0% { background-position: 0% center; }
  100% { background-position: 200% center; }
}

.subtitle {
  color: var(--text-secondary);
  font-size: 14px;
  margin-top: 8px;
  letter-spacing: 2px;
}

.hero-stats {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 30px;
  padding: 16px 24px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: var(--radius-xl);
  border: 1px solid var(--border-color);
  
  .stat-item {
    text-align: center;
    .stat-value { display: block; font-size: 22px; font-weight: 700; color: var(--primary-color); }
    .stat-label { font-size: 11px; color: var(--text-muted); text-transform: uppercase; letter-spacing: 1px; }
  }
  .stat-divider { width: 1px; height: 30px; background: var(--border-color); }
}

.action-section { margin: 30px 0; }

.start-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  width: 100%;
  padding: 20px 30px;
  background: linear-gradient(135deg, #FFD700 0%, #FF8C00 100%);
  border-radius: var(--radius-lg);
  text-decoration: none;
  color: #000;
  font-weight: 700;
  font-size: 18px;
  box-shadow: 0 8px 30px rgba(255, 215, 0, 0.4);
  transition: all var(--transition-normal);
  
  .btn-icon { font-size: 24px; }
  .btn-arrow { transition: transform var(--transition-fast); }
  
  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 12px 40px rgba(255, 215, 0, 0.5);
    .btn-arrow { transform: translateX(4px); }
  }
}

.section { margin-bottom: 30px; }

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  .title-icon { font-size: 20px; }
}

.view-all {
  color: var(--secondary-color);
  text-decoration: none;
  font-size: 13px;
  font-weight: 500;
  &:hover { color: var(--primary-color); }
}

// 首页排行榜预览
.ranking-preview {
  .ranking-item {
    display: flex;
    align-items: center;
    padding: 16px 24px;
    background: var(--bg-card);
    border: 1px solid var(--border-color);
    border-radius: var(--radius-md);
    margin-bottom: 10px;
    transition: all var(--transition-fast);
    
    &:hover {
      border-color: var(--border-glow);
    }
    
    .rank {
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 50%;
      margin-right: 14px;
      font-size: 14px;
      font-weight: 700;
      background: rgba(255, 255, 255, 0.08);
      color: var(--text-muted);
      flex-shrink: 0;
      
      &.gold {
        background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
        color: #000;
      }
      
      &.silver {
        background: linear-gradient(135deg, #E8E8E8 0%, #B8B8B8 100%);
        color: #333;
      }
      
      &.bronze {
        background: linear-gradient(135deg, #CD7F32 0%, #A0522D 100%);
        color: #fff;
      }
    }
    
    .player-info {
      flex: 1;
      min-width: 0;
      
      .details {
        .name {
          font-size: 15px;
          font-weight: 600;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
        .meta {
          font-size: 12px;
          color: var(--text-muted);
          margin-top: 2px;
        }
      }
    }
    
    .score {
      font-size: 18px;
      font-weight: 700;
      color: var(--primary-color);
      margin-left: 12px;
    }
  }
}

.features {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-top: 20px;
}

.feature-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  transition: all var(--transition-normal);
  
  &:hover { border-color: var(--border-glow); transform: translateY(-2px); }
  .feature-icon { font-size: 28px; flex-shrink: 0; }
  .feature-content {
    h4 { font-size: 14px; font-weight: 600; margin-bottom: 2px; }
    p { font-size: 11px; color: var(--text-muted); }
  }
}

// 响应式适配
@media (min-width: 576px) {
  .hero {
    padding: 60px 0 50px;
  }
  
  .pacman-logo {
    width: 120px;
    height: 120px;
  }
  
  .title {
    font-size: 52px;
    letter-spacing: 8px;
  }
  
  .subtitle {
    font-size: 16px;
  }
  
  .hero-stats {
    gap: 32px;
    padding: 20px 32px;
    
    .stat-item .stat-value { font-size: 26px; }
    .stat-item .stat-label { font-size: 12px; }
  }
  
  .start-btn {
    padding: 24px 40px;
    font-size: 20px;
    .btn-icon { font-size: 28px; }
  }
}

@media (min-width: 768px) {
  .hero {
    padding: 80px 0 60px;
    margin: -32px -24px 0;
  }
  
  .pacman-logo {
    width: 140px;
    height: 140px;
  }
  
  .title {
    font-size: 64px;
  }
  
  .hero-stats {
    display: inline-flex;
    gap: 40px;
    padding: 24px 48px;
  }
  
  .features {
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
  }
  
  .feature-card {
    flex-direction: column;
    text-align: center;
    padding: 24px 16px;
    
    .feature-icon { font-size: 36px; }
    .feature-content h4 { font-size: 15px; }
    .feature-content p { font-size: 12px; }
  }
}

@media (min-width: 992px) {
  .hero {
    padding: 100px 0 80px;
  }
  
  .pacman-logo {
    width: 160px;
    height: 160px;
  }
  
  .title {
    font-size: 72px;
    letter-spacing: 10px;
  }
  
  .subtitle {
    font-size: 18px;
  }
  
  .start-btn {
    max-width: 400px;
    margin: 0 auto;
  }
  
  .section-title {
    font-size: 22px;
  }
  
  .feature-card {
    padding: 32px 20px;
    .feature-icon { font-size: 42px; }
  }
}
</style>
