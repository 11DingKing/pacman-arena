<template>
  <div class="payment-page">
    <div class="page-header">
      <div class="header-left">
        <h2>支付配置</h2>
        <p class="header-desc">配置支付宝 SDK，支持模拟与真实环境</p>
      </div>
      <el-button type="primary" size="large" @click="handleSave" :loading="saving">
        保存配置
      </el-button>
    </div>

    <div class="config-content" v-loading="loading">
      <!-- 运行模式卡片 -->
      <div class="mode-card">
        <div class="mode-header">
          <span class="mode-title">运行模式</span>
          <el-tag :type="form.mock === 1 ? 'warning' : 'success'" effect="dark">
            {{ form.mock === 1 ? '模拟中' : '真实支付' }}
          </el-tag>
        </div>
        <div class="mode-options">
          <div 
            class="mode-option" 
            :class="{ active: form.mock === 1 }"
            @click="form.mock = 1"
          >
            <div class="option-icon">🧪</div>
            <div class="option-content">
              <div class="option-title">模拟模式</div>
              <div class="option-desc">不跳转支付宝，点击「模拟完成支付」即完成</div>
            </div>
            <div class="option-check" v-if="form.mock === 1">✓</div>
          </div>
          <div 
            class="mode-option" 
            :class="{ active: form.mock === 0 }"
            @click="form.mock = 0"
          >
            <div class="option-icon">💰</div>
            <div class="option-content">
              <div class="option-title">真实支付宝</div>
              <div class="option-desc">跳转支付宝收银台，真实扣款</div>
            </div>
            <div class="option-check" v-if="form.mock === 0">✓</div>
          </div>
        </div>
      </div>

      <!-- 真实环境配置 -->
      <div class="real-config" :class="{ disabled: form.mock === 1 }">
        <div class="config-header">
          <span class="config-title">🔐 支付宝密钥配置</span>
          <span class="config-tip" v-if="form.mock === 1">切换到真实模式后需填写</span>
        </div>
        
        <div class="config-grid">
          <div class="config-item">
            <label>应用 AppID</label>
            <el-input 
              v-model="form.appId" 
              placeholder="支付宝开放平台应用 ID" 
              :disabled="form.mock === 1"
              clearable 
            />
          </div>
          
          <div class="config-item">
            <label>网关地址</label>
            <el-select 
              v-model="form.gateway" 
              placeholder="选择环境" 
              :disabled="form.mock === 1"
              style="width: 100%"
            >
              <el-option label="🧪 沙箱环境" value="https://openapi-sandbox.dl.alipaydev.com/gateway.do" />
              <el-option label="🚀 正式环境" value="https://openapi.alipay.com/gateway.do" />
            </el-select>
          </div>
          
          <div class="config-item full">
            <label>应用私钥 <span class="label-tip">RSA2 格式</span></label>
            <el-input
              v-model="form.privateKey"
              type="textarea"
              :rows="3"
              placeholder="应用私钥（RSA2），完整 PEM 或一行格式"
              :disabled="form.mock === 1"
            />
            <div class="field-tip">若已配置过且未修改，可留空保留原值</div>
          </div>
          
          <div class="config-item full">
            <label>支付宝公钥 <span class="label-tip">验签用</span></label>
            <el-input
              v-model="form.publicKey"
              type="textarea"
              :rows="2"
              placeholder="支付宝公钥"
              :disabled="form.mock === 1"
            />
          </div>
        </div>

        <div class="config-header" style="margin-top: 24px;">
          <span class="config-title">🔗 回调地址配置</span>
        </div>
        
        <div class="config-grid">
          <div class="config-item full">
            <label>异步通知 URL</label>
            <el-input 
              v-model="form.notifyUrl" 
              placeholder="https://your-domain.com/api/payment/alipay/notify" 
              :disabled="form.mock === 1"
              clearable 
            />
            <div class="field-tip">需公网可访问，支付宝服务器回调地址</div>
          </div>
          
          <div class="config-item full">
            <label>同步返回 URL</label>
            <el-input 
              v-model="form.returnUrl" 
              placeholder="https://your-domain.com/payment/result" 
              :disabled="form.mock === 1"
              clearable 
            />
            <div class="field-tip">支付完成后浏览器跳转地址</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { paymentConfigApi } from '../api'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const saving = ref(false)
