import http from './http'

/**
 * 获取用户学习进度
 */
export async function getProgress (user) {
  const resp = await http.get(`/api/progress/get_progress/${encodeURIComponent(user)}`)
  return resp.data
}

/**
 * 获取每日学习列表
 */
export async function getDailyList (date) {
  const resp = await http.get('/api/progress/daily_list', {
    params: { date }
  })
  return resp.data
}

/**
 * 获取仪表盘大盘数据
 */
export async function getDashboardData (origin, days = 30) {
  const params = { days }
  if (origin) {
    params.origin = origin
  }
  const resp = await http.get('/api/progress/dashboard', { params })
  return resp.data
}

/**
 * 获取 AI 学情分析报告
 * 特别注意：AI 生成内容耗时较长，需单独设置较长的超时时间
 */
export async function getAiAnalysis (origin) {
  const params = {}
  if (origin) {
    params.origin = origin
  }
  
  // 发送 GET 请求，并覆盖默认的 15s 超时限制
  const resp = await http.get('/api/ai/analyze', { 
    params,
    // 将超时设置为 60000ms (60秒)，以匹配后端 DeepSeek 的响应速度
    timeout: 60000 
  })
  return resp.data
}