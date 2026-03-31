<template>
  <div class="page-container shop-page">
    <div class="page-header">
      <h1 class="page-title">🛒 道具商城</h1>
      <p class="page-subtitle">购买道具，助你闯关</p>
    </div>

    <div class="items-grid">
      <ShopItemCard
        v-for="item in items"
        :key="item.id"
        :item="item"
        @buy="openBuyDialog"
      />
    </div>

    <div v-if="items.length === 0" class="empty-state">
      <div class="empty-icon">🏪</div>
      <p class="empty-text">商城正在补货中...</p>
    </div>

    <ShopMyItems :items="myItems" />

    <PaymentDialog
      :show="showDialog"
      :step="payStep"
      :item="selectedItem"
      :quantity="quantity"
      :total-price="totalPrice"
      :order-no="lastOrderNo"
      :is-mock-mode="isMockMode"
      :payment-mode-mock="paymentModeMock"
      :creating="creating"
      :confirming="confirming"
      :error="lastError"
      @close="closeDialog"
      @update:quantity="quantity = $event"
      @start-pay="startPay"
      @cancel-pay="cancelPay"
      @check-result="checkPayResult"
      @success="paySuccess"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { itemApi, paymentApi } from "../api";
import { ElMessage } from "element-plus";
import { ShopItemCard, ShopMyItems, PaymentDialog } from "../components/shop";

const items = ref([]);
const myItems = ref([]);
const showDialog = ref(false);
const selectedItem = ref(null);
const quantity = ref(1);
const payStep = ref("confirm"); // confirm, paying, success
const lastOrderNo = ref(""); // 最近创建的订单号，用于确认支付
const confirming = ref(false); // 正在确认支付
const creating = ref(false); // 正在创建订单，防重复点击
const lastError = ref(""); // 弹窗内持久错误提示，避免只闪 toast
const isMockMode = ref(true); // 当前订单是否为模拟环境（来自 createOrder 返回）
const paymentModeMock = ref(true); // 后端支付模式：模拟则展示蓝框提示，真实则隐藏

// 道具图标映射表
const iconMap = {
  SPEED_UP: "🚀",
  INVINCIBLE: "🛡️",
  DOUBLE_SCORE: "⭐",
  EXTRA_LIFE: "❤️",
  MAGNET: "🧲",
};

// 计算总价
const totalPrice = computed(() => {
  if (!selectedItem.value) return "0.00";
  return (selectedItem.value.price * quantity.value).toFixed(2);
});

function getItemIcon(item) {
  if (item.icon && item.icon.length <= 4 && !/[a-zA-Z0-9]/.test(item.icon)) {
    return item.icon;
  }
  return iconMap[item.effectType] || "📦";
}

async function loadItems() {
  try {
    const res = await itemApi.getList();
    items.value = (res.data || []).map((item) => ({
      ...item,
      displayIcon: getItemIcon(item),
    }));
  } catch (e) {
    console.error(e);
  }
}

async function loadMyItems() {
  try {
    const res = await itemApi.getMyItems();
    myItems.value = (res.data || []).map((userItem) => ({
      ...userItem,
      displayIcon: getItemIcon(userItem),
      displayName: userItem.name || "未知道具",
      displayDesc:
        userItem.duration > 0 ? `持续 ${userItem.duration}秒` : "一次性",
    }));
  } catch (e) {
    console.error(e);
  }
}

async function loadPaymentMode() {
  try {
    const res = await paymentApi.mode();
    paymentModeMock.value = (res.data || {}).mock !== false;
  } catch (e) {
    paymentModeMock.value = true;
  }
}

function openBuyDialog(item) {
  selectedItem.value = item;
  quantity.value = 1;
  payStep.value = "confirm";
  lastOrderNo.value = "";
  confirming.value = false;
  creating.value = false;
  lastError.value = "";
  isMockMode.value = true;
  showDialog.value = true;
}

