<template>
  <div class="page-container ranking-page">
    <div class="page-header">
      <h1 class="page-title">🏆 排行榜</h1>
      <p class="page-subtitle">全球玩家实时排名</p>
    </div>

    <!-- 我的排名卡片 -->
    <div v-if="myBest" class="my-rank-card">
      <div class="my-rank-header">
        <span class="my-rank-label">我的最高分</span>
        <span class="my-rank-position" v-if="myRankPosition"
          >#{{ myRankPosition }}</span
        >
      </div>
      <div class="my-rank-content">
        <div class="my-rank-avatar">
          {{ userStore.user?.nickname?.charAt(0) || "?" }}
        </div>
        <div class="my-rank-info">
          <span class="my-rank-name">{{
            userStore.user?.nickname || "我"
          }}</span>
          <span class="my-rank-meta"
            >关卡 {{ myBest.level }} · {{ formatTime(myBest.playedAt) }}</span
          >
        </div>
        <div class="my-rank-score">{{ myBest.score.toLocaleString() }}</div>
      </div>
    </div>

    <!-- 排行榜列表 -->
    <div class="ranking-list">
      <div
        v-for="(item, index) in ranking"
        :key="item.id"
        class="ranking-item"
        :class="{ 'is-me': item.userId === userStore.user?.id }"
      >
        <div class="rank" :class="getRankClass(index)">
          <span v-if="index < 3" class="rank-medal">{{
            ["🥇", "🥈", "🥉"][index]
          }}</span>
          <span v-else class="rank-number">{{ index + 1 }}</span>
        </div>
        <div class="player-info">
          <div class="avatar" :style="{ background: getAvatarGradient(index) }">
            {{ item.nickname?.charAt(0) || "?" }}
          </div>
          <div class="details">
            <div class="name">{{ item.nickname || "匿名玩家" }}</div>
            <div class="meta">
              <span class="level">Lv.{{ item.level }}</span>
              <span class="time">{{ formatTime(item.playedAt) }}</span>
            </div>
          </div>
        </div>
        <div class="score-section">
          <div class="score">{{ item.score.toLocaleString() }}</div>
          <div class="score-label">分</div>
        </div>
      </div>

      <div v-if="ranking.length === 0 && !loading" class="empty-state">
        <div class="empty-icon">🎯</div>
        <p class="empty-text">暂无排行数据</p>
        <router-link to="/game" class="btn btn-primary">去玩一局</router-link>
      </div>
    </div>

    <!-- 加载更多 -->
    <div v-if="ranking.length > 0" class="load-more-section">
      <button
        v-if="hasMore"
        class="load-more-btn"
        @click="loadMore"
        :disabled="loadingMore"
      >
        <span v-if="loadingMore" class="btn-loading">
          <span class="loading-dot"></span>
          <span class="loading-dot"></span>
          <span class="loading-dot"></span>
        </span>
        <span v-else>加载更多</span>
      </button>
      <div v-else class="no-more">
        <span class="no-more-line"></span>
        <span class="no-more-text">没有更多了</span>
        <span class="no-more-line"></span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { gameApi } from "../api";
import { useUserStore } from "../stores/user";

const userStore = useUserStore();
const ranking = ref([]);
const myBest = ref(null);
const loading = ref(false);
const loadingMore = ref(false);
const currentLimit = ref(10);
const pageSize = 10;
const hasMore = ref(true);

const myRankPosition = computed(() => {
  if (!myBest.value || !userStore.user) return null;
  const idx = ranking.value.findIndex((r) => r.userId === userStore.user.id);
  return idx >= 0 ? idx + 1 : null;
});

function getRankClass(index) {
  if (index === 0) return "gold";
  if (index === 1) return "silver";
  if (index === 2) return "bronze";
  return "";
}

function getAvatarGradient(index) {
  const gradients = [
    "linear-gradient(135deg, #FFD700 0%, #FFA500 100%)",
    "linear-gradient(135deg, #E8E8E8 0%, #B8B8B8 100%)",
    "linear-gradient(135deg, #CD7F32 0%, #A0522D 100%)",
    "linear-gradient(135deg, #A855F7 0%, #7C3AED 100%)",
    "linear-gradient(135deg, #3B82F6 0%, #1D4ED8 100%)",
    "linear-gradient(135deg, #10B981 0%, #059669 100%)",
  ];
  return gradients[index % gradients.length];
}

