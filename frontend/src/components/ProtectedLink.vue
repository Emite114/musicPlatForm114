<script setup lang="ts">
import { computed } from 'vue'
import { useProtectedAsset } from '@/composables/useProtectedAsset'

const props = withDefaults(
  defineProps<{
    href?: string | null
    downloadName?: string
  }>(),
  {
    downloadName: ''
  }
)

const asset = useProtectedAsset(() => props.href)
const resolvedHref = computed(() => asset.resolvedSrc.value || '#')
const target = computed(() => (props.downloadName ? undefined : '_blank'))
const rel = computed(() => (props.downloadName ? undefined : 'noreferrer'))
</script>

<template>
  <a
    :href="resolvedHref"
    :download="downloadName || undefined"
    :target="target"
    :rel="rel"
  >
    <slot />
  </a>
</template>
