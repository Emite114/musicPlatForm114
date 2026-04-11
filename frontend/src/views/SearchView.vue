<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import PaginationBar from '@/components/PaginationBar.vue'
import PostPreviewCard from '@/components/PostPreviewCard.vue'
import SongPreviewCard from '@/components/SongPreviewCard.vue'
import UserPreviewCard from '@/components/UserPreviewCard.vue'
import { postApi, songApi, userApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { useUiStore } from '@/stores/ui'
import { describeError } from '@/utils/errors'
import type { PageResult, PostCard, SongCard, UserCard } from '@/types/api'

type SearchTab = 'posts' | 'songs' | 'users'
type PostSort = 'hotScore' | 'time' | 'like' | 'comment' | 'viewCount' | 'favourite'
type SongSort = 'hotScore' | 'time' | 'favourite' | 'comment'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const ui = useUiStore()

const searchInput = ref('')
const activeTab = ref<SearchTab>('posts')
const postSort = ref<PostSort>('hotScore')
const songSort = ref<SongSort>('hotScore')
const pageNumber = ref(0)

const postPage = ref<PageResult<PostCard> | null>(null)
const songPage = ref<PageResult<SongCard> | null>(null)
const userPage = ref<PageResult<UserCard> | null>(null)
const loading = ref(false)
const currentError = ref('')

const postSortOptions: Array<{ value: PostSort; label: string }> = [
  { value: 'hotScore', label: '综合热度' },
  { value: 'time', label: '最新发布' },
  { value: 'like', label: '最多点赞' },
  { value: 'comment', label: '最多评论' },
  { value: 'viewCount', label: '最多浏览' },
  { value: 'favourite', label: '最多收藏' }
]

const songSortOptions: Array<{ value: SongSort; label: string }> = [
  { value: 'hotScore', label: '综合热度' },
  { value: 'time', label: '最新发布' },
  { value: 'favourite', label: '最多收藏' },
  { value: 'comment', label: '最多评论' }
]

const activePage = computed(() => {
  if (activeTab.value === 'posts') {
    return postPage.value
  }
  if (activeTab.value === 'songs') {
    return songPage.value
  }
  return userPage.value
})

function normalizeTab(value: unknown): SearchTab {
  return value === 'songs' || value === 'users' ? value : 'posts'
}

function normalizePostSort(value: unknown): PostSort {
  return value === 'time' ||
    value === 'like' ||
    value === 'comment' ||
    value === 'viewCount' ||
    value === 'favourite'
    ? value
    : 'hotScore'
}

function normalizeSongSort(value: unknown): SongSort {
  return value === 'time' || value === 'favourite' || value === 'comment' ? value : 'hotScore'
}

function normalizePage(value: unknown) {
  const page = Number(value)
  return Number.isFinite(page) && page >= 0 ? page : 0
}

function resolveQueryState() {
  searchInput.value = typeof route.query.keyword === 'string' ? route.query.keyword : ''
  activeTab.value = normalizeTab(route.query.tab)
  postSort.value = normalizePostSort(route.query.sort)
  songSort.value = normalizeSongSort(route.query.sort)
  pageNumber.value = normalizePage(route.query.page)
}

async function loadResults() {
  loading.value = true
  currentError.value = ''

  try {
    if (activeTab.value === 'posts') {
      postPage.value = await postApi.searchPosts(searchInput.value.trim(), pageNumber.value, 15, postSort.value)
      return
    }

    if (activeTab.value === 'songs') {
      songPage.value = await songApi.searchSongs(searchInput.value.trim(), pageNumber.value, 15, songSort.value)
      return
    }

    userPage.value = await userApi.searchUsers(searchInput.value.trim(), pageNumber.value, 12)
  } catch (error) {
    currentError.value = describeError(error, '搜索失败')
    ui.error(currentError.value)
  } finally {
    loading.value = false
  }
}

watch(
  () => [route.query.keyword, route.query.tab, route.query.sort, route.query.page],
  async () => {
    resolveQueryState()
    await loadResults()
  },
  { immediate: true }
)

async function updateSearchQuery(next: {
  keyword?: string
  tab?: SearchTab
  sort?: string
  page?: number
}) {
  const tab = next.tab ?? activeTab.value
  const query: Record<string, string> = {
    keyword: (next.keyword ?? searchInput.value).trim(),
    tab,
    page: String(next.page ?? 0)
  }

  if (tab === 'posts') {
    query.sort = next.sort ?? postSort.value
  } else if (tab === 'songs') {
    query.sort = next.sort ?? songSort.value
  }

  await router.push({ name: 'search', query })
}

async function switchTab(tab: SearchTab) {
  const defaultSort = tab === 'posts' || tab === 'songs' ? 'hotScore' : undefined
  await updateSearchQuery({ tab, sort: defaultSort, page: 0 })
}

async function switchPostSort(sort: PostSort) {
  await updateSearchQuery({ tab: 'posts', sort, page: 0 })
}

async function switchSongSort(sort: SongSort) {
  await updateSearchQuery({ tab: 'songs', sort, page: 0 })
}

async function changePage(nextPage: number) {
  await updateSearchQuery({ page: nextPage })
}

function openChat(userId: number) {
  if (!userId || userId <= 0) {
    ui.error('当前用户缺少有效 ID，无法发起私信')
    return
  }
  if (userId === auth.profile?.id) {
    ui.error('不能给自己发私信')
    return
  }
  router.push({
    path: '/messages',
    query: {
      with: String(userId)
    }
  })
}

function canOpenChat(userId: number) {
  return Boolean(userId && userId > 0 && userId !== auth.profile?.id)
}

function chatButtonTitle(userId: number) {
  if (!userId || userId <= 0) {
    return '当前用户缺少有效 ID，暂时无法私信'
  }
  if (userId === auth.profile?.id) {
    return '不能给自己发私信'
  }
  return '发起私信'
}

function resultCount(page: PageResult<unknown> | null) {
  return page?.totalElements ?? '...'
}
</script>

<template>
  <section class="panel search-panel">
    <div class="search-tabs">
      <button
        class="search-tab"
        :class="{ active: activeTab === 'posts' }"
        type="button"
        @click="switchTab('posts')"
      >
        帖子
        <span class="search-count">{{ resultCount(postPage) }}</span>
      </button>

      <button
        class="search-tab"
        :class="{ active: activeTab === 'songs' }"
        type="button"
        @click="switchTab('songs')"
      >
        歌曲
        <span class="search-count">{{ resultCount(songPage) }}</span>
      </button>

      <button
        class="search-tab"
        :class="{ active: activeTab === 'users' }"
        type="button"
        @click="switchTab('users')"
      >
        用户
        <span class="search-count">{{ resultCount(userPage) }}</span>
      </button>
    </div>

    <div v-if="activeTab === 'posts'" class="search-filters">
      <button
        v-for="option in postSortOptions"
        :key="option.value"
        class="search-filter"
        :class="{ active: postSort === option.value }"
        type="button"
        @click="switchPostSort(option.value)"
      >
        {{ option.label }}
      </button>
    </div>

    <div v-else-if="activeTab === 'songs'" class="search-filters">
      <button
        v-for="option in songSortOptions"
        :key="option.value"
        class="search-filter"
        :class="{ active: songSort === option.value }"
        type="button"
        @click="switchSongSort(option.value)"
      >
        {{ option.label }}
      </button>
    </div>

    <div v-else class="search-filters">
      <span class="pill">用户结果不区分排序，直接按关键字展示。</span>
    </div>

    <div v-if="currentError" class="empty">{{ currentError }}</div>

    <div v-else-if="activeTab === 'posts' && postPage?.content.length" class="search-grid">
      <PostPreviewCard v-for="post in postPage.content" :key="post.id" :post="post" />
    </div>

    <div v-else-if="activeTab === 'songs' && songPage?.content.length" class="search-grid">
      <SongPreviewCard
        v-for="song in songPage.content"
        :key="song.id"
        :song="song"
      />
    </div>

    <div v-else-if="activeTab === 'users' && userPage?.content.length" class="search-grid">
      <UserPreviewCard v-for="user in userPage.content" :key="user.id" :user="user">
        <template #actions>
          <button
            class="btn btn-secondary"
            type="button"
            :disabled="!canOpenChat(user.id)"
            :title="chatButtonTitle(user.id)"
            @click="openChat(user.id)"
          >
            私信
          </button>
          <RouterLink class="btn btn-secondary" :to="`/users/${user.id}`">主页</RouterLink>
        </template>
      </UserPreviewCard>
    </div>

    <div v-else class="empty">{{ loading ? '搜索中...' : '暂无搜索结果' }}</div>

    <PaginationBar :page="activePage" @prev="changePage(pageNumber - 1)" @next="changePage(pageNumber + 1)" />
  </section>
</template>

<style scoped>
.search-panel {
  display: grid;
  gap: 20px;
}

.search-tabs,
.search-filters {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.search-tab,
.search-filter {
  min-height: 44px;
  padding: 0 18px;
  border-radius: 14px;
  background: rgba(23, 32, 51, 0.05);
  color: var(--muted);
  transition: background 0.2s ease, color 0.2s ease;
}

.search-tab.active,
.search-filter.active {
  background: rgba(14, 165, 233, 0.12);
  color: #0284c7;
}

.search-count {
  margin-left: 8px;
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(23, 32, 51, 0.08);
  font-size: 0.8rem;
}

.search-grid {
  display: grid;
  gap: 18px;
  grid-template-columns: repeat(5, minmax(0, 1fr));
}

@media (max-width: 1400px) {
  .search-grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

@media (max-width: 1100px) {
  .search-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .search-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 620px) {
  .search-grid {
    grid-template-columns: 1fr;
  }
}
</style>
