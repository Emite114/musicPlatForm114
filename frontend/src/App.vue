<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { RouterView, useRoute, useRouter } from 'vue-router'
import AppSidebar from '@/components/AppSidebar.vue'
import GlobalPlayer from '@/components/GlobalPlayer.vue'
import ProtectedImage from '@/components/ProtectedImage.vue'
import { useAuthStore } from '@/stores/auth'
import { usePlayerStore } from '@/stores/player'
import { useRealtimeStore } from '@/stores/realtime'
import { useUiStore } from '@/stores/ui'

const auth = useAuthStore()
const player = usePlayerStore()
const realtime = useRealtimeStore()
const ui = useUiStore()
const route = useRoute()
const router = useRouter()

const isPublicPage = computed(() => Boolean(route.meta.public))
const topbarKeyword = ref('')

watch(
  () => route.query.keyword,
  (value) => {
    topbarKeyword.value = typeof value === 'string' ? value : ''
  },
  { immediate: true }
)

watch(
  () => auth.profile?.id,
  async (userId) => {
    if (userId) {
      await player.bootstrapQueue()
      return
    }
    player.clear()
  },
  { immediate: true }
)

async function handleLogout() {
  try {
    await auth.logout()
  } catch {
    // Even if backend logout fails, local session is already cleared and we still
    // want to return the user to the auth screen.
  } finally {
    await router.push({ name: 'auth' })
  }
}

async function submitGlobalSearch() {
  await router.push({
    name: 'search',
    query: {
      keyword: topbarKeyword.value.trim(),
      tab: 'posts',
      sort: 'hotScore'
    }
  })
}

onMounted(async () => {
  try {
    await auth.bootstrap()
  } catch {
    if (!isPublicPage.value) {
      await router.replace({ name: 'auth' })
    }
  }
})
</script>

<template>
  <div class="app-shell" :class="{ 'app-shell-dense': !isPublicPage }">
    <template v-if="!auth.ready">
      <div class="center-screen">
        <div class="panel panel-soft loading-card">
          <span class="eyebrow">Booting Console</span>
          <h1>正在连接音乐平台后端</h1>
          <p>先校验登录态，再把工作台内容装载起来。</p>
        </div>
      </div>
    </template>

    <template v-else-if="isPublicPage">
      <RouterView />
    </template>

    <template v-else>
      <AppSidebar @logout="handleLogout" />

      <main class="app-main">
        <header class="topbar">
          <div>
            <span class="eyebrow">Music Platform</span>
            <h1>{{ route.meta.title ?? route.name }}</h1>
          </div>

          <form class="topbar-search" @submit.prevent="submitGlobalSearch">
            <input
              v-model.trim="topbarKeyword"
              class="topbar-search-input"
              type="search"
              placeholder="搜索帖子、歌曲、用户"
            />
            <button class="btn btn-primary topbar-search-button" type="submit">搜索</button>
          </form>

          <RouterLink
            v-if="auth.profile"
            class="topbar-user topbar-user-link"
            :to="`/users/${auth.profile.id}`"
          >
            <ProtectedImage
              :src="auth.profile.avatarUrl"
              :alt="auth.profile.username"
              class="avatar"
            />
            <div>
              <strong>{{ auth.profile.username }}</strong>
              <p>
                {{ auth.profile.email }} ·
                {{ realtime.connected ? 'SSE 在线' : realtime.connecting ? 'SSE 连接中' : 'SSE 离线' }}
              </p>
            </div>
          </RouterLink>
        </header>

        <section class="page-wrap">
          <RouterView />
        </section>
      </main>

      <GlobalPlayer v-if="auth.isAuthenticated" />
    </template>

    <div class="toast-stack">
      <div
        v-for="toast in ui.toasts"
        :key="toast.id"
        class="toast"
        :class="`toast-${toast.type}`"
      >
        {{ toast.message }}
      </div>
    </div>
  </div>
</template>
