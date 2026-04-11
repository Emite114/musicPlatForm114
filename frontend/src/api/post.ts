import { request } from '@/api/client'
import { mapComment, mapPage, mapPostCard, mapPostDetail } from '@/api/mappers'
import type { PageResult } from '@/types/api'

export const postApi = {
  async searchPosts(keyword: string, page = 0, size = 10, sort = 'time') {
    const raw = await request<PageResult<any>>({
      method: 'GET',
      url: '/post/page',
      params: { keyword, page, size, sort }
    })
    return mapPage(raw, mapPostCard)
  },

  async getPostDetail(id: number) {
    const raw = await request<any>({
      method: 'GET',
      url: `/post/getPost/${id}`
    })
    return mapPostDetail(raw)
  },

  createPost(payload: { title: string; content: string; mediaIdList: number[] }) {
    return request<null>({
      method: 'POST',
      url: '/post/create',
      data: payload
    })
  },

  togglePostLike(id: number) {
    return request<null>({
      method: 'POST',
      url: `/post/like/${id}`
    })
  },

  togglePostFavourite(id: number) {
    return request<null>({
      method: 'POST',
      url: `/post/favourite/${id}`
    })
  },

  async getFavouritePosts(keyword = '', page = 0, size = 20, sort = 'time') {
    const raw = await request<PageResult<any>>({
      method: 'GET',
      url: '/post/favourite/userOwn',
      params: { keyword, page, size, sort }
    })
    return mapPage(raw, mapPostCard)
  },

  async getOnesFavouritePosts(id: number, keyword = '', page = 0, size = 20, sort = 'time') {
    const raw = await request<PageResult<any>>({
      method: 'GET',
      url: '/post/getOnesFavouritePosts',
      params: { id, keyword, page, size, sort }
    })
    return mapPage(raw, mapPostCard)
  },

  async getPostParentComments(postId: number, page = 0, size = 10, sort = 'time') {
    const raw = await request<PageResult<any>>({
      method: 'GET',
      url: '/post/comment/getParents',
      params: { postId, page, size, sort }
    })
    return mapPage(raw, mapComment)
  },

  async getPostChildrenComments(parentId: number, page = 0, size = 5, sort = 'time') {
    const raw = await request<PageResult<any>>({
      method: 'GET',
      url: '/post/comment/getChildren',
      params: { parentId, page, size, sort }
    })
    return mapPage(raw, mapComment)
  },

  createPostComment(payload: {
    postId: number
    content: string
    parentId?: number | null
    replyToUserId?: number | null
  }) {
    return request<null>({
      method: 'POST',
      url: '/post/comment/create',
      data: payload
    })
  },

  likePostComment(id: number) {
    return request<null>({
      method: 'POST',
      url: `/post/comment/like/${id}`
    })
  },

  deletePostComment(id: number) {
    return request<null>({
      method: 'PUT',
      url: `/post/comment/delete/${id}`
    })
  }
}
