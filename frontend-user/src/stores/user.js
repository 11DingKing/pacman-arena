import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '../api'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  
  const isLoggedIn = computed(() => !!token.value)
  
  async function login(username, password) {
    const res = await authApi.login({ username, password })
    token.value = res.data.token
    user.value = res.data.user
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('user', JSON.stringify(res.data.user))
    return res
  }
  
  async function register(username, password, nickname) {
    const res = await authApi.register({ username, password, nickname })
    token.value = res.data.token
    user.value = res.data.user
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('user', JSON.stringify(res.data.user))
    return res
  }
  
  async function fetchUserInfo() {
    const res = await authApi.getInfo()
    user.value = res.data
    localStorage.setItem('user', JSON.stringify(res.data))
    return res
  }
  
  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }
  
  return { token, user, isLoggedIn, login, register, fetchUserInfo, logout }
})
