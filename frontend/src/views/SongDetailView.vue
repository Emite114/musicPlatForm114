<script setup lang="ts">
import { computed, nextTick, reactive, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import http from '@/api/http'
import PageHeader from '@/components/PageHeader.vue'
import ProtectedImage from '@/components/ProtectedImage.vue'
import ProtectedLink from '@/components/ProtectedLink.vue'
import { reportApi, songApi } from '@/api'
import { usePlayerStore } from '@/stores/player'
import { useUiStore } from '@/stores/ui'
import { getAssetFileName, resolveAssetUrl } from '@/utils/asset'
import { formatDate } from '@/utils/format'
import { parseLyricContent, type TimedLyricLine } from '@/utils/lyric'
import type { CommentItem, PageResult, SongDetail } from '@/types/api'

const route = useRoute()
const ui = useUiStore()
const player = usePlayerStore()

const songId = computed(() => Number(route.params.id))
const song = ref<SongDetail | null>(null)
const comments = ref<PageResult<CommentItem> | null>(null)
const childrenMap = ref<Record<number, CommentItem[]>>({})
const loading = ref(false)
const commentSubmitting = ref(false)
const composer = reactive({
  content: ''
})

const lyricLoading = ref(false)
const timedLyricLines = ref<TimedLyricLine[]>([])
const plainLyricLines = ref<string[]>([])
const lyricLineRefs = ref<HTMLElement[]>([])
const lyricScrollerRef = ref<HTMLElement | null>(null)
let lyricRequestId = 0

const replyState = ref<{
  parentId: number
  replyToUserId: number
  replyToUserName: string
} | null>(null)

const canSyncLyrics = computed(() => {
  return Boolean(
    song.value &&
      player.lyrics.length &&
      song.value.id > 0 &&
      song.value.id === player.currentSongId
  )
})

const displayedTimedLyricLines = computed(() => {
  return canSyncLyrics.value ? player.lyrics : timedLyricLines.value
})

const activeLyricIndex = computed(() => {
  if (!canSyncLyrics.value) {
    return -1
  }
  return player.currentLyricIndex
})

const lyricDisplayLines = computed(() => {
  if (displayedTimedLyricLines.value.length) {
    return displayedTimedLyricLines.value.map((line) => line.text)
  }
  return plainLyricLines.value
})

const lyricState = computed<'loading' | 'empty' | 'plain' | 'timed'>(() => {
  if (lyricLoading.value) {
    return 'loading'
  }
  if (!song.value?.lrcUrl || !lyricDisplayLines.value.length) {
    return 'empty'
  }
  if (displayedTimedLyricLines.value.length) {
    return 'timed'
  }
  return 'plain'
})

async function loadLyrics(detail: SongDetail | null) {
  const requestId = ++lyricRequestId
  lyricLineRefs.value = []
  timedLyricLines.value = []
  plainLyricLines.value = []
  lyricScrollerRef.value?.scrollTo({ top: 0, behavior: 'auto' })

  if (!detail?.lrcUrl) {
    lyricLoading.value = false
    return
  }

  lyricLoading.value = true
  try {
    const response = await http.get<string>(resolveAssetUrl(detail.lrcUrl, 'media'), {
      responseType: 'text'
    })
    if (requestId !== lyricRequestId) {
      return
    }

    const rawText = typeof response.data === 'string' ? response.data : ''
    const parsed = parseLyricContent(rawText)
    timedLyricLines.value = parsed.timedLines
    plainLyricLines.value = parsed.plainLines
    await nextTick()
    lyricScrollerRef.value?.scrollTo({ top: 0, behavior: 'auto' })
  } catch {
    if (requestId !== lyricRequestId) {
      return
    }
    timedLyricLines.value = []
    plainLyricLines.value = []
    await nextTick()
    lyricScrollerRef.value?.scrollTo({ top: 0, behavior: 'auto' })
  } finally {
    if (requestId === lyricRequestId) {
      lyricLoading.value = false
    }
  }
}

async function loadSongDetail() {
  if (!songId.value) {
    return
  }

  loading.value = true
  try {
    const detail = await songApi.getSongDetail(songId.value)
    song.value = detail
    comments.value = await songApi.getSongParentComments(songId.value, 0, 10, 'time')
    await loadLyrics(detail)
  } catch (error) {
    ui.error(error instanceof Error ? error.message : '歌曲详情加载失败')
  } finally {
    loading.value = false
  }
}

async function loadChildren(parentId: number) {
  try {
    const page = await songApi.getSongChildrenComments(parentId, 0, 20, 'time')
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
    await songApi.createSongComment({
      songId: songId.value,
      content: composer.content.trim(),
      parentId: replyState.value?.parentId,
      replyToUserId: replyState.value?.replyToUserId
    })
    ui.success('评论成功')
    composer.content = ''
    replyState.value = null
    childrenMap.value = {}
    await loadSongDetail()
  } catch (error) {
    ui.error(error instanceof Error ? error.message : '评论失败')
  } finally {
    commentSubmitting.value = false
  }
}

async function likeComment(commentId: number) {
  try {
    await songApi.likeSongComment(commentId)
    await loadSongDetail()
  } catch (error) {
    ui.error(error instanceof Error ? error.message : '点赞失败')
  }
}

async function deleteComment(commentId: number) {
  try {
    await songApi.deleteSongComment(commentId)
    ui.success('评论已删除')
    childrenMap.value = {}
    await loadSongDetail()
  } catch (error) {
    ui.error(error instanceof Error ? error.message : '删除失败')
  }
}

async function toggleFavourite() {
  try {
    await songApi.toggleSongFavourite(songId.value)
    ui.success('收藏状态已更新')
    await loadSongDetail()
  } catch (error) {
    ui.error(error instanceof Error ? error.message : '操作失败')
  }
}

async function reportEntity(targetType: 'SONG' | 'SONG_COMMENT', targetId: number) {
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

async function playCurrentSong() {
  if (!song.value) {
    return
  }
  await player.playSong(song.value.id)
}

function lyricDownloadName() {
  return getAssetFileName(song.value?.lrcUrl, '歌词附件.lrc')
}

function audioDownloadName() {
  return getAssetFileName(song.value?.audioUrl, '歌曲音源.mp3')
}

function setLyricLineRef(
  element: Element | { $el?: Element | null } | null,
  index: number
) {
  const target =
    element instanceof HTMLElement
      ? element
      : element && '$el' in element && element.$el instanceof HTMLElement
        ? element.$el
        : null

  if (target) {
    lyricLineRefs.value[index] = target
  }
}

watch(
  () => songId.value,
  async () => {
    childrenMap.value = {}
    replyState.value = null
    lyricLineRefs.value = []
    lyricScrollerRef.value?.scrollTo({ top: 0, behavior: 'auto' })
    await loadSongDetail()
  },
  { immediate: true }
)

watch(
  () => activeLyricIndex.value,
  async (index) => {
    if (!canSyncLyrics.value || index < 0) {
      return
    }

    await nextTick()
    lyricLineRefs.value[index]?.scrollIntoView({
      block: 'center',
      behavior: 'smooth'
    })
  }
)
</script>

<template>
  <section v-if="song" class="panel">
    <PageHeader
      eyebrow="Song Detail"
      :title="song.songName || '歌曲详情'"
      :description="song.songArtist || '当前歌曲已返回分享者、收藏状态与评论数据'"
    >
      <template #actions>
        <button
          class="detail-action"
          :class="{ active: song.isFavourite, favourite: song.isFavourite }"
          type="button"
          @click="toggleFavourite"
        >
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path
              d="m12 4 2.5 5.1 5.6.8-4 3.9.9 5.5-5-2.7-5 2.7.9-5.5-4-3.9 5.6-.8z"
              :fill="song.isFavourite ? 'currentColor' : 'none'"
              stroke="currentColor"
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="1.8"
            />
          </svg>
          {{ song.favouriteCount }}
        </button>

        <button class="detail-action detail-action-danger" type="button" @click="reportEntity('SONG', songId)">
          举报
        </button>
      </template>
    </PageHeader>

    <div class="song-detail-layout">
      <div class="stack">
        <ProtectedImage :src="song.avatarUrl" class="song-cover" />

        <div class="detail-stats">
          <span class="pill">播放 {{ song.playCount }}</span>
          <span class="pill">评论 {{ song.commentCount }}</span>
          <span class="pill">收藏 {{ song.favouriteCount }}</span>
          <span class="pill">ID {{ song.id }}</span>
        </div>
      </div>

      <div class="song-detail-card">
        <aside class="song-detail-sidebar">
          <div class="song-meta-row">
            <span class="song-meta-label">歌手</span>
            <strong>{{ song.songArtist || '未知歌手' }}</strong>
          </div>

          <div v-if="song.sharedByUserId" class="song-meta-row">
            <span class="song-meta-label">分享者</span>
            <RouterLink class="song-meta-link" :to="`/users/${song.sharedByUserId}`">
              {{ song.sharedByUsername || `用户 #${song.sharedByUserId}` }}
            </RouterLink>
          </div>

          <div v-else class="song-meta-row">
            <span class="song-meta-label">分享者</span>
            <span class="muted">暂无分享者信息</span>
          </div>

          <div class="song-detail-actions">
            <button class="btn btn-primary" type="button" @click="playCurrentSong">立即播放</button>

            <ProtectedLink
              v-if="song.audioUrl"
              class="btn btn-secondary song-download-button"
              :href="song.audioUrl"
              :download-name="audioDownloadName()"
            >
              下载音源附件
            </ProtectedLink>

            <ProtectedLink
              v-if="song.lrcUrl"
              class="btn btn-secondary song-download-button"
              :href="song.lrcUrl"
              :download-name="lyricDownloadName()"
            >
              下载歌词附件
            </ProtectedLink>
          </div>
        </aside>

        <section class="song-lyric-panel">
          <div v-if="lyricState === 'loading'" class="song-lyric-empty">
            <p>歌词加载中...</p>
          </div>

          <div v-else-if="lyricState === 'empty'" class="song-lyric-empty">
            <p>暂无歌词</p>
          </div>

          <div
            v-else-if="lyricState === 'plain'"
            ref="lyricScrollerRef"
            class="song-lyric-display song-lyric-display-plain"
          >
            <p v-for="(line, index) in lyricDisplayLines" :key="`plain-${index}`" class="song-lyric-line">
              {{ line }}
            </p>
          </div>

          <div v-else ref="lyricScrollerRef" class="song-lyric-display">
            <p
              v-for="(line, index) in lyricDisplayLines"
              :key="`timed-${index}`"
              :ref="(element) => setLyricLineRef(element, index)"
              class="song-lyric-line"
              :class="{
                active: index === activeLyricIndex,
                dimmed: canSyncLyrics && activeLyricIndex >= 0 && index !== activeLyricIndex
              }"
            >
              {{ line }}
            </p>
          </div>
        </section>
      </div>
    </div>
  </section>

  <section class="panel">
    <PageHeader
      eyebrow="Comment"
      title="歌曲评论"
      description="支持主评论、子评论、点赞、删除和举报评论。"
    />

    <form class="stack" @submit.prevent="submitComment">
      <div v-if="replyState" class="pill">
        回复 {{ replyState.replyToUserName }}
        <button class="btn btn-ghost" type="button" @click="replyState = null">取消</button>
      </div>

      <div class="field">
        <textarea v-model="composer.content" placeholder="写下你对这首歌的想法..." />
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
          <button class="btn btn-danger" type="button" @click="reportEntity('SONG_COMMENT', comment.id)">
            举报
          </button>
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
              <button class="btn btn-danger" type="button" @click="reportEntity('SONG_COMMENT', child.id)">
                举报
              </button>
            </div>
          </article>
        </div>
      </article>
    </div>

    <div v-else class="empty">{{ loading ? '加载中...' : '这首歌还没有评论' }}</div>
  </section>
</template>

<style scoped>
.song-detail-layout {
  display: grid;
  grid-template-columns: minmax(260px, 360px) minmax(0, 1fr);
  gap: 24px;
}

.song-cover {
  width: 100%;
  aspect-ratio: 1 / 1;
  object-fit: cover;
  border-radius: 26px;
}

.detail-stats {
  display: flex;
  gap: 10px;
}

.song-detail-card {
  display: grid;
  grid-template-columns: minmax(220px, 260px) minmax(0, 1fr);
  gap: 24px;
  padding: 22px;
  border-radius: 24px;
  background: rgba(23, 32, 51, 0.03);
  min-height: 340px;
}

.song-detail-sidebar {
  display: flex;
  flex-direction: column;
  gap: 18px;
  align-items: flex-start;
}

.song-meta-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.song-meta-label {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(14, 165, 233, 0.1);
  color: #0369a1;
  font-size: 0.82rem;
}

.song-meta-link {
  font-weight: 600;
  color: #0369a1;
}

.song-detail-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: flex-start;
}

