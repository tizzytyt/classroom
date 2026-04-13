const { baseURL } = require('./config')

function getToken() {
  try {
    return wx.getStorageSync('token') || ''
  } catch (e) {
    return ''
  }
}

function clearAuth() {
  try {
    wx.removeStorageSync('token')
    wx.removeStorageSync('user')
  } catch (e) {}
}

function normalizeErrorMessage(res) {
  if (!res) return '网络异常'
  if (typeof res === 'string') return res
  if (res.data && typeof res.data === 'string') return res.data
  if (res.data && res.data.message) return res.data.message
  if (res.errMsg) return res.errMsg
  return '请求失败'
}

function request(path, method, data) {
  const token = getToken()
  const header = {}
  if (token) header.Authorization = 'Bearer ' + token

  return new Promise((resolve, reject) => {
    wx.request({
      url: baseURL + path,
      method: method || 'GET',
      data: data || {},
      header,
      success: res => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res.data)
          return
        }
        try {
          console.error('[http error]', method || 'GET', path, res.statusCode, res.data)
        } catch (e) {}
        if (res.statusCode === 401) {
          clearAuth()
          wx.reLaunch({ url: '/pages/login/login' })
        }
        reject(new Error(normalizeErrorMessage(res)))
      },
      fail: err => reject(new Error(normalizeErrorMessage(err)))
    })
  })
}

function postJson(path, body) {
  const token = getToken()
  const header = { 'Content-Type': 'application/json' }
  if (token) header.Authorization = 'Bearer ' + token

  return new Promise((resolve, reject) => {
    wx.request({
      url: baseURL + path,
      method: 'POST',
      data: body || {},
      header,
      success: res => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res.data)
          return
        }
        try {
          console.error('[http error]', 'POST', path, res.statusCode, res.data)
        } catch (e) {}
        if (res.statusCode === 401) {
          clearAuth()
          wx.reLaunch({ url: '/pages/login/login' })
        }
        reject(new Error(normalizeErrorMessage(res)))
      },
      fail: err => reject(new Error(normalizeErrorMessage(err)))
    })
  })
}

function putJson(path, body) {
  const token = getToken()
  const header = { 'Content-Type': 'application/json' }
  if (token) header.Authorization = 'Bearer ' + token

  return new Promise((resolve, reject) => {
    wx.request({
      url: baseURL + path,
      method: 'PUT',
      data: body || {},
      header,
      success: res => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res.data)
          return
        }
        try {
          console.error('[http error]', 'PUT', path, res.statusCode, res.data)
        } catch (e) {}
        if (res.statusCode === 401) {
          clearAuth()
          wx.reLaunch({ url: '/pages/login/login' })
        }
        reject(new Error(normalizeErrorMessage(res)))
      },
      fail: err => reject(new Error(normalizeErrorMessage(err)))
    })
  })
}

module.exports = {
  request,
  postJson,
  putJson
}
