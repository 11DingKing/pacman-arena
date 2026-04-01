<template>
  <div class="game-page">
    <!-- 得分提交提示 -->
    <transition name="toast">
      <div v-if="showScoreToast" class="score-toast">
        <span class="toast-icon">✓</span>
        <span>得分已提交！</span>
      </div>
    </transition>

    <!-- 死亡提示弹窗 -->
    <DeathHintModal :show="showDeathHint" :lives="lives" />

    <!-- 游戏结束弹窗 -->
    <GameOverModal
      v-if="gameState === 'over'"
      :score="score"
      :level="level"
      :lives="lives"
      :duration="formattedDuration"
      @restart="restartGame"
    />

    <!-- HUD 独立显示 -->
    <div class="hud-container">
      <GameHud
        :score="score"
        :level="level"
        :lives="lives"
        :score-multiplier="scoreMultiplier"
        :sound-enabled="userStore.soundEnabled"
        @toggle-sound="userStore.toggleSound"
        class="top-hud"
      />
    </div>

    <!-- 激活效果显示 -->
    <ActiveEffects
      :effects="activeEffects"
      :is-invincible="isInvincibleAfterDeath"
      class="top-effects"
    />

    <!-- 游戏画布容器 -->
    <div class="game-wrapper">
      <div
        class="game-container"
        :class="{ 'game-active': gameState === 'playing' }"
        :style="{ '--canvas-size': canvasSize + 'px' }"
      >
        <canvas
          ref="gameCanvas"
          :width="canvasSize"
          :height="canvasSize"
        ></canvas>

        <!-- 游戏开始界面 -->
        <div v-if="gameState === 'ready'" class="game-overlay overlay-ready">
          <div class="overlay-content">
            <div class="ready-icon">🎮</div>
            <h2>吃豆人</h2>
            <p>使用方向键或虚拟按键控制移动</p>
            <button class="btn-start" @click="startGame">
              <span class="btn-icon">▶</span>
              <span>开始游戏</span>
            </button>
          </div>
        </div>

        <!-- 游戏暂停界面 -->
        <div v-if="gameState === 'paused'" class="game-overlay overlay-paused">
          <div class="overlay-content">
            <div class="pause-icon">⏸️</div>
            <h2>游戏暂停</h2>
            <div class="pause-actions">
              <button class="btn-primary" @click="resumeGame">
                <span class="btn-icon">▶</span>
                <span>继续游戏</span>
              </button>
              <button class="btn-secondary" @click="restartGame">
                <span class="btn-icon">🔄</span>
                <span>重新开始</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 游戏控制 -->
    <GameControls
      v-if="
        gameState === 'playing' ||
        gameState === 'ready' ||
        gameState === 'paused'
      "
      :is-playing="gameState === 'playing'"
      @direction="handleTouch"
      @toggle-pause="togglePause"
    />

    <!-- 道具栏（始终显示，方便查看拥有的道具） -->
    <ItemsToolbar
      ref="itemsToolbarRef"
      :items="myItems"
      @use-item="useItem"
      @toggle-expanded="handleItemsToggle"
    />
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from "vue";
import { gameApi, itemApi } from "../api";
import { useUserStore } from "../stores/user";
import { ElMessage } from "element-plus";
import { GameEngine, GameEvents } from "../game";
import {
  GameHud,
  GameControls,
  GameOverModal,
  DeathHintModal,
  ItemsToolbar,
  ActiveEffects,
} from "../components/game";

// ===== 用户状态 =====
const userStore = useUserStore();

// ===== 游戏引擎 =====
const gameCanvas = ref(null);
let engine = null;

// ===== UI 状态 =====
const canvasSize = ref(320);
const score = ref(0);
const level = ref(1);
const lives = ref(3);
const gameState = ref("ready");
const myItems = ref([]);
const itemsToolbarRef = ref(null);
const showScoreToast = ref(false);
const showDeathHint = ref(false);
const isInvincibleAfterDeath = ref(false);
const formattedDuration = ref("0:00");

// 道具效果状态
const activeEffects = ref([]);
const scoreMultiplier = ref(1);

// 定时器
let continueTimeoutId = null;
let effectTimers = {};

