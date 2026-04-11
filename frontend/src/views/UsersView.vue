<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import MediaUploader from '@/components/MediaUploader.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import PostPreviewCard from '@/components/PostPreviewCard.vue'
import ProtectedImage from '@/components/ProtectedImage.vue'
import SongPreviewCard from '@/components/SongPreviewCard.vue'
import UserPreviewCard from '@/components/UserPreviewCard.vue'
import { authApi, postApi, reportApi, songApi, userApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { useUiStore } from '@/stores/ui'
import { describeError } from '@/utils/errors'
import { formatCompactNumber } from '@/utils/format'
import type {
  Gender,
  PageResult,
  PostCard,
  SongCard,
  UploadedMedia,
  UserCard,
  UserSummary
} from '@/types/api'

type UserView =
  | 'posts'
  | 'favouritePosts'
  | 'favouriteSongs'
  | 'sharedSongs'
  | 'follows'
  | 'fans'

type PostSort = 'hotScore' | 'time' | 'like' | 'comment' | 'viewCount' | 'favourite'
type SongSort = 'hotScore' | 'time' | 'favourite' | 'comment'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const ui = useUiStore()

const profile = ref<UserSummary | null>(null)
const activeView = ref<UserView>('favouriteSongs')
const userKeyword = ref('')
const pageNumber = ref(0)
const postSort = ref<PostSort>('hotScore')
const songSort = ref<SongSort>('hotScore')

const postsPage = ref<PageResult<PostCard> | null>(null)
const favouritePostsPage = ref<PageResult<PostCard> | null>(null)
const favouriteSongsPage = ref<PageResult<SongCard> | null>(null)
const sharedSongsPage = ref<PageResult<SongCard> | null>(null)
const followsPage = ref<PageResult<UserCard> | null>(null)
const fansPage = ref<PageResult<UserCard> | null>(null)

const profileEditorOpen = ref(false)
const savingProfile = ref(false)
const editGender = ref<'unknown' | 'male' | 'female'>('unknown')
const editAvatarUploads = ref<UploadedMedia[]>([])
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
  { value: 'time', label: '最新分享' },
  { value: 'favourite', label: '最多收藏' },
  { value: 'comment', label: '最多评论' }
]

const contentTabs: Array<{ key: UserView; label: string }> = [
  { key: 'favouriteSongs', label: '收藏歌曲' },
  { key: 'favouritePosts', label: '收藏帖子' },
  { key: 'posts', label: '帖子投稿' },
  { key: 'sharedSongs', label: '分享歌曲' }
]

const targetUserId = computed(() => {
  const routeId = Number(route.params.id ?? 0)
  if (Number.isFinite(routeId) && routeId > 0) {
    return routeId
  }
  return auth.profile?.id ?? 0
})

const isSelf = computed(() => Boolean(targetUserId.value && targetUserId.value === auth.profile?.id))

const relationTabs = computed<Array<{ key: UserView; label: string; value: number }>>(() => [
  { key: 'follows', label: '关注数', value: Number(profile.value?.followCount ?? 0) },
  { key: 'fans', label: '粉丝数', value: Number(profile.value?.fanCount ?? 0) }
])

const currentPage = computed(() => {
  switch (activeView.value) {
    case 'posts':
      return postsPage.value
    case 'favouritePosts':
      return favouritePostsPage.value
    case 'favouriteSongs':
      return favouriteSongsPage.value
    case 'sharedSongs':
      return sharedSongsPage.value
    case 'follows':
      return followsPage.value
    case 'fans':
      return fansPage.value
  }
})

const sectionTitle = computed(() => {
  switch (activeView.value) {
    case 'posts':
      return '帖子投稿'
    case 'favouritePosts':
      return '收藏帖子'
    case 'favouriteSongs':
      return '收藏歌曲'
    case 'sharedSongs':
      return '分享歌曲'
    case 'follows':
      return '关注列表'
    case 'fans':
      return '粉丝列表'
  }
})

