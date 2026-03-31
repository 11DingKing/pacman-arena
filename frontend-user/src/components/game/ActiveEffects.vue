<template>
  <div class="active-effects">
    <div v-if="isInvincible" class="effect-badge invincible-badge">
      <span class="effect-icon">🛡️</span>
      <span class="effect-name">保护中</span>
    </div>
    <div
      v-for="effect in effects"
      :key="effect.type"
      class="effect-badge"
    >
      <span class="effect-icon">{{ getEffectIcon(effect.type) }}</span>
      <span class="effect-name">{{ effect.name }}</span>
    </div>
  </div>
</template>

<script setup>
defineProps({
  effects: {
    type: Array,
    default: () => [],
  },
  isInvincible: {
    type: Boolean,
    default: false,
  },
});

function getEffectIcon(type) {
  const icons = {
    SPEED_UP: "🚀",
    INVINCIBLE: "🛡️",
    DOUBLE_SCORE: "⭐",
    MAGNET: "🧲",
  };
  return icons[type] || "✨";
}
</script>

<style lang="scss" scoped>
.active-effects {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  min-height: 40px;
  margin-bottom: 16px;
  flex-wrap: wrap;

  .effect-badge {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 6px 12px;
    background: rgba(0, 212, 255, 0.15);
    border: 1px solid rgba(0, 212, 255, 0.3);
    border-radius: 20px;
    animation: effectPulse 2s ease-in-out infinite;

    .effect-icon {
      font-size: 14px;
    }

    .effect-name {
      font-size: 12px;
      font-weight: 600;
      color: #00d4ff;
    }

    &.invincible-badge {
      background: rgba(16, 185, 129, 0.2);
      border-color: rgba(16, 185, 129, 0.5);
      animation: protectionPulse 0.5s ease-in-out infinite;

      .effect-name {
        color: #10b981;
      }
    }
  }
}

@keyframes protectionPulse {
  0%, 100% {
    box-shadow: 0 0 5px rgba(16, 185, 129, 0.3);
    opacity: 1;
  }
  50% {
    box-shadow: 0 0 15px rgba(16, 185, 129, 0.6);
    opacity: 0.8;
  }
}

@keyframes effectPulse {
  0%, 100% {
    box-shadow: 0 0 5px rgba(0, 212, 255, 0.3);
  }
  50% {
    box-shadow: 0 0 15px rgba(0, 212, 255, 0.5);
  }
}
</style>
