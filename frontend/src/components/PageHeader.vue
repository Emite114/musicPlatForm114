<script setup lang="ts">
defineProps<{
  eyebrow?: string
  title: string
  description?: string
}>()
</script>

<template>
  <div class="panel-header">
    <div>
      <span v-if="eyebrow" class="eyebrow">{{ eyebrow }}</span>
      <h2>{{ title }}</h2>
      <p v-if="description" class="muted">{{ description }}</p>
    </div>

    <div class="actions">
      <slot name="actions" />
    </div>
  </div>
</template>
