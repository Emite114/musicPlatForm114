<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'
import { RouterLink } from 'vue-router'
import { songApi } from '@/api'
import ProtectedImage from '@/components/ProtectedImage.vue'
import { usePlayerStore } from '@/stores/player'
import { useUiStore } from '@/stores/ui'

const player = usePlayerStore()
const ui = useUiStore()
const audioRef = ref<HTMLAudioElement | null>(null)
const favouriteLoading = ref(false)

const hasTrack = computed(() => Boolean(player.currentSong && player.audioSrc))
const currentProgress = computed(() => player.duration || 0)
const currentLyric = computed(() => player.currentLyric)
const nextLyric = computed(() => player.nextLyric)
const currentFavouriteCount = computed(() => player.currentSong?.favouriteCount ?? 0)
const isCurrentFavourite = computed(() => Boolean(player.currentSong?.isFavourite))

function formatDuration(value: number) {
  if (!Number.isFinite(value) || value <= 0) {
    return '00:00'
  }

  const totalSeconds = Math.floor(value)
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = totalSeconds % 60
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
}

async function tryAutoplay() {
  if (!player.playRequested || !audioRef.value || !player.audioSrc) {
    return
  }

  if (audioRef.value.readyState < 2) {
    return
  }

  try {
    await audioRef.value.play()
    player.consumeAutoplay()
  } catch {
    player.setPlaying(false)
  }
}

function handlePlaybackStarted() {
  player.setPlaying(true)
  if (player.playRequested) {
    player.consumeAutoplay()
  }
}

function applyPendingSeek() {
  if (!audioRef.value) {
    return
  }

  const nextSeek = player.consumePendingSeek()
  if (nextSeek == null || nextSeek <= 0) {
    return
  }

  try {
    audioRef.value.currentTime = nextSeek
    player.syncTime(nextSeek)
  } catch {
    player.syncTime(0)
  }
}

async function togglePlayback() {
  if (!audioRef.value || !player.audioSrc) {
    return
  }

  if (audioRef.value.paused) {
    try {
      await audioRef.value.play()
    } catch {
      player.setPlaying(false)
    }
    return
  }

  audioRef.value.pause()
}

function seekPlayback(event: Event) {
  if (!audioRef.value) {
    return
  }

  const nextTime = Number((event.target as HTMLInputElement).value)
  audioRef.value.currentTime = nextTime
  player.syncTime(nextTime)
}

async function handleTrackEnded() {
  await player.handleTrackEnded()
}

async function toggleCurrentFavourite() {
  if (!player.currentSong || favouriteLoading.value) {
    return
  }

  const previousFavourite = Boolean(player.currentSong.isFavourite)
  const previousCount = Number(player.currentSong.favouriteCount ?? 0)
  favouriteLoading.value = true

  player.currentSong.isFavourite = !previousFavourite
  player.currentSong.favouriteCount = Math.max(
    previousCount + (previousFavourite ? -1 : 1),
    0
  )

  try {
    await songApi.toggleSongFavourite(player.currentSong.id)
  } catch (error) {
    player.currentSong.isFavourite = previousFavourite
    player.currentSong.favouriteCount = previousCount
    ui.error(error instanceof Error ? error.message : '收藏更新失败')
  } finally {
    favouriteLoading.value = false
  }
}

watch(
  () => player.audioSrc,
  async () => {
    await nextTick()
    if (!audioRef.value) {
      return
    }

    audioRef.value.currentTime = 0
    audioRef.value.load()
  }
)
</script>

