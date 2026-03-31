<template>
  <div class="pay-step success-step">
    <div class="success-icon">✓</div>
    <h3>支付成功</h3>
    <p class="success-desc">
      道具已发放到您的背包，可前往游戏中直接使用。
    </p>
    <div class="success-item">
      <span class="success-item-icon">{{ item?.displayIcon }}</span>
      <span class="success-item-name">{{ item?.name }}</span>
      <span class="success-item-qty">x{{ quantity }}</span>
    </div>
    <button class="done-btn" @click="$emit('done')">完成</button>
  </div>
</template>

<script setup>
defineProps({
  item: Object,
  quantity: Number,
});

defineEmits(['done']);
</script>

<style lang="scss" scoped>
.success-step {
  text-align: center;

  h3 {
    font-size: 20px;
    margin-bottom: 8px;
    color: #52c41a;
  }

  .success-desc {
    font-size: 13px;
    color: var(--text-secondary);
    margin-bottom: 24px;
    line-height: 1.5;
  }
}

.success-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  color: #fff;
  animation: successPop 0.5s ease;
}

@keyframes successPop {
  0% {
    transform: scale(0);
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
  }
}

.success-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 16px;
  background: rgba(82, 196, 26, 0.1);
  border: 1px solid rgba(82, 196, 26, 0.3);
  border-radius: var(--radius-md);
  margin-bottom: 24px;

  .success-item-icon {
    font-size: 32px;
  }
  .success-item-name {
    font-size: 15px;
    font-weight: 500;
  }
  .success-item-qty {
    font-size: 14px;
    color: var(--primary-color);
  }
}

.done-btn {
  width: 100%;
  height: 48px;
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
  color: #fff;
  border: none;
  border-radius: var(--radius-md);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
  &:hover {
    transform: scale(1.02);
  }
}
</style>
