export interface TimedLyricLine {
  time: number
  text: string
}

export interface ParsedLyricContent {
  timedLines: TimedLyricLine[]
  plainLines: string[]
}

export function parseTimedLyricLines(text: string) {
  const lines: TimedLyricLine[] = []

  for (const rawLine of text.split(/\r?\n/)) {
    const matches = [...rawLine.matchAll(/\[(\d{2}):(\d{2})(?:\.(\d{1,3}))?\]/g)]
    const content = rawLine.replace(/\[[^\]]+\]/g, '').trim()

    if (!matches.length || !content) {
      continue
    }

    for (const match of matches) {
      const minutes = Number(match[1] ?? 0)
      const seconds = Number(match[2] ?? 0)
      const millis = Number((match[3] ?? '0').padEnd(3, '0'))
      const time = minutes * 60 + seconds + millis / 1000

      if (Number.isFinite(time)) {
        lines.push({ time, text: content })
      }
    }
  }

  return lines.sort((left, right) => left.time - right.time)
}

export function parsePlainLyricLines(text: string) {
  return text
    .split(/\r?\n/)
    .map((line) => line.replace(/\[[^\]]+\]/g, '').trim())
    .filter(Boolean)
}

export function parseLyricContent(text: string): ParsedLyricContent {
  const timedLines = parseTimedLyricLines(text)
  if (timedLines.length) {
    return {
      timedLines,
      plainLines: timedLines.map((line) => line.text)
    }
  }

  return {
    timedLines: [],
    plainLines: parsePlainLyricLines(text)
  }
}
