<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="bg-gradient"></div>
      <div class="bg-grid"></div>
    </div>
    
    <div class="login-container">
      <div class="login-card">
        <div class="login-header">
          <div class="logo">
            <span class="logo-icon">🟡</span>
            <div class="logo-glow"></div>
          </div>
          <h1>Pac-Man Admin</h1>
          <p>管理后台登录</p>
        </div>
        
        <el-form :model="form" @submit.prevent="handleLogin" class="login-form">
          <el-form-item>
            <el-input 
              v-model="form.username" 
              placeholder="用户名" 
              prefix-icon="User" 
              size="large"
              clearable
            />
          </el-form-item>
          <el-form-item>
            <el-input 
              v-model="form.password" 
              type="password" 
              placeholder="密码" 
              prefix-icon="Lock" 
              size="large"
              show-password
            />
          </el-form-item>
          <el-form-item>
            <el-button 
              type="primary" 
              native-type="submit" 
              :loading="loading" 
              size="large" 
              class="login-btn"
            >
              {{ loading ? '登录中...' : '登录' }}
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="login-footer">
          <p>默认账号: admin / admin123</p>
        </div>
      </div>
      
      <div class="login-info">
        <div class="info-item">
          <span class="info-icon">📊</span>
          <span>实时数据监控</span>
        </div>
        <div class="info-item">
          <span class="info-icon">👥</span>
          <span>用户管理</span>
        </div>
        <div class="info-item">
          <span class="info-icon">🎮</span>
          <span>游戏记录查看</span>
        </div>
        <div class="info-item">
          <span class="info-icon">📦</span>
          <span>订单管理</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const form = ref({ username: '', password: '' })
const loading = ref(false)

async function handleLogin() {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  
  loading.value = true
  try {
    await userStore.login(form.value.username, form.value.password)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.login-bg {
  position: absolute;
  inset: 0;
  
  .bg-gradient {
    position: absolute;
    inset: 0;
    background: 
      radial-gradient(ellipse at 20% 20%, rgba(255, 215, 0, 0.08) 0%, transparent 50%),
      radial-gradient(ellipse at 80% 80%, rgba(59, 130, 246, 0.08) 0%, transparent 50%),
      linear-gradient(135deg, #0F172A 0%, #1E293B 100%);
  }
  
  .bg-grid {
    position: absolute;
    inset: 0;
    background-image: 
      linear-gradient(rgba(255, 255, 255, 0.02) 1px, transparent 1px),
      linear-gradient(90deg, rgba(255, 255, 255, 0.02) 1px, transparent 1px);
    background-size: 50px 50px;
  }
}

.login-container {
  display: flex;
  align-items: center;
  gap: 60px;
  position: relative;
  z-index: 1;
}

.login-card {
  width: 420px;
  padding: 48px 40px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.login-header {
  text-align: center;
  margin-bottom: 36px;
  
  .logo {
    position: relative;
    display: inline-block;
    margin-bottom: 20px;
    
    .logo-icon {
      font-size: 56px;
      position: relative;
      z-index: 1;
    }
    
    .logo-glow {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      width: 100px;
      height: 100px;
      background: radial-gradient(circle, rgba(255, 215, 0, 0.3) 0%, transparent 70%);
      border-radius: 50%;
      animation: pulse 2s ease-in-out infinite;
    }
  }
  
  h1 {
    font-size: 26px;
    font-weight: 700;
    margin-bottom: 8px;
    background: linear-gradient(135deg, var(--primary) 0%, #fff 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }
  
  p {
    color: var(--text-secondary);
    font-size: 14px;
  }
}

@keyframes pulse {
  0%, 100% { opacity: 0.5; transform: translate(-50%, -50%) scale(1); }
  50% { opacity: 0.8; transform: translate(-50%, -50%) scale(1.1); }
}

.login-form {
  .el-form-item {
    margin-bottom: 20px;
  }
  
  .login-btn {
    width: 100%;
    height: 48px;
    font-size: 16px;
    font-weight: 600;
  }
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  
  p {
    font-size: 12px;
    color: var(--text-muted);
  }
}

.login-info {
  display: flex;
  flex-direction: column;
  gap: 20px;
  
  .info-item {
    display: flex;
    align-items: center;
    gap: 14px;
    padding: 16px 20px;
    background: rgba(255, 255, 255, 0.02);
    border: 1px solid var(--border-color);
    border-radius: var(--radius-md);
    transition: all 0.3s ease;
    
    &:hover {
      border-color: var(--primary);
      background: rgba(255, 215, 0, 0.05);
      transform: translateX(4px);
    }
    
    .info-icon {
      font-size: 24px;
    }
    
    span:last-child {
      font-size: 14px;
      color: var(--text-secondary);
    }
  }
}

@media (max-width: 900px) {
  .login-container {
    flex-direction: column;
    gap: 30px;
  }
  
  .login-info {
    flex-direction: row;
    flex-wrap: wrap;
    justify-content: center;
    
    .info-item {
      padding: 12px 16px;
    }
  }
}
</style>
