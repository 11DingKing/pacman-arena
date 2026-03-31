<template>
  <div class="item-card" @click="$emit('buy', item)">
    <div class="item-badge" v-if="item.price <= 2">热卖</div>
    <div class="item-icon-wrapper">
      <span class="item-icon">{{ item.displayIcon }}</span>
      <div class="item-glow"></div>
    </div>
    <h3 class="item-name">{{ item.name }}</h3>
    <p class="item-desc">{{ item.description }}</p>
    <div class="item-effect-chip">
      <span class="effect-icon">⏱️</span>
      <span v-if="item.duration > 0">持续 {{ item.duration }}秒</span>
      <span v-else>一次性</span>
    </div>
    <div class="item-footer">
      <div class="item-price">
        <span class="price-symbol">¥</span>
        <span class="price-value">{{ item.price }}</span>
      </div>
      <button class="buy-btn">购买</button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  item: {
    type: Object,
    required: true,
  },
});

defineEmits(['buy']);
</script>

<style lang="scss" scoped>
.item-card {
  position: relative;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 20px 16px;
  text-align: center;
  cursor: pointer;
  transition: all var(--transition-normal);

  &:hover {
    transform: translateY(-4px);
    border-color: var(--primary-glow);
    box-shadow:
      0 12px 40px rgba(0, 0, 0, 0.3),
      0 0 20px var(--primary-glow);
  }

  .item-badge {
    position: absolute;
    top: -8px;
    right: -8px;
    background: linear-gradient(135deg, #ff6b9d 0%, #ff4757 100%);
    color: #fff;
    font-size: 10px;
    font-weight: 700;
    padding: 4px 10px;
    border-radius: var(--radius-full);
    box-shadow: 0 4px 12px rgba(255, 107, 157, 0.4);
  }

  .item-icon-wrapper {
    position: relative;
    margin-bottom: 12px;
    .item-icon {
      font-size: 48px;
      position: relative;
      z-index: 1;
    }
    .item-glow {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      width: 60px;
      height: 60px;
      background: radial-gradient(
        circle,
        rgba(255, 215, 0, 0.2) 0%,
        transparent 70%
      );
      border-radius: 50%;
    }
  }

  .item-name {
    font-size: 15px;
    font-weight: 600;
    margin-bottom: 6px;
  }
  .item-desc {
    font-size: 12px;
    color: var(--text-secondary);
    line-height: 1.5;
    margin-bottom: 10px;
    min-height: 36px;
  }
  .item-effect-chip {
    font-size: 11px;
    color: var(--secondary-color);
    margin-bottom: 12px;
    min-height: 24px;
    padding: 4px 10px;
    background: rgba(0, 212, 255, 0.08);
    border: 1px solid rgba(0, 212, 255, 0.2);
    border-radius: var(--radius-full);
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
  }

  .item-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding-top: 12px;
    border-top: 1px solid var(--border-color);
  }

  .item-price {
    .price-symbol {
      font-size: 12px;
      color: var(--primary-color);
    }
    .price-value {
      font-size: 22px;
      font-weight: 700;
      color: var(--primary-color);
    }
  }

  .buy-btn {
    background: linear-gradient(
      135deg,
      var(--primary-color) 0%,
      var(--primary-dark) 100%
    );
    color: #000;
    border: none;
    padding: 8px 18px;
    border-radius: var(--radius-md);
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
    transition: all var(--transition-fast);
    box-shadow: 0 2px 8px rgba(255, 215, 0, 0.25);
    &:hover {
      transform: translateY(-2px) scale(1.02);
      box-shadow: 0 4px 16px rgba(255, 215, 0, 0.35);
    }
    &:active {
      transform: translateY(0) scale(0.98);
    }
  }
}

// 响应式适配
@media (min-width: 576px) {
  .item-card {
    padding: 24px 20px;

    .item-icon-wrapper .item-icon {
      font-size: 56px;
    }
    .item-name {
      font-size: 17px;
    }
    .item-desc {
      font-size: 13px;
      min-height: 40px;
    }
  }
}

@media (min-width: 992px) {
  .item-card {
    padding: 28px 24px;

    .item-icon-wrapper .item-icon {
      font-size: 64px;
    }
    .item-price .price-value {
      font-size: 26px;
    }
    .buy-btn {
      padding: 10px 20px;
      font-size: 14px;
    }
  }
}
</style>
