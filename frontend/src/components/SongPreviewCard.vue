<script setup lang="ts">
import { ref, watch } from 'vue'
import { RouterLink } from 'vue-router'
import ProtectedImage from '@/components/ProtectedImage.vue'
import { songApi } from '@/api'
import { usePlayerStore } from '@/stores/player'
import { useUiStore } from '@/stores/ui'
import { formatCompactNumber } from '@/utils/format'
import type { SongCard } from '@/types/api'

const props = withDefaults(
  defineProps<{
    song: SongCard
    rank?: number
    queueMode?: 'temporary' | 'favourite'
    favouriteOwnerId?: number | null
  }>(),
  {
    rank: undefined,
    queueMode: 'temporary',
    favouriteOwnerId: null
  }
)

const player = usePlayerStore()
const ui = useUiStore()
const isFavourite = ref(false)
const favouriteCount = ref(0)
const favouriteLoading = ref(false)

watch(
  () => props.song,
  (song) => {
    isFavourite.value = Boolean(song.isFavourite)
    favouriteCount.value = Number(song.favouriteCount ?? 0)
  },
  { immediate: true, deep: true }
)

async function playCurrentSong() {
  if (props.queueMode === 'favourite') {
    await player.playFromFavouriteQueue(props.song.id, props.favouriteOwnerId)
    return
  }

  await player.playSong(props.song.id)
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
    await songApi.toggleSongFavourite(props.song.id)
  } catch (error) {
    isFavourite.value = previousFavourite
    favouriteCount.value = previousCount
    ui.error(error instanceof Error ? error.message : '歌曲收藏失败')
  } finally {
    favouriteLoading.value = false
  }
}
</script>

<template>
  <article class="preview-card">
    <RouterLink class="preview-cover-link" :to="`/songs/${song.id}`">
      <ProtectedImage :src="song.avatarUrl" class="preview-cover" />

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
          {{ formatCompactNumber(song.playCount) }}
        </span>

        <span class="preview-stat">
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
          {{ formatCompactNumber(song.commentCount) }}
        </span>

        <span v-if="rank" class="preview-rank">#{{ rank }}</span>
      </div>
    </RouterLink>

    <div class="preview-body">
      <RouterLink class="preview-title-link" :to="`/songs/${song.id}`">
        <h3 class="preview-title">{{ song.songName || '未命名歌曲' }}</h3>
      </RouterLink>

      <p class="preview-artist">{{ song.songArtist || '未知歌手' }}</p>

      <div class="preview-state-row">
        <button class="state-pill play-pill" type="button" @click="playCurrentSong">
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path d="m8 6 10 6-10 6z" fill="currentColor" />
          </svg>
          播放
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
  gap: 12px;
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

.preview-cover-link {
  position: relative;
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
  gap: 8px;
}

.preview-title {
  margin: 0;
  font-size: 1rem;
  line-height: 1.45;
}

.preview-artist {
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

.play-pill {
  color: white;
  background: linear-gradient(135deg, var(--primary) 0%, #fb923c 100%);
}

.state-pill-button:disabled {
  opacity: 0.65;
  cursor: wait;
}

.state-pill.active.favourite {
  color: #d97706;
  background: rgba(245, 158, 11, 0.14);
}
</style>
