<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import MediaUploader from '@/components/MediaUploader.vue'
import PageHeader from '@/components/PageHeader.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import PostPreviewCard from '@/components/PostPreviewCard.vue'
import ProtectedImage from '@/components/ProtectedImage.vue'
import UserPreviewCard from '@/components/UserPreviewCard.vue'
import { authApi, userApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { useUiStore } from '@/stores/ui'
import { formatCompactNumber, formatDate } from '@/utils/format'
import type { PageResult, PostCard, UploadedMedia, UserCard } from '@/types/api'

const auth = useAuthStore()
const ui = useUiStore()

const genderValue = ref<'unknown' | 'male' | 'female'>('unknown')
const avatarUploads = ref<UploadedMedia[]>([])
const saving = ref(false)
const posts = ref<PageResult<PostCard> | null>(null)
const follows = ref<PageResult<UserCard> | null>(null)
const fans = ref<PageResult<UserCard> | null>(null)

const genderMeta = computed(() => {
  if (genderValue.value === 'male') {
    return { icon: '♂', label: '男性', tone: 'male' }
  }
  if (genderValue.value === 'female') {
    return { icon: '♀', label: '女性', tone: 'female' }
  }
  return { icon: '○', label: '保密', tone: 'unknown' }
})

async function loadPosts(page = 0) {
  if (!auth.profile?.id) {
    return
  }
  posts.value = await userApi.getUserPosts(auth.profile.id, page, 15)
}

async function loadProfileData() {
  try {
    await auth.refreshProfile()
    genderValue.value = (auth.profile?.gender ?? 'unknown') as 'unknown' | 'male' | 'female'
    await loadPosts(0)
    ;[follows.value, fans.value] = await Promise.all([
      userApi.getMyFollows(0, 6),
      userApi.getMyFans(0, 6)
    ])
  } catch (error) {
    ui.error(error instanceof Error ? error.message : '个人资料加载失败')
  }
}

async function saveProfile() {
  saving.value = true
  try {
    if (avatarUploads.value.length) {
      await authApi.updateAvatarUrl(avatarUploads.value[0].url)
    }
    if (auth.profile?.gender !== genderValue.value) {
      await authApi.updateGender(genderValue.value)
    }
    avatarUploads.value = []
    await loadProfileData()
    ui.success('资料已更新')
  } catch (error) {
    ui.error(error instanceof Error ? error.message : '资料更新失败')
  } finally {
    saving.value = false
  }
}

onMounted(loadProfileData)
</script>

<template>
  <section class="grid-2">
    <div class="panel profile-hero" v-if="auth.profile">
      <PageHeader
        eyebrow="Profile"
        title="我的主页"
      >
        <template #actions>
          <RouterLink class="btn btn-secondary" :to="`/users/${auth.profile.id}`">查看公开主页</RouterLink>
        </template>
      </PageHeader>

      <div class="profile-summary">
        <RouterLink :to="`/users/${auth.profile.id}`">
          <ProtectedImage :src="auth.profile.avatarUrl" class="profile-avatar" />
        </RouterLink>
        <div class="profile-copy">
          <div class="profile-name-row">
            <RouterLink :to="`/users/${auth.profile.id}`">
              <h3>{{ auth.profile.username }}</h3>
            </RouterLink>
            <span class="gender-badge" :class="genderMeta.tone">
              <span>{{ genderMeta.icon }}</span>
              {{ genderMeta.label }}
            </span>
          </div>
          <p>{{ auth.profile.email }}</p>
          <div class="actions">
            <span class="pill">UID {{ auth.profile.id }}</span>
            <span class="pill">注册于 {{ formatDate(auth.profile.registerDate) }}</span>
          </div>
        </div>
      </div>

      <div class="grid-3 profile-metrics">
        <div class="metric">
          <strong>{{ formatCompactNumber(auth.profile.followCount ?? follows?.totalElements ?? 0) }}</strong>
          <p>我的关注</p>
        </div>
        <div class="metric">
          <strong>{{ formatCompactNumber(auth.profile.fanCount ?? fans?.totalElements ?? 0) }}</strong>
          <p>我的粉丝</p>
        </div>
        <div class="metric">
          <strong>{{ formatCompactNumber(posts?.totalElements ?? 0) }}</strong>
          <p>已发布帖子</p>
        </div>
      </div>
    </div>

    <div class="panel">
      <PageHeader eyebrow="Edit" title="编辑资料" />

      <form class="stack" @submit.prevent="saveProfile">
        <div class="field">
          <label>上传新头像</label>
          <MediaUploader v-model="avatarUploads" accept=".jpg,.jpeg,.png,image/*" button-text="上传头像" />
        </div>
        <div class="field">
          <label for="profile-gender">修改性别</label>
          <select id="profile-gender" v-model="genderValue">
            <option value="unknown">保密</option>
            <option value="male">男</option>
            <option value="female">女</option>
          </select>
        </div>
        <button class="btn btn-primary" type="submit" :disabled="saving">
          {{ saving ? '保存中...' : '保存资料' }}
        </button>
      </form>
    </div>
  </section>

  <section class="panel">
    <PageHeader eyebrow="My Posts" title="我的帖子" />
    <div v-if="posts?.content.length" class="grid-5">
      <PostPreviewCard v-for="post in posts.content" :key="post.id" :post="post" />
    </div>
    <div v-else class="empty">你还没有发布帖子</div>
    <PaginationBar
      :page="posts"
      @prev="loadPosts((posts?.number ?? 1) - 1)"
      @next="loadPosts((posts?.number ?? 0) + 1)"
    />
  </section>

  <section class="grid-2">
    <div class="panel">
      <PageHeader eyebrow="Following" title="我的关注" />
      <div v-if="follows?.content.length" class="grid-2">
        <UserPreviewCard v-for="user in follows.content" :key="user.id" :user="user" />
      </div>
      <div v-else class="empty">暂无关注</div>
    </div>

    <div class="panel">
      <PageHeader eyebrow="Fans" title="我的粉丝" />
      <div v-if="fans?.content.length" class="grid-2">
        <UserPreviewCard v-for="user in fans.content" :key="user.id" :user="user" />
      </div>
      <div v-else class="empty">暂无粉丝</div>
    </div>
  </section>
</template>

<style scoped>
.profile-hero {
  background:
    radial-gradient(circle at right top, rgba(255, 183, 77, 0.22), transparent 24%),
    linear-gradient(135deg, #fff8ef 0%, #ffffff 46%, #edf8ff 100%);
}

.profile-summary {
  display: flex;
  gap: 18px;
  align-items: center;
}

.profile-avatar {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  object-fit: cover;
}

.profile-copy {
  display: grid;
  gap: 10px;
}

.profile-copy h3,
.profile-copy p {
  margin: 0;
}

.profile-name-row {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.gender-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 32px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 0.88rem;
  background: rgba(23, 32, 51, 0.08);
}

.gender-badge.male {
  color: #0284c7;
  background: rgba(56, 189, 248, 0.12);
}

.gender-badge.female {
  color: #db2777;
  background: rgba(244, 114, 182, 0.14);
}

.gender-badge.unknown {
  color: var(--muted);
}

.profile-metrics {
  margin-top: 20px;
}

@media (max-width: 900px) {
  .profile-summary {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>

