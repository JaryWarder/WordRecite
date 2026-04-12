export function decodeEntities (input) {
  const s = String(input || '')
  return s
    .replace(/&quot;/g, '"')
    .replace(/&#34;/g, '"')
    .replace(/&apos;/g, "'")
    .replace(/&#39;/g, "'")
    .replace(/&amp;/g, '&')
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
}

export function extractExamples (line) {
  const s = String(line || '').trim()
  if (!s) return { main: '', examples: [] }

  const examples = []
  const re = /"([^"]+)"/g
  let m
  while ((m = re.exec(s)) !== null) {
    if (m[1] && m[1].trim()) {
      examples.push(`"${m[1].trim()}"`)
    }
  }

  const main = s
    .replace(/"[^"]*"/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()

  return { main, examples }
}

export function formatDefinition (raw) {
  const text = decodeEntities(raw)
    .replace(/\r\n/g, '\n')
    .replace(/\r/g, '\n')
    .replace(/\s+/g, ' ')
    .trim()

  if (!text) return []

  const chunks = text
    .split('**')
    .map((c) => c.trim())
    .filter(Boolean)

  const blocks = []
  const tagRe = /^\{([^}]+)\}\+\+([\s\S]*)$/
  for (const chunk of chunks) {
    const m = chunk.match(tagRe)
    const tag = m ? m[1].trim() : ''
    const body = (m ? m[2] : chunk).trim()

    const { main, examples } = extractExamples(body)
    const lines = []
    if (main) lines.push({ kind: 'text', text: main })
    if (examples.length) lines.push({ kind: 'example', text: examples.join('  ') })

    blocks.push({ tag, lines })
  }

  return blocks
}
