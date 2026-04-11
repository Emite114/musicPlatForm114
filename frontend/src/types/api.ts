export interface ApiResponse<T> {
  code: number
  data: T
  msg: string
}

export interface PageResult<T> {
  content: T[]
  totalPages: number
  totalElements: number
  size: number
  number: number
  first: boolean
  last: boolean
  empty: boolean
}

export type Gender = 'male' | 'female' | 'unknown'
export type ReportTargetType =
  | 'USER'
  | 'SONG'
  | 'POST'
  | 'SONG_COMMENT'
  | 'POST_COMMENT'

export interface UploadedMedia {
  mediaId: number
  url: string
  originalName: string
}

export interface UserProfile {
  id: number
  username: string
  email: string
  avatarUrl: string
  gender: Gender
  registerDate?: string
  fanCount?: number
  followCount?: number
}

export interface UserCard {
  id: number
  username: string
  avatarUrl: string
  postCount: number
  fanCount: number
  isFollowed: boolean
  isMyFan: boolean
}

export interface UserSummary {
  id: number
  username: string
  avatarUrl: string
  followCount: number
  fanCount: number
  gender: Gender
  isFollowed: boolean
  isMyFan: boolean
}

export interface SongCard {
  id: number
  songName: string
  songArtist: string
  avatarUrl: string
  playCount: number
  commentCount: number
  favouriteCount: number
  isFavourite: boolean
}

export interface SongDetail {
  id: number
  songName: string
  songArtist: string
  audioUrl: string
  lrcUrl?: string
  avatarUrl: string
  playCount: number
  commentCount: number
  favouriteCount: number
  isFavourite: boolean
  sharedByUserId?: number
  sharedByUsername?: string
}

export interface PostCard {
  id: number
  userId: number
  username: string
  title: string
  content: string
  userAvatar: string
  coverUrl: string
  viewCount: number
  commentCount?: number
  likeCount: number
  favouriteCount: number
  isLiked: boolean
  isFavourite: boolean
}

export interface PostDetail {
  id: number
  userId: number
  username: string
  title: string
  content: string
  createdDate?: string
  updatedDate?: string
  userAvatarUrl: string
  mediaUrlList: string[]
  likeCount: number
  favouriteCount: number
  commentCount: number
  viewCount: number
  isFollowed: boolean
  isMyFan: boolean
  isLiked: boolean
  isFavourite: boolean
}

export interface CommentItem {
  id: number
  parentId: number
  replyToUserId: number
  replyToUserName?: string
  likeCount: number
  content: string
  userId: number
  userName?: string
  userAvatar: string
  createTime?: string
  countOfChildren: number
}

export interface ConversationSummary {
  id: number
  talkingToUserId: number
  talkingToUserName: string
  talkingToUserAvatar: string
  ownAvatar: string
  lastMessageSenderId: number
  lastMessageSenderName?: string
  lastMessageContent: string
  unreadCount: number
  lastTime?: string
}

export interface MessageItem {
  id: number
  content: string
  speakingUserId: number
  receiveUserId: number
  conversationId: number
  createTime?: string
}

export interface ReportRecord {
  targetType: ReportTargetType
  targetId: number
  reason: string
  createTime?: string
  result?: string
  handlerNote?: string
}

export interface MessageEventPayload {
  type?: string
  conversationId: number
  speakingUserId: number
  receiveUserId: number
  content: string
  createTime?: string
  unreadCount?: number
}