// ===== 游戏引擎初始化 =====
function initEngine() {
  if (!gameCanvas.value) return;

  engine = new GameEngine(gameCanvas.value);
  canvasSize.value = engine.getCanvasSize();

  // 监听游戏事件
  engine.on(GameEvents.SCORE_CHANGE, (data) => {
    score.value = data.score;
  });
  engine.on(GameEvents.LEVEL_CHANGE, (data) => {
    level.value = data.level;
  });
  engine.on(GameEvents.LIVES_CHANGE, (data) => {
    lives.value = data.lives;
  });
  engine.on(GameEvents.STATE_CHANGE, (data) => {
    gameState.value = data.state;
  });
  engine.on(GameEvents.DEATH, (data) => {
    handleDeath(data.lives);
  });
  engine.on(GameEvents.GAME_OVER, (data) => {
    handleGameOver(data);
  });

  engine.init();
}

// ===== 游戏控制 =====
function startGame() {
  if (!engine) initEngine();
  engine.start();
  score.value = engine.getScore();
  level.value = engine.getLevel();
  lives.value = engine.getLives();
}

function togglePause() {
  if (!engine) return;
  if (gameState.value === "playing") engine.pause();
  else if (gameState.value === "paused") engine.resume();
  else if (gameState.value === "ready") startGame();
}

// 收起所有浮动面板（背包、导航）
function collapseAllPanels() {
  // 收起背包
  itemsToolbarRef.value?.collapse();
  // 通知 Layout 收起导航（通过自定义事件）
  window.dispatchEvent(new CustomEvent("collapse-nav"));
}

function resumeGame() {
  collapseAllPanels();
  if (engine) engine.resume();
}

function restartGame() {
  collapseAllPanels();
  clearContinueTimeout();
  showDeathHint.value = false;
  clearAllEffects();
  if (engine) {
    engine.restart();
    score.value = engine.getScore();
    level.value = engine.getLevel();
    lives.value = engine.getLives();
  }
}

// ===== 输入处理 =====
function handleTouch(direction) {
  if (gameState.value !== "playing" || !engine) return;
  engine.setDirection(direction);
}

// 背包展开/收起时控制暂停
function handleItemsToggle(isExpanded) {
  if (!engine) return;
  if (isExpanded && gameState.value === "playing") {
    engine.pause();
  } else if (!isExpanded && gameState.value === "paused") {
    engine.resume();
  }
}

function handleKeydown(e) {
  const keyMap = {
    ArrowUp: "up",
    ArrowDown: "down",
    ArrowLeft: "left",
    ArrowRight: "right",
    w: "up",
    W: "up",
    s: "down",
    S: "down",
    a: "left",
    A: "left",
    d: "right",
    D: "right",
  };
  if (keyMap[e.key]) {
    e.preventDefault();
    handleTouch(keyMap[e.key]);
  }
  if (e.key === "Escape" || e.key === "p" || e.key === "P") togglePause();
  if (e.key === " " && gameState.value === "ready") startGame();
}

// ===== 死亡处理 =====
function handleDeath(remainingLives) {
  if (remainingLives <= 0) return;
  showDeathHint.value = true;

  continueTimeoutId = setTimeout(() => {
    continueTimeoutId = null;
    showDeathHint.value = false;
    isInvincibleAfterDeath.value = true;
    setTimeout(() => {
      isInvincibleAfterDeath.value = false;
    }, 2000);
    if (engine) engine.continueAfterDeath();
  }, 2000);
}

function clearContinueTimeout() {
  if (continueTimeoutId) {
    clearTimeout(continueTimeoutId);
    continueTimeoutId = null;
  }
}

// ===== 游戏结束 =====
function handleGameOver(data) {
  showDeathHint.value = false;
  formattedDuration.value = engine ? engine.formatDuration() : "0:00";

  if (userStore.isLoggedIn) {
    gameApi
      .submitScore({
        score: data.score,
        level: data.level,
        duration: data.duration,
      })
      .then(() => {
        showScoreToast.value = true;
        setTimeout(() => {
          showScoreToast.value = false;
        }, 3000);
      })
      .catch((e) => console.error(e));
  }
}

// ===== 道具系统 =====
async function loadMyItems() {
  if (!userStore.isLoggedIn) return;
  try {
    const res = await itemApi.getMyItems();
    myItems.value = res.data || [];
  } catch (e) {
    console.error(e);
  }
}

