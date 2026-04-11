<script setup lang="ts">
import { computed } from 'vue'
import { useProtectedAsset } from '@/composables/useProtectedAsset'

defineOptions({
  inheritAttrs: false
})

const props = withDefaults(
  defineProps<{
    src?: string | null
    alt?: string
    fallbackText?: string
  }>(),
  {
    alt: '',
    fallbackText: '图片加载失败'
  }
)

const asset = useProtectedAsset(() => props.src)
const imageSrc = computed(() => asset.resolvedSrc.value)
const canRender = computed(() => Boolean(imageSrc.value))
</script>

<template>
  <img v-if="canRender" v-bind="$attrs" :src="imageSrc" :alt="alt" />
  <div v-else v-bind="$attrs" class="media-fallback">
    {{ fallbackText }}
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