.song-download-button {
  justify-content: flex-start;
}

.song-lyric-panel {
  min-height: 296px;
  height: 100%;
  overflow: hidden;
  min-width: 0;
}

.song-lyric-display {
  height: 100%;
  max-height: 296px;
  overflow-y: auto;
  overflow-x: hidden;
  padding-right: 10px;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  gap: 10px;
}

.song-lyric-line {
  margin: 0;
  width: 100%;
  color: rgba(15, 23, 42, 0.76);
  font-size: 1rem;
  line-height: 1.95;
  text-align: center;
  white-space: normal;
  word-break: break-word;
  overflow-wrap: anywhere;
  transition: color 0.2s ease, transform 0.2s ease, opacity 0.2s ease;
}

.song-lyric-line.active {
  color: var(--ink);
  font-size: 1.08rem;
  font-weight: 700;
  transform: scale(1.02);
}

.song-lyric-line.dimmed {
  opacity: 0.35;
}

.song-lyric-empty {
  min-height: 296px;
  display: grid;
  place-items: center;
  color: rgba(15, 23, 42, 0.52);
  font-size: 1rem;
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

.detail-action.active.favourite {
  color: #d97706;
  background: rgba(245, 158, 11, 0.14);
}

.detail-action-danger {
  background: rgba(185, 28, 28, 0.12);
  color: var(--danger);
}

.detail-user-link {
  color: inherit;
}

@media (max-width: 1180px) {
  .song-detail-card {
    grid-template-columns: 1fr;
  }

  .song-lyric-panel,
  .song-lyric-display,
  .song-lyric-empty {
    min-height: 260px;
    max-height: 260px;
  }
}

@media (max-width: 960px) {
  .song-detail-layout {
    grid-template-columns: 1fr;
  }
}
</style>
