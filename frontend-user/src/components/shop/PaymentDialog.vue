<template>
  <Teleport to="body">
    <div v-if="show" class="dialog-overlay" @click.self="handleOverlayClick">
      <div class="dialog-content">
        <div class="pay-flow-steps">
          <span :class="['step-dot', step === 'confirm' && 'active']">1</span>
          <span class="step-line"></span>
          <span :class="['step-dot', step === 'paying' && 'active']">2</span>
          <span class="step-line"></span>
          <span :class="['step-dot', step === 'success' && 'active']">3</span>
        </div>
        <p class="pay-flow-hint">
          {{ step === "confirm" ? "确认订单" : step === "paying" ? "支付" : "完成" }}
        </p>

        <!-- 步骤1: 确认订单 -->
        <ConfirmStep
          v-if="step === 'confirm'"
          :item="item"
          :quantity="quantity"
          :total-price="totalPrice"
          :payment-mode-mock="paymentModeMock"
          :creating="creating"
          :error="error"
          @close="$emit('close')"
          @update:quantity="$emit('update:quantity', $event)"
          @start-pay="$emit('start-pay')"
        />

        <!-- 步骤2: 支付中 -->
        <PayingStep
          v-if="step === 'paying'"
          :item="item"
          :quantity="quantity"
          :total-price="totalPrice"
          :order-no="orderNo"
          :is-mock-mode="isMockMode"
          :confirming="confirming"
          :error="error"
          @cancel="$emit('cancel-pay')"
          @check-result="$emit('check-result')"
        />

        <!-- 步骤3: 成功 -->
        <SuccessStep
          v-if="step === 'success'"
          :item="item"
          :quantity="quantity"
          @done="$emit('success')"
        />
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import ConfirmStep from './steps/ConfirmStep.vue';
import PayingStep from './steps/PayingStep.vue';
import SuccessStep from './steps/SuccessStep.vue';

const props = defineProps({
  show: Boolean,
  step: {
    type: String,
    default: 'confirm',
  },
  item: Object,
  quantity: {
    type: Number,
    default: 1,
  },
  totalPrice: String,
  orderNo: String,
  isMockMode: Boolean,
  paymentModeMock: Boolean,
  creating: Boolean,
  confirming: Boolean,
  error: String,
});

const emit = defineEmits([
  'close',
  'update:quantity',
  'start-pay',
  'cancel-pay',
  'check-result',
  'success',
]);

function handleOverlayClick() {
  if (props.step === 'paying') return;
  emit('close');
}
</script>

<style lang="scss" scoped>
.dialog-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.dialog-content {
  width: 100%;
  max-width: 360px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  padding: 30px 24px;
  position: relative;
  animation: dialogIn 0.3s ease;
}

@keyframes dialogIn {
  from {
    opacity: 0;
    transform: scale(0.9) translateY(20px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.pay-flow-steps {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  margin-bottom: 6px;
}

.step-dot {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid var(--border-color);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-muted);
  transition: all 0.2s ease;

  &.active {
    background: var(--primary-color);
    border-color: var(--primary-color);
    color: #000;
  }
}

.step-line {
  width: 32px;
  height: 2px;
  background: var(--border-color);
}

.pay-flow-hint {
  font-size: 12px;
  color: var(--text-muted);
  text-align: center;
  margin-bottom: 20px;
}

@media (min-width: 576px) {
  .dialog-content {
    max-width: 400px;
    padding: 36px 32px;
  }
}
</style>
