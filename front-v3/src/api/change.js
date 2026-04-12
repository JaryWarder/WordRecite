import http from './http'

export async function changeBook ({ username, newTitle }) {
  const resp = await http.get(`/api/change/change_book/${encodeURIComponent(newTitle)}`, {
    params: { username }
  })
  return resp.data
}
