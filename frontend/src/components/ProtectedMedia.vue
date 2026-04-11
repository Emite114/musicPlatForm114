<script setup lang="ts">
import { computed } from 'vue'
import { useProtectedAsset } from '@/composables/useProtectedAsset'

defineOptions({
  inheritAttrs: false
})

const props = withDefaults(
  defineProps<{
    src?: string | null
    kind: 'audio' | 'video'
  }>(),
  {}
)

const asset = useProtectedAsset(() => props.src)
const tagName = computed(() => props.kind)
const mediaSrc = computed(() => asset.resolvedSrc.value)
</script>

<template>
  <component
    :is="tagName"
    v-if="mediaSrc"
    v-bind="$attrs"
    :src="mediaSrc"
  />
  <div v-else v-bind="$attrs" class="media-fallback">
    媒体加载失败
  </div>
</template>

<style scoped>
.media-fallback {
  display: grid;
  place-items: center;
  color: var(--muted);
  background: rgba(23, 32, 51, 0.06);
}
</style>
