import { request } from '@/api/client'
import { mapPage, mapReport } from '@/api/mappers'
import type { PageResult, ReportTargetType } from '@/types/api'

export const reportApi = {
  createReport(payload: {
    targetType: ReportTargetType
    targetId: number
    reason: string
  }) {
    return request<null>({
      method: 'POST',
      url: '/report/create',
      data: payload
    })
  },

  async getMyReports(page = 0, size = 10) {
    const raw = await request<PageResult<any>>({
      method: 'GET',
      url: '/report/getList',
      params: { page, size }
    })
    return mapPage(raw, mapReport)
  },

  getNextReport() {
    return request<any>({
      method: 'GET',
      url: '/admin/report/getNext'
    })
  },

  handleReport(payload: { id: number; handlerNote: string; result: string }) {
    return request<null>({
      method: 'POST',
      url: '/admin/report/handle',
      data: payload
    })
  },

  banUser(id: number) {
    return request<null>({
      method: 'DELETE',
      url: `/admin/banUser/${id}`
    })
  },

  banPost(id: number) {
    return request<null>({
      method: 'DELETE',
      url: `/admin/banPost/${id}`
    })
  },

  banPostComment(id: number) {
    return request<null>({
      method: 'DELETE',
      url: `/admin/banPostComment/${id}`
    })
  },

  banSong(id: number) {
    return request<null>({
      method: 'DELETE',
      url: `/admin/banSong/${id}`
    })
  },

  banSongComment(id: number) {
    return request<null>({
      method: 'DELETE',
      url: `/admin/banSongComment/${id}`
    })
  },

  addAdmin(id: number) {
    return request<null>({
      method: 'POST',
      url: `/superAdmin/addAdmin/${id}`
    })
  }
}
