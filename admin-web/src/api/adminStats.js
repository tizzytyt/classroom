import { http } from './http'

export async function getAdminStatsApi() {
  const resp = await http.get('/api/admin/stats')
  return resp.data
}

