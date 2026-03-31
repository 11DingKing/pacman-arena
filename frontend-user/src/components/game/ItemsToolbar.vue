<template>
  <div 
    v-if="items.length > 0" 
    class="items-toolbar" 
    :class="{ expanded, dragging }"
    :style="toolbarStyle"
    @touchstart="onDragStart"
    @touchmove="onDragMove"
    @touchend="onDragEnd"
    @mousedown="onDragStart"
  >
    <!-- 收起状态：仅显示背包图标 -->
    <button v-if="!expanded" class="toolbar-toggle" @click.stop="handleToggle">
      <span class="toggle-icon">🎒</span>
      <span class="toggle-badge">{{ totalCount }}</span>
    </button>

    <!-- 展开状态：竖向道具列表 -->
    <div v-else class="toolbar-content">
      <button class="toolbar-close" @click="toggleExpanded">✕</button>
      <div class="toolbar-items">
        <button
          v-for="item in items"
          :key="item.id"
          class="toolbar-item"
          @click="handleUseItem(item)"
        >
          <span class="item-icon">{{ getItemIcon(item) }}</span>
          <span class="item-count">{{ item.quantity }}</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';

const props = defineProps({
  items: {
    type: Array,
    default: () => [],
  },
});

const emit = defineEmits(['use-item', 'toggle-expanded']);

// 展开/收起状态
const expanded = ref(false);
const dragging = ref(false);
const hasMoved = ref(false);

// 暴露收起方法供父组件调用
function collapse() {
  if (expanded.value) {
    expanded.value = false;
    emit('toggle-expanded', false);
  }
}

defineExpose({ collapse });

// 位置状态（默认右侧，与导航对称）
const position = ref({ x: 6, y: 200 });

// 计算样式
const toolbarStyle = computed(() => ({
  right: `${position.value.x}px`,
  bottom: `${position.value.y}px`,
}));

// 拖拽相关
let startX = 0;
let startY = 0;
let startPosX = 0;
let startPosY = 0;

function onDragStart(e) {
  if (expanded.value) return;
  
  dragging.value = true;
  hasMoved.value = false;
  
  const touch = e.touches ? e.touches[0] : e;
  startX = touch.clientX;
  startY = touch.clientY;
  startPosX = position.value.x;
  startPosY = position.value.y;
  
  if (!e.touches) {
    window.addEventListener('mousemove', onDragMove);
    window.addEventListener('mouseup', onDragEnd);
  }
}

function onDragMove(e) {
  if (!dragging.value) return;
  
  const touch = e.touches ? e.touches[0] : e;
  // X轴反转，因为用right定位
  const deltaX = startX - touch.clientX;
  // Y轴反转，因为用bottom定位
  const deltaY = startY - touch.clientY;
  
  if (Math.abs(deltaX) > 5 || Math.abs(deltaY) > 5) {
    hasMoved.value = true;
  }
  
  // 限制在屏幕范围内
  const maxX = window.innerWidth - 60;
  const maxY = window.innerHeight - 60;
  
  position.value.x = Math.max(0, Math.min(maxX, startPosX + deltaX));
  position.value.y = Math.max(0, Math.min(maxY, startPosY + deltaY));
}

function onDragEnd() {
  dragging.value = false;
  
  window.removeEventListener('mousemove', onDragMove);
  window.removeEventListener('mouseup', onDragEnd);
  
  // 保存位置到 localStorage
  localStorage.setItem('items-toolbar-position', JSON.stringify(position.value));
}

function handleToggle() {
  if (!hasMoved.value) {
    toggleExpanded();
  }
}

// 切换展开状态
function toggleExpanded() {
  expanded.value = !expanded.value;
  emit('toggle-expanded', expanded.value);
}

// 道具总数量
const totalCount = computed(() => {
  return props.items.reduce((sum, item) => sum + (item.quantity || 0), 0);
});

// 道具图标映射
const iconMap = {
  SPEED_UP: "🚀",
  INVINCIBLE: "🛡️",
  DOUBLE_SCORE: "⭐",
  EXTRA_LIFE: "❤️",
  MAGNET: "🧲",
};

function getItemIcon(item) {
  if (!item) return "📦";
  if (item.icon && item.icon.length <= 4 && !/[a-zA-Z0-9]/.test(item.icon)) {
    return item.icon;
  }
  return iconMap[item.effectType] || "📦";
}

function handleUseItem(item) {
  emit('use-item', item);
  // 使用后如果道具用完，自动收起
  if (props.items.length <= 1 && item.quantity <= 1) {
    expanded.value = false;
  }
}