<template>
  <section class="global-player" :class="{ disabled: !hasTrack }">
    <audio
      ref="audioRef"
      :src="player.audioSrc"
      :autoplay="player.playRequested"
      preload="auto"
      @canplaythrough="tryAutoplay"
      @canplay="tryAutoplay"
      @durationchange="player.syncDuration(audioRef?.duration ?? 0)"
      @ended="handleTrackEnded"
      @loadedmetadata="applyPendingSeek"
      @loadeddata="tryAutoplay"
      @pause="player.setPlaying(false)"
      @play="handlePlaybackStarted"
      @timeupdate="player.syncTime(audioRef?.currentTime ?? 0)"
    />

    <div class="player-track">
      <RouterLink
        v-if="player.currentSong"
        class="player-cover-link"
        :to="`/songs/${player.currentSong.id}`"
      >
        <ProtectedImage
          :src="player.currentSong.avatarUrl || '/default/defaultSongAvatar.png'"
          class="player-cover"
        />
      </RouterLink>
      <ProtectedImage
        v-else
        src="/default/defaultSongAvatar.png"
        class="player-cover"
      />

      <div class="player-copy">
        <div class="player-title-row">
          <RouterLink
            v-if="player.currentSong"
            class="player-title-link"
            :to="`/songs/${player.currentSong.id}`"
          >
            {{ player.currentSong.songName || '未选择歌曲' }}
          </RouterLink>
          <strong v-else>暂无播放歌曲</strong>
        </div>

        <p class="player-subtitle">
          {{ player.currentSong?.songArtist || '等待播放列表' }}
        </p>

        <RouterLink
          v-if="player.currentSong?.sharedByUserId"
          class="player-meta-link"
          :to="`/users/${player.currentSong.sharedByUserId}`"
        >
          分享者 {{ player.currentSong.sharedByUsername || `#${player.currentSong.sharedByUserId}` }}
        </RouterLink>
        <p v-else class="player-meta-link muted">当前歌曲没有分享者信息</p>
      </div>
    </div>

    <div class="player-main">
      <div class="player-control-strip">
        <div class="player-badge-row">
          <button class="player-badge player-mode-button" type="button" @click="player.cyclePlayMode">
            {{ player.playModeLabel }}
          </button>

          <span class="player-badge player-queue-badge">{{ player.queueLabel }}</span>

          <button
            class="player-badge player-favourite-button"
            :class="{ active: isCurrentFavourite }"
            type="button"
            :disabled="!player.currentSong || favouriteLoading"
            @click="toggleCurrentFavourite"
          >
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <path
                d="m12 4 2.5 5.1 5.6.8-4 3.9.9 5.5-5-2.7-5 2.7.9-5.5-4-3.9 5.6-.8z"
                :fill="isCurrentFavourite ? 'currentColor' : 'none'"
                stroke="currentColor"
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="1.8"
              />
            </svg>
            {{ currentFavouriteCount }}
          </button>
        </div>

        <div class="player-controls">
          <button
            class="player-icon-button"
            type="button"
            :disabled="!player.hasQueue"
            @click="player.playPrevious"
          >
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <path d="M7 6v12M17 7l-7 5 7 5z" fill="currentColor" />
            </svg>
          </button>

          <button
            class="player-icon-button player-play-button"
            type="button"
            :disabled="!hasTrack"
            @click="togglePlayback"
          >
            <svg v-if="player.isPlaying" viewBox="0 0 24 24" aria-hidden="true">
              <path d="M7 6h3v12H7zm7 0h3v12h-3z" fill="currentColor" />
            </svg>
            <svg v-else viewBox="0 0 24 24" aria-hidden="true">
              <path d="m8 6 10 6-10 6z" fill="currentColor" />
            </svg>
          </button>

          <button
            class="player-icon-button"
            type="button"
            :disabled="!player.hasQueue"
            @click="player.playNext"
          >
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <path d="M17 6v12M7 7l7 5-7 5z" fill="currentColor" />
            </svg>
          </button>
        </div>

        <div class="player-control-spacer" aria-hidden="true"></div>
      </div>

      <div class="player-progress-row">
        <span>{{ formatDuration(player.currentTime) }}</span>
        <input
          class="player-progress"
          type="range"
          min="0"
          :max="currentProgress"
          step="0.1"
          :value="player.currentTime"
          :disabled="!hasTrack"
          @input="seekPlayback"
        />
        <span>{{ formatDuration(player.duration) }}</span>
      </div>
    </div>

    <div v-if="currentLyric || nextLyric" class="player-lyric">
      <p class="player-lyric-current">{{ currentLyric || '...' }}</p>
      <p v-if="nextLyric" class="player-lyric-next">{{ nextLyric }}</p>
    </div>

    <div v-else class="player-lyric player-lyric-empty">
      <p>{{ player.error || '歌词暂不可用' }}</p>
    </div>
  </section>
