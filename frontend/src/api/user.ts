import { request } from '@/api/client'
import { mapPage, mapPostCard, mapUserCard, mapUserSummary } from '@/api/mappers'
import type { PageResult } from '@/types/api'

export const userApi = {
  ifIsAdmin() {
    return request<boolean>({
      method: 'GET',
      url: '/user/ifIsAdmin'
    })
  },

  async searchUsers(keyword: string, page = 0, size = 10) {
    const raw = await request<PageResult<any>>({
      method: 'GET',
      url: '/user/search',
      params: { keyword, page, size }
    })
    return mapPage(raw, mapUserCard)
  },

  async getMyFollows(page = 0, size = 20) {
    const raw = await request<PageResult<any>>({
      method: 'GET',
      url: '/user/main/getFollows',
      params: { page, size }
    })
    return mapPage(raw, mapUserCard)
  },

  async getMyFans(page = 0, size = 20) {
    const raw = await request<PageResult<any>>({
      method: 'GET',
      url: '/user/main/getFans',
      params: { page, size }
    })
    return mapPage(raw, mapUserCard)
  },

  async getOnesFollows(id: number, page = 0, size = 20) {
    const raw = await request<PageResult<any>>({
      method: 'GET',
      url: '/user/getOnesFollows',
      params: { id, page, size }
    })
    return mapPage(raw, mapUserCard)
  },

  async getOnesFans(id: number, page = 0, size = 20) {
    const raw = await request<PageResult<any>>({
      method: 'GET',
      url: '/user/getOnesFans',
      params: { id, page, size }
    })
    return mapPage(raw, mapUserCard)
  },

  toggleFollow(id: number) {
    return request<null>({
      method: 'POST',
      url: `/user/follow/${id}`
    })
  },

  async getUserDetail(id: number) {
    const raw = await request<any>({
      method: 'GET',
      url: `/user/getOnesDetail/${id}`
    })
    return mapUserSummary(raw)
  },

  async getUserPosts(userId: number, page = 0, size = 10) {
    const raw = await request<PageResult<any>>({
      method: 'GET',
      url: `/user/main/${userId}/posts`,
      params: { page, size }
    })
    return mapPage(raw, mapPostCard)
  }
}
