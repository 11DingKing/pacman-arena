<template>
  <div class="game-controls-wrapper">
    <!-- 虚拟方向键 - 移动端显示 -->
    <div class="game-controls mobile-controls">
      <div class="dpad">
        <button
          class="dpad-btn dpad-up"
          @touchstart.prevent="$emit('direction', 'up')"
          @mousedown="$emit('direction', 'up')"
        >
          <span>▲</span>
        </button>
        <button
          class="dpad-btn dpad-left"
          @touchstart.prevent="$emit('direction', 'left')"
          @mousedown="$emit('direction', 'left')"
        >
          <span>◀</span>
        </button>
        <button class="dpad-btn dpad-center" @click="$emit('toggle-pause')">
          <span>{{ isPlaying ? "⏸" : "▶" }}</span>
        </button>
        <button
          class="dpad-btn dpad-right"
          @touchstart.prevent="$emit('direction', 'right')"
          @mousedown="$emit('direction', 'right')"
        >
          <span>▶</span>
        </button>
        <button
          class="dpad-btn dpad-down"
          @touchstart.prevent="$emit('direction', 'down')"
          @mousedown="$emit('direction', 'down')"
        >
          <span>▼</span>
        </button>
      </div>
    </div>

    <!-- 键盘提示 - 桌面端显示 -->
    <div class="keyboard-hint desktop-controls">
      <div class="hint-keys">
        <div class="key-row">
          <span class="key">↑</span>
        </div>
        <div class="key-row">
          <span class="key">←</span>
          <span class="key">↓</span>
          <span class="key">→</span>
        </div>
      </div>
      <div class="hint-text">使用方向键控制 · 空格键暂停</div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  isPlaying: {
    type: Boolean,
    default: false,
  },
});

defineEmits(['direction', 'toggle-pause']);
</script>

<style lang="scss" scoped>
.game-controls-wrapper {
  position: fixed;
  bottom: 8px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 40;
  padding-bottom: env(safe-area-inset-bottom, 0px);
}

.game-controls {
  margin: 0;
}

.dpad {
  display: grid;
  grid-template-columns: repeat(3, 52px);
  grid-template-rows: repeat(3, 52px);
  gap: 4px;

  .dpad-btn {
    background: rgba(30, 41, 59, 0.9);
    border: 2px solid rgba(255, 215, 0, 0.2);
    border-radius: 12px;
    color: #fff;
    font-size: 20px;
    cursor: pointer;
    transition: all 0.15s ease;
    display: flex;
    align-items: center;
    justify-content: center;
    user-select: none;
    -webkit-tap-highlight-color: transparent;

    &:hover,
    &:active {
      background: #ffd700;
      color: #000;
      border-color: #ffd700;
      transform: scale(0.95);
    }
  }

  .dpad-up {
    grid-column: 2;
    grid-row: 1;
  }
  .dpad-left {
    grid-column: 1;
    grid-row: 2;
  }
  .dpad-center {
    grid-column: 2;
    grid-row: 2;
    background: rgba(255, 215, 0, 0.1);
    border-color: rgba(255, 215, 0, 0.3);
  }
  .dpad-right {
    grid-column: 3;
    grid-row: 2;
  }
  .dpad-down {
    grid-column: 2;
    grid-row: 3;
  }
}

// 桌面端键盘提示
.desktop-controls {
  display: none;
}

.keyboard-hint {
  flex-direction: column;
  align-items: center;
  gap: 16px;
  margin-top: 24px;
  padding: 20px;

  .hint-keys {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 6px;

    .key-row {
      display: flex;
      gap: 6px;
    }

    .key {
      width: 44px;
      height: 44px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: rgba(255, 255, 255, 0.08);
      border: 1px solid rgba(255, 255, 255, 0.15);
      border-radius: 8px;
      font-size: 18px;
      color: rgba(255, 255, 255, 0.6);
    }
  }

  .hint-text {
    font-size: 13px;
    color: rgba(255, 255, 255, 0.4);
  }
}

// 小屏适配
@media (max-height: 700px) {
  .game-controls-wrapper {
    bottom: 4px;
  }

  .dpad {
    grid-template-columns: repeat(3, 46px);
    grid-template-rows: repeat(3, 46px);
    gap: 3px;

    .dpad-btn {
      font-size: 16px;
      border-radius: 10px;
    }
  }
}

@media (max-height: 600px) {
  .dpad {
    grid-template-columns: repeat(3, 42px);
    grid-template-rows: repeat(3, 42px);
    gap: 2px;

    .dpad-btn {
      font-size: 14px;
      border-radius: 8px;
    }
  }
}

@media (min-width: 768px) {
  .game-controls-wrapper {
    position: static;
    transform: none;
    padding-bottom: 0;
  }

  .mobile-controls {
    display: none;
  }

  .desktop-controls {
    display: flex;
  }

  .game-controls {
    max-width: 500px;
    margin: 20px auto 0;
  }
}
</style>
