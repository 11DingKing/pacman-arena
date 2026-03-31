<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="logo">
        <span class="logo-icon">🟡</span>
        <span class="logo-text">Pac-Man Admin</span>
      </div>
      
      <el-menu :default-active="route.path" router>
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据概览</span>
        </el-menu-item>
        <el-menu-item index="/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/games">
          <el-icon><Trophy /></el-icon>
          <span>游戏记录</span>
        </el-menu-item>
        <el-menu-item index="/items">
          <el-icon><Present /></el-icon>
          <span>道具管理</span>
        </el-menu-item>
        <el-menu-item index="/orders">
          <el-icon><List /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/payment">
          <el-icon><Wallet /></el-icon>
          <span>支付配置</span>
        </el-menu-item>
      </el-menu>
      
      <div class="sidebar-footer">
        <div class="version">v1.0.0</div>
      </div>
    </aside>
    
    <div class="main-wrapper">
      <header class="header">
        <div class="header-left">
          <span class="breadcrumb">{{ currentPageTitle }}</span>
        </div>
        <div class="header-right">
          <div class="user-info">
            <div class="user-avatar">{{ user?.nickname?.charAt(0) || 'A' }}</div>
            <span class="user-name">{{ user?.nickname || user?.username }}</span>
          </div>
          <el-button type="text" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
          </el-button>
        </div>
      </header>
      
      <main class="main-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const user = computed(() => userStore.user)

const pageTitles = {
  '/dashboard': '数据概览',
  '/users': '用户管理',
  '/games': '游戏记录',
  '/items': '道具管理',
  '/orders': '订单管理',
  '/payment': '支付配置'
}

const currentPageTitle = computed(() => pageTitles[route.path] || '管理后台')

function handleLogout() {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style lang="scss" scoped>
.layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.sidebar {
  width: var(--sidebar-width);
  background: var(--bg-sidebar);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  
  .sidebar-footer {
    margin-top: auto;
    padding: 16px;
    text-align: center;
    border-top: 1px solid var(--border-color);
    
    .version {
      font-size: 12px;
      color: var(--text-muted);
    }
  }
}

.main-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 24px;
  min-height: 0;

  :deep(> *) {
    flex: 1;
    min-height: 0;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }
}
</style>
