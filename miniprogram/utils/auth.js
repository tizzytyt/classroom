const { request } = require('./request')

function setToken(token) {
  wx.setStorageSync('token', token || '')
}

function getToken() {
  return wx.getStorageSync('token') || ''
}

function setUser(user) {
  wx.setStorageSync('user', user || null)
}

function getUser() {
  return wx.getStorageSync('user') || null
}

function clearAuth() {
  wx.removeStorageSync('token')
  wx.removeStorageSync('user')
}

async function login(username, password) {
  const resp = await request('/api/auth/login', 'POST', { username, password })
  setToken(resp.token)
  setUser({ id: resp.userId, username: resp.username, realName: resp.realName, roleCode: resp.roleCode })
  return resp
}

function logout() {
  clearAuth()
  wx.reLaunch({ url: '/pages/login/login' })
}

function isLoggedIn() {
  return !!getToken()
}

async function refreshProfile() {
  const u = await request('/api/auth/profile', 'GET')
  const safe = {
    id: u && u.id ? u.id : null,
    username: u && u.username ? u.username : '',
    realName: u && u.realName ? u.realName : '',
    roleCode: u && u.roleCode ? u.roleCode : ''
  }
  setUser(safe)
  return safe
}

module.exports = {
  login,
  logout,
  isLoggedIn,
  refreshProfile,
  getUser
}
