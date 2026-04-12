import http from './http'

export async function submitPlan ({ user, numWords }) {
  const resp = await http.post('/api/plan/submit_plan', null, {
    params: { user, numWords }
  })
  return resp.data
}
