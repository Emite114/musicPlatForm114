<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import PageHeader from '@/components/PageHeader.vue'
import PostPreviewCard from '@/components/PostPreviewCard.vue'
import SongPreviewCard from '@/components/SongPreviewCard.vue'
import { postApi, songApi } from '@/api'
import { describeError } from '@/utils/errors'
import type { PageResult, PostCard, SongCard } from '@/types/api'

const route = useRoute()

const hotSongs = ref<PageResult<SongCard> | null>(null)
const hotPosts = ref<PageResult<PostCard> | null>(null)
const loadingSongs = ref(false)
const loadingPosts = ref(false)
const hotSongNotice = ref('')
const hotPostNotice = ref('')

const showSongs = computed(() => route.name === 'hot-songs')
const showPosts = computed(() => route.name === 'hot-posts')

async function loadHotSongs() {
  loadingSongs.value = true
  hotSongNotice.value = ''
  try {
    hotSongs.value = await songApi.searchSongs('', 0, 15, 'hotScore')
  } catch (error) {
    hotSongs.value = null
    hotSongNotice.value = `歌曲热榜暂不可用：${describeError(error, '加载失败')}`
  } finally {
    loadingSongs.value = false
  }
}

async function loadHotPosts() {
  loadingPosts.value = true
  hotPostNotice.value = ''
  try {
    hotPosts.value = await postApi.searchPosts('', 0, 15, 'hotScore')
  } catch (error) {
    hotPosts.value = null
    hotPostNotice.value = `帖子热榜暂不可用：${describeError(error, '加载失败')}`
  } finally {
    loadingPosts.value = false
  }
}

watch(
  () => route.name,
  async (name) => {
    if (name === 'hot-songs') {
      await loadHotSongs()
      return
    }

    if (name === 'hot-posts') {
      await loadHotPosts()
    }
  },
  { immediate: true }
)
</script>

<template>
  <section v-if="showSongs" class="panel">
    <PageHeader
      eyebrow="Songs"
      title="热门歌曲"
      description="按热度展示 15 首歌曲，每行展示 5 个。"
    >
      <template #actions>
        <RouterLink class="btn btn-secondary" :to="{ path: '/upload', query: { section: 'song' } }">
          分享歌曲
        </RouterLink>
      </template>
    </PageHeader>

    <div v-if="hotSongNotice" class="empty">{{ hotSongNotice }}</div>
    <div v-else-if="hotSongs?.content.length" class="grid-5">
      <SongPreviewCard
        v-for="(song, index) in hotSongs.content"
        :key="song.id"
        :song="song"
        :rank="index + 1"
      />
    </div>
    <div v-else class="empty">{{ loadingSongs ? '加载中...' : '暂无热门歌曲' }}</div>
  </section>

  <section v-else-if="showPosts" class="panel">
    <PageHeader
      eyebrow="Posts"
      title="热门帖子"
      description="按热度展示 15 篇帖子，每行展示 5 个。"
    >
      <template #actions>
        <RouterLink class="btn btn-secondary" :to="{ path: '/upload', query: { section: 'post' } }">
          分享帖子
        </RouterLink>
      </template>
    </PageHeader>

    <div v-if="hotPostNotice" class="empty">{{ hotPostNotice }}</div>
    <div v-else-if="hotPosts?.content.length" class="grid-5">
      <PostPreviewCard
        v-for="(post, index) in hotPosts.content"
        :key="post.id"
        :post="post"
        :rank="index + 1"
      />
    </div>
    <div v-else class="empty">{{ loadingPosts ? '加载中...' : '暂无热门帖子' }}</div>
  </section>
</template>
