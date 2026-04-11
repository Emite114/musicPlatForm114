<script setup lang="ts">
import { nextTick, onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import PaginationBar from '@/components/PaginationBar.vue'
import ProtectedImage from '@/components/ProtectedImage.vue'
import { messageApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { useRealtimeStore } from '@/stores/realtime'
import { useUiStore } from '@/stores/ui'
import { formatDate } from '@/utils/format'
import type { ConversationSummary, MessageItem, PageResult } from '@/types/api'

const route = useRoute()
const auth = useAuthStore()
const realtime = useRealtimeStore()
const ui = useUiStore()

const conversations = ref<PageResult<ConversationSummary> | null>(null)
const messages = ref<PageResult<MessageItem> | null>(null)
const selectedConversation = ref<ConversationSummary | null>(null)
const draftReceiveUserId = ref<number | null>(null)
const composer = ref('')
const loading = ref(false)
const sending = ref(false)
const threadViewport = ref<HTMLElement | null>(null)

function queryTarget() {
  const raw = route.query.with
  const value = Number(raw)
  return Number.isFinite(value) && value > 0 ? value : null
}

function conversationPreview(conversation: ConversationSummary) {
  const sender =
    conversation.lastMessageSenderId === auth.profile?.id
      ? auth.profile?.username || '我'
      : conversation.lastMessageSenderId === conversation.talkingToUserId
        ? conversation.talkingToUserName || `用户 #${conversation.talkingToUserId}`
        : conversation.lastMessageSenderName || '系统'
  const content = conversation.lastMessageContent || '暂无消息'
  return `${sender}: ${content}`
}

function conversationTitle(conversation: ConversationSummary) {
  return conversation.talkingToUserName || `用户 #${conversation.talkingToUserId}`
}

function messageSenderName(message: MessageItem) {
  if (message.speakingUserId === auth.profile?.id) {
    return auth.profile?.username || '我'
  }
  return selectedConversation.value?.talkingToUserName || `用户 #${message.speakingUserId}`
}

async function scrollThreadToBottom() {
  await nextTick()
  const viewport = threadViewport.value
  if (!viewport) {
    return
  }
  viewport.scrollTop = viewport.scrollHeight
}

async function loadConversations(page = 0) {
  loading.value = true
  try {
    conversations.value = await messageApi.getConversations(page, 20)
    const targetId = queryTarget()

    if (targetId) {
      const found = conversations.value.content.find((item) => item.talkingToUserId === targetId)
      if (found && selectedConversation.value?.id !== found.id) {
        await openConversation(found)
      } else if (!found) {
        draftReceiveUserId.value = targetId
      }
      return
    }

    if (!selectedConversation.value && conversations.value.content.length) {
      await openConversation(conversations.value.content[0])
    }
  } catch (error) {
    ui.error(error instanceof Error ? error.message : '会话列表加载失败')
  } finally {
    loading.value = false
  }
}

async function openConversation(conversation: ConversationSummary) {
  selectedConversation.value = conversation
  draftReceiveUserId.value = null

  try {
    messages.value = await messageApi.getMessages(conversation.id, 0, 80)
    await scrollThreadToBottom()
    await loadConversations(conversations.value?.number ?? 0)
  } catch (error) {
    ui.error(error instanceof Error ? error.message : '消息加载失败')
  }
}

async function sendMessage() {
  const targetUserId = selectedConversation.value?.talkingToUserId ?? draftReceiveUserId.value
  if (!targetUserId) {
    ui.error('请先选择一个会话或指定接收用户')
    return
  }
  if (targetUserId === auth.profile?.id) {
    ui.error('不能给自己发私信')
    return
  }
  if (!composer.value.trim()) {
    ui.error('消息内容不能为空')
    return
  }

  sending.value = true
  try {
    await messageApi.sendMessage({
      content: composer.value.trim(),
      receiveUserId: targetUserId
    })
    composer.value = ''
    await loadConversations(conversations.value?.number ?? 0)
    const nextConversation = conversations.value?.content.find(
      (item) => item.talkingToUserId === targetUserId
    )
    if (nextConversation) {
      await openConversation(nextConversation)
    }
    await scrollThreadToBottom()
    ui.success('消息已发送')
  } catch (error) {
    ui.error(error instanceof Error ? error.message : '消息发送失败')
  } finally {
    sending.value = false
  }
}

watch(
  () => route.query.with,
  async () => {
    draftReceiveUserId.value = queryTarget()
    await loadConversations(conversations.value?.number ?? 0)
  }
)

watch(
  () => realtime.inboxVersion,
  async () => {
    const payload = realtime.lastEvent
    await loadConversations(conversations.value?.number ?? 0)
    if (selectedConversation.value && payload?.conversationId === selectedConversation.value.id) {
      try {
        messages.value = await messageApi.getMessages(payload.conversationId, 0, 80)
        await scrollThreadToBottom()
      } catch (error) {
        ui.error(error instanceof Error ? error.message : '消息加载失败')
      }
    }
  }
)

onMounted(async () => {
  draftReceiveUserId.value = queryTarget()
  await loadConversations()
})
</script>

<template>
  <section class="panel">
    <div class="message-shell messages-layout">
      <div class="panel panel-soft conversation-panel">
        <div v-if="draftReceiveUserId" class="item-card">
          <strong>准备新建会话</strong>
          <p class="muted">将向用户 #{{ draftReceiveUserId }} 发送第一条私信。</p>
        </div>

        <div v-if="conversations?.content.length" class="conversation-list-scroll">
          <div class="card-list">
            <article
              v-for="conversation in conversations.content"
              :key="conversation.id"
              class="conversation-item conversation-card"
              :class="{ active: selectedConversation?.id === conversation.id }"
              @click="openConversation(conversation)"
            >
              <div class="conversation-head">
                <RouterLink
                  class="conversation-user-link"
                  :to="`/users/${conversation.talkingToUserId}`"
                  @click.stop
                >
                  <ProtectedImage :src="conversation.talkingToUserAvatar" class="avatar conversation-avatar" />
                  <strong class="conversation-name">{{ conversationTitle(conversation) }}</strong>
                </RouterLink>

                <span v-if="conversation.unreadCount" class="pill">{{ conversation.unreadCount }}</span>
              </div>

              <p class="conversation-preview">{{ conversationPreview(conversation) }}</p>
              <p class="muted conversation-time">{{ formatDate(conversation.lastTime) }}</p>
            </article>
          </div>
        </div>

        <div v-else class="empty conversation-empty">{{ loading ? '加载中...' : '暂无会话' }}</div>

        <PaginationBar
          :page="conversations"
          @prev="loadConversations((conversations?.number ?? 1) - 1)"
          @next="loadConversations((conversations?.number ?? 0) + 1)"
        />
      </div>

      <div class="panel message-thread-panel">
        <div class="panel-header thread-header">
          <div v-if="selectedConversation" class="thread-user-head">
            <RouterLink
              class="thread-user-link"
              :to="`/users/${selectedConversation.talkingToUserId}`"
            >
              <ProtectedImage :src="selectedConversation.talkingToUserAvatar" class="avatar thread-avatar" />
              <div class="thread-user-copy">
                <h3>{{ conversationTitle(selectedConversation) }}</h3>
                <p class="muted">当前会话与后端消息列表保持同步。</p>
              </div>
            </RouterLink>
          </div>

          <div v-else>
            <h3>{{ draftReceiveUserId ? `用户 #${draftReceiveUserId}` : '选择一个会话' }}</h3>
            <p class="muted">可以直接输入第一条消息。</p>
          </div>
        </div>

        <div ref="threadViewport" class="message-scroll">
          <div v-if="messages?.content.length" class="message-list">
            <article
              v-for="message in messages.content"
              :key="message.id"
              class="bubble"
              :class="{ 'bubble-self': message.speakingUserId === auth.profile?.id }"
            >
              <p class="bubble-content">
                <strong>{{ messageSenderName(message) }}:</strong>
                <span>{{ message.content }}</span>
              </p>
              <small>{{ formatDate(message.createTime) }}</small>
            </article>
          </div>

          <div v-else class="empty message-empty">这里还没有消息，先发一条吧。</div>
        </div>

        <div class="divider" />

        <form class="stack composer-form" @submit.prevent="sendMessage">
          <div class="field">
            <textarea v-model="composer" placeholder="输入消息内容..." />
          </div>
          <button class="btn btn-primary" type="submit" :disabled="sending">
            {{ sending ? '发送中...' : '发送消息' }}
          </button>
        </form>
      </div>
    </div>
  </section>
</template>

<style scoped>
.messages-layout {
  align-items: stretch;
  height: clamp(840px, calc(100vh - 170px), 980px);
  min-height: 0;
}

.conversation-panel,
.message-thread-panel {
  min-height: 0;
  height: 100%;
  overflow: hidden;
}

.conversation-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.conversation-list-scroll {
  min-height: 0;
  overflow-y: auto;
  padding-right: 6px;
}

.conversation-empty {
  align-self: start;
}

.conversation-card {
  padding: 18px;
}

.conversation-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.conversation-user-link {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.conversation-avatar {
  width: 54px;
  height: 54px;
}

.conversation-name {
  display: block;
  font-size: 1.16rem;
  line-height: 1.1;
}

.conversation-preview {
  margin: 14px 0 6px;
  color: var(--ink);
  line-height: 1.55;
  word-break: break-word;
}

.conversation-time {
  margin: 0;
}

.message-thread-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.thread-header {
  margin-bottom: 0;
}

.thread-user-head,
.thread-user-link {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
  color: inherit;
}

.thread-avatar {
  width: 56px;
  height: 56px;
}

.thread-user-copy h3 {
  margin: 0;
  font-size: 1.22rem;
}

.thread-user-copy p {
  margin: 4px 0 0;
}

.message-scroll {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding-right: 8px;
}

.message-list {
  min-height: 100%;
  padding-right: 2px;
}

.message-empty {
  align-self: start;
}

.bubble {
  max-width: min(80%, 760px);
}

.bubble-content {
  margin: 0 0 8px;
  line-height: 1.7;
  word-break: break-word;
}

.bubble-content strong {
  margin-right: 6px;
}

.composer-form {
  margin-top: auto;
  gap: 14px;
}

@media (max-width: 1180px) {
  .messages-layout {
    height: auto;
  }

  .conversation-panel,
  .message-thread-panel {
    min-height: 420px;
    height: auto;
  }
}

@media (max-width: 900px) {
  .message-thread-panel {
    min-height: 520px;
  }

  .bubble {
    max-width: 100%;
  }
}
</style>