</template>

<style scoped>
.global-player {
  position: fixed;
  left: 320px;
  right: 36px;
  bottom: 20px;
  z-index: 35;
  display: grid;
  grid-template-columns: minmax(280px, 360px) minmax(320px, 1.2fr) minmax(220px, 320px);
  gap: 14px;
  align-items: center;
  padding: 14px 18px;
  border: 1px solid rgba(23, 32, 51, 0.1);
  border-radius: 26px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 28px 70px rgba(20, 38, 76, 0.18);
  backdrop-filter: blur(16px);
}

.global-player.disabled {
  opacity: 0.92;
}

.player-track {
  display: flex;
  gap: 12px;
  align-items: center;
  min-width: 0;
}

.player-cover {
  width: 100px;
  height: 100px;
  border-radius: 16px;
  object-fit: cover;
  flex: none;
}

.player-cover-link {
  display: inline-flex;
  flex: none;
  border-radius: 16px;
  overflow: hidden;
}

.player-copy {
  min-width: 0;
  display: grid;
  gap: 4px;
}

.player-title-row,
.player-badge-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.player-badge-row {
  min-height: 28px;
  justify-content: center;
  margin: 2px 0;
}

.player-title-link,
.player-copy strong {
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 1rem;
  font-weight: 700;
  color: var(--ink);
}

.player-subtitle,
.player-meta-link {
  margin: 0;
  color: var(--muted);
  font-size: 0.84rem;
}

.player-meta-link {
  display: inline-flex;
  width: fit-content;
}

.player-badge {
  display: inline-flex;
  align-items: center;
  min-height: 30px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(234, 88, 12, 0.1);
  color: #c2410c;
  font-size: 0.82rem;
  font-weight: 600;
}

.player-mode-button {
  border: 0;
  cursor: pointer;
}

.player-favourite-button {
  border: 0;
  cursor: pointer;
  gap: 6px;
  background: rgba(245, 158, 11, 0.08);
  color: #b45309;
}

.player-favourite-button svg {
  width: 16px;
  height: 16px;
}

.player-favourite-button.active {
  background: rgba(245, 158, 11, 0.16);
  color: #d97706;
}

.player-favourite-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.player-queue-badge {
  background: rgba(15, 118, 110, 0.1);
  color: #0f766e;
}

.player-main {
  display: grid;
  gap: 10px;
  align-content: center;
}

.player-control-strip {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto minmax(0, 1fr);
  align-items: center;
  gap: 14px;
}

.player-controls {
  display: flex;
  justify-content: center;
  gap: 14px;
}

.player-control-spacer {
  min-height: 1px;
}

.player-icon-button {
  width: 48px;
  height: 48px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  background: rgba(23, 32, 51, 0.06);
  color: var(--ink);
}

.player-icon-button:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.player-play-button {
  width: 58px;
  height: 58px;
  background: linear-gradient(135deg, var(--primary) 0%, #fb923c 100%);
  color: white;
}

.player-icon-button svg {
  width: 18px;
  height: 18px;
}

.player-progress-row {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  gap: 8px;
  align-items: center;
  color: var(--muted);
  font-size: 0.8rem;
}

.player-progress {
  width: 100%;
}

.player-lyric {
  min-width: 0;
  display: grid;
  gap: 6px;
  justify-items: end;
  text-align: right;
}

.player-lyric p {
  margin: 0;
}

.player-lyric-current {
  font-size: 0.94rem;
  font-weight: 700;
  color: var(--ink);
}

.player-lyric-next,
.player-lyric-empty {
  color: var(--muted);
  font-size: 0.82rem;
}

@media (max-width: 1180px) {
  .global-player {
    left: 20px;
    right: 20px;
    grid-template-columns: 1fr;
    justify-items: stretch;
    text-align: left;
  }

  .player-lyric {
    justify-items: start;
    text-align: left;
  }
}

@media (max-width: 720px) {
  .global-player {
    left: 14px;
    right: 14px;
    bottom: 14px;
    padding: 14px;
  }

  .player-track {
    align-items: flex-start;
  }
}
</style>