function formatTime(time) {
  if (!time) return "";
  const date = new Date(time);
  const now = new Date();
  const diff = now - date;

  if (diff < 60000) return "刚刚";
  if (diff < 3600000) return Math.floor(diff / 60000) + "分钟前";
  if (diff < 86400000) return Math.floor(diff / 3600000) + "小时前";
  if (diff < 604800000) return Math.floor(diff / 86400000) + "天前";

  return `${date.getMonth() + 1}/${date.getDate()}`;
}

async function loadMore() {
  if (loadingMore.value || !hasMore.value) return;

  loadingMore.value = true;
  try {
    currentLimit.value += pageSize;
    const res = await gameApi.getRanking(currentLimit.value);
    const newData = res.data || [];

    if (newData.length <= ranking.value.length) {
      hasMore.value = false;
    } else {
      ranking.value = newData;
      if (newData.length < currentLimit.value) {
        hasMore.value = false;
      }
    }
  } catch (e) {
    console.error(e);
    currentLimit.value -= pageSize;
  } finally {
    loadingMore.value = false;
  }
}

onMounted(async () => {
  loading.value = true;
  try {
    const res = await gameApi.getRanking(currentLimit.value);
    ranking.value = res.data || [];

    if (ranking.value.length < currentLimit.value) {
      hasMore.value = false;
    }

    if (userStore.isLoggedIn) {
      const bestRes = await gameApi.getMyBest();
      myBest.value = bestRes.data;
    }
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
});
</script>

<style lang="scss" scoped>
.ranking-page {
  // padding-bottom 由 Layout.vue 统一管理
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  
  .ranking-list {
    width: 100%;
    max-width: 800px;
  }
  
  .my-rank-card {
    width: 100%;
    max-width: 800px;
  }
  
  .load-more-section {
    width: 100%;
    max-width: 800px;
  }
}

.page-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  margin-bottom: 32px;
  width: 100%;

  .page-title {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    font-size: 28px;
  }

  .page-subtitle {
    color: var(--text-secondary);
    font-size: 15px;
    margin-top: 10px;
  }
}

.my-rank-card {
  background: linear-gradient(
    135deg,
    rgba(255, 215, 0, 0.1) 0%,
    rgba(168, 85, 247, 0.1) 100%
  );
  border: 1px solid var(--primary-glow);
  border-radius: var(--radius-lg);
  padding: 16px;
  margin-bottom: 24px;

  .my-rank-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;

    .my-rank-label {
      font-size: 12px;
      color: var(--text-muted);
      text-transform: uppercase;
      letter-spacing: 1px;
    }

    .my-rank-position {
      font-size: 14px;
      font-weight: 700;
      color: var(--primary-color);
    }
  }

  .my-rank-content {
    display: flex;
    align-items: center;
    gap: 14px;
  }

  .my-rank-avatar {
    width: 50px;
    height: 50px;
    border-radius: var(--radius-full);
    background: linear-gradient(
      135deg,
      var(--primary-color) 0%,
      var(--accent-purple) 100%
    );
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    font-weight: 700;
    box-shadow: 0 4px 15px var(--primary-glow);
  }

  .my-rank-info {
    flex: 1;

    .my-rank-name {
      font-size: 16px;
      font-weight: 600;
      display: block;
      margin-bottom: 4px;
    }

    .my-rank-meta {
      font-size: 12px;
      color: var(--text-muted);
    }
  }

  .my-rank-score {
    font-size: 28px;
    font-weight: 700;
    color: var(--primary-color);
    text-shadow: 0 0 20px var(--primary-glow);
  }
}