async function useItem(item) {
  if (gameState.value !== "playing" && gameState.value !== "paused") {
    ElMessage.warning("请先开始游戏再使用道具");
    return;
  }
  // 如果游戏暂停，先恢复游戏
  if (gameState.value === "paused" && engine) {
    engine.resume();
  }
  if (item.quantity <= 0) {
    ElMessage.warning("道具数量不足");
    return;
  }
  if (!engine) return;

  try {
    await itemApi.useItem(item.itemId);
    const { effectType, duration = 10, effectValue = 50 } = item;
    const nameMap = {
      SPEED_UP: "加速药水",
      INVINCIBLE: "无敌护盾",
      DOUBLE_SCORE: "双倍积分",
      EXTRA_LIFE: "额外生命",
      MAGNET: "磁铁道具",
    };
    const itemName = nameMap[effectType] || item.name || "道具";

    switch (effectType) {
      case "SPEED_UP":
        engine.applySpeedUp(effectValue, duration);
        addActiveEffect("SPEED_UP", duration, `速度+${effectValue}%`);
        ElMessage.success(
          `🚀 ${itemName} - 速度提升${effectValue}%，持续${duration}秒`,
        );
        break;
      case "INVINCIBLE":
        engine.applyInvincible(duration);
        addActiveEffect("INVINCIBLE", duration, "无敌");
        ElMessage.success(`🛡️ ${itemName} - 无敌状态，持续${duration}秒`);
        break;
      case "DOUBLE_SCORE":
        engine.applyDoubleScore(duration);
        scoreMultiplier.value = 2;
        addActiveEffect("DOUBLE_SCORE", duration, "双倍积分");
        if (effectTimers.DOUBLE_SCORE) clearTimeout(effectTimers.DOUBLE_SCORE);
        effectTimers.DOUBLE_SCORE = setTimeout(() => {
          scoreMultiplier.value = 1;
        }, duration * 1000);
        ElMessage.success(`⭐ ${itemName} - 双倍积分，持续${duration}秒`);
        break;
      case "EXTRA_LIFE":
        engine.addLife();
        lives.value = engine.getLives();
        ElMessage.success(`❤️ ${itemName} - 生命+1，当前${lives.value}条命`);
        break;
      case "MAGNET":
        engine.applyMagnet(duration);
        addActiveEffect("MAGNET", duration, "磁铁");
        ElMessage.success(`🧲 ${itemName} - 磁铁效果，持续${duration}秒`);
        break;
      default:
        ElMessage.success(`使用了 ${itemName}`);
    }
    loadMyItems();
  } catch (e) {
    console.error(e);
    ElMessage.error("道具使用失败");
  }
}

// ===== 效果管理 =====
function addActiveEffect(type, duration, name) {
  const existing = activeEffects.value.find((e) => e.type === type);
  if (existing) {
    existing.endTime = Date.now() + duration * 1000;
  } else {
    activeEffects.value.push({
      type,
      name,
      endTime: Date.now() + duration * 1000,
    });
  }
  if (effectTimers[type]) clearTimeout(effectTimers[type]);
  effectTimers[type] = setTimeout(() => {
    activeEffects.value = activeEffects.value.filter((e) => e.type !== type);
  }, duration * 1000);
}

function clearAllEffects() {
  activeEffects.value = [];
  scoreMultiplier.value = 1;
  Object.values(effectTimers).forEach((timer) => clearTimeout(timer));
  effectTimers = {};
}

// ===== 生命周期 =====
watch(
  gameState,
  (s) => {
    document.body.style.overflow = s === "over" ? "hidden" : "";
  },
  { immediate: true },
);

onMounted(() => {
  initEngine();
  loadMyItems();
  window.addEventListener("keydown", handleKeydown);
});

onUnmounted(() => {
  document.body.style.overflow = "";
  clearContinueTimeout();
  clearAllEffects();
  if (engine) {
    engine.destroy();
    engine = null;
  }
  window.removeEventListener("keydown", handleKeydown);
});
</script>

<style lang="scss" scoped>
.game-page {
  min-height: 100vh;
  min-height: 100dvh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  padding: 4px;
  padding-top: 8px;
  background: linear-gradient(180deg, #0a0e1a 0%, #1a1f35 100%);
  overflow: hidden;
}

// 得分提交提示
.score-toast {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: #fff;
  padding: 10px 20px;
  border-radius: 30px;
  font-size: 13px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 4px 20px rgba(16, 185, 129, 0.4);
  z-index: 100;

  .toast-icon {
    width: 18px;
    height: 18px;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 11px;
  }
}

.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}
.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(-20px);
}

// 顶部 HUD - 独立显示
.top-hud {
  width: 100%;
  max-width: 340px;
  margin-bottom: 8px;

  :deep(.game-hud) {
    margin: 0;
    padding: 8px 12px;
    gap: 8px;
    background: rgba(30, 41, 59, 0.9);
    backdrop-filter: blur(12px);
    border-radius: 12px;
    border: 1px solid rgba(255, 215, 0, 0.2);

    .hud-item {
      padding: 4px 2px;
      background: transparent;
      border: none;
    }

    .hud-label {
      font-size: 10px;
      margin-bottom: 2px;
    }

    .hud-value {
      font-size: 20px;
    }

    .lives .life {
      font-size: 14px;
    }
  }
}

