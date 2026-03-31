<template>
  <div class="my-items-section" v-if="items.length > 0">
    <h2 class="section-title"><span>🎒</span> 我的背包 · 已购道具</h2>
    <div class="my-items-grid">
      <div v-for="item in items" :key="item.id" class="my-item-card">
        <span class="my-item-icon">{{ item.displayIcon }}</span>
        <div class="my-item-info">
          <span class="my-item-name">{{ item.displayName }}</span>
          <span class="my-item-desc">{{ item.displayDesc }}</span>
          <span class="my-item-qty">数量 ×{{ item.quantity }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  items: {
    type: Array,
    required: true,
  },
});
</script>

<style lang="scss" scoped>
.my-items-section {
  margin-top: 40px;
  .section-title {
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 16px;
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.my-items-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.my-item-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  min-width: 0;

  .my-item-icon {
    font-size: 32px;
    flex-shrink: 0;
  }
  .my-item-info {
    display: flex;
    flex-direction: column;
    gap: 2px;
    min-width: 0;
  }
  .my-item-name {
    font-size: 15px;
    font-weight: 600;
    color: var(--text-primary);
  }
  .my-item-desc {
    font-size: 12px;
    color: var(--text-secondary);
  }
  .my-item-qty {
    font-size: 12px;
    color: var(--primary-color);
    font-weight: 500;
    margin-top: 2px;
  }
}

@media (min-width: 768px) {
  .my-items-grid {
    gap: 16px;
  }

  .my-item-card {
    padding: 14px 20px;
    .my-item-icon {
      font-size: 36px;
    }
    .my-item-name {
      font-size: 16px;
    }
    .my-item-desc {
      font-size: 13px;
    }
  }
}
</style>
