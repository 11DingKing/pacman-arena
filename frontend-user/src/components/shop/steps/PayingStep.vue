<template>
  <div class="pay-step paying-step">
    <button class="dialog-close" @click="$emit('cancel')">✕</button>
    <div class="paying-icon">
      <div class="paying-spinner"></div>
    </div>
    <h3>订单已创建，请完成支付</h3>
    <p class="paying-desc">
      <template v-if="isMockMode">
        模拟环境：不跳转支付宝、不扣款。点击「模拟完成支付」即视为支付成功，道具将发放至背包；若遇异常可重试。
      </template>
      <template v-else>
        真实环境：已新窗口打开支付宝。请在支付宝完成支付后，点击「我已完成支付」查询结果；若遇异常可重试。
      </template>
    </p>
    <div class="paying-info">
      <div class="info-row">
        <span>订单号</span>
        <span class="info-order-no">{{ orderNo }}</span>
      </div>
      <div class="info-row">
        <span>订单金额</span>
        <span class="info-price">¥{{ totalPrice }}</span>
      </div>
      <div class="info-row">
        <span>商品</span>
        <span>{{ item?.name }} x{{ quantity }}</span>
      </div>
    </div>
    <p v-if="error" class="step-error paying-error">
      {{ error }}
    </p>
    <button
      class="check-btn"
      :disabled="confirming"
      @click="$emit('check-result')"
    >
      {{
        confirming
          ? "处理中..."
          : isMockMode
            ? "模拟完成支付"
            : "我已完成支付"
      }}
    </button>
    <button class="cancel-link" @click="$emit('cancel')">取消支付</button>
  </div>
</template>

<script setup>
defineProps({
  item: Object,
  quantity: Number,
  totalPrice: String,
  orderNo: String,
  isMockMode: Boolean,
  confirming: Boolean,
  error: String,
});

defineEmits(['cancel', 'check-result']);
</script>

<style lang="scss" scoped>
.dialog-close {
  position: absolute;
  top: 16px;
  right: 16px;
  background: none;
  border: none;
  color: var(--text-muted);
  font-size: 18px;
  cursor: pointer;
  &:hover {
    color: var(--text-primary);
  }
}

.paying-step {
  text-align: center;

  h3 {
    font-size: 18px;
    margin-bottom: 8px;
  }

  .paying-desc {
    font-size: 13px;
    color: var(--text-secondary);
    margin-bottom: 24px;
    line-height: 1.5;
    padding: 0 8px;
  }
}

.paying-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.paying-spinner {
  width: 60px;
  height: 60px;
  border: 4px solid rgba(22, 119, 255, 0.2);
  border-top-color: #1677ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.paying-info {
  background: rgba(255, 255, 255, 0.05);
  border-radius: var(--radius-md);
  padding: 16px;
  margin-bottom: 24px;

  .info-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 13px;
    color: var(--text-secondary);
    &:not(:last-child) {
      margin-bottom: 10px;
    }
    .info-price {
      color: var(--primary-color);
      font-weight: 600;
    }
    .info-order-no {
      font-family: ui-monospace, monospace;
      font-size: 12px;
      color: var(--text-muted);
      word-break: break-all;
      max-width: 60%;
      text-align: right;
    }
  }
}

.step-error {
  font-size: 13px;
  color: var(--danger, #ef4444);
  margin-top: 12px;
  padding: 10px 12px;
  background: rgba(239, 68, 68, 0.08);
  border: 1px solid rgba(239, 68, 68, 0.25);
  border-radius: var(--radius-sm);
}

.paying-error {
  margin-bottom: 16px;
}

.check-btn {
  width: 100%;
  height: 48px;
  background: linear-gradient(
    135deg,
    var(--primary-color) 0%,
    var(--primary-dark) 100%
  );
  color: #000;
  border: none;
  border-radius: var(--radius-md);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  margin-bottom: 12px;
  transition: all var(--transition-fast);
  &:hover:not(:disabled) {
    transform: scale(1.02);
  }
  &:disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }
}

.cancel-link {
  background: none;
  border: none;
  color: var(--text-muted);
  font-size: 13px;
  cursor: pointer;
  &:hover {
    color: var(--text-secondary);
  }
}
</style>