// 效果显示
.top-effects {
  margin-bottom: 6px;
}

.game-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  max-width: calc(100vw - 16px);
}

.game-container {
  position: relative;
  background: #0a0e1a;
  border: 2px solid rgba(37, 99, 235, 0.5);
  border-radius: 10px;
  padding: 4px;
  box-shadow:
    0 0 30px rgba(37, 99, 235, 0.3),
    inset 0 0 30px rgba(0, 0, 0, 0.5);

  &.game-active {
    border-color: rgba(255, 215, 0, 0.5);
    box-shadow:
      0 0 40px rgba(255, 215, 0, 0.3),
      inset 0 0 30px rgba(0, 0, 0, 0.5);
  }

  canvas {
    display: block;
    border-radius: 4px;
    width: var(--canvas-size);
    height: var(--canvas-size);
    // 更大的画布空间
    max-width: calc(100vw - 16px);
    max-height: calc(100dvh - 260px);
    object-fit: contain;
  }
}

.game-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(10, 14, 26, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  z-index: 10;

  .overlay-content {
    text-align: center;
    padding: 20px 16px;
    width: 100%;
    max-width: 280px;

    h2 {
      font-size: 24px;
      font-weight: 700;
      margin: 12px 0;
      color: #ffd700;
      text-shadow: 0 0 20px rgba(255, 215, 0, 0.5);
    }

    p {
      color: rgba(255, 255, 255, 0.6);
      font-size: 13px;
      margin-bottom: 20px;
    }
  }

  .ready-icon,
  .pause-icon {
    font-size: 48px;
    animation: bounce 1s ease-in-out infinite;
  }
}

// 按钮样式
.btn-start,
.btn-primary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 28px;
  background: #ffd700;
  color: #000;
  border: none;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(255, 215, 0, 0.4);
  }
  &:active {
    transform: translateY(0);
  }
}

.btn-secondary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 24px;
  background: transparent;
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 12px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
  width: 100%;

  &:hover {
    background: rgba(255, 255, 255, 0.1);
    border-color: rgba(255, 255, 255, 0.5);
    color: #ffd700;
  }
}

.pause-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  margin-top: 16px;
}

@keyframes bounce {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

// 小屏手机
@media (max-height: 700px) {
  .top-hud {
    margin-bottom: 4px;

    :deep(.game-hud) {
      padding: 4px 8px;
      .hud-label {
        font-size: 8px;
      }
      .hud-value {
        font-size: 16px;
      }
      .lives .life {
        font-size: 12px;
      }
    }
  }

  .top-effects {
    margin-bottom: 4px;
  }

  .game-container canvas {
    max-height: calc(100dvh - 260px);
  }
}

@media (max-height: 600px) {
  .top-hud :deep(.game-hud) {
    padding: 3px 6px;
    .hud-value {
      font-size: 14px;
    }
    .lives .life {
      font-size: 10px;
    }
  }

  .game-container canvas {
    max-height: calc(100dvh - 220px);
  }
}

// 正常手机
@media (min-width: 375px) and (min-height: 700px) {
  .top-hud {
    max-width: 360px;
  }

  .game-container canvas {
    max-height: calc(100dvh - 280px);
  }
}

// 平板及以上
@media (min-width: 576px) {
  .game-page {
    padding: 12px;
    justify-content: flex-start;
    padding-top: 16px;
  }

  .top-hud {
    max-width: 400px;
    margin-bottom: 12px;

    :deep(.game-hud) {
      padding: 10px 16px;
      .hud-value {
        font-size: 22px;
      }
      .lives .life {
        font-size: 16px;
      }
    }
  }

  .game-container {
    border-radius: 14px;
    padding: 8px;
    border-width: 3px;
  }
}

@media (min-width: 768px) {
  .game-page {
    padding-top: 24px;
  }

  .game-wrapper {
    max-width: 500px;
  }

  .top-hud {
    max-width: 460px;
    margin-bottom: 16px;

    :deep(.game-hud) {
      padding: 12px 20px;
      gap: 16px;
      .hud-value {
        font-size: 26px;
      }
    }
  }

  .game-container canvas {
    max-height: calc(100dvh - 240px);
  }
}

@media (min-width: 992px) {
  .game-wrapper {
    max-width: 560px;
  }

  .game-container {
    border-radius: 18px;
    padding: 10px;
  }
}
</style>
