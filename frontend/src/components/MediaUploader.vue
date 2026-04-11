<script setup lang="ts">
import { ref } from 'vue'
import { authApi } from '@/api'
import type { UploadedMedia } from '@/types/api'
import { useUiStore } from '@/stores/ui'

const props = withDefaults(
  defineProps<{
    modelValue: UploadedMedia[]
    accept?: string
    multiple?: boolean
    buttonText?: string
  }>(),
  {
    accept: '*/*',
    multiple: false,
    buttonText: '上传文件'
  }
)

const emit = defineEmits<{
  'update:modelValue': [UploadedMedia[]]
}>()

const ui = useUiStore()
const uploading = ref(false)

async function onSelect(event: Event) {
  const input = event.target as HTMLInputElement
  const files = Array.from(input.files ?? [])
  if (!files.length) {
    return
  }

  uploading.value = true
  try {
    const uploaded: UploadedMedia[] = []
    for (const file of files) {
      uploaded.push(await authApi.uploadMedia(file))
    }
    const nextValue = props.multiple ? [...props.modelValue, ...uploaded] : uploaded.slice(-1)
    emit('update:modelValue', nextValue)
    ui.success('文件上传成功')
  } catch (error) {
    ui.error(error instanceof Error ? error.message : '文件上传失败')
  } finally {
    uploading.value = false
    input.value = ''
  }
}

function remove(mediaId: number) {
  emit(
    'update:modelValue',
    props.modelValue.filter((item) => item.mediaId !== mediaId)
  )
}
</script>

<template>
  <div class="stack">
    <label class="btn btn-secondary">
      <input
        hidden
        type="file"
        :accept="accept"
        :multiple="multiple"
        :disabled="uploading"
        @change="onSelect"
      />
      {{ uploading ? '上传中...' : buttonText }}
    </label>

    <div class="upload-list" v-if="modelValue.length">
      <div v-for="item in modelValue" :key="item.mediaId" class="upload-chip">
        <div>
          <strong>{{ item.originalName }}</strong>
          <p class="muted">{{ item.url }}</p>
        </div>
        <button class="btn btn-ghost" type="button" @click="remove(item.mediaId)">
          移除
        </button>
      </div>
    </div>
  </div>
</template>
