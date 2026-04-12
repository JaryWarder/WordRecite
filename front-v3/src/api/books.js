import http from './http'

export async function getBooks (user) {
  const resp = await http.get(`/api/books/get_books/${encodeURIComponent(user)}`)
  return resp.data
}

export async function getCurrentBook (user) {
  const resp = await http.get(`/api/books/get_book/${encodeURIComponent(user)}`)
  return resp.data
}
