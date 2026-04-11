import { ref } from 'vue'
import { defineStore } from 'pinia'
import { connectMessageStream } from '@/api/realtime'
import type { MessageEventPayload } from '@/types/api'
import { useUiStore } from '@/stores/ui'

export const useRealtimeStore = defineStore('realtime', () => {
  const connected = ref(false)
  const connecting = ref(false)
  const lastEvent = ref<MessageEventPayload | null>(null)
  const inboxVersion = ref(0)

  let controller: AbortController | null = null
  let reconnectTimer: number | null = null
  let currentToken = ''

  function clearReconnectTimer() {
    if (reconnectTimer !== null) {
      window.clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
  }

  function stop() {
    clearReconnectTimer()
    currentToken = ''
    connected.value = false
    connecting.value = false
    controller?.abort()
    controller = null
  }

  function scheduleReconnect(token: string) {
    clearReconnectTimer()
    reconnectTimer = window.setTimeout(() => {
      reconnectTimer = null
      void start(token)
    }, 2000)
  }

  async function start(token: string) {
    if (!token) {
      stop()
      return
    }

    if (currentToken === token && (controller || connected.value || connecting.value)) {
      return
    }

    clearReconnectTimer()
    controller?.abort()
    controller = new AbortController()
    currentToken = token
    connecting.value = true
    connected.value = false

    const localController = controller

    try {
      await connectMessageStream(token, localController, {
        onOpen() {
          if (controller !== localController) {
            return
          }
          connecting.value = false
          connected.value = true
        },
        onMessage(payload) {
          lastEvent.value = payload
          inboxVersion.value += 1
          if (payload.type === 'NEW_MESSAGE') {
            useUiStore().info(`收到来自用户 #${payload.speakingUserId} 的新消息`)
          }
        },
        onError() {
          if (controller !== localController) {
            return
          }
          connecting.value = false
          connected.value = false
        }
      })
    } catch {
      if (controller !== localController || currentToken !== token) {
        return
      }
      connecting.value = false
      connected.value = false
      scheduleReconnect(token)
    }
  }

  return {
    connected,
    connecting,
    lastEvent,
    inboxVersion,
    start,
    stop
  }
})
