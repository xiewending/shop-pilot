import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

import { loginApi } from '../api/auth'
import type { UserInfo } from '../types/auth'

const TOKEN_KEY = 'shop_pilot_token'
const USER_KEY = 'shop_pilot_user'

function readStoredUser() {
  const value = localStorage.getItem(USER_KEY)
  return value ? (JSON.parse(value) as UserInfo) : null
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) ?? '')
  const user = ref<UserInfo | null>(readStoredUser())
  const isLoggedIn = computed(() => Boolean(token.value))

  async function login(username: string, password: string) {
    const { data } = await loginApi({ username, password })
    token.value = data.data.token
    user.value = data.data.user
    localStorage.setItem(TOKEN_KEY, token.value)
    localStorage.setItem(USER_KEY, JSON.stringify(user.value))
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  return {
    token,
    user,
    isLoggedIn,
    login,
    logout
  }
})