// 加载保存的位置
onMounted(() => {
  const saved = localStorage.getItem('items-toolbar-position');
  if (saved) {
    try {
      const pos = JSON.parse(saved);
      // 确保位置在屏幕范围内
      position.value.x = Math.max(0, Math.min(window.innerWidth - 60, pos.x));
      position.value.y = Math.max(0, Math.min(window.innerHeight - 60, pos.y));
    } catch (e) {
      // 忽略解析错误
    }
  }
});
</script>

<style lang="scss" scoped>
.items-toolbar {
  position: fixed;
  z-index: 50;
  touch-action: none;
  
  &.dragging {
    .toolbar-toggle {
      cursor: grabbing;
      transform: scale(1.1);
      animation: none;
    }
  }
}

// 收起状态 - 浮动按钮
.toolbar-toggle {
  position: relative;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(
    145deg,
    rgba(26, 31, 53, 0.95) 0%,
    rgba(13, 18, 32, 0.98) 100%
  );
  backdrop-filter: blur(20px);
  border: 2px solid rgba(255, 215, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: grab;
  box-shadow:
    0 4px 20px rgba(0, 0, 0, 0.4),
    0 0 15px rgba(255, 215, 0, 0.15);
  transition: transform 0.15s ease, border-color 0.15s ease;
  animation: pulse-glow 2s ease-in-out infinite;
  outline: none;
  user-select: none;

  .toggle-icon {
    font-size: 22px;
  }

  .toggle-badge {
    position: absolute;
    top: -4px;
    right: -4px;
    min-width: 18px;
    height: 18px;
    background: linear-gradient(135deg, #ff6b9d 0%, #ff4757 100%);
    color: #fff;
    font-size: 10px;
    font-weight: 700;
    border-radius: 9px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 4px;
    box-shadow: 0 2px 8px rgba(255, 75, 87, 0.4);
  }
}

@keyframes pulse-glow {
  0%, 100% {
    box-shadow:
      0 4px 20px rgba(0, 0, 0, 0.4),
      0 0 15px rgba(255, 215, 0, 0.15);
  }
  50% {
    box-shadow:
      0 4px 20px rgba(0, 0, 0, 0.4),
      0 0 25px rgba(255, 215, 0, 0.3);
  }
}

// 展开状态 - 竖向道具条
.toolbar-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 10px;
  background: linear-gradient(
    145deg,
    rgba(26, 31, 53, 0.95) 0%,
    rgba(13, 18, 32, 0.98) 100%
  );
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 215, 0, 0.3);
  border-radius: 24px;
  box-shadow:
    0 4px 24px rgba(0, 0, 0, 0.4),
    0 0 0 1px rgba(255, 255, 255, 0.04);
  animation: popIn 0.15s ease;
}

@keyframes popIn {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.toolbar-close {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  color: rgba(255, 255, 255, 0.5);
  font-size: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.15s ease;
  outline: none;
  user-select: none;

  &:hover {
    background: rgba(255, 255, 255, 0.2);
    color: #fff;
  }
}

.toolbar-items {
  display: flex;
  flex-direction: column;
  gap: 10px;
  overflow-y: auto;
  max-height: 220px;
  padding: 4px;

  // 隐藏滚动条
  scrollbar-width: none;
  &::-webkit-scrollbar {
    display: none;
  }
}

.toolbar-item {
  position: relative;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  flex-shrink: 0;
  transition: all 0.15s ease;
  outline: none;
  user-select: none;

  &:hover {
    background: rgba(255, 215, 0, 0.15);
    border-color: rgba(255, 215, 0, 0.4);
  }

  &:active {
    transform: scale(0.95);
  }

  .item-icon {
    font-size: 18px;
  }

  .item-count {
    position: absolute;
    top: -3px;
    right: -3px;
    min-width: 14px;
    height: 14px;
    background: #ffd700;
    color: #000;
    font-size: 9px;
    font-weight: 700;
    border-radius: 7px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 2px;
  }
}

// 小屏适配
@media (max-height: 600px) {
  .toolbar-toggle {
    width: 40px;
    height: 40px;

    .toggle-icon {
      font-size: 20px;
    }
  }

  .toolbar-item {
    width: 32px;
    height: 32px;
    
    .item-icon {
      font-size: 16px;
    }
  }

  .toolbar-close {
    width: 28px;
    height: 28px;
  }
}

// 平板及以上
@media (min-width: 768px) {
  .toolbar-toggle {
    width: 52px;
    height: 52px;

    .toggle-icon {
      font-size: 26px;
    }
  }

  .toolbar-items {
    max-height: 280px;
    gap: 6px;
  }

  .toolbar-item {
    width: 44px;
    height: 44px;

    .item-icon {
      font-size: 22px;
    }
  }

  .toolbar-close {
    width: 36px;
    height: 36px;
  }
}
</style>
