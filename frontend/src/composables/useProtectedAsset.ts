import { onBeforeUnmount, ref, watch } from 'vue'
import http from '@/api/http'
import { resolveAssetUrl } from '@/utils/asset'

export function useProtectedAsset(source: () => string | null | undefined) {
  const resolvedSrc = ref('')
  const loading = ref(false)
  const error = ref('')

  let objectUrl: string | null = null
  let requestId = 0

  function cleanupObjectUrl() {
    if (objectUrl) {
      URL.revokeObjectURL(objectUrl)
      objectUrl = null
    }
  }

  async function load() {
    const currentSource = source()
    const normalized = resolveAssetUrl(currentSource, 'media')
    const currentRequestId = ++requestId

    cleanupObjectUrl()
    error.value = ''

    if (!normalized) {
      resolvedSrc.value = ''
      return
    }

    if (!normalized.startsWith('/media/')) {
      resolvedSrc.value = normalized
      return
    }

    loading.value = true
    try {
      const response = await http.get<Blob>(normalized, {
        responseType: 'blob'
      })

      if (currentRequestId !== requestId) {
        return
      }

      objectUrl = URL.createObjectURL(response.data)
      resolvedSrc.value = objectUrl
    } catch (caught) {
      if (currentRequestId !== requestId) {
        return
      }
      resolvedSrc.value = normalized
      error.value = caught instanceof Error ? caught.message : '资源加载失败'
    } finally {
      if (currentRequestId === requestId) {
        loading.value = false
      }
    }
  }

  watch(source, load, {
    immediate: true
  })

  onBeforeUnmount(() => {
    cleanupObjectUrl()
  })

  return {
    resolvedSrc,
    loading,
    error,
    reload: load
  }
}
