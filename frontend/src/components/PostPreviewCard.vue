<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { RouterLink } from 'vue-router'
import ProtectedImage from '@/components/ProtectedImage.vue'
import { postApi } from '@/api'
import { useUiStore } from '@/stores/ui'
import { formatCompactNumber, truncate } from '@/utils/format'
import type { PostCard } from '@/types/api'

const props = withDefaults(
  defineProps<{
    post: PostCard
    rank?: number
    showAuthor?: boolean
    showSummary?: boolean
  }>(),
  {
    rank: undefined,
    showAuthor: true,
    showSummary: true
  }
)

const ui = useUiStore()

const summary = computed(() => truncate(props.post.content || '这个帖子还没有正文摘要。', 72))
const isLiked = ref(false)
const likeCount = ref(0)
const isFavourite = ref(false)
const favouriteCount = ref(0)
const likeLoading = ref(false)
const favouriteLoading = ref(false)

watch(
  () => props.post,
  (post) => {
    isLiked.value = Boolean(post.isLiked)
    likeCount.value = Number(post.likeCount ?? 0)
    isFavourite.value = Boolean(post.isFavourite)
    favouriteCount.value = Number(post.favouriteCount ?? 0)
  },
  { immediate: true, deep: true }
)

async function toggleLike() {
  if (likeLoading.value) {
    return
  }

  likeLoading.value = true
  const previousLiked = isLiked.value
  const previousCount = likeCount.value
  isLiked.value = !previousLiked
  likeCount.value = Math.max(0, previousCount + (previousLiked ? -1 : 1))

  try {
    await postApi.togglePostLike(props.post.id)
  } catch (error) {
    isLiked.value = previousLiked
    likeCount.value = previousCount
    ui.error(error instanceof Error ? error.message : '帖子点赞失败')
  } finally {
    likeLoading.value = false
  }
}

async function toggleFavourite() {
  if (favouriteLoading.value) {
    return
  }

  favouriteLoading.value = true
  const previousFavourite = isFavourite.value
  const previousCount = favouriteCount.value
  isFavourite.value = !previousFavourite
  favouriteCount.value = Math.max(0, previousCount + (previousFavourite ? -1 : 1))

  try {
    await postApi.togglePostFavourite(props.post.id)
  } catch (error) {
    isFavourite.value = previousFavourite
    favouriteCount.value = previousCount
    ui.error(error instanceof Error ? error.message : '帖子收藏失败')
  } finally {
    favouriteLoading.value = false
  }
}
</script>

