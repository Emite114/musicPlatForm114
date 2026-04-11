<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { RouterLink } from 'vue-router'
import MediaUploader from '@/components/MediaUploader.vue'
import PageHeader from '@/components/PageHeader.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import SongPreviewCard from '@/components/SongPreviewCard.vue'
import { songApi } from '@/api'
import { useUiStore } from '@/stores/ui'
import type { PageResult, SongCard, UploadedMedia } from '@/types/api'

type SongSort = 'hotScore' | 'time' | 'favourite' | 'comment'

const ui = useUiStore()

const uploadForm = reactive({
  songName: '',
  songArtist: ''
})

const collectionKeyword = ref('')
const sortMode = ref<SongSort>('hotScore')
const audioFiles = ref<UploadedMedia[]>([])
const lrcFiles = ref<UploadedMedia[]>([])
const avatarFiles = ref<UploadedMedia[]>([])
const favouritePage = ref<PageResult<SongCard> | null>(null)
const loading = ref(false)
const submitting = ref(false)

const sortOptions: Array<{ value: SongSort; label: string }> = [
  { value: 'hotScore', label: '综合热度' },
  { value: 'time', label: '最新收藏' },
  { value: 'favourite', label: '最多收藏' },
  { value: 'comment', label: '最多评论' }
]

async function loadFavourites(page = 0) {
  loading.value = true
  try {
    favouritePage.value = await songApi.getFavouriteSongs(
      collectionKeyword.value.trim(),
      page,
      15,
      sortMode.value
    )
  } catch (error) {
    ui.error(error instanceof Error ? error.message : '收藏歌曲加载失败')
  } finally {
    loading.value = false
  }
}

async function toggleFavourite(songId: number) {
  try {
    await songApi.toggleSongFavourite(songId)
    ui.success('收藏状态已更新')
    await loadFavourites(favouritePage.value?.number ?? 0)
  } catch (error) {
    ui.error(error instanceof Error ? error.message : '收藏操作失败')
  }
}

async function submitSong() {
  if (!uploadForm.songName.trim()) {
    ui.error('歌曲名不能为空')
    return
  }
  if (!audioFiles.value.length) {
    ui.error('请先上传音频文件')
    return
  }

  submitting.value = true
  try {
    await songApi.uploadSong({
      songName: uploadForm.songName.trim(),
      songArtist: uploadForm.songArtist.trim() || 'N/A',
      audioUrl: audioFiles.value[0].url,
      lrcUrl: lrcFiles.value[0]?.url,
      avatarUrl: avatarFiles.value[0]?.url
    })
    ui.success('歌曲上传成功')
    uploadForm.songName = ''
    uploadForm.songArtist = ''
    audioFiles.value = []
    lrcFiles.value = []
    avatarFiles.value = []
    await loadFavourites(0)
  } catch (error) {
    ui.error(error instanceof Error ? error.message : '歌曲上传失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  void loadFavourites()
})
</script>

<template>
  <section class="panel">
    <PageHeader
      eyebrow="Library"
      title="我收藏的歌曲"
      description="歌曲卡片会直接展示播放量、评论量和收藏状态，和首页热榜保持同一套展示。"
    >
      <template #actions>
        <RouterLink class="btn btn-secondary" to="/search">去搜索中心</RouterLink>
      </template>
    </PageHeader>

    <form class="collection-toolbar" @submit.prevent="loadFavourites(0)">
      <input
        v-model.trim="collectionKeyword"
        class="collection-input"
        type="search"
        placeholder="在收藏歌曲中搜索歌名或歌手"
      />
      <button class="btn btn-primary" type="submit">搜索</button>
    </form>

    <div class="search-filters">
      <button
        v-for="option in sortOptions"
        :key="option.value"
        class="search-filter"
        :class="{ active: sortMode === option.value }"
        type="button"
        @click="sortMode = option.value; loadFavourites(0)"
      >
        {{ option.label }}
      </button>
    </div>

    <div v-if="favouritePage?.content.length" class="grid-5">
      <div v-for="song in favouritePage.content" :key="song.id" class="collection-card-wrap">
        <SongPreviewCard :song="song" queue-mode="favourite" />
        <button class="btn btn-danger collection-action" type="button" @click="toggleFavourite(song.id)">
          取消收藏
        </button>
      </div>
    </div>

    <div v-else class="empty">{{ loading ? '加载中...' : '还没有收藏歌曲' }}</div>

    <PaginationBar
      :page="favouritePage"
      @prev="loadFavourites((favouritePage?.number ?? 1) - 1)"
      @next="loadFavourites((favouritePage?.number ?? 0) + 1)"
    />
  </section>

  <section class="panel">
    <PageHeader eyebrow="Upload" title="上传歌曲" />

    <form class="stack" @submit.prevent="submitSong">
      <div class="field">
        <label for="song-name">歌曲名</label>
        <input id="song-name" v-model.trim="uploadForm.songName" />
      </div>
      <div class="field">
        <label for="song-artist">歌手</label>
        <input id="song-artist" v-model.trim="uploadForm.songArtist" placeholder="可留空，默认 N/A" />
      </div>
      <div class="field">
        <label>音频文件</label>
        <MediaUploader v-model="audioFiles" accept=".mp3,.wav,audio/*" button-text="上传音频" />
      </div>
      <div class="field">
        <label>歌词文件</label>
        <MediaUploader v-model="lrcFiles" accept=".lrc,text/plain" button-text="上传歌词" />
      </div>
      <div class="field">
        <label>封面文件</label>
        <MediaUploader v-model="avatarFiles" accept=".jpg,.jpeg,.png,image/*" button-text="上传封面" />
      </div>
      <button class="btn btn-primary" type="submit" :disabled="submitting">
        {{ submitting ? '提交中...' : '创建歌曲' }}
      </button>
    </form>
  </section>
</template>

<style scoped>
.collection-toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.collection-input {
  flex: 1;
  min-width: 0;
  padding: 12px 14px;
  border: 1px solid rgba(23, 32, 51, 0.12);
  border-radius: 16px;
  background: rgba(248, 250, 253, 0.9);
}

.search-filters {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 18px;
}

.search-filter {
  min-height: 42px;
  padding: 0 16px;
  border-radius: 14px;
  background: rgba(23, 32, 51, 0.05);
  color: var(--muted);
}

.search-filter.active {
  background: rgba(14, 165, 233, 0.12);
  color: #0284c7;
}

.collection-card-wrap {
  display: grid;
  gap: 10px;
}

.collection-action {
  width: 100%;
}

@media (max-width: 900px) {
  .collection-toolbar {
    flex-direction: column;
  }
}
</style>
