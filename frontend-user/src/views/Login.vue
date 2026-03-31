<template>
  <div class="auth-page">
    <div class="auth-container">
      <div class="auth-header">
        <div class="logo-wrapper">
          <div class="logo">🟡</div>
          <div class="logo-glow"></div>
        </div>
        <h1>欢迎回来</h1>
        <p>登录您的账号继续游戏</p>
      </div>
      
      <form @submit.prevent="handleLogin" class="auth-form">
        <div class="form-group">
          <label>用户名</label>
          <div class="input-wrapper">
            <span class="input-icon">👤</span>
            <input v-model="form.username" type="text" class="input" placeholder="请输入用户名" required />
          </div>
        </div>
        
        <div class="form-group">
          <label>密码</label>
          <div class="input-wrapper">
            <span class="input-icon">🔒</span>
            <input v-model="form.password" type="password" class="input" placeholder="请输入密码" required />
          </div>
        </div>
        
        <button type="submit" class="btn btn-primary btn-block" :disabled="loading">
          <span v-if="loading" class="loading-spinner"></span>
          <span v-else>登录</span>
        </button>
      </form>
      
      <div class="auth-footer">
        <span>还没有账号？</span>
        <router-link to="/register">立即注册</router-link>
      </div>
    </div>
    
    <div class="auth-decoration">
      <div class="dot dot-1"></div>
      <div class="dot dot-2"></div>
      <div class="dot dot-3"></div>
      <div class="ghost ghost-1">👻</div>
      <div class="ghost ghost-2">👻</div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const form = ref({ username: '', password: '' })
const loading = ref(false)

async function handleLogin() {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请填写用户名和密码')
    return
  }
  
  loading.value = true
  try {
    await userStore.login(form.value.username, form.value.password)
    ElMessage.success('登录成功！')
    router.push(route.query.redirect || '/')
  } catch (e) {
    // 错误已在 API 拦截器中处理，这里不再重复提示
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: 
      radial-gradient(ellipse at 30% 20%, rgba(255, 215, 0, 0.1) 0%, transparent 50%),
      radial-gradient(ellipse at 70% 80%, rgba(0, 212, 255, 0.08) 0%, transparent 50%);
    z-index: 0;
  }
}

.auth-container {
  width: 100%;
  max-width: 400px;
  padding: 40px 30px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  backdrop-filter: blur(20px);
  position: relative;
  z-index: 1;
}

.auth-header {
  text-align: center;
  margin-bottom: 36px;
  
  .logo-wrapper {
    position: relative;
    display: inline-block;
    margin-bottom: 20px;
    
    .logo {
      font-size: 60px;
      position: relative;
      z-index: 1;
      animation: float 3s ease-in-out infinite;
    }
    
    .logo-glow {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      width: 100px;
      height: 100px;
      background: radial-gradient(circle, rgba(255, 215, 0, 0.4) 0%, transparent 70%);
      border-radius: 50%;
      animation: pulse 2s ease-in-out infinite;
    }
  }
  
  h1 {
    font-size: 28px;
    font-weight: 700;
    margin-bottom: 8px;
    background: linear-gradient(135deg, var(--primary-color) 0%, #fff 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }
  
  p {
    color: var(--text-secondary);
    font-size: 14px;
  }
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

@keyframes pulse {
  0%, 100% { opacity: 0.5; transform: translate(-50%, -50%) scale(1); }
  50% { opacity: 0.8; transform: translate(-50%, -50%) scale(1.1); }
}

.auth-form {
  .form-group {
    margin-bottom: 20px;
    
    label {
      display: block;
      font-size: 13px;
      font-weight: 500;
      color: var(--text-secondary);
      margin-bottom: 8px;
    }
  }
  
  .input-wrapper {
    position: relative;
    
    .input-icon {
      position: absolute;
      left: 16px;
      top: 50%;
      transform: translateY(-50%);
      font-size: 16px;
      z-index: 1;
    }
    
    .input {
      padding-left: 48px;
    }
  }
  
  .btn-block {
    margin-top: 8px;
    height: 52px;
  }
}

.auth-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: var(--text-secondary);
  
  a {
    color: var(--primary-color);
    text-decoration: none;
    font-weight: 600;
    margin-left: 6px;
    transition: color var(--transition-fast);
    
    &:hover {
      color: var(--primary-light);
      text-decoration: underline;
    }
  }
}

.auth-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
  
  .dot {
    position: absolute;
    width: 8px;
    height: 8px;
    background: var(--primary-color);
    border-radius: 50%;
    opacity: 0.6;
    
    &-1 { top: 20%; left: 10%; animation: float 4s ease-in-out infinite; }
    &-2 { top: 60%; right: 15%; animation: float 5s ease-in-out infinite 1s; }
    &-3 { bottom: 30%; left: 20%; animation: float 6s ease-in-out infinite 2s; }
  }
  
  .ghost {
    position: absolute;
    font-size: 30px;
    opacity: 0.3;
    
    &-1 { top: 15%; right: 10%; animation: float 5s ease-in-out infinite; }
    &-2 { bottom: 20%; left: 8%; animation: float 6s ease-in-out infinite 1.5s; }
  }
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(0, 0, 0, 0.2);
  border-top-color: #000;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
