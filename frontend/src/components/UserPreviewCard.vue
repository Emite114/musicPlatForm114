<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import ProtectedImage from '@/components/ProtectedImage.vue'
import { formatCompactNumber } from '@/utils/format'
import type { UserCard } from '@/types/api'

const props = defineProps<{
  user: UserCard
}>()

const relationLabel = computed(() => {
  if (props.user.isFollowed && props.user.isMyFan) {
    return '互相关注'
  }
  if (props.user.isFollowed) {
    return '已关注'
  }
  if (props.user.isMyFan) {
    return '关注了你'
  }
  return '未关注'
})

const relationTone = computed(() => {
  if (props.user.isFollowed && props.user.isMyFan) {
    return 'mutual'
  }
  if (props.user.isFollowed) {
    return 'followed'
  }
  if (props.user.isMyFan) {
    return 'fan'
  }
  return 'plain'
})
</script>

<template>
  <article class="user-card">
    <RouterLink class="user-card-main" :to="`/users/${user.id}`">
      <ProtectedImage :src="user.avatarUrl" class="user-card-avatar" />

      <div class="user-card-copy">
        <h3>{{ user.username || '匿名用户' }}</h3>

        <div class="user-card-stats">
          <span class="pill">帖子 {{ formatCompactNumber(user.postCount) }}</span>
          <span class="pill">粉丝 {{ formatCompactNumber(user.fanCount) }}</span>
        </div>
      </div>
    </RouterLink>

    <div class="user-card-footer">
      <span class="relation-pill" :class="relationTone">{{ relationLabel }}</span>
      <div v-if="$slots.actions" class="user-card-actions">
        <slot name="actions" />
      </div>
    </div>
  </article>
</template>

<style scoped>
.user-card {
  min-height: 100%;
  display: grid;
  gap: 14px;
  padding: 18px;
  border: 1px solid var(--line);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.96);
}

.user-card-main {
  display: flex;
  gap: 14px;
  align-items: center;
}

.user-card-avatar {
  width: 68px;
  height: 68px;
  border-radius: 50%;
  object-fit: cover;
}

.user-card-copy {
  min-width: 0;
  display: grid;
  gap: 8px;
}

.user-card-copy h3 {
  margin: 0;
}

.user-card-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.user-card-footer {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  justify-content: space-between;
}

.relation-pill {
  display: inline-flex;
  align-items: center;
  min-height: 32px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(23, 32, 51, 0.06);
  color: var(--muted);
  font-size: 0.85rem;
}

.relation-pill.followed {
  background: rgba(14, 165, 233, 0.12);
  color: #0284c7;
}

.relation-pill.fan {
  background: rgba(34, 197, 94, 0.12);
  color: #15803d;
}

.relation-pill.mutual {
  background: rgba(234, 88, 12, 0.12);
  color: var(--primary);
}

.user-card-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
</style>
