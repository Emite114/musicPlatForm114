import type {
  CommentItem,
  ConversationSummary,
  MessageItem,
  PageResult,
  PostCard,
  PostDetail,
  ReportRecord,
  ReportTargetType,
  SongCard,
  SongDetail,
  UserCard,
  UserProfile,
  UserSummary
} from '@/types/api'

export function mapPage<TInput, TOutput>(
  page: PageResult<TInput>,
  mapper: (item: TInput) => TOutput
): PageResult<TOutput> {
  return {
    ...page,
    content: (page.content ?? []).map(mapper)
  }
}

export function mapUserProfile(raw: any): UserProfile {
  return {
    id: Number(raw?.UserId ?? raw?.userId ?? raw?.id ?? 0),
    username: raw?.username ?? raw?.userName ?? '',
    email: raw?.email ?? '',
    avatarUrl: raw?.avatarUrl ?? '/default/defaultUserAvatar.png',
    gender: raw?.gender ?? 'unknown',
    registerDate: raw?.registerDate,
    fanCount: raw?.fanCount == null ? undefined : Number(raw.fanCount),
    followCount: raw?.followCount == null ? undefined : Number(raw.followCount)
  }
}

export function mapUserCard(raw: any): UserCard {
  return {
    id: Number(raw?.Id ?? raw?.UserId ?? raw?.userId ?? raw?.id ?? 0),
    username: raw?.username ?? raw?.userName ?? '',
    avatarUrl: raw?.avatarUrl ?? '/default/defaultUserAvatar.png',
    postCount: Number(raw?.postCount ?? 0),
    fanCount: Number(raw?.fanCount ?? 0),
    isFollowed: Boolean(raw?.ifIsFollowed),
    isMyFan: Boolean(raw?.ifIsMyFan)
  }
}

export function mapUserSummary(raw: any): UserSummary {
  return {
    id: Number(raw?.Id ?? raw?.UserId ?? raw?.userId ?? raw?.id ?? 0),
    username: raw?.username ?? raw?.userName ?? '',
    avatarUrl: raw?.avatarUrl ?? '/default/defaultUserAvatar.png',
    followCount: Number(raw?.followCount ?? 0),
    fanCount: Number(raw?.fanCount ?? 0),
    gender: raw?.gender ?? 'unknown',
    isFollowed: Boolean(raw?.ifIsFollowed),
    isMyFan: Boolean(raw?.ifIsMyFan)
  }
}

export function mapSongCard(raw: any): SongCard {
  return {
    id: Number(raw?.Id ?? raw?.id ?? 0),
    songName: raw?.songName ?? '',
    songArtist: raw?.songArtist ?? 'N/A',
    avatarUrl: raw?.avatarUrl ?? '/default/defaultSongAvatar.png',
    playCount: Number(raw?.playCount ?? 0),
    commentCount: Number(raw?.commentCount ?? 0),
    favouriteCount: Number(raw?.favoriteCount ?? raw?.favouriteCount ?? 0),
    isFavourite: Boolean(raw?.ifIsFavourite ?? raw?.ifIsFavorite)
  }
}

export function mapSongDetail(raw: any): SongDetail {
  return {
    id: Number(raw?.id ?? 0),
    songName: raw?.songName ?? '',
    songArtist: raw?.songArtist ?? 'N/A',
    audioUrl: raw?.audioUrl ?? '',
    lrcUrl: raw?.lrcUrl,
    avatarUrl: raw?.avatarUrl ?? '/default/defaultSongAvatar.png',
    playCount: Number(raw?.playCount ?? 0),
    commentCount: Number(raw?.commentCount ?? 0),
    favouriteCount: Number(raw?.favoriteCount ?? raw?.favouriteCount ?? 0),
    isFavourite: Boolean(raw?.ifIsFavourite ?? raw?.ifIsFavorite),
    sharedByUserId:
      raw?.sharedByUserId == null ? undefined : Number(raw.sharedByUserId),
    sharedByUsername: raw?.sharedByUsername ?? ''
  }
}

