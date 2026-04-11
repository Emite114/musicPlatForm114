<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import PageHeader from '@/components/PageHeader.vue'
import ProtectedImage from '@/components/ProtectedImage.vue'
import ProtectedLink from '@/components/ProtectedLink.vue'
import ProtectedMedia from '@/components/ProtectedMedia.vue'
import { postApi, reportApi, userApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { useUiStore } from '@/stores/ui'
import { formatDate } from '@/utils/format'
import {
  getAssetFileName,
  isAudioAsset,
  isImageAsset,
  isLyricAsset,
  isVideoAsset
} from '@/utils/asset'
import type { CommentItem, PageResult, PostDetail } from '@/types/api'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const ui = useUiStore()

const postId = computed(() => Number(route.params.id))
const detail = ref<PostDetail | null>(null)
const comments = ref<PageResult<CommentItem> | null>(null)
const childrenMap = ref<Record<number, CommentItem[]>>({})
const loading = ref(false)
const commentSubmitting = ref(false)
const composer = reactive({
  content: ''
})

const replyState = ref<{
  parentId: number
  replyToUserId: number
  replyToUserName: string
} | null>(null)

const isSelf = computed(() => Boolean(detail.value && detail.value.userId === auth.profile?.id))

async function loadPostDetail() {
  if (!postId.value) {
    return
  }

  loading.value = true
  try {
    detail.value = await postApi.getPostDetail(postId.value)
    comments.value = await postApi.getPostParentComments(postId.value, 0, 10, 'time')
  } catch (error) {
    ui.error(error instanceof Error ? error.message : '帖子详情加载失败')
  } finally {
    loading.value = false
  }
}

async function loadChildren(parentId: number) {
  try {
    const page = await postApi.getPostChildrenComments(parentId, 0, 20, 'time')
    childrenMap.value = {
      ...childrenMap.value,
      [parentId]: page.content
    }
  } catch (error) {
    ui.error(error instanceof Error ? error.message : '子评论加载失败')
  }
}

function commentAuthorName(comment: CommentItem) {
  return comment.userName || `用户 #${comment.userId}`
}

function openReply(parentComment: CommentItem, targetComment = parentComment) {
  replyState.value = {
    parentId: parentComment.id,
    replyToUserId: targetComment.userId,
    replyToUserName:
      targetComment.userName ||
      targetComment.replyToUserName ||
      `用户 ${targetComment.userId}`
  }
}

async function submitComment() {
  if (!composer.content.trim()) {
    ui.error('评论内容不能为空')
    return
  }

  commentSubmitting.value = true
  try {
    await postApi.createPostComment({
      postId: postId.value,
      content: composer.content.trim(),
      parentId: replyState.value?.parentId,
      replyToUserId: replyState.value?.replyToUserId
    })
    ui.success('评论成功')
    composer.content = ''
    replyState.value = null
    childrenMap.value = {}
    await loadPostDetail()
  } catch (error) {
    ui.error(error instanceof Error ? error.message : '评论失败')
  } finally {
    commentSubmitting.value = false
  }
}

async function toggleLike() {
  try {
    await postApi.togglePostLike(postId.value)
    ui.success('点赞状态已更新')
    await loadPostDetail()
  } catch (error) {
    ui.error(error instanceof Error ? error.message : '点赞失败')
  }
}

async function toggleFavourite() {
  try {
    await postApi.togglePostFavourite(postId.value)
    ui.success('收藏状态已更新')
    await loadPostDetail()
  } catch (error) {
    ui.error(error instanceof Error ? error.message : '收藏失败')
  }
}

async function toggleFollow() {
  if (!detail.value || isSelf.value) {
    return
  }
  try {
    await userApi.toggleFollow(detail.value.userId)
    ui.success('关注状态已更新')
    await loadPostDetail()
  } catch (error) {
    ui.error(error instanceof Error ? error.message : '关注失败')
  }
}

async function likeComment(commentId: number) {
  try {
    await postApi.likePostComment(commentId)
    await loadPostDetail()
  } catch (error) {
    ui.error(error instanceof Error ? error.message : '点赞失败')
  }
}

async function deleteComment(commentId: number) {
  try {
    await postApi.deletePostComment(commentId)
    ui.success('评论已删除')
    childrenMap.value = {}
    await loadPostDetail()
  } catch (error) {
    ui.error(error instanceof Error ? error.message : '删除失败')
  }
}

async function reportEntity(targetType: 'POST' | 'POST_COMMENT', targetId: number) {
  const reason = window.prompt('请输入举报理由')?.trim()
  if (!reason) {
    return
  }

  try {
    await reportApi.createReport({
      targetType,
      targetId,
      reason
    })
    ui.success('举报已提交')
  } catch (error) {
    ui.error(error instanceof Error ? error.message : '举报失败')
  }
}

function openChat() {
  if (!detail.value || isSelf.value) {
    return
  }
  router.push({
    path: '/messages',
    query: {
      with: String(detail.value.userId)
    }
  })
}

function attachmentDownloadName(item: string) {
  if (isLyricAsset(item)) {
    return getAssetFileName(item, '歌词附件.lrc')
  }
  return getAssetFileName(item)
}

watch(
  () => postId.value,
  async () => {
    childrenMap.value = {}
    replyState.value = null
    await loadPostDetail()
  },
  { immediate: true }
)
</script>

<template>
  <section v-if="detail" class="panel">
    <PageHeader
      eyebrow="Post Detail"
      :title="detail.title || '帖子详情'"
      :description="detail.content || '当前帖子已返回作者、媒体和交互状态。'"
    >
      <template #actions>
        <button
          class="detail-action"
          :class="{ active: detail.isLiked, liked: detail.isLiked }"
          type="button"
          @click="toggleLike"
        >
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path
              d="M12 20.5 4.8 13.8a4.7 4.7 0 0 1 6.6-6.7L12 7.8l.6-.7a4.7 4.7 0 0 1 6.6 6.7z"
              :fill="detail.isLiked ? 'currentColor' : 'none'"
              stroke="currentColor"
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="1.8"
            />
          </svg>
          {{ detail.likeCount }}
        </button>

        <button
          class="detail-action"
          :class="{ active: detail.isFavourite, favourite: detail.isFavourite }"
          type="button"
          @click="toggleFavourite"
        >
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path
              d="m12 4 2.5 5.1 5.6.8-4 3.9.9 5.5-5-2.7-5 2.7.9-5.5-4-3.9 5.6-.8z"
              :fill="detail.isFavourite ? 'currentColor' : 'none'"
              stroke="currentColor"
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="1.8"
            />
          </svg>
          {{ detail.favouriteCount }}
        </button>

        <button
          v-if="!isSelf"
          class="detail-action"
          :class="{ active: detail.isFollowed, follow: detail.isFollowed }"
          type="button"
          @click="toggleFollow"
        >
          {{ detail.isFollowed ? '已关注作者' : '关注作者' }}
        </button>

        <button v-if="!isSelf" class="detail-action" type="button" @click="openChat">私信作者</button>

        <button class="detail-action detail-action-danger" type="button" @click="reportEntity('POST', postId)">
          举报
        </button>
      </template>
    </PageHeader>

    <RouterLink class="entity-row detail-user-link" :to="`/users/${detail.userId}`">
      <ProtectedImage :src="detail.userAvatarUrl" class="avatar avatar-lg" />
      <div class="entity-copy">
        <h3>{{ detail.username }}</h3>
        <p>
          发布于 {{ formatDate(detail.createdDate) }}
          <span v-if="detail.updatedDate"> · 更新于 {{ formatDate(detail.updatedDate) }}</span>
        </p>
      </div>
    </RouterLink>

    <div class="detail-stats">
      <span class="pill">浏览 {{ detail.viewCount }}</span>
      <span class="pill">点赞 {{ detail.likeCount }}</span>
      <span class="pill">收藏 {{ detail.favouriteCount }}</span>
      <span class="pill">评论 {{ detail.commentCount }}</span>
      <span class="pill">作者 ID {{ detail.userId }}</span>
    </div>

    <div v-if="detail.mediaUrlList.length" class="media-grid" style="margin-top: 18px">
      <template v-for="item in detail.mediaUrlList" :key="item">
        <ProtectedImage v-if="isImageAsset(item)" :src="item" class="item-cover" />
        <ProtectedMedia v-else-if="isAudioAsset(item)" kind="audio" :src="item" controls style="width: 100%" />
        <ProtectedMedia v-else-if="isVideoAsset(item)" kind="video" :src="item" class="item-cover" controls />
        <ProtectedLink
          v-else
          class="item-card attachment-link-card"
          :href="item"
          :download-name="attachmentDownloadName(item)"
        >
          <template v-if="isLyricAsset(item)">
            <span class="attachment-link-title">歌词附件</span>
            <span class="attachment-link-subtitle">点击下载</span>
          </template>
          <template v-else>
            <span class="attachment-link-title">附件下载</span>
            <span class="attachment-link-subtitle">{{ attachmentDownloadName(item) }}</span>
          </template>
        </ProtectedLink>
      </template>
    </div>
  </section>

  <section class="panel">
    <PageHeader eyebrow="Comment" title="帖子评论" />

    <form class="stack" @submit.prevent="submitComment">
      <div v-if="replyState" class="pill">
        回复 {{ replyState.replyToUserName }}
        <button class="btn btn-ghost" type="button" @click="replyState = null">取消</button>
      </div>

      <div class="field">
        <textarea v-model="composer.content" placeholder="聊聊你的看法..." />
      </div>

      <button class="btn btn-primary" type="submit" :disabled="commentSubmitting">
        {{ commentSubmitting ? '提交中...' : '发表评论' }}
      </button>
    </form>

    <div class="divider" style="margin: 18px 0" />

    <div v-if="comments?.content.length" class="comment-list">
      <article v-for="comment in comments.content" :key="comment.id" class="comment-card">
        <div class="entity-row justify-between">
          <RouterLink class="entity-row detail-user-link" :to="`/users/${comment.userId}`">
            <ProtectedImage :src="comment.userAvatar" class="avatar" />
            <div class="entity-copy">
              <h4>{{ commentAuthorName(comment) }}</h4>
              <p>{{ formatDate(comment.createTime) }}</p>
            </div>
          </RouterLink>
          <span class="pill">赞 {{ comment.likeCount }}</span>
        </div>

        <p>{{ comment.content }}</p>

        <div class="actions">
          <button class="btn btn-secondary" type="button" @click="openReply(comment)">回复</button>
          <button class="btn btn-secondary" type="button" @click="likeComment(comment.id)">点赞</button>
          <button class="btn btn-danger" type="button" @click="deleteComment(comment.id)">删除</button>
          <button class="btn btn-danger" type="button" @click="reportEntity('POST_COMMENT', comment.id)">举报</button>
          <button
            v-if="comment.countOfChildren > 0"
            class="btn btn-ghost"
            type="button"
            @click="loadChildren(comment.id)"
          >
            展开 {{ comment.countOfChildren }} 条回复
          </button>
        </div>

        <div v-if="childrenMap[comment.id]?.length" class="comment-children">
          <article v-for="child in childrenMap[comment.id]" :key="child.id" class="comment-card">
            <div class="entity-row justify-between">
              <RouterLink class="entity-row detail-user-link" :to="`/users/${child.userId}`">
                <ProtectedImage :src="child.userAvatar" class="avatar" />
                <div class="entity-copy">
                  <h4>{{ commentAuthorName(child) }}</h4>
                  <p>{{ formatDate(child.createTime) }}</p>
                </div>
              </RouterLink>
              <span class="pill">赞 {{ child.likeCount }}</span>
            </div>

            <p>
              <RouterLink v-if="child.replyToUserId" :to="`/users/${child.replyToUserId}`">
                <strong v-if="child.replyToUserName">@{{ child.replyToUserName }}：</strong>
              </RouterLink>
              <strong v-else-if="child.replyToUserName">@{{ child.replyToUserName }}：</strong>
              {{ child.content }}
            </p>

            <div class="actions">
              <button class="btn btn-secondary" type="button" @click="openReply(comment, child)">回复</button>
              <button class="btn btn-secondary" type="button" @click="likeComment(child.id)">点赞</button>
              <button class="btn btn-danger" type="button" @click="deleteComment(child.id)">删除</button>
              <button class="btn btn-danger" type="button" @click="reportEntity('POST_COMMENT', child.id)">举报</button>
            </div>
          </article>
        </div>
      </article>
    </div>

    <div v-else class="empty">{{ loading ? '加载中...' : '这篇帖子还没有评论' }}</div>

    <div class="actions" style="margin-top: 18px">
      <RouterLink class="btn btn-secondary" to="/hot-posts">回到热门帖子</RouterLink>
    </div>
  </section>
</template>

<style scoped>
.detail-user-link {
  color: inherit;
}

.detail-stats {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 14px;
}

.detail-action {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-height: 42px;
  padding: 0 14px;
  border-radius: 14px;
  background: rgba(23, 32, 51, 0.06);
  color: var(--ink);
  font-weight: 600;
}

.detail-action svg {
  width: 16px;
  height: 16px;
}

.detail-action.active.liked {
  color: #dc2626;
  background: rgba(220, 38, 38, 0.1);
}

.detail-action.active.favourite {
  color: #d97706;
  background: rgba(245, 158, 11, 0.14);
}

.detail-action.active.follow {
  color: #0f766e;
  background: rgba(15, 118, 110, 0.12);
}

.detail-action-danger {
  background: rgba(185, 28, 28, 0.12);
  color: var(--danger);
}

.attachment-link-card {
  display: grid;
  gap: 6px;
  align-content: center;
  min-height: 120px;
  padding: 18px;
}

.attachment-link-title {
  font-size: 1rem;
  font-weight: 700;
  color: var(--ink);
}

.attachment-link-subtitle {
  font-size: 0.88rem;
  color: var(--muted);
}
</style>

