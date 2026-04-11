import { request } from '@/api/client'
import { mapComment, mapPage, mapSongCard, mapSongDetail } from '@/api/mappers'
import type { PageResult } from '@/types/api'

export const songApi = {
  async searchSongs(keyword: string, page = 0, size = 10, sort = 'time') {
    const raw = await request<PageResult<any>>({
      method: 'GET',
      url: '/song/page',
      params: { keyword, page, size, sort }
    })
    return mapPage(raw, mapSongCard)
  },

  async getSongDetail(id: number) {
    const raw = await request<any>({
      method: 'GET',
      url: '/song/getSong/',
      params: { id }
    })
    return mapSongDetail(raw)
  },

  uploadSong(payload: {
    songName: string
    songArtist: string
    audioUrl: string
    lrcUrl?: string
    avatarUrl?: string
  }) {
    return request<null>({
      method: 'POST',
      url: '/user/uploadSong',
      data: payload
    })
  },

  toggleSongFavourite(id: number) {
    return request<null>({
      method: 'POST',
      url: `/song/favourite/${id}`
    })
  },

  async getFavouriteSongs(keyword = '', page = 0, size = 10, sort = 'time') {
    const raw = await request<PageResult<any>>({
      method: 'GET',
      url: '/song/favourite/userOwn',
      params: { keyword, page, size, sort }
    })
    return mapPage(raw, mapSongCard)
  },

  async getOnesFavouriteSongs(id: number, keyword = '', page = 0, size = 10, sort = 'time') {
    const raw = await request<PageResult<any>>({
      method: 'GET',
      url: '/song/getOnesFavouriteSongs',
      params: { id, keyword, page, size, sort }
    })
    return mapPage(raw, mapSongCard)
  },

  async getOnesSharedSongs(id?: number, keyword = '', page = 0, size = 15, sort = 'time') {
    const params: Record<string, unknown> = { keyword, page, size, sort }
    if (id != null) {
      params.id = id
    }
    const raw = await request<PageResult<any>>({
      method: 'GET',
      url: '/song/getOnesSharedSongList',
      params
    })
    return mapPage(raw, mapSongCard)
  },

  async getOnesFavouriteSongIdList(id?: number, page = 0, size = 500, sort = 'time') {
    const params: Record<string, unknown> = { page, size, sort }
    if (id != null) {
      params.id = id
    }
    const raw = await request<any[]>({
      method: 'GET',
      url: '/song/getOnesFavouriteSongIdList',
      params
    })
    return (raw ?? [])
      .map((item) => Number(item))
      .filter((item) => Number.isFinite(item) && item > 0)
  },

  async getSongParentComments(songId: number, page = 0, size = 10, sort = 'time') {
    const raw = await request<PageResult<any>>({
      method: 'GET',
      url: '/song/comment/getParents',
      params: { songId, page, size, sort }
    })
    return mapPage(raw, mapComment)
  },

  async getSongChildrenComments(parentId: number, page = 0, size = 10, sort = 'time') {
    const raw = await request<PageResult<any>>({
      method: 'GET',
      url: '/song/comment/getChildren',
      params: { parentId, page, size, sort }
    })
    return mapPage(raw, mapComment)
  },

  createSongComment(payload: {
    songId: number
    content: string
    parentId?: number | null
    replyToUserId?: number | null
  }) {
    return request<null>({
      method: 'POST',
      url: '/song/createComment',
      data: payload
    })
  },

  likeSongComment(id: number) {
    return request<null>({
      method: 'POST',
      url: `/song/comment/like/${id}`
    })
  },

  deleteSongComment(id: number) {
    return request<null>({
      method: 'PUT',
      url: `/song/comment/delete/${id}`
    })
  }
}
