export function resolveAssetUrl(
  value?: string | null,
  mode: 'plain' | 'media' = 'plain'
) {
  if (!value) {
    return ''
  }
  const normalized = value.trim()
  if (/^(https?:)?\/\//.test(normalized) || normalized.startsWith('data:')) {
    return normalized
  }
  if (normalized.startsWith('/media/') || normalized.startsWith('/default/')) {
    return normalized
  }
  if (normalized.startsWith('media/') || normalized.startsWith('default/')) {
    return `/${normalized}`
  }
  if (normalized.startsWith('/')) {
    return normalized
  }
  if (mode === 'media') {
    return `/media/${normalized}`
  }
  if (/\.(png|jpe?g|gif|webp|bmp|svg|mp3|wav|lrc|mp4|webm|ogg|mov|m4v)$/i.test(normalized)) {
    return `/media/${normalized}`
  }
  if (normalized.includes('/')) {
    return `/media/${normalized}`
  }
  if (/^[a-f0-9]{16,}$/i.test(normalized)) {
    return `/media/${normalized}`
  }
  return normalized
}

export function isImageAsset(value?: string | null) {
  if (!value) {
    return false
  }
  return /\.(png|jpe?g|gif|webp|bmp|svg)$/i.test(value)
}

export function isVideoAsset(value?: string | null) {
  if (!value) {
    return false
  }
  return /\.(mp4|webm|ogg|mov|m4v)$/i.test(value)
}

export function isAudioAsset(value?: string | null) {
  if (!value) {
    return false
  }
  return /\.(mp3|wav|m4a|aac|flac|ogg)$/i.test(value)
}

export function isLyricAsset(value?: string | null) {
  if (!value) {
    return false
  }
  return /\.(lrc|txt)$/i.test(value)
}

export function getAssetFileName(value?: string | null, fallback = 'attachment') {
  if (!value) {
    return fallback
  }

  const sanitized = value.split(/[?#]/)[0] || ''
  const segments = sanitized.split('/').filter(Boolean)
  const rawName = segments.length ? segments[segments.length - 1] : fallback

  try {
    return decodeURIComponent(rawName)
  } catch {
    return rawName
  }
}
