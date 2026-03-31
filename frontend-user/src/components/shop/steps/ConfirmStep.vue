<template>
  <div class="pay-step">
    <button class="dialog-close" @click="$emit('close')">✕</button>
    <div class="dialog-header">
      <div class="dialog-icon">{{ item?.displayIcon }}</div>
      <h3>{{ item?.name }}</h3>
      <p class="item-desc">{{ item?.description }}</p>
    </div>

    <div class="dialog-body">
      <div class="quantity-row">
        <span class="row-label">购买数量</span>
        <div class="qty-selector">
          <button class="qty-btn" @click="quantity > 1 && $emit('update:quantity', quantity - 1)">
            −
          </button>
          <span class="qty-value">{{ quantity }}</span>
          <button class="qty-btn" @click="quantity < 99 && $emit('update:quantity', quantity + 1)">
            +
          </button>
        </div>
      </div>

      <div class="price-row">
        <span class="row-label">单价</span>
        <span class="row-value">¥{{ item?.price.toFixed(2) }}</span>
      </div>

      <div class="total-row">
        <span class="row-label">应付金额</span>
        <span class="total-price">¥{{ totalPrice }}</span>
      </div>
      <p v-if="paymentModeMock" class="step-hint step-hint-sim">
        支持模拟与真实：模拟时不跳转、不扣款；配置支付宝后为真实环境，将跳转支付。确认后点击「支付宝支付」创建订单；若遇异常可重试。
      </p>
      <p class="step-hint">
        确认商品与金额后，点击「支付宝支付」创建订单。
      </p>
      <p v-if="error" class="step-error">{{ error }}</p>
    </div>

    <div class="dialog-actions">
      <button class="action-btn cancel" @click="$emit('close')">
        取消
      </button>
      <button
        class="action-btn pay"
        :disabled="creating"
        @click="$emit('start-pay')"
      >
        <svg
          class="alipay-icon"
          viewBox="0 0 1024 1024"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            d="M1024.0512 701.0304V196.864A196.9664 196.9664 0 0 0 827.136 0H196.864A196.9664 196.9664 0 0 0 0 196.864v630.272A196.9152 196.9152 0 0 0 196.864 1024h630.272a197.12 197.12 0 0 0 193.8432-162.0992c-52.224-22.6304-278.528-120.32-396.4416-176.64-89.7024 108.6976-183.7056 173.9264-325.3248 173.9264s-236.1856-87.2448-224.8192-194.048c7.4752-70.0416 55.552-184.576 264.2944-164.9664 110.08 10.3424 160.4096 30.8736 250.1632 60.5184 23.1936-42.5984 42.496-89.4464 57.1392-139.264H248.064v-39.424h196.9152V311.1424H204.8V267.776h240.128V165.632s2.1504-15.9744 19.8144-15.9744h98.4576V267.776h256v43.4176h-256V381.952h208.8448a805.9904 805.9904 0 0 1-84.8384 212.6848c60.672 22.016 336.7936 106.3936 336.7936 106.3936zM283.5456 791.6032c-149.6576 0-173.312-94.464-165.376-133.9392 7.8336-39.3216 51.2-90.624 134.4-90.624 95.5904 0 181.248 24.4736 284.0576 74.5472-72.192 94.0032-160.9216 150.016-253.0816 150.016z"
            fill="#fff"
          />
        </svg>
        {{ creating ? "创建中..." : "支付宝支付" }}
      </button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  item: Object,
  quantity: Number,
  totalPrice: String,
  paymentModeMock: Boolean,
  creating: Boolean,
  error: String,
});

defineEmits(['close', 'update:quantity', 'start-pay']);
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

.dialog-header {
  text-align: center;
  margin-bottom: 24px;
  .dialog-icon {
    font-size: 56px;
    margin-bottom: 12px;
  }
  h3 {
    font-size: 20px;
    font-weight: 600;
    margin-bottom: 8px;
  }
  p {
    font-size: 13px;
    color: var(--text-secondary);
  }
}

.dialog-body {
  margin-bottom: 24px;
}

.quantity-row,
.price-row,
.total-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--border-color);
}

.row-label {
  font-size: 14px;
  color: var(--text-secondary);
}
.row-value {
  font-size: 14px;
  color: var(--text-primary);
}

.qty-selector {
  display: flex;
  align-items: center;
  gap: 16px;

  .qty-btn {
    width: 36px;
    height: 36px;
    border: 1px solid var(--border-color);
    background: rgba(255, 255, 255, 0.05);
    color: var(--text-primary);
    border-radius: var(--radius-sm);
    font-size: 18px;
    cursor: pointer;
    transition: all var(--transition-fast);
    &:hover {
      background: var(--primary-color);
      color: #000;
      border-color: var(--primary-color);
    }
  }

  .qty-value {
    font-size: 24px;
    font-weight: 700;
    min-width: 40px;
    text-align: center;
  }
}

.total-row {
  border-bottom: none;
  padding-top: 16px;
  .total-price {
    font-size: 24px;
    font-weight: 700;
    color: var(--primary-color);
  }
}

.step-hint {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid var(--border-color);
}

.step-hint-sim {
  margin-top: 0;
  padding-top: 0;
  border-top: none;
  padding: 8px 12px;
  background: rgba(0, 212, 255, 0.08);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: var(--radius-sm);
  color: var(--secondary-color, #00d4ff);
  font-size: 11px;
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

.dialog-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

.action-btn {
  flex: 1;
  height: 48px;
  border: none;
  border-radius: var(--radius-md);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;

  &.cancel {
    background: rgba(255, 255, 255, 0.1);
    color: var(--text-secondary);
    &:hover {
      background: rgba(255, 255, 255, 0.15);
    }
  }

  &.pay {
    background: linear-gradient(135deg, #1677ff 0%, #0958d9 100%);
    color: #fff;
    &:hover:not(:disabled) {
      transform: scale(1.02);
      box-shadow: 0 4px 16px rgba(22, 119, 255, 0.4);
    }
    &:disabled {
      opacity: 0.7;
      cursor: not-allowed;
    }
  }

  .alipay-icon {
    width: 20px;
    height: 20px;
  }
}
</style>
