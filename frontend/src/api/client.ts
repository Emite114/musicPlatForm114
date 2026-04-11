import type { AxiosRequestConfig } from 'axios'
import type { ApiResponse } from '@/types/api'
import http from '@/api/http'

export async function request<T>(config: AxiosRequestConfig) {
  const response = await http.request<ApiResponse<T>>(config)
  const payload = response.data
  if (!payload || payload.code !== 0) {
    throw new Error(payload?.msg ?? '请求失败')
  }
  return payload.data
}