const form = ref({
  mock: 1,
  appId: '',
  privateKey: '',
  publicKey: '',
  gateway: 'https://openapi-sandbox.dl.alipaydev.com/gateway.do',
  notifyUrl: '',
  returnUrl: ''
})

async function loadConfig() {
  loading.value = true
  try {
    const res = await paymentConfigApi.getConfig()
    const d = res.data || {}
    form.value = {
      mock: d.mock != null ? d.mock : 1,
      appId: d.appId || '',
      privateKey: d.privateKey || '',
      publicKey: d.publicKey || '',
      gateway: d.gateway || 'https://openapi-sandbox.dl.alipaydev.com/gateway.do',
      notifyUrl: d.notifyUrl || '',
      returnUrl: d.returnUrl || ''
    }
  } catch (e) {
    ElMessage.error('加载配置失败')
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  saving.value = true
  try {
    const payload = { ...form.value }
    if (payload.privateKey === '****已配置****') delete payload.privateKey
    if (payload.publicKey === '****已配置****') delete payload.publicKey
    await paymentConfigApi.updateConfig(payload)
    ElMessage.success('配置已保存并生效')
    await loadConfig()
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadConfig()
})
</script>

<style lang="scss" scoped>
.payment-page {
  width: 100%;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 28px;

  .header-left {
    h2 {
      margin: 0 0 6px;
      font-size: 22px;
    }
    
    .header-desc {
      margin: 0;
      font-size: 14px;
      color: var(--text-secondary);
    }
  }
}

.config-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

// 模式选择卡片
.mode-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 24px;
}

.mode-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  
  .mode-title {
    font-size: 16px;
    font-weight: 600;
  }
}

.mode-options {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.mode-option {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.02);
  border: 2px solid var(--border-color);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.25s ease;
  
  &:hover {
    border-color: rgba(255, 255, 255, 0.15);
    background: rgba(255, 255, 255, 0.04);
  }
  
  &.active {
    border-color: var(--primary);
    background: rgba(255, 215, 0, 0.08);
  }
  
  .option-icon {
    font-size: 32px;
    flex-shrink: 0;
  }
  
  .option-content {
    flex: 1;
    
    .option-title {
      font-size: 15px;
      font-weight: 600;
      margin-bottom: 4px;
    }
    
    .option-desc {
      font-size: 12px;
      color: var(--text-secondary);
      line-height: 1.4;
    }
  }
  
  .option-check {
    width: 24px;
    height: 24px;
    background: var(--primary);
    color: #000;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    font-weight: 700;
    flex-shrink: 0;
  }
}

// 真实环境配置
.real-config {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 24px;
  transition: opacity 0.3s ease;
  
  &.disabled {
    opacity: 0.5;
    pointer-events: none;
  }
}

.config-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  
  .config-title {
    font-size: 15px;
    font-weight: 600;
  }
  
  .config-tip {
    font-size: 12px;
    color: var(--text-muted);
    padding: 4px 10px;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 4px;
  }
}

.config-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  
  .config-item {
    display: flex;
    flex-direction: column;
    gap: 8px;
    
    &.full {
      grid-column: 1 / -1;
    }
    
    label {
      font-size: 13px;
      font-weight: 500;
      color: var(--text-secondary);
      
      .label-tip {
        font-weight: 400;
        color: var(--text-muted);
        margin-left: 6px;
      }
    }
    
    .field-tip {
      font-size: 12px;
      color: var(--text-muted);
      margin-top: 4px;
    }
  }
}

// 响应式
@media (max-width: 768px) {
  .mode-options {
    grid-template-columns: 1fr;
  }
  
  .config-grid {
    grid-template-columns: 1fr;
    
    .config-item.full {
      grid-column: 1;
    }
  }
}
</style>
