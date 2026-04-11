<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { RouterLink } from 'vue-router'
import MediaUploader from '@/components/MediaUploader.vue'
import PageHeader from '@/components/PageHeader.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import PostPreviewCard from '@/components/PostPreviewCard.vue'
import { postApi } from '@/api'
import { useUiStore } from '@/stores/ui'
import type { PageResult, PostCard, UploadedMedia } from '@/types/api'

type PostSort = 'hotScore' | 'time' | 'like' | 'comment' | 'viewCount' | 'favourite'

const ui = useUiStore()

const createForm = reactive({
  title: '',
  content: ''
})

const collectionKeyword = ref('')
const sortMode = ref<PostSort>('hotScore')
const mediaFiles = ref<UploadedMedia[]>([])
const favouritePage = ref<PageResult<PostCard> | null>(null)
const loading = ref(false)
const submitting = ref(false)

const sortOptions: Array<{ value: PostSort; label: string }> = [
  { value: 'hotScore', label: '综合热度' },
  { value: 'time', label: '最新收藏' },
  { value: 'like', label: '最多点赞' },
  { value: 'comment', label: '最多评论' },
  { value: 'viewCount', label: '最多浏览' },
  { value: 'favourite', label: '最多收藏' }
]

async function loadFavourites(page = 0) {
  loading.value = true
  try {
    favouritePage.value = await postApi.getFavouritePosts(
      collectionKeyword.value.trim(),
      page,
      15,
      sortMode.value
    )
  } catch (error) {
    ui.error(error instanceof Error ? error.message : '收藏帖子加载失败')
  } finally {
    loading.value = false
  }
}

async function toggleFavourite(postId: number) {
  try {
    await postApi.togglePostFavourite(postId)
    ui.success('收藏状态已更新')
    await loadFavourites(favouritePage.value?.number ?? 0)
  } catch (error) {
    ui.error(error instanceof Error ? error.message : '收藏操作失败')
  }
}

async function submitPost() {
  if (!createForm.content.trim()) {
    ui.error('帖子内容不能为空')
    return
  }

  submitting.value = true
  try {
    await postApi.createPost({
      title: createForm.title.trim(),
      content: createForm.content.trim(),
      mediaIdList: mediaFiles.value.map((item) => item.mediaId)
    })
    ui.success('帖子发布成功')
    createForm.title = ''
    createForm.content = ''
    mediaFiles.value = []
  } catch (error) {
    ui.error(error instanceof Error ? error.message : '发帖失败')
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
      eyebrow="Collection"
      title="我收藏的帖子"
      description="这里保留收藏帖子管理，卡片会直接展示作者头像、浏览量、点赞和收藏状态。"
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
        placeholder="在收藏帖子中搜索标题或正文"
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
      <div v-for="post in favouritePage.content" :key="post.id" class="collection-card-wrap">
        <PostPreviewCard :post="post" />
        <button class="btn btn-danger collection-action" type="button" @click="toggleFavourite(post.id)">
          取消收藏
        </button>
      </div>
    </div>

    <div v-else class="empty">{{ loading ? '加载中...' : '还没有收藏帖子' }}</div>

    <PaginationBar
      :page="favouritePage"
      @prev="loadFavourites((favouritePage?.number ?? 1) - 1)"
      @next="loadFavourites((favouritePage?.number ?? 0) + 1)"
    />
  </section>

  <section class="panel">
    <PageHeader eyebrow="Create" title="发布帖子" />

    <form class="stack" @submit.prevent="submitPost">
      <div class="field">
        <label for="post-title">标题</label>
        <input id="post-title" v-model.trim="createForm.title" placeholder="可以为空，但建议写标题" />
      </div>
      <div class="field">
        <label for="post-content">内容</label>
        <textarea id="post-content" v-model.trim="createForm.content" placeholder="写点什么..." />
      </div>
      <div class="field">
        <label>媒体资源</label>
        <MediaUploader v-model="mediaFiles" accept="image/*,video/*" multiple button-text="上传图片或视频" />
      </div>
      <button class="btn btn-primary" type="submit" :disabled="submitting">
        {{ submitting ? '发布中...' : '发布帖子' }}
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