const searchPlaceholder = computed(() => {
  switch (activeView.value) {
    case 'posts':
      return '筛选当前帖子标题、正文或作者'
    case 'favouritePosts':
      return '搜索收藏帖子'
    case 'favouriteSongs':
      return '搜索收藏歌曲'
    case 'sharedSongs':
      return '搜索分享歌曲'
    case 'follows':
      return '筛选关注列表'
    case 'fans':
      return '筛选粉丝列表'
  }
})

const showPostSort = computed(() => activeView.value === 'favouritePosts')
const showSongSort = computed(
  () => activeView.value === 'favouriteSongs' || activeView.value === 'sharedSongs'
)

const genderMeta = computed(() => resolveGenderMeta(profile.value?.gender ?? 'unknown'))
const editGenderMeta = computed(() => resolveGenderMeta(editGender.value))

const visiblePosts = computed(() => filterPosts(postsPage.value?.content ?? []))
const visibleFavouritePosts = computed(() => filterPosts(favouritePostsPage.value?.content ?? []))
const visibleFavouriteSongs = computed(() => filterSongs(favouriteSongsPage.value?.content ?? []))
const visibleSharedSongs = computed(() => filterSongs(sharedSongsPage.value?.content ?? []))
const visibleFollows = computed(() => filterUsers(followsPage.value?.content ?? []))
const visibleFans = computed(() => filterUsers(fansPage.value?.content ?? []))

function resolveGenderMeta(gender: Gender) {
  if (gender === 'male') {
    return { icon: '♂', label: '男性', tone: 'male' }
  }
  if (gender === 'female') {
    return { icon: '♀', label: '女性', tone: 'female' }
  }
  return { icon: '○', label: '保密', tone: 'unknown' }
}

function matchesKeyword(source: Array<string | number | undefined>) {
  const keyword = userKeyword.value.trim().toLowerCase()
  if (!keyword) {
    return true
  }
  return source.some((item) => String(item ?? '').toLowerCase().includes(keyword))
}

function filterPosts(items: PostCard[]) {
  return items.filter((item) => matchesKeyword([item.title, item.content, item.username]))
}

function filterSongs(items: SongCard[]) {
  return items.filter((item) => matchesKeyword([item.songName, item.songArtist]))
}

function filterUsers(items: UserCard[]) {
  return items.filter((item) => matchesKeyword([item.username]))
}

async function loadProfile() {
  if (!targetUserId.value) {
    profile.value = null
    return
  }
  profile.value = await userApi.getUserDetail(targetUserId.value)
}

async function loadCurrentView(page = 0) {
  if (!targetUserId.value) {
    return
  }

  pageNumber.value = page
  loading.value = true
  currentError.value = ''

  try {
    if (activeView.value === 'posts') {
      postsPage.value = await userApi.getUserPosts(targetUserId.value, page, 15)
      return
    }

    if (activeView.value === 'favouritePosts') {
      favouritePostsPage.value = isSelf.value
        ? await postApi.getFavouritePosts(userKeyword.value.trim(), page, 15, postSort.value)
        : await postApi.getOnesFavouritePosts(
            targetUserId.value,
            userKeyword.value.trim(),
            page,
            15,
            postSort.value
          )
      return
    }

    if (activeView.value === 'favouriteSongs') {
      favouriteSongsPage.value = isSelf.value
        ? await songApi.getFavouriteSongs(userKeyword.value.trim(), page, 15, songSort.value)
        : await songApi.getOnesFavouriteSongs(
            targetUserId.value,
            userKeyword.value.trim(),
            page,
            15,
            songSort.value
          )
      return
    }

    if (activeView.value === 'sharedSongs') {
      sharedSongsPage.value = await songApi.getOnesSharedSongs(
        targetUserId.value,
        userKeyword.value.trim(),
        page,
        15,
        songSort.value
      )
      return
    }

    if (activeView.value === 'follows') {
      followsPage.value = isSelf.value
        ? await userApi.getMyFollows(page, 12)
        : await userApi.getOnesFollows(targetUserId.value, page, 12)
      return
    }

    fansPage.value = isSelf.value
      ? await userApi.getMyFans(page, 12)
      : await userApi.getOnesFans(targetUserId.value, page, 12)
  } catch (error) {
    currentError.value = describeError(error, '加载失败')
    ui.error(currentError.value)
  } finally {
    loading.value = false
  }
}

