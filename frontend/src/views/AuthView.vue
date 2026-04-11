<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useUiStore } from '@/stores/ui'

const auth = useAuthStore()
const ui = useUiStore()
const route = useRoute()
const router = useRouter()

const mode = ref<'login' | 'register'>('login')
const loading = ref(false)

const loginForm = reactive({
  account: '',
  password: ''
})

const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  gender: 'unknown'
})

async function submitLogin() {
  loading.value = true
  try {
    await auth.login(loginForm)
    ui.success('登录成功')
    await router.push((route.query.redirect as string) || '/')
  } catch (error) {
    ui.error(error instanceof Error ? error.message : '登录失败')
  } finally {
    loading.value = false
  }
}

async function submitRegister() {
  if (registerForm.password !== registerForm.confirmPassword) {
    ui.error('两次输入的密码不一致')
    return
  }
  loading.value = true
  try {
    await auth.register({
      username: registerForm.username,
      email: registerForm.email,
      password: registerForm.password,
      gender: registerForm.gender
    })
    ui.success('注册成功，请直接登录')
    loginForm.account = registerForm.email
    loginForm.password = registerForm.password
    mode.value = 'login'
  } catch (error) {
    ui.error(error instanceof Error ? error.message : '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-layout">
    <section class="auth-copy">
      <h1>Music Platform</h1>
      <p>登录或注册后即可进入平台。</p>
      <div class="grid-2">
        <div class="metric">
          <strong>社交化</strong>
          <p>帖子、关注、私信、举报一套串起来。</p>
        </div>
        <div class="metric">
          <strong>音乐化</strong>
          <p>歌曲上传、试听、收藏、评论集中管理。</p>
        </div>
      </div>
    </section>

    <section class="auth-card">
      <div class="panel">
        <div class="tab-switch">
          <button
            class="btn"
            :class="mode === 'login' ? 'btn-primary' : 'btn-ghost'"
            type="button"
            @click="mode = 'login'"
          >
            登录
          </button>
          <button
            class="btn"
            :class="mode === 'register' ? 'btn-primary' : 'btn-ghost'"
            type="button"
            @click="mode = 'register'"
          >
            注册
          </button>
        </div>

        <div class="divider" style="margin: 18px 0" />

        <form v-if="mode === 'login'" class="stack" @submit.prevent="submitLogin">
          <div class="field">
            <label for="account">账号</label>
            <input id="account" v-model.trim="loginForm.account" placeholder="用户名或邮箱" />
          </div>
          <div class="field">
            <label for="password">密码</label>
            <input
              id="password"
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
            />
          </div>
          <button class="btn btn-primary" type="submit" :disabled="loading">
            {{ loading ? '登录中...' : '登录' }}
          </button>
        </form>

        <form v-else class="stack" @submit.prevent="submitRegister">
          <div class="field">
            <label for="username">用户名</label>
            <input id="username" v-model.trim="registerForm.username" placeholder="2-30 位更合适" />
          </div>
          <div class="field">
            <label for="email">邮箱</label>
            <input id="email" v-model.trim="registerForm.email" type="email" />
          </div>
          <div class="field">
            <label for="register-password">密码</label>
            <input
              id="register-password"
              v-model="registerForm.password"
              type="password"
              placeholder="长度需大于 6"
            />
          </div>
          <div class="field">
            <label for="confirm-password">确认密码</label>
            <input id="confirm-password" v-model="registerForm.confirmPassword" type="password" />
          </div>
          <div class="field">
            <label for="gender">性别</label>
            <select id="gender" v-model="registerForm.gender">
              <option value="unknown">保密</option>
              <option value="male">男</option>
              <option value="female">女</option>
            </select>
          </div>
          <button class="btn btn-primary" type="submit" :disabled="loading">
            {{ loading ? '注册中...' : '创建账号' }}
          </button>
        </form>
      </div>
    </section>
  </div>
</template>

