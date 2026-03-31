<template>
  <div class="auth-page">
    <div class="auth-container">
      <div class="auth-header">
        <div class="logo-wrapper">
          <div class="logo">🟡</div>
          <div class="logo-glow"></div>
        </div>
        <h1>创建账号</h1>
        <p>加入吃豆人，开启冒险之旅</p>
      </div>
      
      <form @submit.prevent="handleRegister" class="auth-form">
        <div class="form-group">
          <label>用户名</label>
          <div class="input-wrapper">
            <span class="input-icon">👤</span>
            <input v-model="form.username" type="text" class="input" placeholder="请输入用户名" required />
          </div>
        </div>
        
        <div class="form-group">
          <label>昵称 <span class="optional">(选填)</span></label>
          <div class="input-wrapper">
            <span class="input-icon">✨</span>
            <input v-model="form.nickname" type="text" class="input" placeholder="游戏中显示的名字" />
          </div>
        </div>
        
        <div class="form-group">
          <label>密码</label>
          <div class="input-wrapper">
            <span class="input-icon">🔒</span>
            <input v-model="form.password" type="password" class="input" placeholder="请输入密码" required />
          </div>
        </div>
        
        <div class="form-group">
          <label>确认密码</label>
          <div class="input-wrapper">
            <span class="input-icon">🔐</span>
            <input v-model="form.confirmPassword" type="password" class="input" placeholder="请再次输入密码" required />
          </div>
        </div>
        
        <button type="submit" class="btn btn-primary btn-block" :disabled="loading">
          <span v-if="loading" class="loading-spinner"></span>
          <span v-else>注册</span>
        </button>
      </form>
      
      <div class="auth-footer">
        <span>已有账号？</span>
        <router-link to="/login">立即登录</router-link>
      </div>
    </div>
    
    <div class="auth-decoration">
      <div class="dot dot-1"></div>
      <div class="dot dot-2"></div>
      <div class="ghost ghost-1">👻</div>
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

const form = ref({ username: '', nickname: '', password: '', confirmPassword: '' })
const loading = ref(false)

async function handleRegister() {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请填写用户名和密码')
    return
  }
  if (form.value.password !== form.value.confirmPassword) {
    ElMessage.warning('两次密码输入不一致')
    return
  }
  if (form.value.password.length < 6) {
    ElMessage.warning('密码长度至少6位')
    return
  }
  
  loading.value = true
  try {
    await userStore.register(form.value.username, form.value.password, form.value.nickname)
    ElMessage.success('注册成功！欢迎加入')
    router.push('/')
  } catch (e) {
    // 错误已在 API 拦截器中处理
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
  padding: 36px 28px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  backdrop-filter: blur(20px);
  position: relative;
  z-index: 1;
}

.auth-header {
  text-align: center;
  margin-bottom: 32px;
  
  .logo-wrapper {
    position: relative;
    display: inline-block;
    margin-bottom: 16px;
    
    .logo {
      font-size: 56px;
      position: relative;
      z-index: 1;
      animation: float 3s ease-in-out infinite;
    }
    
    .logo-glow {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      width: 90px;
      height: 90px;
      background: radial-gradient(circle, rgba(255, 215, 0, 0.4) 0%, transparent 70%);
      border-radius: 50%;
      animation: pulse 2s ease-in-out infinite;
    }
  }
  
  h1 {
    font-size: 26px;
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
    margin-bottom: 18px;
    
    label {
      display: block;
      font-size: 13px;
      font-weight: 500;
      color: var(--text-secondary);
      margin-bottom: 8px;
      
      .optional {
        font-weight: 400;
        color: var(--text-muted);
      }
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
    height: 50px;
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
    
    &:hover {
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
    
    &-1 { top: 25%; left: 12%; animation: float 4s ease-in-out infinite; }
    &-2 { bottom: 35%; right: 10%; animation: float 5s ease-in-out infinite 1s; }
  }
  
  .ghost {
    position: absolute;
    font-size: 28px;
    opacity: 0.3;
    
    &-1 { top: 18%; right: 12%; animation: float 5s ease-in-out infinite; }
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