<template>
  <article class="preview-card">
    <div class="preview-cover-shell">
      <RouterLink class="preview-cover-link" :to="`/posts/${post.id}`">
        <ProtectedImage :src="post.coverUrl" class="preview-cover" />
      </RouterLink>

      <RouterLink v-if="showAuthor" class="preview-author-badge" :to="`/users/${post.userId}`">
        <ProtectedImage :src="post.userAvatar" class="preview-avatar" />
        <strong>{{ post.username || '匿名用户' }}</strong>
      </RouterLink>

      <div class="preview-overlay">
        <span class="preview-stat">
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path
              d="M4 7.5A2.5 2.5 0 0 1 6.5 5h11A2.5 2.5 0 0 1 20 7.5v7A2.5 2.5 0 0 1 17.5 17h-11A2.5 2.5 0 0 1 4 14.5z"
              fill="none"
              stroke="currentColor"
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="1.8"
            />
            <path
              d="m10 9 5 3-5 3z"
              fill="currentColor"
              stroke="currentColor"
              stroke-linejoin="round"
              stroke-width="1.2"
            />
          </svg>
          {{ formatCompactNumber(post.viewCount) }}
        </span>

        <span v-if="typeof post.commentCount === 'number'" class="preview-stat">
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path
              d="M6.5 6h11A2.5 2.5 0 0 1 20 8.5v5A2.5 2.5 0 0 1 17.5 16H11l-3.8 2.8c-.4.3-1 .1-1-.5V16.8A2.5 2.5 0 0 1 4 14.5v-6A2.5 2.5 0 0 1 6.5 6Z"
              fill="none"
              stroke="currentColor"
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="1.8"
            />
          </svg>
          {{ formatCompactNumber(post.commentCount) }}
        </span>

        <span v-if="rank" class="preview-rank">#{{ rank }}</span>
      </div>
    </div>

    <div class="preview-body">
      <RouterLink class="preview-title-link" :to="`/posts/${post.id}`">
        <h3 class="preview-title">{{ post.title || '无标题帖子' }}</h3>
      </RouterLink>

      <p v-if="showSummary" class="preview-summary">{{ summary }}</p>

      <div class="preview-state-row">
        <button
          class="state-pill state-pill-button"
          :class="{ active: isLiked, liked: isLiked }"
          type="button"
          :disabled="likeLoading"
          @click="toggleLike"
        >
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path
              d="M12 20.5 4.8 13.8a4.7 4.7 0 0 1 6.6-6.7L12 7.8l.6-.7a4.7 4.7 0 0 1 6.6 6.7z"
              :fill="isLiked ? 'currentColor' : 'none'"
              stroke="currentColor"
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="1.8"
            />
          </svg>
          {{ formatCompactNumber(likeCount) }}
        </button>

        <button
          class="state-pill state-pill-button"
          :class="{ active: isFavourite, favourite: isFavourite }"
          type="button"
          :disabled="favouriteLoading"
          @click="toggleFavourite"
        >
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path
              d="m12 4 2.5 5.1 5.6.8-4 3.9.9 5.5-5-2.7-5 2.7.9-5.5-4-3.9 5.6-.8z"
              :fill="isFavourite ? 'currentColor' : 'none'"
              stroke="currentColor"
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="1.8"
            />
          </svg>
          {{ formatCompactNumber(favouriteCount) }}
        </button>
      </div>
    </div>
  </article>
</template>

<style scoped>
.preview-card {
  min-height: 100%;
  display: grid;
  gap: 14px;
  padding: 14px;
  border: 1px solid var(--line);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.96);
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.preview-card:hover {
  transform: translateY(-2px);
  border-color: rgba(14, 165, 233, 0.22);
  box-shadow: 0 18px 34px rgba(20, 38, 76, 0.1);
}

.preview-cover-shell {
  position: relative;
}

.preview-cover-link {
  display: block;
  overflow: hidden;
  border-radius: 18px;
}

.preview-cover {
  width: 100%;
  aspect-ratio: 16 / 10;
  object-fit: cover;
  transition: transform 0.25s ease;
}

.preview-card:hover .preview-cover {
  transform: scale(1.03);
}

.preview-author-badge {
  position: absolute;
  inset: 12px auto auto 12px;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  max-width: calc(100% - 24px);
  padding: 10px 14px 10px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.92);
  color: var(--ink);
  box-shadow: 0 10px 24px rgba(20, 38, 76, 0.12);
  backdrop-filter: blur(10px);
}

.preview-author-badge strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 1.02rem;
}

.preview-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
  flex: none;
}

.preview-overlay {
  position: absolute;
  inset: auto 10px 10px 10px;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.preview-stat,
.preview-rank {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  min-height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(17, 24, 39, 0.72);
  color: #fff;
  font-size: 0.82rem;
  backdrop-filter: blur(8px);
}

.preview-rank {
  margin-left: auto;
  background: rgba(234, 88, 12, 0.92);
}

.preview-stat svg,
.state-pill svg {
  width: 15px;
  height: 15px;
  flex: none;
}

.preview-body {
  display: grid;
  gap: 10px;
}

.preview-title-link {
  color: inherit;
}

.preview-title {
  margin: 0;
  font-size: 1.06rem;
  line-height: 1.45;
}

.preview-summary {
  margin: 0;
  color: var(--muted);
  font-size: 0.92rem;
}

.preview-state-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.state-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  min-height: 32px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(23, 32, 51, 0.05);
  color: var(--muted);
  font-size: 0.85rem;
}

.state-pill-button:disabled {
  opacity: 0.65;
  cursor: wait;
}

.state-pill.active.liked {
  color: #dc2626;
  background: rgba(220, 38, 38, 0.1);
}

.state-pill.active.favourite {
  color: #d97706;
  background: rgba(245, 158, 11, 0.14);
}
</style>
