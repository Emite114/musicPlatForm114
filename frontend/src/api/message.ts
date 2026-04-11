import { request } from '@/api/client'
import { mapConversation, mapMessage, mapPage } from '@/api/mappers'
import type { PageResult } from '@/types/api'

export const messageApi = {
  async getConversations(page = 0, size = 20) {
    const raw = await request<PageResult<any>>({
      method: 'GET',
      url: '/message/getConversationList',
      params: { page, size }
    })
    return mapPage(raw, mapConversation)
  },

  async getMessages(conversationId: number, page = 0, size = 20) {
    const raw = await request<PageResult<any>>({
      method: 'GET',
      url: '/message/getMessage',
      params: { conversationId, page, size }
    })
    return mapPage(raw, mapMessage)
  },

  sendMessage(payload: { content: string; receiveUserId: number }) {
    return request<any>({
      method: 'POST',
      url: '/message/sendMessage',
      data: payload
    })
  },

  markConversationRead(conversationId: number) {
    return request<null>({
      method: 'POST',
      url: '/message/markAsRead',
      data: conversationId
    })
  }
}