export function mapPostCard(raw: any): PostCard {
  return {
    id: Number(raw?.id ?? 0),
    userId: Number(raw?.userId ?? 0),
    username: raw?.username ?? '',
    title: raw?.title ?? '',
    content: raw?.content ?? '',
    userAvatar: raw?.userAvatar ?? '/default/defaultUserAvatar.png',
    coverUrl: raw?.coverUrl ?? '/default/defaultSongAvatar.png',
    viewCount: Number(raw?.viewCount ?? 0),
    commentCount: raw?.commentCount == null ? undefined : Number(raw.commentCount),
    likeCount: Number(raw?.likeCount ?? 0),
    favouriteCount: Number(raw?.favouriteCount ?? raw?.favoriteCount ?? 0),
    isLiked: Boolean(raw?.ifIsLiked),
    isFavourite: Boolean(raw?.ifIsFavourite)
  }
}

export function mapPostDetail(raw: any): PostDetail {
  return {
    id: Number(raw?.id ?? 0),
    userId: Number(raw?.userId ?? 0),
    username: raw?.username ?? '',
    title: raw?.title ?? '',
    content: raw?.content ?? '',
    createdDate: raw?.createdDate,
    updatedDate: raw?.updatedDate,
    userAvatarUrl: raw?.userAvatarUrl ?? '/default/defaultUserAvatar.png',
    mediaUrlList: Array.isArray(raw?.mediaUrlList) ? raw.mediaUrlList : [],
    likeCount: Number(raw?.likeCount ?? 0),
    favouriteCount: Number(raw?.favouriteCount ?? raw?.favoriteCount ?? 0),
    commentCount: Number(raw?.commentCount ?? 0),
    viewCount: Number(raw?.viewCount ?? 0),
    isFollowed: Boolean(raw?.ifIsFollowed),
    isMyFan: Boolean(raw?.ifIsMyFan),
    isLiked: Boolean(raw?.ifIsLiked),
    isFavourite: Boolean(raw?.ifIsFavourite)
  }
}

export function mapComment(raw: any): CommentItem {
  return {
    id: Number(raw?.Id ?? raw?.id ?? 0),
    parentId: Number(raw?.parentId ?? 0),
    replyToUserId: Number(raw?.replyToUserId ?? 0),
    replyToUserName: raw?.replyToUserName ?? '',
    likeCount: Number(raw?.likeCount ?? 0),
    content: raw?.content ?? '',
    userId: Number(raw?.userId ?? 0),
    userName: raw?.userName ?? raw?.username ?? '',
    userAvatar: raw?.userAvatar ?? '/default/defaultUserAvatar.png',
    createTime: raw?.createTime,
    countOfChildren: Number(raw?.countOfChildren ?? 0)
  }
}

export function mapConversation(raw: any): ConversationSummary {
  return {
    id: Number(raw?.id ?? 0),
    talkingToUserId: Number(raw?.talkingToUserId ?? 0),
    talkingToUserName: raw?.talkingToUserName ?? '',
    talkingToUserAvatar: raw?.talkingToUserAvatar ?? '/default/defaultUserAvatar.png',
    ownAvatar: raw?.userAvatar ?? raw?.OwnAvatar ?? raw?.ownAvatar ?? '/default/defaultUserAvatar.png',
    lastMessageSenderId: Number(raw?.lastMessageSenderId ?? 0),
    lastMessageSenderName: raw?.lastMessageSenderName ?? '',
    lastMessageContent: raw?.lastMessageContent ?? '',
    unreadCount: Number(raw?.unreadCount ?? 0),
    lastTime: raw?.lastTime
  }
}

export function mapMessage(raw: any): MessageItem {
  return {
    id: Number(raw?.id ?? 0),
    content: raw?.content ?? '',
    speakingUserId: Number(raw?.speakingUserId ?? 0),
    receiveUserId: Number(raw?.receiveUserId ?? 0),
    conversationId: Number(raw?.conversationId ?? 0),
    createTime: raw?.createTime
  }
}

export function mapReport(raw: any): ReportRecord {
  return {
    targetType: raw?.targetType as ReportTargetType,
    targetId: Number(raw?.targetId ?? 0),
    reason: raw?.reason ?? '',
    createTime: raw?.createTime,
    result: raw?.result,
    handlerNote: raw?.handlerNote
  }
}
