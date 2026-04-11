import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import http from '@/api/http'
import { songApi } from '@/api/song'
import { resolveAssetUrl } from '@/utils/asset'
import { parseTimedLyricLines, type TimedLyricLine } from '@/utils/lyric'
import type { SongDetail } from '@/types/api'

interface ResumeSnapshot {
  songId: number
  index: number
  time: number
}

export type PlayMode = 'list-loop' | 'single-loop' | 'shuffle'

const PLAY_MODE_KEY = 'musicplatform_play_mode'
const PLAY_MODE_ORDER: PlayMode[] = ['list-loop', 'single-loop', 'shuffle']

function normalizeQueue(ids: number[]) {
  return [...new Set(ids.map((item) => Number(item)).filter((item) => Number.isFinite(item) && item > 0))]
}

export const usePlayerStore = defineStore('player', () => {
  const queueIds = ref<number[]>([])
  const currentIndex = ref(-1)
  const currentSong = ref<SongDetail | null>(null)
  const audioSrc = ref('')
  const lyrics = ref<TimedLyricLine[]>([])
  const currentLyricIndex = ref(-1)
  const currentTime = ref(0)
  const duration = ref(0)
  const isPlaying = ref(false)
  const loading = ref(false)
  const queueLoading = ref(false)
  const bootstrapped = ref(false)
  const playRequested = ref(false)
  const pendingSeekTime = ref<number | null>(null)
  const error = ref('')
  const playMode = ref<PlayMode>((localStorage.getItem(PLAY_MODE_KEY) as PlayMode) || 'list-loop')
  const temporarySongId = ref(0)
  const resumeSnapshot = ref<ResumeSnapshot | null>(null)

  let trackRequestId = 0
  let audioObjectUrl: string | null = null

  const baseSongId = computed(() => {
    if (currentIndex.value >= 0 && currentIndex.value < queueIds.value.length) {
      return queueIds.value[currentIndex.value]
    }
    return 0
  })

  const isTemporaryPlayback = computed(() => temporarySongId.value > 0)
  const currentSongId = computed(() => temporarySongId.value || baseSongId.value || currentSong.value?.id || 0)
  const currentLyric = computed(() => lyrics.value[currentLyricIndex.value]?.text ?? '')
  const nextLyric = computed(() => lyrics.value[currentLyricIndex.value + 1]?.text ?? '')
  const hasQueue = computed(() => queueIds.value.length > 0)
  const queueLabel = computed(() => {
    const baseLabel = queueIds.value.length ? `${Math.max(currentIndex.value + 1, 0)}/${queueIds.value.length}` : '0/0'
    return isTemporaryPlayback.value ? `插播 | ${baseLabel}` : baseLabel
  })
  const playModeLabel = computed(() => {
    if (playMode.value === 'single-loop') {
      return '单曲循环'
    }
    if (playMode.value === 'shuffle') {
      return '随机播放'
    }
    return '列表循环'
  })

  function cleanupAudioUrl() {
    if (audioObjectUrl) {
      URL.revokeObjectURL(audioObjectUrl)
      audioObjectUrl = null
    }
  }

  function clearTemporaryState() {
    temporarySongId.value = 0
    resumeSnapshot.value = null
  }

  function resetTrackState() {
    cleanupAudioUrl()
    currentSong.value = null
    audioSrc.value = ''
    lyrics.value = []
    currentLyricIndex.value = -1
    currentTime.value = 0
    duration.value = 0
    isPlaying.value = false
    playRequested.value = false
    pendingSeekTime.value = null
  }

  function updateLyricIndex(time: number) {
    if (!lyrics.value.length) {
      currentLyricIndex.value = -1
      return
    }

    let index = -1
    for (let cursor = 0; cursor < lyrics.value.length; cursor += 1) {
      if (lyrics.value[cursor].time <= time + 0.08) {
        index = cursor
      } else {
        break
      }
    }
    currentLyricIndex.value = index
  }

  function rememberQueueSongForResume() {
    if (temporarySongId.value || !baseSongId.value) {
      return
    }

    resumeSnapshot.value = {
      songId: baseSongId.value,
      index: currentIndex.value,
      time: currentTime.value
    }
  }

  function persistPlayMode(mode: PlayMode) {
    playMode.value = mode
    localStorage.setItem(PLAY_MODE_KEY, mode)
  }

  function cyclePlayMode() {
    const currentCursor = PLAY_MODE_ORDER.indexOf(playMode.value)
    const nextMode = PLAY_MODE_ORDER[(currentCursor + 1) % PLAY_MODE_ORDER.length]
    persistPlayMode(nextMode)
  }

  function pickRandomIndex() {
    if (!queueIds.value.length) {
      return -1
    }
    if (queueIds.value.length === 1) {
      return 0
    }

    let nextIndex = currentIndex.value
    while (nextIndex === currentIndex.value) {
      nextIndex = Math.floor(Math.random() * queueIds.value.length)
    }
    return nextIndex
  }

  function stepQueue(direction: 'next' | 'previous') {
    if (!queueIds.value.length) {
      return
    }

    if (playMode.value === 'single-loop') {
      currentIndex.value = Math.max(currentIndex.value, 0)
      return
    }

    if (playMode.value === 'shuffle') {
      currentIndex.value = pickRandomIndex()
      return
    }

    if (direction === 'next') {
      currentIndex.value = (currentIndex.value + 1 + queueIds.value.length) % queueIds.value.length
      return
    }

    currentIndex.value = (currentIndex.value - 1 + queueIds.value.length) % queueIds.value.length
  }

  async function fetchAudioObjectUrl(source: string) {
    const normalized = resolveAssetUrl(source, 'media')
    const response = await http.get<Blob>(normalized, {
      responseType: 'blob'
    })
    return URL.createObjectURL(response.data)
  }

  async function fetchLyricLines(source?: string) {
    if (!source) {
      return []
    }

    const normalized = resolveAssetUrl(source, 'media')
    const response = await http.get<string>(normalized, {
      responseType: 'text'
    })
    const text = typeof response.data === 'string' ? response.data : ''
    return parseTimedLyricLines(text)
  }

  async function loadSong(songId: number, autoplay = true, seekTime = 0) {
    const requestId = ++trackRequestId
    loading.value = true
    error.value = ''
    playRequested.value = false
    currentTime.value = 0
    duration.value = 0
    currentLyricIndex.value = -1
    lyrics.value = []
    pendingSeekTime.value = seekTime > 0 ? seekTime : null

    try {
      const detail = await songApi.getSongDetail(songId)
      if (requestId !== trackRequestId) {
        return
      }

      const nextAudioUrl = await fetchAudioObjectUrl(detail.audioUrl)
      if (requestId !== trackRequestId) {
        URL.revokeObjectURL(nextAudioUrl)
        return
      }

      cleanupAudioUrl()
      currentSong.value = detail
      audioObjectUrl = nextAudioUrl
      playRequested.value = autoplay
      audioSrc.value = nextAudioUrl
      updateLyricIndex(seekTime)

      try {
        const nextLyrics = await fetchLyricLines(detail.lrcUrl)
        if (requestId !== trackRequestId) {
          return
        }
        lyrics.value = nextLyrics
        updateLyricIndex(seekTime || currentTime.value)
      } catch {
        if (requestId !== trackRequestId) {
          return
        }
        lyrics.value = []
        currentLyricIndex.value = -1
      }
    } catch (caught) {
      if (requestId !== trackRequestId) {
        return
      }
      if (temporarySongId.value === songId) {
        clearTemporaryState()
      }
      resetTrackState()
      error.value = caught instanceof Error ? caught.message : '歌曲加载失败'
    } finally {
      if (requestId === trackRequestId) {
        loading.value = false
      }
    }
  }

  async function playCurrentQueueSong(autoplay = true, seekTime = 0) {
    if (!baseSongId.value) {
      resetTrackState()
      return
    }
    await loadSong(baseSongId.value, autoplay, seekTime)
  }

  async function setQueue(ids: number[], startSongId?: number, autoplay = true) {
    const nextQueue = normalizeQueue(ids)
    queueIds.value = nextQueue
    clearTemporaryState()

    if (!nextQueue.length) {
      currentIndex.value = -1
      resetTrackState()
      return
    }

    const nextIndex =
      startSongId != null ? nextQueue.indexOf(startSongId) : Math.max(currentIndex.value, 0)
    currentIndex.value = nextIndex >= 0 ? nextIndex : 0
    await playCurrentQueueSong(autoplay)
  }

  async function playSong(songId: number) {
    const targetId = Number(songId)
    if (!Number.isFinite(targetId) || targetId <= 0) {
      return
    }

    rememberQueueSongForResume()
    temporarySongId.value = targetId
    await loadSong(targetId, true)
  }

  async function playFromFavouriteQueue(songId: number, ownerId?: number | null) {
    const targetId = Number(songId)
    if (!Number.isFinite(targetId) || targetId <= 0) {
      return
    }

    queueLoading.value = true
    error.value = ''

    try {
      const ids = await songApi.getOnesFavouriteSongIdList(ownerId ?? undefined, 0, 500, 'time')
      let nextQueue = normalizeQueue(ids)

      if (!nextQueue.length) {
        nextQueue = [targetId]
      } else if (!nextQueue.includes(targetId)) {
        nextQueue = normalizeQueue([targetId, ...nextQueue])
      }

      bootstrapped.value = true
      await setQueue(nextQueue, targetId, true)
    } catch (caught) {
      error.value = caught instanceof Error ? caught.message : '播放列表加载失败'
    } finally {
      queueLoading.value = false
    }
  }

  async function bootstrapQueue(force = false) {
    if (bootstrapped.value && !force) {
      return
    }

    queueLoading.value = true
    error.value = ''

    try {
      const ids = await songApi.getOnesFavouriteSongIdList(undefined, 0, 500, 'time')
      bootstrapped.value = true

      if (!ids.length) {
        queueIds.value = []
        currentIndex.value = -1
        clearTemporaryState()
        resetTrackState()
        return
      }

      await setQueue(ids, ids[0], false)
    } catch (caught) {
      bootstrapped.value = true
      queueIds.value = []
      currentIndex.value = -1
      clearTemporaryState()
      resetTrackState()
      error.value = caught instanceof Error ? caught.message : '播放列表加载失败'
    } finally {
      queueLoading.value = false
    }
  }

  async function resumeQueueSong() {
    const snapshot = resumeSnapshot.value
    temporarySongId.value = 0
    resumeSnapshot.value = null

    if (!snapshot || !queueIds.value.length) {
      setPlaying(false)
      playRequested.value = false
      return
    }

    let nextIndex = snapshot.index
    if (nextIndex < 0 || nextIndex >= queueIds.value.length || queueIds.value[nextIndex] !== snapshot.songId) {
      nextIndex = queueIds.value.indexOf(snapshot.songId)
    }

    if (nextIndex < 0) {
      nextIndex = 0
    }

    currentIndex.value = nextIndex
    await playCurrentQueueSong(true, snapshot.time)
  }

  async function playNext() {
    if (temporarySongId.value) {
      clearTemporaryState()
    }

    if (!queueIds.value.length) {
      return
    }

    stepQueue('next')
    await playCurrentQueueSong(true)
  }

  async function playPrevious() {
    if (temporarySongId.value) {
      clearTemporaryState()
    }

    if (!queueIds.value.length) {
      return
    }

    stepQueue('previous')
    await playCurrentQueueSong(true)
  }

  async function handleTrackEnded() {
    if (temporarySongId.value) {
      await resumeQueueSong()
      return
    }

    if (!queueIds.value.length) {
      setPlaying(false)
      playRequested.value = false
      return
    }

    stepQueue('next')
    await playCurrentQueueSong(true)
  }

  function syncTime(time: number) {
    currentTime.value = Number.isFinite(time) ? time : 0
    updateLyricIndex(currentTime.value)
  }

  function syncDuration(value: number) {
    duration.value = Number.isFinite(value) ? value : 0
  }

  function setPlaying(value: boolean) {
    isPlaying.value = value
  }

  function requestAutoplay() {
    playRequested.value = true
  }

  function consumeAutoplay() {
    playRequested.value = false
  }

  function consumePendingSeek() {
    const nextSeek = pendingSeekTime.value
    pendingSeekTime.value = null
    return nextSeek
  }

  function clear() {
    bootstrapped.value = false
    queueIds.value = []
    currentIndex.value = -1
    error.value = ''
    clearTemporaryState()
    resetTrackState()
  }

  return {
    queueIds,
    currentIndex,
    currentSong,
    currentSongId,
    audioSrc,
    lyrics,
    currentLyricIndex,
    currentLyric,
    nextLyric,
    currentTime,
    duration,
    isPlaying,
    isTemporaryPlayback,
    loading,
    queueLoading,
    playRequested,
    error,
    hasQueue,
    queueLabel,
    playMode,
    playModeLabel,
    bootstrapQueue,
    clear,
    consumeAutoplay,
    consumePendingSeek,
    cyclePlayMode,
    handleTrackEnded,
    playFromFavouriteQueue,
    playNext,
    playPrevious,
    playSong,
    requestAutoplay,
    setPlaying,
    setQueue,
    syncDuration,
    syncTime
  }
})
