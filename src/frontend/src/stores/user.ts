import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, getCurrentUser } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<any>(null)
  const roles = ref<string[]>([])

  const isLoggedIn = computed(() => !!token.value)

  async function loginAction(username: string, password: string) {
    const res = await login({ username, password })
    if (res.code === 200 && res.data) {
      token.value = res.data.accessToken
      userInfo.value = res.data
      roles.value = res.data.roles || []
      localStorage.setItem('token', res.data.accessToken)
      localStorage.setItem('refreshToken', res.data.refreshToken)
      return true
    }
    return false
  }

  async function fetchUserInfo() {
    if (!token.value) return
    try {
      const res = await getCurrentUser()
      if (res.code === 200) {
        userInfo.value = res.data
      }
    } catch (e) {
      console.error('获取用户信息失败', e)
    }
  }

  function logoutAction() {
    token.value = ''
    userInfo.value = null
    roles.value = []
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
  }

  // 兼容别名
  const logout = logoutAction

  function hasRole(role: string) {
    return roles.value.includes(role)
  }

  return {
    token,
    userInfo,
    roles,
    isLoggedIn,
    loginAction,
    fetchUserInfo,
    logoutAction,
    logout,
    hasRole
  }
})
