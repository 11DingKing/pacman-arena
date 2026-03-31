<template>
  <div class="layout" :class="{ 'has-bottom-nav': !isGamePage }">
    <router-view />
    
    <!-- 游戏页面：侧边可拖拽浮动导航 -->
    <div 
      v-if="isGamePage"
      class="side-nav" 
      :class="{ expanded, dragging }"
      :style="navStyle"
      @touchstart="onDragStart"
      @touchmove="onDragMove"
      @touchend="onDragEnd"
      @mousedown="onDragStart"
    >
      <!-- 收起状态：菜单按钮 -->
      <button v-if="!expanded" class="nav-toggle" @click.stop="handleToggle">
        <span class="current-icon">{{ currentIcon }}</span>
      </button>

      <!-- 展开状态：竖向导航菜单 -->
      <div v-else class="nav-menu">
        <button class="nav-close" @click="expanded = false">✕</button>
        <router-link 
          v-for="item in navItems" 
          :key="item.path"
          :to="item.path" 
          class="nav-item" 
          :class="{ active: route.path === item.path }"
          @click="expanded = false"
        >
          <span class="nav-icon">{{ item.icon }}</span>
        </router-link>
      </div>
    </div>

    <!-- 其他页面：底部固定导航栏 -->
    <nav v-else class="bottom-nav">
      <router-link 
        v-for="item in navItems" 
        :key="item.path"
        :to="item.path" 
        class="bottom-nav-item" 
        :class="{ active: route.path === item.path }"
      >
        <span class="bottom-nav-icon">{{ item.icon }}</span>
        <span class="bottom-nav-label">{{ item.name }}</span>
      </router-link>
    </nav>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const expanded = ref(false)
const dragging = ref(false)
const hasMoved = ref(false)

// 判断是否是游戏页面
const isGamePage = computed(() => route.path === '/game')

// 路由变化时收起导航
watch(() => route.path, () => {
  expanded.value = false
})

// 监听收起导航事件（从游戏页面触发）
function handleCollapseNav() {
  expanded.value = false
}

// 位置状态
const position = ref({ x: 6, y: 200 })

const navItems = [
  { path: '/', icon: '🏠', name: '首页' },
  { path: '/game', icon: '🎮', name: '游戏' },
  { path: '/ranking', icon: '🏆', name: '排行' },
  { path: '/shop', icon: '🛒', name: '商城' },
  { path: '/profile', icon: '👤', name: '我的' },
]

const currentIcon = computed(() => {
  const item = navItems.find(i => i.path === route.path)
  return item ? item.icon : '🎮'
})

const navStyle = computed(() => ({
  left: `${position.value.x}px`,
  bottom: `${position.value.y}px`,
}))

// 拖拽相关
let startX = 0
let startY = 0
let startPosX = 0
let startPosY = 0

function onDragStart(e) {
  if (expanded.value) return
  
  dragging.value = true
  hasMoved.value = false
  
  const touch = e.touches ? e.touches[0] : e
  startX = touch.clientX
  startY = touch.clientY
  startPosX = position.value.x
  startPosY = position.value.y
  
  if (!e.touches) {
    window.addEventListener('mousemove', onDragMove)
    window.addEventListener('mouseup', onDragEnd)
  }
}

function onDragMove(e) {
  if (!dragging.value) return
  
  const touch = e.touches ? e.touches[0] : e
  const deltaX = touch.clientX - startX
  const deltaY = startY - touch.clientY // Y轴反转，因为用bottom定位
  
  if (Math.abs(deltaX) > 5 || Math.abs(deltaY) > 5) {
    hasMoved.value = true
  }
  
  // 限制在屏幕范围内
  const maxX = window.innerWidth - 60
  const maxY = window.innerHeight - 60
  
  position.value.x = Math.max(0, Math.min(maxX, startPosX + deltaX))
  position.value.y = Math.max(0, Math.min(maxY, startPosY + deltaY))
}

function onDragEnd() {
  dragging.value = false
  
  window.removeEventListener('mousemove', onDragMove)
  window.removeEventListener('mouseup', onDragEnd)
  
  // 保存位置到 localStorage
  localStorage.setItem('nav-position', JSON.stringify(position.value))
}

function handleToggle() {
  if (!hasMoved.value) {
    expanded.value = true
  }
}

