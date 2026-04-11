import { request } from '@/api/client'
import { mapUserProfile } from '@/api/mappers'

export const authApi = {
  login(payload: { account: string; password: string }) {
    return request<string>({
      method: 'POST',
      url: '/user/login',
      data: payload
    })
  },

  register(payload: {
    username: string
    email: string
    password: string
    avatarUrl?: string | null
    gender?: string
  }) {
    return request<null>({
      method: 'POST',
      url: '/user/register',
      data: payload
    })
  },

  logout() {
    return request<null>({
      method: 'POST',
      url: '/user/logout'
    })
  },

  async getMyProfile() {
    const raw = await request<any>({
      method: 'GET',
      url: '/user/myDetails'
    })
    return mapUserProfile(raw)
  },

  updateAvatarUrl(avatarUrl: string) {
    return request<null>({
      method: 'PUT',
      url: '/user/update/avatarUrl',
      data: avatarUrl,
      headers: {
        'Content-Type': 'text/plain; charset=utf-8'
      }
    })
  },

  updateGender(gender: string) {
    return request<null>({
      method: 'PUT',
      url: '/user/update/gender',
      data: gender,
      headers: {
        'Content-Type': 'text/plain; charset=utf-8'
      }
    })
  },

  uploadMedia(file: File) {
    const formData = new FormData()
    formData.append('file', file)
    return request<{ mediaId: number; url: string; originalName: string }>({
      method: 'POST',
      url: '/user/uploadMedia',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
}
