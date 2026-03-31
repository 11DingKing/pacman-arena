<template>
  <div class="game-hud">
    <div class="hud-item score-hud">
      <span class="hud-label">得分</span>
      <span class="hud-value" :class="{ double: scoreMultiplier > 1 }">{{
        score.toLocaleString()
      }}</span>
    </div>
    <div class="hud-item level-hud">
      <span class="hud-label">关卡</span>
      <span class="hud-value">{{ level }}</span>
    </div>
    <div class="hud-item lives-hud">
      <span class="hud-label">生命</span>
      <div class="lives">
        <span v-for="i in lives" :key="i" class="life active">❤️</span>
        <span v-for="i in maxLives - lives" :key="'empty-' + i" class="life">💔</span>
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
    default: 3,
  },
  maxLives: {
    type: Number,
    default: 3,
  },
  scoreMultiplier: {
    type: Number,
    default: 1,
  },
});
</script>

<style lang="scss" scoped>
.game-hud {
  display: flex;
  justify-content: space-between;
  width: 100%;
  gap: 8px;

  .hud-item {
    flex: 1;
    background: transparent;
    border: none;
    padding: 4px;
    text-align: center;

    .hud-label {
      display: block;
      font-size: 10px;
      color: rgba(255, 255, 255, 0.5);
      text-transform: uppercase;
      letter-spacing: 0.5px;
      margin-bottom: 2px;
    }

    .hud-value {
      font-size: 20px;
      font-weight: 700;
      color: #ffd700;
      transition: all 0.3s ease;

      &.double {
        color: #00d4ff;
        text-shadow: 0 0 10px rgba(0, 212, 255, 0.5);
        animation: pulse 1s ease-in-out infinite;
      }
    }
  }

  .lives-hud {
    .lives {
      display: flex;
      justify-content: center;
      gap: 1px;

      .life {
        font-size: 14px;
        opacity: 0.3;
        filter: grayscale(100%);

        &.active {
          opacity: 1;
          filter: none;
        }
      }
    }
  }
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}
</style>