// 加载保存的位置
onMounted(() => {
  const saved = localStorage.getItem('nav-position')
  if (saved) {
    try {
      const pos = JSON.parse(saved)
      // 确保位置在屏幕范围内
      position.value.x = Math.max(0, Math.min(window.innerWidth - 60, pos.x))
      position.value.y = Math.max(0, Math.min(window.innerHeight - 60, pos.y))
    } catch (e) {
      // 忽略解析错误
    }
  }
  // 监听收起导航事件
  window.addEventListener('collapse-nav', handleCollapseNav)
})

onUnmounted(() => {
  window.removeEventListener('collapse-nav', handleCollapseNav)
})
</script>

<style lang="scss" scoped>
.layout {
  min-height: 100vh;
  min-height: 100dvh;

  // 非游戏页面需要为底部导航留空间
  &.has-bottom-nav {
    padding-bottom: 70px; // 56px导航 + 14px安全边距
  }
}

// ==========================================
// 底部导航栏（非游戏页面）
// ==========================================
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 56px;
  background: linear-gradient(
    180deg,
    rgba(26, 31, 53, 0.98) 0%,
    rgba(13, 18, 32, 0.99) 100%
  );
  backdrop-filter: blur(20px);
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding-bottom: env(safe-area-inset-bottom, 0);
  z-index: 100;
}

.bottom-nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  padding: 6px 12px;
  text-decoration: none;
  border-radius: 12px;
  transition: all 0.2s ease;
  outline: none;
  user-select: none;

  &:hover {
    background: rgba(255, 255, 255, 0.05);
  }

  &:active {
    transform: scale(0.95);
  }

  &.active {
    .bottom-nav-icon {
      transform: scale(1.1);
    }
    .bottom-nav-label {
      color: #ffd700;
    }
  }
}

.bottom-nav-icon {
  font-size: 22px;
  transition: transform 0.2s ease;
}

.bottom-nav-label {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.6);
  transition: color 0.2s ease;
}

// ==========================================
// 侧边浮动导航（游戏页面）
// ==========================================
.side-nav {
  position: fixed;
  z-index: 100;
  touch-action: none;
  
  &.dragging {
    .nav-toggle {
      cursor: grabbing;
      transform: scale(1.1);
    }
  }
}

// 收起状态 - 浮动按钮
.nav-toggle {
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
  outline: none;
  user-select: none;

  .current-icon {
    font-size: 22px;
  }
}

// 展开状态 - 竖向导航菜单
.nav-menu {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 6px;
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

.nav-close {
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
  transition: all 0.15s ease;
  outline: none;
  user-select: none;
  flex-shrink: 0;

  &:hover {
    background: rgba(255, 255, 255, 0.2);
    color: #fff;
  }
}

.nav-item {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  text-decoration: none;
  transition: all 0.15s ease;
  outline: none;
  user-select: none;
  flex-shrink: 0;

  &:hover {
    background: rgba(255, 215, 0, 0.15);
    border-color: rgba(255, 215, 0, 0.4);
  }

  &:active {
    transform: scale(0.95);
  }

  &.active {
    background: rgba(255, 215, 0, 0.2);
    border-color: rgba(255, 215, 0, 0.5);
    box-shadow: 0 0 10px rgba(255, 215, 0, 0.3);
  }

  .nav-icon {
    font-size: 18px;
  }
}

// ==========================================
// 响应式适配
// ==========================================

// 小屏适配
@media (max-height: 600px) {
  .nav-toggle {
    width: 40px;
    height: 40px;

    .current-icon {
      font-size: 20px;
    }
  }

  .nav-item {
    width: 32px;
    height: 32px;
    
    .nav-icon {
      font-size: 16px;
    }
  }

  .nav-close {
    width: 28px;
    height: 28px;
  }
}

// 平板及以上
@media (min-width: 768px) {
  .bottom-nav {
    height: 64px;
  }

  .bottom-nav-icon {
    font-size: 26px;
  }

  .bottom-nav-label {
    font-size: 12px;
  }

  .nav-toggle {
    width: 52px;
    height: 52px;

    .current-icon {
      font-size: 26px;
    }
  }

  .nav-item {
    width: 44px;
    height: 44px;

    .nav-icon {
      font-size: 22px;
    }
  }

  .nav-close {
    width: 36px;
    height: 36px;
  }
}
</style>
