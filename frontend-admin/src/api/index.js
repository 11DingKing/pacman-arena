import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 防止同一错误在短时间内重复弹 toast（总体只弹一次）
let lastErrorTime = 0
let lastErrorMsg = ''

function showError(msg) {
  const now = Date.now()
  if (msg === lastErrorMsg && now - lastErrorTime < 1000) return
  lastErrorTime = now
  lastErrorMsg = msg
  ElMessage.error(msg)
}

api.interceptors.request.use(config => {
  const token = localStorage.getItem('admin_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      showError(res.message || '请求失败')
      if (res.code === 401) {
        localStorage.removeItem('admin_token')
        window.location.href = '/login'
      }
      return Promise.reject(res)
    }
    return res
  },
  error => {
    const msg = error.response?.data?.message || error.message || '网络错误'
    showError(msg)
    return Promise.reject(error)
  }
)

// Auth
export const authApi = {
  login: (data) => api.post('/auth/admin/login', data),
  getInfo: () => api.get('/auth/info')
}

// Admin User
export const userApi = {
  list: (params) => api.get('/admin/user/list', { params }),
  updateStatus: (data) => api.put('/admin/user/status', data),
  detail: (id) => api.get(`/admin/user/detail/${id}`)
}

// Admin Game
export const gameApi = {
  records: (params) => api.get('/admin/game/records', { params }),
  statistics: () => api.get('/admin/game/statistics')
}

// Admin Item
export const itemApi = {
  list: (params) => api.get('/admin/item/list', { params }),
  create: (data) => api.post('/admin/item/create', data),
  update: (data) => api.put('/admin/item/update', data),
  delete: (id) => api.delete(`/admin/item/delete/${id}`)
}

// Admin Order
export const orderApi = {
  list: (params) => api.get('/admin/order/list', { params }),
  statistics: () => api.get('/admin/order/statistics')
}

// Admin 支付配置（支付宝真实/模拟一键配置）
export const paymentConfigApi = {
  getConfig: () => api.get('/admin/payment/config'),
  updateConfig: (data) => api.put('/admin/payment/config', data)
}

export default api
