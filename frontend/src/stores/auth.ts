import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { authApi } from '@/api'
import { usePlayerStore } from '@/stores/player'
import { useRealtimeStore } from '@/stores/realtime'
import type { UserProfile } from '@/types/api'

const TOKEN_KEY = 'musicplatform_token'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) ?? '')
  const profile = ref<UserProfile | null>(null)
  const ready = ref(false)
  const bootstrapping = ref(false)

  const isAuthenticated = computed(() => Boolean(token.value))

  function setToken(value: string) {
    token.value = value
    localStorage.setItem(TOKEN_KEY, value)
  }

  function clearSession() {
    useRealtimeStore().stop()
    usePlayerStore().clear()
    token.value = ''
    profile.value = null
    localStorage.removeItem(TOKEN_KEY)
  }

  async function refreshProfile() {
    profile.value = await authApi.getMyProfile()
    return profile.value
  }

  async function bootstrap() {
    if (ready.value || bootstrapping.value) {
      return
    }
    bootstrapping.value = true
    try {
      if (token.value) {
        void useRealtimeStore().start(token.value)
        await refreshProfile()
      }
    } catch (error) {
      clearSession()
      throw error
    } finally {
      ready.value = true
      bootstrapping.value = false
    }
  }

  async function login(payload: { account: string; password: string }) {
    const nextToken = await authApi.login(payload)
    setToken(nextToken)
    void useRealtimeStore().start(nextToken)
    await refreshProfile()
  }

  async function register(payload: {
    username: string
    email: string
    password: string
    avatarUrl?: string | null
    gender?: string
  }) {
    await authApi.register(payload)
  }

  async function logout() {
    try {
      if (token.value) {
        await authApi.logout()
      }
    } finally {
      clearSession()
    }
  }

  return {
    token,
    profile,
    ready,
    isAuthenticated,
    bootstrap,
    login,
    register,
    logout,
    refreshProfile
  }
})
