import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '../api'
import { soundManager } from '../game/SoundManager'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const soundEnabled = ref(
    user.value?.soundEnabled !== undefined ? user.value.soundEnabled : true
  )
  
  const isLoggedIn = computed(() => !!token.value)
  
  async function login(username, password) {
    const res = await authApi.login({ username, password })
    token.value = res.data.token
    user.value = res.data.user
    soundEnabled.value = res.data.user.soundEnabled !== undefined ? res.data.user.soundEnabled : true
    soundManager.setEnabled(soundEnabled.value)
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('user', JSON.stringify(res.data.user))
    return res
  }
  
  async function register(username, password, nickname) {
    const res = await authApi.register({ username, password, nickname })
    token.value = res.data.token
    user.value = res.data.user
    soundEnabled.value = res.data.user.soundEnabled !== undefined ? res.data.user.soundEnabled : true
    soundManager.setEnabled(soundEnabled.value)
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('user', JSON.stringify(res.data.user))
    return res
  }
  
  async function fetchUserInfo() {
    const res = await authApi.getInfo()
    user.value = res.data
    soundEnabled.value = res.data.soundEnabled !== undefined ? res.data.soundEnabled : true
    soundManager.setEnabled(soundEnabled.value)
    localStorage.setItem('user', JSON.stringify(res.data))
    return res
  }
  
  async function fetchUserSettings() {
    const res = await authApi.getSettings()
    soundEnabled.value = res.data.soundEnabled !== undefined ? res.data.soundEnabled : true
    soundManager.setEnabled(soundEnabled.value)
    return res
  }
  
  async function updateSoundSettings(enabled) {
    soundEnabled.value = enabled
    soundManager.setEnabled(enabled)
    if (isLoggedIn.value) {
      await authApi.updateSettings({ soundEnabled: enabled })
      if (user.value) {
        user.value.soundEnabled = enabled
        localStorage.setItem('user', JSON.stringify(user.value))
      }
    }
  }
  
  function toggleSound() {
    return updateSoundSettings(!soundEnabled.value)
  }
  
  function logout() {
    token.value = ''
    user.value = null
    soundEnabled.value = true
    soundManager.setEnabled(true)
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }
  
  return {
    token,
    user,
    isLoggedIn,
    soundEnabled,
    login,
    register,
    fetchUserInfo,
    fetchUserSettings,
    updateSoundSettings,
    toggleSound,
    logout
  }
})
