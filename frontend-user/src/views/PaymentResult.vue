<template>
  <div class="result-page">
    <div class="result-container">
      <div v-if="loading" class="result-card">
        <div class="paying-spinner large"></div>
        <h2>查询支付结果中...</h2>
        <p>请稍候</p>
      </div>
      <div v-else class="result-card">
        <div class="result-icon" :class="{ success: isSuccess }">
          <span v-if="isSuccess">✓</span>
          <span v-else>✕</span>
        </div>
        <h2>{{ isSuccess ? "支付成功" : "支付失败" }}</h2>
        <p v-if="isSuccess">道具已发放到您的账户</p>
        <p v-else>请稍后重试或联系客服</p>

        <div class="result-actions">
          <router-link to="/game" class="btn btn-primary">
            <span>🎮</span> 开始游戏
          </router-link>
          <router-link to="/shop" class="btn btn-ghost">返回商城</router-link>
        </div>
      </div>
    </div>

    <div class="confetti" v-if="isSuccess && !loading">
      <span
        v-for="i in 20"
        :key="i"
        class="confetti-piece"
        :style="getConfettiStyle(i)"
      ></span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRoute } from "vue-router";
import { paymentApi } from "../api";
import { useUserStore } from "../stores/user";

const route = useRoute();
const userStore = useUserStore();
const isSuccess = ref(true);
const loading = ref(false);

function getConfettiStyle(i) {
  const colors = ["#FFD700", "#00D4FF", "#FF6B9D", "#A855F7", "#10B981"];
  return {
    left: `${Math.random() * 100}%`,
    animationDelay: `${Math.random() * 2}s`,
    backgroundColor: colors[i % colors.length],
  };
}

async function pollOrderStatus(orderNo) {
  const maxAttempts = 20;
  const interval = 1500;
  for (let i = 0; i < maxAttempts; i++) {
    try {
      const res = await paymentApi.orderStatus(orderNo);
      const st = (res.data || {}).status;
      if (st === 1) {
        isSuccess.value = true;
        return;
      }
    } catch (e) {
      console.error(e);
    }
    await new Promise((r) => setTimeout(r, interval));
  }
  isSuccess.value = false;
}

onMounted(async () => {
  const orderNo = route.query.out_trade_no || route.query.orderNo;
  if (orderNo && userStore.isLoggedIn) {
    loading.value = true;
    await pollOrderStatus(orderNo);
    loading.value = false;
  } else {
    isSuccess.value = route.query.success !== "false";
  }
});
</script>

<style lang="scss" scoped>
.result-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
  overflow: hidden;
}

.result-container {
  width: 100%;
  max-width: 400px;
  z-index: 1;
}

.paying-spinner.large {
  width: 64px;
  height: 64px;
  margin: 0 auto 20px;
  border: 4px solid var(--border-color);
  border-top-color: var(--primary-color);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.result-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  padding: 50px 30px;
  text-align: center;

  .result-icon {
    width: 100px;
    height: 100px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 48px;
    margin: 0 auto 24px;
    background: rgba(239, 68, 68, 0.15);
    color: var(--danger);

    &.success {
      background: linear-gradient(
        135deg,
        rgba(16, 185, 129, 0.2) 0%,
        rgba(16, 185, 129, 0.1) 100%
      );
      color: var(--success);
      box-shadow: 0 0 40px rgba(16, 185, 129, 0.3);
      animation: pulse-success 2s ease-in-out infinite;
    }
  }

  h2 {
    font-size: 24px;
    font-weight: 700;
    margin-bottom: 8px;
  }

  p {
    color: var(--text-secondary);
    font-size: 14px;
    margin-bottom: 32px;
  }

  .result-actions {
    display: flex;
    flex-direction: column;
    gap: 12px;

    .btn {
      span {
        margin-right: 8px;
      }
    }
  }
}

@keyframes pulse-success {
  0%,
  100% {
    box-shadow: 0 0 40px rgba(16, 185, 129, 0.3);
  }
  50% {
    box-shadow: 0 0 60px rgba(16, 185, 129, 0.5);
  }
}

.confetti {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 0;

  .confetti-piece {
    position: absolute;
    width: 10px;
    height: 10px;
    top: -20px;
    border-radius: 2px;
    animation: confetti-fall 4s ease-in-out infinite;
  }
}

@keyframes confetti-fall {
  0% {
    transform: translateY(0) rotate(0deg);
    opacity: 1;
  }
  100% {
    transform: translateY(100vh) rotate(720deg);
    opacity: 0;
  }
}
</style>