function closeDialog() {
  if (payStep.value === "paying") return; // 支付中需点「取消支付」关闭
  showDialog.value = false;
  payStep.value = "confirm";
  lastOrderNo.value = "";
  lastError.value = "";
}

// 开始支付 - 创建订单后进入支付中（模拟 / 真实双模式）
async function startPay() {
  lastError.value = "";
  if (creating.value) return;
  creating.value = true;
  try {
    const res = await paymentApi.createOrder({
      itemId: selectedItem.value.id,
      quantity: quantity.value,
    });
    const data = res.data || {};
    lastOrderNo.value = data.orderNo || "";
    if (!lastOrderNo.value) {
      lastError.value = "创建订单失败，未返回订单号，请重试。";
      return;
    }
    isMockMode.value = data.mock !== false;
    payStep.value = "paying";
    ElMessage.success("订单已创建，请完成支付");
    // 真实环境：新窗口打开支付宝表单并自动提交，跳转支付宝
    if (!isMockMode.value && data.payForm) {
      try {
        const w = window.open("", "_blank");
        if (w) {
          w.document.write(data.payForm);
          w.document.close();
        }
      } catch (e) {
        console.error(e);
        lastError.value = "跳转支付宝失败，请重试或使用模拟支付。";
      }
    }
  } catch (e) {
    console.error(e);
    const msg = e?.response?.data?.message || e?.message || "创建订单失败";
    lastError.value = /重试|稍后/.test(msg) ? msg : msg + " 请重试。";
  } finally {
    creating.value = false;
  }
}

// 模拟完成支付（模拟）或 查询支付结果（真实）- 统一按钮「我已完成支付」
async function checkPayResult() {
  if (!lastOrderNo.value) {
    ElMessage.warning("请先创建订单");
    return;
  }
  if (confirming.value) return;
  lastError.value = "";
  confirming.value = true;
  try {
    if (isMockMode.value) {
      await paymentApi.confirmOrder(lastOrderNo.value);
      payStep.value = "success";
      lastOrderNo.value = "";
      ElMessage.success("支付成功，道具已发放");
    } else {
      const res = await paymentApi.orderStatus(lastOrderNo.value);
      const st = (res.data || {}).status;
      if (st === 1) {
        payStep.value = "success";
        lastOrderNo.value = "";
        ElMessage.success("支付成功，道具已发放");
      } else {
        lastError.value = "尚未支付或处理中，请在支付宝完成支付后重试。";
      }
    }
  } catch (e) {
    console.error(e);
    const msg = e?.response?.data?.message || e?.message || "查询失败";
    lastError.value = /重试|稍后/.test(msg) ? msg : msg + " 请重试。";
  } finally {
    confirming.value = false;
  }
}

// 取消支付
function cancelPay() {
  payStep.value = "confirm";
  lastOrderNo.value = "";
  confirming.value = false;
  lastError.value = "";
  ElMessage.warning("支付已取消");
}

// 支付成功，关闭弹窗并刷新背包
async function paySuccess() {
  showDialog.value = false;
  payStep.value = "confirm";
  lastOrderNo.value = "";
  await loadMyItems(); // 刷新背包
}

onMounted(() => {
  loadItems();
  loadMyItems();
  loadPaymentMode();
});
</script>

<style lang="scss" scoped>
.shop-page {
  // padding-bottom 由 Layout.vue 统一管理
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
  .page-subtitle {
    color: var(--text-secondary);
    font-size: 14px;
    margin-top: 8px;
  }
}

.items-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
}

// 响应式适配
@media (min-width: 576px) {
  .items-grid {
    gap: 20px;
  }
}

@media (min-width: 768px) {
  .items-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 24px;
  }
}

@media (min-width: 992px) {
  .items-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (min-width: 1200px) {
  .items-grid {
    grid-template-columns: repeat(5, 1fr);
  }
}
</style>
