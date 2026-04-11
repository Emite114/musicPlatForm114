export function describeError(error: unknown, fallback = '请求失败') {
  if (error instanceof Error && error.message) {
    return error.message
  }
  if (typeof error === 'string' && error) {
    return error
  }
  return fallback
}

export function isForbiddenError(error: unknown) {
  const message = describeError(error, '')
  return message.includes('403') || message.includes('Forbidden')
}