async function initializePage() {
  if (!targetUserId.value) {
    return
  }

  userKeyword.value = ''
  activeView.value = 'favouriteSongs'
  pageNumber.value = 0

  try {
    await loadProfile()
    await loadCurrentView(0)
  } catch (error) {
    currentError.value = describeError(error, '用户主页加载失败')
    ui.error(currentError.value)
  }
}

watch(
  () => targetUserId.value,
  async () => {
    await initializePage()
  },
  { immediate: true }
)

async function switchView(view: UserView) {
  activeView.value = view
  pageNumber.value = 0
  await loadCurrentView(0)
}

async function submitViewSearch() {
  if (
    activeView.value === 'favouritePosts' ||
    activeView.value === 'favouriteSongs' ||
    activeView.value === 'sharedSongs'
  ) {
    await loadCurrentView(0)
  }
}

async function changePage(nextPage: number) {
  await loadCurrentView(nextPage)
}

async function refreshProfileAndView() {
  await loadProfile()
  await loadCurrentView(pageNumber.value)
}

async function toggleCurrentProfileFollow() {
  if (!targetUserId.value || isSelf.value) {
    return
  }

  try {
    await userApi.toggleFollow(targetUserId.value)
    ui.success('关注状态已更新')
    await refreshProfileAndView()
  } catch (error) {
    ui.error(error instanceof Error ? error.message : '关注操作失败')
  }
}

async function toggleListFollow(userId: number) {
  if (!userId || userId === auth.profile?.id) {
    return
  }

  try {
    await userApi.toggleFollow(userId)
    ui.success('关注状态已更新')
    await loadCurrentView(pageNumber.value)
    await loadProfile()
  } catch (error) {
    ui.error(error instanceof Error ? error.message : '关注操作失败')
  }
}

async function reportCurrentUser() {
  if (!targetUserId.value || isSelf.value) {
    return
  }

  const reason = window.prompt('请输入举报理由')?.trim()
  if (!reason) {
    return
  }

  try {
    await reportApi.createReport({
      targetType: 'USER',
      targetId: targetUserId.value,
      reason
    })
    ui.success('举报已提交')
  } catch (error) {
    ui.error(error instanceof Error ? error.message : '举报失败')
  }
}

function openProfileEditor() {
  editGender.value = (auth.profile?.gender ?? 'unknown') as 'unknown' | 'male' | 'female'
  editAvatarUploads.value = []
  profileEditorOpen.value = true
}

async function saveProfileEditor() {
  savingProfile.value = true
  try {
    if (editAvatarUploads.value.length) {
      await authApi.updateAvatarUrl(editAvatarUploads.value[0].url)
    }
    if (auth.profile?.gender !== editGender.value) {
      await authApi.updateGender(editGender.value)
    }
    await auth.refreshProfile()
    await loadProfile()
    editAvatarUploads.value = []
    profileEditorOpen.value = false
    ui.success('资料已更新')
  } catch (error) {
    ui.error(error instanceof Error ? error.message : '资料更新失败')
  } finally {
    savingProfile.value = false
  }
}

