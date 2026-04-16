import { http } from './http'

export async function listUsersApi(params) {
  const resp = await http.get('/api/admin/users', { params })
  return resp.data
}

export async function updateUserStatusApi(id, status) {
  const resp = await http.patch(`/api/admin/users/${id}/status`, { status })
  return resp.data
}

export async function resetUserPasswordApi(id, password) {
  const resp = await http.post(`/api/admin/users/${id}/reset-password`, password ? { password } : {})
  return resp.data
}

export async function deleteUserApi(id) {
  const resp = await http.delete(`/api/admin/users/${id}`)
  return resp.data
}

export async function createUserApi(payload) {
  const resp = await http.post('/api/admin/users', payload)
  return resp.data
}

export async function updateUserProfileApi(id, payload) {
  const resp = await http.put(`/api/admin/users/${id}`, payload)
  return resp.data
}

export async function updateUserRoleApi(id, roleCode) {
  const resp = await http.patch(`/api/admin/users/${id}/role`, { roleCode })
  return resp.data
}

