import http from './http'

export async function getWordInfo (obj) {
  const resp = await http.get('/api/word/get_wordInfo', { params: { obj: JSON.stringify(obj) } })
  return resp.data
}
