<script setup lang="ts">
import { fetchEventSource } from '@microsoft/fetch-event-source'
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import { reportApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { useUiStore } from '@/stores/ui'
import { describeError, isForbiddenError } from '@/utils/errors'
import { formatDate } from '@/utils/format'

const auth = useAuthStore()
const ui = useUiStore()

const accessDenied = ref(false)
const adminReady = ref(false)
const adminConnecting = ref(false)
const adminConnected = ref(false)
const adminStatus = ref('未建立管理员连接')
const currentReport = ref<any | null>(null)
const addAdminId = ref<number | null>(null)
const banId = ref<number | null>(null)
const banningType = ref<'USER' | 'POST' | 'POST_COMMENT' | 'SONG' | 'SONG_COMMENT'>('USER')

const handleForm = reactive({
  handlerNote: '',
  result: 'PASS'
})

let adminController: AbortController | null = null

function stopAdminChannel() {
  adminController?.abort()
  adminController = null
  adminConnecting.value = false
  adminConnected.value = false
  adminReady.value = false
}

function markPermissionDenied(showToast = false) {
  stopAdminChannel()
  accessDenied.value = true
  currentReport.value = null
  adminStatus.value = '当前账号权限不足'
  if (showToast) {
    ui.error('权限不足')
  }
}

function handleAdminError(error: unknown, fallback: string) {
  if (isForbiddenError(error)) {
    markPermissionDenied(true)
    return
  }
  ui.error(describeError(error, fallback))
}

async function startAdminChannel() {
  stopAdminChannel()
  accessDenied.value = false

  if (!auth.token) {
    adminStatus.value = '未找到登录令牌'
    return
  }

  adminConnecting.value = true
  adminStatus.value = '正在建立管理员专属连接...'

  const localController = new AbortController()
  adminController = localController

  void fetchEventSource('/connect', {
    method: 'GET',
    headers: {
      Authorization: `Bearer ${auth.token}`
    },
    signal: localController.signal,
    openWhenHidden: true,
    async onopen(response) {
      if (response.status === 403) {
        markPermissionDenied()
        throw new Error('403 Forbidden')
      }
      if (!response.ok) {
        adminConnecting.value = false
        adminConnected.value = false
        adminReady.value = false
        adminStatus.value = `管理员连接失败 (${response.status})`
        throw new Error(`Admin connect failed with status ${response.status}`)
      }
      adminConnecting.value = false
      adminConnected.value = true
      adminReady.value = true
      accessDenied.value = false
      adminStatus.value = '管理员专属连接已建立'
    },
    onmessage() {},
    onerror(error) {
      if (localController.signal.aborted) {
        throw error
      }

      if (isForbiddenError(error)) {
        markPermissionDenied(true)
      } else {
        const hasConnected = adminConnected.value || adminReady.value
        adminConnecting.value = false
        adminConnected.value = false
        adminReady.value = false
        adminStatus.value = hasConnected
          ? '管理员连接已断开，请重新建立连接'
          : describeError(error, '管理员连接失败')
      }
      throw error
    }
  }).catch((error) => {
    if (localController.signal.aborted) {
      return
    }

    if (isForbiddenError(error)) {
      markPermissionDenied()
      return
    }

    if (adminConnecting.value) {
      adminConnecting.value = false
      adminConnected.value = false
      adminReady.value = false
      adminStatus.value = describeError(error, '管理员连接失败')
    }
  })
}

async function claimNextReport() {
  if (!adminReady.value) {
    ui.error('请先建立管理员连接')
    return
  }

  try {
    const next = await reportApi.getNextReport()
    currentReport.value = next
    if (!next) {
      ui.info('当前没有待处理举报')
    }
  } catch (error) {
    handleAdminError(error, '领取举报失败')
  }
}

async function handleCurrentReport() {
  if (!currentReport.value) {
    ui.error('请先领取一条举报')
    return
  }

  try {
    await reportApi.handleReport({
      id: Number(currentReport.value.id),
      handlerNote: handleForm.handlerNote,
      result: handleForm.result
    })
    ui.success('举报处理完成')
    currentReport.value = null
    handleForm.handlerNote = ''
    handleForm.result = 'PASS'
  } catch (error) {
    handleAdminError(error, '处理举报失败')
  }
}

async function banCurrentTarget() {
  if (!currentReport.value) {
    ui.error('请先领取举报')
    return
  }

  try {
    const targetId = Number(currentReport.value.targetId)
    const targetType = currentReport.value.targetType

    if (targetType === 'USER') await reportApi.banUser(targetId)
    if (targetType === 'POST') await reportApi.banPost(targetId)
    if (targetType === 'POST_COMMENT') await reportApi.banPostComment(targetId)
    if (targetType === 'SONG') await reportApi.banSong(targetId)
    if (targetType === 'SONG_COMMENT') await reportApi.banSongComment(targetId)

    ui.success('已对当前举报目标执行封禁')
  } catch (error) {
    handleAdminError(error, '封禁失败')
  }
}

async function manualBan() {
  if (!banId.value) {
    ui.error('请输入目标 ID')
    return
  }

  try {
    if (banningType.value === 'USER') await reportApi.banUser(banId.value)
    if (banningType.value === 'POST') await reportApi.banPost(banId.value)
    if (banningType.value === 'POST_COMMENT') await reportApi.banPostComment(banId.value)
    if (banningType.value === 'SONG') await reportApi.banSong(banId.value)
    if (banningType.value === 'SONG_COMMENT') await reportApi.banSongComment(banId.value)

    ui.success('手动封禁成功')
    banId.value = null
  } catch (error) {
    handleAdminError(error, '手动封禁失败')
  }
}

async function addAdmin() {
  if (!addAdminId.value) {
    ui.error('请输入用户 ID')
    return
  }

  try {
    await reportApi.addAdmin(addAdminId.value)
    ui.success('管理员添加成功')
    addAdminId.value = null
  } catch (error) {
    handleAdminError(error, '添加管理员失败')
  }
}

onMounted(() => {
  void startAdminChannel()
})

onBeforeUnmount(() => {
  stopAdminChannel()
})
</script>

<template>
  <section class="grid-2">
    <div class="panel">
      <PageHeader
        eyebrow="Admin"
        title="管理员页面"
        description="进入页面后会先建立管理员专属连接；管理相关的 403 会统一视为权限不足。"
      >
        <template #actions>
          <span class="pill">
            {{
              adminConnecting
                ? '管理员连接中'
                : adminConnected
                  ? '管理员连接在线'
                  : accessDenied
                    ? '权限不足'
                    : '管理员连接未建立'
            }}
          </span>
          <button class="btn btn-secondary" type="button" @click="startAdminChannel">重新建立连接</button>
          <button class="btn btn-primary" type="button" :disabled="!adminReady" @click="claimNextReport">
            领取下一条
          </button>
        </template>
      </PageHeader>

      <div v-if="adminConnecting" class="empty" style="margin-bottom: 16px">
        {{ adminStatus }}
      </div>

      <div v-else-if="accessDenied" class="empty" style="margin-bottom: 16px">
        当前账号没有管理员权限，因此无法进入管理员页面。
      </div>

      <div v-else-if="!adminReady" class="empty" style="margin-bottom: 16px">
        {{ adminStatus }}
      </div>

      <div v-else-if="currentReport" class="stack">
        <div class="item-card">
          <strong>{{ currentReport.targetType }} #{{ currentReport.targetId }}</strong>
          <p class="muted">举报人 ID: {{ currentReport.reporterId }}</p>
          <p>{{ currentReport.reason }}</p>
          <p class="muted">创建时间：{{ formatDate(currentReport.createTime) }}</p>
        </div>

        <button class="btn btn-danger" type="button" @click="banCurrentTarget">直接封禁当前目标</button>

        <form class="stack" @submit.prevent="handleCurrentReport">
          <div class="field">
            <label for="handle-result">处理结果</label>
            <select id="handle-result" v-model="handleForm.result">
              <option value="PASS">通过并处理</option>
              <option value="REJECT">驳回举报</option>
            </select>
          </div>
          <div class="field">
            <label for="handle-note">管理员备注</label>
            <textarea id="handle-note" v-model="handleForm.handlerNote" />
          </div>
          <button class="btn btn-primary" type="submit">提交处理结果</button>
        </form>
      </div>

      <div v-else class="empty">
        管理员连接已就绪。点击“领取下一条”开始处理举报。
      </div>
    </div>

    <div class="stack">
      <div class="panel">
        <PageHeader eyebrow="Manual Ban" title="手动封禁" />
        <form class="stack" @submit.prevent="manualBan">
          <div class="field">
            <label for="ban-type">目标类型</label>
            <select id="ban-type" v-model="banningType">
              <option value="USER">用户</option>
              <option value="POST">帖子</option>
              <option value="POST_COMMENT">帖子评论</option>
              <option value="SONG">歌曲</option>
              <option value="SONG_COMMENT">歌曲评论</option>
            </select>
          </div>
          <div class="field">
            <label for="ban-id">目标 ID</label>
            <input id="ban-id" v-model.number="banId" type="number" min="1" />
          </div>
          <button class="btn btn-danger" type="submit" :disabled="!adminReady">执行封禁</button>
        </form>
      </div>

      <div class="panel">
        <PageHeader eyebrow="Super Admin" title="添加管理员" />
        <form class="stack" @submit.prevent="addAdmin">
          <div class="field">
            <label for="add-admin-id">用户 ID</label>
            <input id="add-admin-id" v-model.number="addAdminId" type="number" min="1" />
          </div>
          <button class="btn btn-primary" type="submit" :disabled="!adminReady">添加管理员</button>
        </form>
      </div>
    </div>
  </section>
</template>
