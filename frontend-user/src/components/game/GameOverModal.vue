<template>
  <div class="game-over-overlay">
    <div class="game-over-modal">
      <div class="result-badge" :class="score >= 500 ? 'win' : 'lose'">
        <span class="badge-icon">{{ score >= 500 ? "🏆" : "💀" }}</span>
      </div>
      <div class="score-area">
        <div class="score-label">最终得分</div>
        <div class="score-value">{{ score.toLocaleString() }}</div>
      </div>
      <div class="game-stats">
        <div class="stat-box">
          <span class="stat-icon">🎯</span>
          <span class="stat-num">{{ level }}</span>
          <span class="stat-name">关卡</span>
        </div>
        <div class="stat-box">
          <span class="stat-icon">⏱️</span>
          <span class="stat-num">{{ duration }}</span>
          <span class="stat-name">用时</span>
        </div>
        <div class="stat-box">
          <span class="stat-icon">❤️</span>
          <span class="stat-num">{{ lives }}</span>
          <span class="stat-name">剩余</span>
        </div>
      </div>
      <div class="modal-buttons">
        <button class="btn-restart" @click="$emit('restart')">再来一局</button>
        <div class="btn-row">
          <router-link to="/ranking" class="btn-link">
            <span>🏅</span> 排行榜
          </router-link>
          <router-link to="/" class="btn-link">
            <span>🏠</span> 首页
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  score: {
    type: Number,
    default: 0,
  },
  level: {
    type: Number,
    default: 1,
  },
  lives: {
    type: Number,
    default: 0,
  },
  duration: {
    type: String,
    default: "0:00",
  },
});

defineEmits(['restart']);
</script>

<style lang="scss" scoped>
.game-over-overlay {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(10, 14, 26, 0.95);
  backdrop-filter: blur(10px);
  z-index: 250;
}

.game-over-modal {
  text-align: center;
  padding: 32px 24px;
  width: 100%;
  max-width: 320px;

  .result-badge {
    width: 72px;
    height: 72px;
    margin: 0 auto 20px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;

    .badge-icon {
      font-size: 36px;
    }

    &.win {
      background: linear-gradient(
        135deg,
        rgba(255, 215, 0, 0.2) 0%,
        rgba(255, 165, 0, 0.2) 100%
      );
      border: 2px solid rgba(255, 215, 0, 0.5);
      box-shadow: 0 0 30px rgba(255, 215, 0, 0.3);
    }

    &.lose {
      background: linear-gradient(
        135deg,
        rgba(255, 255, 255, 0.1) 0%,
        rgba(255, 255, 255, 0.05) 100%
      );
      border: 2px solid rgba(255, 255, 255, 0.2);
    }
  }

  .score-area {
    margin-bottom: 24px;

    .score-label {
      font-size: 13px;
      color: rgba(255, 255, 255, 0.5);
      margin-bottom: 8px;
      text-transform: uppercase;
      letter-spacing: 2px;
    }

    .score-value {
      font-size: 56px;
      font-weight: 800;
      color: #ffd700;
      line-height: 1;
      text-shadow: 0 0 40px rgba(255, 215, 0, 0.5);
    }
  }

  .game-stats {
    display: flex;
    justify-content: center;
    gap: 24px;
    margin-bottom: 28px;
    padding: 16px 0;
    border-top: 1px solid rgba(255, 255, 255, 0.1);
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);

    .stat-box {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 4px;

      .stat-icon {
        font-size: 18px;
      }

      .stat-num {
        font-size: 20px;
        font-weight: 700;
        color: #fff;
      }

      .stat-name {
        font-size: 11px;
        color: rgba(255, 255, 255, 0.4);
      }
    }
  }

  .modal-buttons {
    .btn-restart {
      width: 100%;
      padding: 16px 24px;
      background: linear-gradient(135deg, #ffd700 0%, #ffa500 100%);
      border: none;
      border-radius: 12px;
      font-size: 17px;
      font-weight: 700;
      color: #000;
      cursor: pointer;
      transition: all 0.2s ease;
      margin-bottom: 16px;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 24px rgba(255, 215, 0, 0.4);
      }

      &:active {
        transform: translateY(0);
      }
    }

    .btn-row {
      display: flex;
      justify-content: center;
      gap: 32px;
    }

    .btn-link {
      display: flex;
      align-items: center;
      gap: 6px;
      color: rgba(255, 255, 255, 0.5);
      text-decoration: none;
      font-size: 14px;
      transition: color 0.2s;

      span {
        font-size: 16px;
      }

      &:hover {
        color: #ffd700;
      }
    }
  }
}

@media (min-width: 768px) {
  .game-over-modal {
    max-width: 360px;
    padding: 40px 32px;

    .score-area .score-value {
      font-size: 64px;
    }

    .game-stats {
      gap: 32px;
    }
  }
}
</style>
