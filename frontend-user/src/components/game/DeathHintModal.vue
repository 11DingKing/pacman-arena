<template>
  <transition name="death-hint">
    <div v-if="show" class="death-hint-overlay">
      <div class="death-hint-modal">
        <div class="death-icon">💀</div>
        <h3>被幽灵抓住了！</h3>
        <div class="lives-remaining">
          <span class="lives-label">剩余生命</span>
          <div class="lives-display">
            <span v-for="i in lives" :key="i" class="life-icon">❤️</span>
            <span
              v-for="i in maxLives - lives"
              :key="'empty-' + i"
              class="life-icon empty"
            >💔</span>
          </div>
        </div>
        <p class="hint-text">
          {{ lives > 0 ? "准备好继续挑战！" : "游戏结束" }}
        </p>
        <div class="countdown-bar">
          <div class="countdown-progress"></div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
defineProps({
  show: {
    type: Boolean,
    default: false,
  },
  lives: {
    type: Number,
    default: 0,
  },
  maxLives: {
    type: Number,
    default: 3,
  },
});
</script>

<style lang="scss" scoped>
.death-hint-overlay {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(10, 14, 26, 0.9);
  backdrop-filter: blur(10px);
  z-index: 200;
}

.death-hint-modal {
  text-align: center;
  padding: 32px 40px;
  background: linear-gradient(145deg, #1a1f35 0%, #0d1220 100%);
  border: 2px solid rgba(239, 68, 68, 0.5);
  border-radius: 20px;
  box-shadow: 0 0 40px rgba(239, 68, 68, 0.3);
  animation: shakeModal 0.5s ease;

  .death-icon {
    font-size: 56px;
    margin-bottom: 12px;
    animation: pulse 1s ease-in-out infinite;
  }

  h3 {
    font-size: 22px;
    font-weight: 700;
    color: #ef4444;
    margin-bottom: 20px;
  }

  .lives-remaining {
    margin-bottom: 16px;

    .lives-label {
      display: block;
      font-size: 12px;
      color: rgba(255, 255, 255, 0.5);
      text-transform: uppercase;
      letter-spacing: 1px;
      margin-bottom: 10px;
    }

    .lives-display {
      display: flex;
      justify-content: center;
      gap: 8px;

      .life-icon {
        font-size: 28px;
        transition: all 0.3s ease;

        &.empty {
          opacity: 0.3;
          filter: grayscale(100%);
        }
      }
    }
  }

  .hint-text {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.7);
    margin-bottom: 20px;
  }

  .countdown-bar {
    width: 200px;
    height: 4px;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 2px;
    overflow: hidden;
    margin: 0 auto;

    .countdown-progress {
      height: 100%;
      background: linear-gradient(90deg, #ffd700, #ffa500);
      border-radius: 2px;
      animation: countdownShrink 2s linear forwards;
    }
  }
}

@keyframes shakeModal {
  0%, 100% { transform: translateX(0); }
  10%, 30%, 50%, 70%, 90% { transform: translateX(-5px); }
  20%, 40%, 60%, 80% { transform: translateX(5px); }
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

@keyframes countdownShrink {
  from { width: 100%; }
  to { width: 0%; }
}

.death-hint-enter-active {
  animation: fadeInScale 0.3s ease;
}

.death-hint-leave-active {
  animation: fadeOutScale 0.3s ease;
}

@keyframes fadeInScale {
  from { opacity: 0; transform: scale(0.8); }
  to { opacity: 1; transform: scale(1); }
}

@keyframes fadeOutScale {
  from { opacity: 1; transform: scale(1); }
  to { opacity: 0; transform: scale(0.8); }
}
</style>
