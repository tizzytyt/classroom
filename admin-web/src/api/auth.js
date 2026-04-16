import { http } from './http'

export async function loginApi({ username, password }) {
  const resp = await http.post('/api/auth/login', { username, password })
  return resp.data
}