.ranking-list {
  .ranking-item {
    display: flex;
    align-items: center;
    padding: 14px 20px;
    background: var(--bg-card);
    border: 1px solid var(--border-color);
    border-radius: var(--radius-md);
    margin-bottom: 10px;
    transition: all var(--transition-normal);

    &:hover {
      transform: translateX(4px);
      border-color: rgba(255, 255, 255, 0.15);
    }

    &.is-me {
      border-color: var(--primary-glow);
      background: linear-gradient(
        135deg,
        rgba(255, 215, 0, 0.05) 0%,
        transparent 100%
      );
    }

    .rank {
      width: 44px;
      height: 44px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: var(--radius-full);
      margin-right: 14px;
      background: rgba(255, 255, 255, 0.05);

      .rank-medal {
        font-size: 24px;
      }
      .rank-number {
        font-size: 16px;
        font-weight: 700;
        color: var(--text-muted);
      }

      &.gold {
        background: linear-gradient(135deg, #ffd700 0%, #ffa500 100%);
        box-shadow: 0 4px 15px rgba(255, 215, 0, 0.4);
      }

      &.silver {
        background: linear-gradient(135deg, #e8e8e8 0%, #b8b8b8 100%);
        box-shadow: 0 4px 15px rgba(192, 192, 192, 0.3);
      }

      &.bronze {
        background: linear-gradient(135deg, #cd7f32 0%, #a0522d 100%);
        box-shadow: 0 4px 15px rgba(205, 127, 50, 0.3);
      }
    }

    .player-info {
      flex: 1;
      display: flex;
      align-items: center;

      .avatar {
        width: 40px;
        height: 40px;
        border-radius: var(--radius-full);
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: 700;
        font-size: 15px;
        margin-right: 12px;
        color: #fff;
      }

      .details {
        .name {
          font-size: 15px;
          font-weight: 600;
          margin-bottom: 2px;
        }

        .meta {
          font-size: 12px;
          color: var(--text-muted);
          display: flex;
          gap: 10px;

          .level {
            color: var(--secondary-color);
          }
        }
      }
    }

    .score-section {
      text-align: right;

      .score {
        font-size: 20px;
        font-weight: 700;
        color: var(--primary-color);
        line-height: 1;
      }

      .score-label {
        font-size: 11px;
        color: var(--text-muted);
        margin-top: 2px;
      }
    }
  }
}

.load-more-section {
  margin-top: 24px;
  text-align: center;
}

.load-more-btn {
  width: 100%;
  max-width: 200px;
  height: 44px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  color: var(--text-secondary);
  font-size: 14px;
  cursor: pointer;
  transition: all var(--transition-fast);

  &:hover:not(:disabled) {
    background: rgba(255, 255, 255, 0.1);
    border-color: var(--primary-glow);
    color: var(--primary-color);
  }

  &:disabled {
    cursor: not-allowed;
  }
}

.btn-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.loading-dot {
  width: 8px;
  height: 8px;
  background: var(--primary-color);
  border-radius: 50%;
  animation: loadingBounce 1.4s ease-in-out infinite both;

  &:nth-child(1) {
    animation-delay: -0.32s;
  }
  &:nth-child(2) {
    animation-delay: -0.16s;
  }
  &:nth-child(3) {
    animation-delay: 0s;
  }
}

@keyframes loadingBounce {
  0%,
  80%,
  100% {
    transform: scale(0.6);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

.no-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--text-muted);
  font-size: 13px;
  padding: 8px 0;
}

.no-more-line {
  width: 40px;
  height: 1px;
  background: linear-gradient(
    90deg,
    transparent,
    var(--border-color),
    transparent
  );
}

.no-more-text {
  white-space: nowrap;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  grid-column: 1 / -1; // 跨越所有列
  
  .empty-icon {
    font-size: 64px;
    margin-bottom: 20px;
  }
  .empty-text {
    color: var(--text-muted);
    font-size: 16px;
    margin-bottom: 24px;
  }
}

// 响应式适配
@media (min-width: 576px) {
  .my-rank-card {
    padding: 20px 24px;

    .my-rank-avatar {
      width: 60px;
      height: 60px;
      font-size: 24px;
    }
    .my-rank-name {
      font-size: 18px;
    }
    .my-rank-score {
      font-size: 32px;
    }
  }

  .ranking-list .ranking-item {
    padding: 18px 20px;

    .rank {
      width: 50px;
      height: 50px;
    }
    .player-info .avatar {
      width: 46px;
      height: 46px;
    }
    .player-info .details .name {
      font-size: 16px;
    }
    .score-section .score {
      font-size: 24px;
    }
  }
}

@media (min-width: 768px) {
  .ranking-list {
    .ranking-item {
      margin-bottom: 12px;
    }
  }

  .load-more-btn {
    max-width: 240px;
  }
}

@media (min-width: 992px) {
  .my-rank-card {
    padding: 24px 32px;

    .my-rank-avatar {
      width: 70px;
      height: 70px;
      font-size: 28px;
    }
    .my-rank-score {
      font-size: 36px;
    }
  }

  .ranking-list .ranking-item {
    padding: 20px 24px;

    .rank {
      width: 56px;
      height: 56px;
      font-size: 22px;
    }
    .player-info .avatar {
      width: 52px;
      height: 52px;
      font-size: 20px;
    }
    .score-section .score {
      font-size: 28px;
    }
  }
}
</style>
