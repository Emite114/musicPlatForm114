import { fetchEventSource } from '@microsoft/fetch-event-source'
import type { MessageEventPayload } from '@/types/api'

export interface StreamHandlers {
  onOpen?: () => void
  onMessage?: (payload: MessageEventPayload, event: string) => void
  onError?: (error: unknown) => void
}

export async function connectMessageStream(
  token: string,
  controller: AbortController,
  handlers: StreamHandlers = {}
) {
  await fetchEventSource('/connect', {
    method: 'GET',
    headers: {
      Authorization: `Bearer ${token}`
    },
    signal: controller.signal,
    openWhenHidden: true,
    async onopen() {
      handlers.onOpen?.()
    },
    onmessage(event) {
      if (!event.data || event.event === 'connected') {
        return
      }
      try {
        const payload = JSON.parse(event.data) as MessageEventPayload
        handlers.onMessage?.(payload, event.event)
      } catch (error) {
        handlers.onError?.(error)
      }
    },
    onerror(error) {
      handlers.onError?.(error)
      throw error
    }
  })
}
