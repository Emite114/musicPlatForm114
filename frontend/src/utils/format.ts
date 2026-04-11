export function formatDate(value?: string | null) {
  if (!value) {
    return '暂无时间'
  }

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }

  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}

export function formatDateShort(value?: string | null) {
  if (!value) {
    return '暂无时间'
  }

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }

  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit'
  }).format(date)
}

export function formatCompactNumber(value?: number | string | null) {
  const amount = Number(value ?? 0)
  if (!Number.isFinite(amount)) {
    return '0'
  }

  if (Math.abs(amount) >= 10000) {
    const scaled = amount / 10000
    const fixed = Math.abs(scaled) >= 100 ? scaled.toFixed(0) : scaled.toFixed(1)
    return `${fixed.replace(/\.0$/, '')}万`
  }

  return String(Math.round(amount))
}

export function clampKeyword(
  value: string,
  { minLength = 0, fallback = '' }: { minLength?: number; fallback?: string } = {}
) {
  const normalized = value.trim()
  if (!normalized) {
    return fallback
  }
  if (normalized.length < minLength) {
    throw new Error(`请输入至少 ${minLength} 个字符`)
  }
  return normalized
}

export function truncate(value: string, max = 120) {
  if (value.length <= max) {
    return value
  }
  return `${value.slice(0, max)}...`
}
