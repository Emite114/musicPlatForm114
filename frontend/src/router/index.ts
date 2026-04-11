import { createRouter, createWebHashHistory } from 'vue-router'
import { userApi } from '@/api'
import { pinia } from '@/stores'
import { useAuthStore } from '@/stores/auth'
import { useUiStore } from '@/stores/ui'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/auth',
      name: 'auth',
      component: () => import('@/views/AuthView.vue'),
      meta: { public: true, title: '登录与注册' }
    },
    {
      path: '/',
      name: 'hot-songs',
      component: () => import('@/views/DashboardView.vue'),
      meta: { title: '热门歌曲' }
    },
    {
      path: '/hot-posts',
      name: 'hot-posts',
      component: () => import('@/views/DashboardView.vue'),
      meta: { title: '热门帖子' }
    },
    {
      path: '/search',
      name: 'search',
      component: () => import('@/views/SearchView.vue'),
      meta: { title: '搜索' }
    },
    {
      path: '/upload',
      name: 'upload',
      component: () => import('@/views/UploadView.vue'),
      meta: { title: '分享' }
    },
    {
      path: '/songs',
      redirect: { name: 'upload', query: { section: 'song' } }
    },
    {
      path: '/songs/:id',
      name: 'song-detail',
      component: () => import('@/views/SongDetailView.vue'),
      meta: { title: '歌曲详情' }
    },
    {
      path: '/posts',
      redirect: { name: 'upload', query: { section: 'post' } }
    },
    {
      path: '/posts/:id',
      name: 'post-detail',
      component: () => import('@/views/PostDetailView.vue'),
      meta: { title: '帖子详情' }
    },
    {
      path: '/users/:id?',
      name: 'users',
      component: () => import('@/views/UsersView.vue'),
      meta: { title: '用户主页' }
    },
    {
      path: '/messages',
      name: 'messages',
      component: () => import('@/views/MessagesView.vue'),
      meta: { title: '私信中心' }
    },
    {
      path: '/profile',
      name: 'profile',
      redirect: '/users'
    },
    {
      path: '/reports',
      name: 'reports',
      component: () => import('@/views/ReportsView.vue'),
      meta: { title: '举报中心' }
    },
    {
      path: '/admin',
      name: 'admin',
      component: () => import('@/views/AdminView.vue'),
      meta: { title: '管理台' }
    }
  ]
})

router.beforeEach(async (to, from) => {
  const auth = useAuthStore(pinia)
  const ui = useUiStore(pinia)

  if (!auth.ready) {
    try {
      await auth.bootstrap()
    } catch {
      if (!to.meta.public) {
        return { name: 'auth', query: { redirect: to.fullPath } }
      }
    }
  }

  if (to.meta.public) {
    if (auth.isAuthenticated && to.name === 'auth') {
      return { name: 'hot-songs' }
    }
    return true
  }

  if (!auth.isAuthenticated) {
    return { name: 'auth', query: { redirect: to.fullPath } }
  }

  if (to.name === 'admin') {
    try {
      const isAdmin = await userApi.ifIsAdmin()
      if (!isAdmin) {
        ui.error('当前账号不是管理员，无法进入管理台')
        return from.name ? false : { name: 'hot-songs' }
      }
    } catch {
      ui.error('管理员权限校验失败，暂时无法进入管理台')
      return false
    }
  }

  return true
})

export default router