function openChat(userId = targetUserId.value) {
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

function followButtonText(user: UserCard) {
  if (user.id === auth.profile?.id) {
    return '自己'
  }
  if (user.isFollowed) {
    return '取消关注'
  }
  return user.isMyFan ? '回关' : '关注'
}
</script>

<template>
  <section v-if="profile" class="panel user-hero">
    <div class="user-hero-main">
      <div class="user-identity">
        <ProtectedImage :src="profile.avatarUrl" class="user-hero-avatar" />

        <div class="user-hero-copy">
          <div class="user-hero-name">
            <h2>{{ profile.username }}</h2>
            <span class="gender-badge" :class="genderMeta.tone">
              <span>{{ genderMeta.icon }}</span>
              {{ genderMeta.label }}
            </span>
          </div>

          <p class="user-hero-email">
            {{ isSelf ? auth.profile?.email || '当前账号未返回邮箱' : '公开主页' }}
          </p>

          <div class="hero-pill-row">
            <span class="hero-pill">UID {{ profile.id }}</span>
            <span v-if="!isSelf && profile.isFollowed" class="hero-pill">已关注</span>
            <span v-if="!isSelf && profile.isMyFan" class="hero-pill">TA 也关注了你</span>
          </div>
        </div>
      </div>

      <div class="user-hero-actions">
        <button v-if="isSelf" class="btn btn-secondary" type="button" @click="openProfileEditor">
          编辑资料
        </button>
        <button
          v-else
          class="btn"
          :class="profile.isFollowed ? 'btn-secondary' : 'btn-primary'"
          type="button"
          @click="toggleCurrentProfileFollow"
        >
          {{ profile.isFollowed ? '已关注' : '+ 关注' }}
        </button>
        <button v-if="!isSelf" class="btn btn-secondary" type="button" @click="openChat()">
          发消息
        </button>
        <button
          v-if="!isSelf"
          class="icon-action"
          type="button"
          title="举报用户"
          @click="reportCurrentUser"
        >
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path
              d="M6 4v16M8.5 5.5h7.4l-1.5 3 1.5 3H8.5"
              fill="none"
              stroke="currentColor"
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="1.8"
            />
          </svg>
        </button>
      </div>
    </div>

    <div class="space-switchbar">
      <div class="space-tab-group">
        <button
          v-for="tab in contentTabs"
          :key="tab.key"
          class="space-tab"
          :class="{ active: activeView === tab.key }"
          type="button"
          @click="switchView(tab.key)"
        >
          {{ tab.label }}
        </button>
      </div>

      <div class="space-stat-group">
        <button
          v-for="item in relationTabs"
          :key="item.key"
          class="space-stat-pill"
          :class="{ active: activeView === item.key }"
          type="button"
          @click="switchView(item.key)"
        >
          <span>{{ item.label }}</span>
          <strong>{{ formatCompactNumber(item.value) }}</strong>
        </button>
      </div>
    </div>
  </section>

  <section class="panel">
    <div class="user-content-toolbar">
      <div>
        <h3 class="content-title">{{ sectionTitle }}</h3>
      </div>

      <form class="user-search-form" @submit.prevent="submitViewSearch">
        <input
          v-model.trim="userKeyword"
          class="user-search-input"
          type="search"
          :placeholder="searchPlaceholder"
        />
        <button class="btn btn-primary" type="submit">搜索</button>
      </form>
    </div>

    <div v-if="showPostSort" class="search-filters">
      <button
        v-for="option in postSortOptions"
        :key="option.value"
        class="search-filter"
        :class="{ active: postSort === option.value }"
        type="button"
        @click="postSort = option.value; loadCurrentView(0)"
      >
        {{ option.label }}
      </button>
    </div>

    <div v-else-if="showSongSort" class="search-filters">
      <button
        v-for="option in songSortOptions"
        :key="option.value"
        class="search-filter"
        :class="{ active: songSort === option.value }"
        type="button"
        @click="songSort = option.value; loadCurrentView(0)"
      >
        {{ option.label }}
      </button>
    </div>

    <div v-if="currentError" class="empty" style="margin-top: 18px">{{ currentError }}</div>

    <div v-else-if="activeView === 'posts' && visiblePosts.length" class="grid-5 results-grid">
      <PostPreviewCard v-for="post in visiblePosts" :key="post.id" :post="post" />
    </div>

    <div
      v-else-if="activeView === 'favouritePosts' && visibleFavouritePosts.length"
      class="grid-5 results-grid"
    >
      <PostPreviewCard v-for="post in visibleFavouritePosts" :key="post.id" :post="post" />
    </div>

    <div
      v-else-if="activeView === 'favouriteSongs' && visibleFavouriteSongs.length"
      class="grid-5 results-grid"
    >
      <SongPreviewCard
        v-for="song in visibleFavouriteSongs"
        :key="song.id"
        :song="song"
        queue-mode="favourite"
        :favourite-owner-id="targetUserId"
      />
    </div>

    <div
      v-else-if="activeView === 'sharedSongs' && visibleSharedSongs.length"
      class="grid-5 results-grid"
    >
      <SongPreviewCard
        v-for="song in visibleSharedSongs"
        :key="song.id"
        :song="song"
      />
    </div>

    <div v-else-if="activeView === 'follows' && visibleFollows.length" class="grid-4 results-grid">
      <UserPreviewCard v-for="user in visibleFollows" :key="user.id" :user="user">
        <template #actions>
          <button
            class="btn btn-secondary"
            type="button"
            :disabled="!canOpenChat(user.id)"
            @click="openChat(user.id)"
          >
            私信
          </button>
          <button
            class="btn btn-secondary"
            type="button"
            :disabled="user.id === auth.profile?.id"
            @click="toggleListFollow(user.id)"
          >
            {{ followButtonText(user) }}
          </button>
        </template>
      </UserPreviewCard>
    </div>

    <div v-else-if="activeView === 'fans' && visibleFans.length" class="grid-4 results-grid">
      <UserPreviewCard v-for="user in visibleFans" :key="user.id" :user="user">
        <template #actions>
          <button
            class="btn btn-secondary"
            type="button"
            :disabled="!canOpenChat(user.id)"
            @click="openChat(user.id)"
          >
            私信
          </button>
          <button
            class="btn btn-secondary"
            type="button"
            :disabled="user.id === auth.profile?.id"
            @click="toggleListFollow(user.id)"
          >
            {{ followButtonText(user) }}
          </button>
        </template>
      </UserPreviewCard>
    </div>

    <div v-else class="empty results-grid">
      {{ loading ? '加载中...' : '当前分区还没有内容' }}
    </div>

    <PaginationBar :page="currentPage" @prev="changePage(pageNumber - 1)" @next="changePage(pageNumber + 1)" />
  </section>

  <div v-if="profileEditorOpen" class="modal-mask" @click.self="profileEditorOpen = false">
    <section class="modal-card">
      <div class="modal-header">
        <div>
          <h3>编辑资料</h3>
        </div>
        <button class="btn btn-ghost" type="button" @click="profileEditorOpen = false">关闭</button>
      </div>

      <div v-if="auth.profile" class="modal-preview">
        <ProtectedImage :src="auth.profile.avatarUrl" class="modal-avatar" />
        <div class="modal-copy">
          <strong>{{ auth.profile.username }}</strong>
          <span>{{ auth.profile.email }}</span>
          <span class="gender-badge" :class="editGenderMeta.tone">
            <span>{{ editGenderMeta.icon }}</span>
            {{ editGenderMeta.label }}
          </span>
        </div>
      </div>

      <form class="stack" @submit.prevent="saveProfileEditor">
        <div class="field">
          <label>上传新头像</label>
          <MediaUploader
            v-model="editAvatarUploads"
            accept=".jpg,.jpeg,.png,image/*"
            button-text="上传头像"
          />
        </div>

        <div class="field">
          <label for="edit-gender">修改性别</label>
          <select id="edit-gender" v-model="editGender">
            <option value="unknown">保密</option>
            <option value="male">男</option>
            <option value="female">女</option>
          </select>
        </div>

        <div class="modal-actions">
          <button class="btn btn-secondary" type="button" @click="profileEditorOpen = false">
            取消
          </button>
          <button class="btn btn-primary" type="submit" :disabled="savingProfile">
            {{ savingProfile ? '保存中...' : '保存' }}
          </button>
        </div>
      </form>
    </section>
  </div>
</template>

<style scoped>
.user-hero {
  overflow: hidden;
  background:
    radial-gradient(circle at right top, rgba(56, 189, 248, 0.18), transparent 26%),
    linear-gradient(120deg, #183153 0%, #27446e 36%, #315a86 100%);
  color: white;
}

.user-hero-main {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  align-items: flex-start;
}

.user-identity {
  display: flex;
  gap: 18px;
  align-items: center;
}

.user-hero-avatar {
  width: 104px;
  height: 104px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid rgba(255, 255, 255, 0.26);
}

.user-hero-copy {
  display: grid;
  gap: 10px;
}

.user-hero-name {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.user-hero-name h2 {
  margin: 0;
  font-size: clamp(1.8rem, 3vw, 2.6rem);
}

.user-hero-email {
  margin: 0;
  color: rgba(255, 255, 255, 0.82);
}

.hero-pill-row {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.hero-pill {
  display: inline-flex;
  align-items: center;
  min-height: 32px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.14);
  color: white;
  font-size: 0.88rem;
}

.gender-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 32px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 0.88rem;
  background: rgba(255, 255, 255, 0.16);
}

.gender-badge.male {
  background: rgba(56, 189, 248, 0.18);
}

.gender-badge.female {
  background: rgba(244, 114, 182, 0.2);
}

.user-hero-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.icon-action {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.14);
  color: white;
}

.icon-action svg {
  width: 18px;
  height: 18px;
}

.space-switchbar {
  margin-top: 22px;
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  flex-wrap: wrap;
}

.space-tab-group,
.space-stat-group,
.search-filters {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.space-tab {
  min-height: 44px;
  padding: 0 18px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  color: rgba(255, 255, 255, 0.84);
  border: 1px solid transparent;
  transition: background 0.2s ease, color 0.2s ease, transform 0.2s ease;
}

.space-tab.active {
  background: rgba(255, 255, 255, 0.96);
  color: #183153;
  transform: translateY(-1px);
}

.space-stat-pill {
  min-height: 48px;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 0 16px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  color: white;
  border: 1px solid transparent;
}

.space-stat-pill span {
  color: rgba(255, 255, 255, 0.82);
  font-size: 0.88rem;
}

.space-stat-pill strong {
  font-size: 1rem;
}

.space-stat-pill.active {
  background: rgba(255, 255, 255, 0.96);
  color: #183153;
}

.space-stat-pill.active span {
  color: #46627f;
}

.user-content-toolbar {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  margin-bottom: 16px;
}

.content-title {
  margin: 4px 0 0;
}

.user-search-form {
  display: flex;
  gap: 12px;
  min-width: min(100%, 420px);
}

.user-search-input {
  flex: 1;
  min-width: 0;
  padding: 12px 14px;
  border: 1px solid rgba(23, 32, 51, 0.12);
  border-radius: 16px;
  background: rgba(248, 250, 253, 0.9);
}

.search-filter {
  min-height: 42px;
  padding: 0 16px;
  border-radius: 999px;
  background: rgba(23, 32, 51, 0.05);
  color: var(--muted);
}

.search-filter.active {
  background: rgba(14, 165, 233, 0.12);
  color: #0284c7;
}

.results-grid {
  margin-top: 18px;
}

.modal-mask {
  position: fixed;
  inset: 0;
  display: grid;
  place-items: center;
  padding: 24px;
  background: rgba(9, 18, 33, 0.45);
  backdrop-filter: blur(8px);
  z-index: 60;
}

.modal-card {
  width: min(100%, 520px);
  display: grid;
  gap: 18px;
  padding: 24px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 30px 80px rgba(20, 38, 76, 0.22);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.modal-header h3 {
  margin: 4px 0 0;
}

.modal-preview {
  display: flex;
  gap: 14px;
  align-items: center;
  padding: 16px;
  border-radius: 18px;
  background: rgba(23, 32, 51, 0.04);
}

.modal-avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  object-fit: cover;
}

.modal-copy {
  display: grid;
  gap: 6px;
}

.modal-copy span {
  color: var(--muted);
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 1100px) {
  .user-hero-main,
  .user-content-toolbar {
    flex-direction: column;
    align-items: flex-start;
  }

  .user-hero-actions {
    justify-content: flex-start;
  }
}

@media (max-width: 900px) {
  .user-identity {
    align-items: flex-start;
    flex-direction: column;
  }

  .space-switchbar {
    flex-direction: column;
    align-items: stretch;
  }

  .user-search-form {
    width: 100%;
    min-width: 0;
  }

  .modal-card {
    padding: 20px;
  }
}
</style>

