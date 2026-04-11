<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

defineEmits<{
  logout: []
}>()

const route = useRoute()
const auth = useAuthStore()

const links = computed(() => [
  { name: 'hot-songs', label: '热门歌曲', to: '/' },
  { name: 'hot-posts', label: '热门帖子', to: '/hot-posts' },
  { name: 'search', label: '搜索', to: '/search' },
  { name: 'upload', label: '分享', to: '/upload' },
  { name: 'users', label: '用户', to: auth.profile?.id ? `/users/${auth.profile.id}` : '/users' },
  { name: 'messages', label: '私信', to: '/messages' },
  { name: 'reports', label: '举报', to: '/reports' },
  { name: 'admin', label: '管理台', to: '/admin' }
])

function isLinkActive(name: string) {
  return route.name === name
}
</script>

<template>
  <aside class="sidebar">
    <div class="sidebar-brand panel panel-soft">
      <h2>Music Platform</h2>
    </div>

    <nav class="sidebar-nav panel">
      <RouterLink
        v-for="link in links"
        :key="link.name"
        :to="link.to"
        class="nav-link"
        :class="{ active: isLinkActive(link.name) }"
      >
        {{ link.label }}
      </RouterLink>
    </nav>

    <button class="btn btn-danger sidebar-logout" type="button" @click="$emit('logout')">
      退出登录
    </button>
  </aside>
</template>

<style scoped>
.sidebar {
  position: fixed;
  inset: 0 auto 0 0;
  width: 280px;
  padding: 24px;
  display: grid;
  align-content: start;
  gap: 18px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.78), rgba(255, 255, 255, 0.55));
  backdrop-filter: blur(20px);
  border-right: 1px solid rgba(23, 32, 51, 0.08);
}

.sidebar-brand {
  display: grid;
  place-items: center start;
  min-height: 96px;
}

.sidebar-brand h2 {
  margin: 0;
}

.sidebar-nav {
  display: grid;
  gap: 10px;
}

.nav-link {
  display: inline-flex;
  min-height: 44px;
  align-items: center;
  padding: 0 14px;
  border-radius: 14px;
  color: var(--muted);
  transition: background 0.2s ease, color 0.2s ease, transform 0.2s ease;
}

.nav-link:hover,
.nav-link.active {
  background: rgba(234, 88, 12, 0.12);
  color: var(--primary);
  transform: translateX(2px);
}

.sidebar-logout {
  width: 100%;
}

@media (max-width: 1180px) {
  .sidebar {
    position: static;
    width: auto;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .sidebar-brand,
  .sidebar-nav,
  .sidebar-logout {
    grid-column: 1 / -1;
  }
}
</style>
