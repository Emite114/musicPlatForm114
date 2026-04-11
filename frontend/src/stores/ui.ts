import { ref } from 'vue'
import { defineStore } from 'pinia'

export interface ToastItem {
  id: number
  type: 'success' | 'error' | 'info'
  message: string
}

export const useUiStore = defineStore('ui', () => {
  const toasts = ref<ToastItem[]>([])

  function push(type: ToastItem['type'], message: string) {
    const item = {
      id: Date.now() + Math.floor(Math.random() * 1000),
      type,
      message
    }
    toasts.value.push(item)
    window.setTimeout(() => {
      toasts.value = toasts.value.filter((toast) => toast.id !== item.id)
    }, 3000)
  }

  return {
    toasts,
    success(message: string) {
      push('success', message)
    },
    error(message: string) {
      push('error', message)
    },
    info(message: string) {
      push('info', message)
    }
  }
})
