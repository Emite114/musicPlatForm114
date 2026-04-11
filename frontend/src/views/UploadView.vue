<script setup lang="ts">
import { nextTick, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import MediaUploader from '@/components/MediaUploader.vue'
import PageHeader from '@/components/PageHeader.vue'
import { postApi, songApi } from '@/api'
import { useUiStore } from '@/stores/ui'
import type { UploadedMedia } from '@/types/api'

const route = useRoute()
const ui = useUiStore()

const postSectionRef = ref<HTMLElement | null>(null)
const songSectionRef = ref<HTMLElement | null>(null)

const postForm = reactive({
  title: '',
  content: ''
})

const songForm = reactive({
  songName: '',
  songArtist: ''
})

const postSubmitting = ref(false)
const songSubmitting = ref(false)
const postMediaFiles = ref<UploadedMedia[]>([])
const audioFiles = ref<UploadedMedia[]>([])
const lyricFiles = ref<UploadedMedia[]>([])
const coverFiles = ref<UploadedMedia[]>([])

async function scrollToRequestedSection() {
  await nextTick()

  if (route.query.section === 'song') {
    songSectionRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
    return
  }

  if (route.query.section === 'post') {
    postSectionRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

async function submitPost() {
  if (!postForm.content.trim()) {
    ui.error('帖子内容不能为空')
    return
  }

  postSubmitting.value = true
  try {
    await postApi.createPost({
      title: postForm.title.trim(),
      content: postForm.content.trim(),
      mediaIdList: postMediaFiles.value.map((item) => item.mediaId)
    })
    postForm.title = ''
    postForm.content = ''
    postMediaFiles.value = []
    ui.success('帖子发布成功')
  } catch (error) {
    ui.error(error instanceof Error ? error.message : '发帖失败')
  } finally {
    postSubmitting.value = false
  }
}

async function submitSong() {
  if (!songForm.songName.trim()) {
    ui.error('歌曲名不能为空')
    return
  }

  if (!audioFiles.value.length) {
    ui.error('请先选择音频文件')
    return
  }

  songSubmitting.value = true
  try {
    await songApi.uploadSong({
      songName: songForm.songName.trim(),
      songArtist: songForm.songArtist.trim() || 'N/A',
      audioUrl: audioFiles.value[0].url,
      lrcUrl: lyricFiles.value[0]?.url,
      avatarUrl: coverFiles.value[0]?.url
    })
    songForm.songName = ''
    songForm.songArtist = ''
    audioFiles.value = []
    lyricFiles.value = []
    coverFiles.value = []
    ui.success('歌曲分享成功')
  } catch (error) {
    ui.error(error instanceof Error ? error.message : '分享歌曲失败')
  } finally {
    songSubmitting.value = false
  }
}

watch(
  () => route.query.section,
  () => {
    void scrollToRequestedSection()
  }
)

onMounted(() => {
  void scrollToRequestedSection()
})
</script>

<template>
  <section ref="postSectionRef" class="panel upload-section">
    <PageHeader
      eyebrow="Post"
      title="发布帖子"
    />

    <form class="stack upload-form" @submit.prevent="submitPost">
      <div class="field">
        <label for="upload-post-title">标题</label>
        <input id="upload-post-title" v-model.trim="postForm.title" placeholder="可以为空，但建议写标题" />
      </div>

      <div class="field">
        <label for="upload-post-content">内容</label>
        <textarea
          id="upload-post-content"
          v-model.trim="postForm.content"
          placeholder="写下你想分享的帖子内容"
        />
      </div>

      <div class="field">
        <label>媒体附件</label>
        <MediaUploader
          v-model="postMediaFiles"
          accept="image/*,video/*"
          multiple
          button-text="选择图片或视频"
        />
      </div>

      <div class="submit-row">
        <button class="btn btn-primary" type="submit" :disabled="postSubmitting">
          {{ postSubmitting ? '发布中...' : '发布帖子' }}
        </button>
      </div>
    </form>
  </section>

  <section ref="songSectionRef" class="panel upload-section">
    <PageHeader
      eyebrow="Song"
      title="分享歌曲"
    />

    <form class="stack upload-form" @submit.prevent="submitSong">
      <div class="field">
        <label for="upload-song-name">歌曲名</label>
        <input id="upload-song-name" v-model.trim="songForm.songName" placeholder="请输入歌曲名" />
      </div>

      <div class="field">
        <label for="upload-song-artist">歌手</label>
        <input
          id="upload-song-artist"
          v-model.trim="songForm.songArtist"
          placeholder="可留空，默认 N/A"
        />
      </div>

      <div class="field">
        <label>音频文件</label>
        <MediaUploader v-model="audioFiles" accept=".mp3,.wav,audio/*" button-text="选择音频" />
      </div>

      <div class="field">
        <label>歌词文件</label>
        <MediaUploader v-model="lyricFiles" accept=".lrc,text/plain" button-text="选择歌词" />
      </div>

      <div class="field">
        <label>封面文件</label>
        <MediaUploader v-model="coverFiles" accept=".jpg,.jpeg,.png,image/*" button-text="选择封面" />
      </div>

      <div class="submit-row">
        <button class="btn btn-primary" type="submit" :disabled="songSubmitting">
          {{ songSubmitting ? '分享中...' : '分享歌曲' }}
        </button>
      </div>
    </form>
  </section>
</template>

<style scoped>
.upload-section {
  scroll-margin-top: 24px;
}

.upload-form {
  margin-top: 18px;
}

.submit-row {
  display: flex;
  justify-content: flex-start;
}
</style>

