<script setup lang="ts">
import type { PageResult } from '@/types/api'

defineProps<{
  page: PageResult<unknown> | null
}>()

const emit = defineEmits<{
  prev: []
  next: []
}>()
</script>

<template>
  <div v-if="page" class="inline justify-between">
    <p class="muted">第 {{ page.number + 1 }} / {{ Math.max(page.totalPages, 1) }} 页，共 {{ page.totalElements }} 条</p>

    <div class="actions">
      <button class="btn btn-secondary" type="button" :disabled="page.first" @click="emit('prev')">
        上一页
      </button>
      <button class="btn btn-secondary" type="button" :disabled="page.last" @click="emit('next')">
        下一页
      </button>
    </div>
  </div>
</template>
