const { request } = require('../../../utils/request')
const { getUser } = require('../../../utils/auth')

Page({
  data: {
    courseId: null,
    user: null,
    isTeacher: false,
    loading: false,
    students: []
  },

  onLoad(options) {
    const courseId = options && options.courseId != null ? String(options.courseId).trim() : ''
    const user = getUser()
    this.setData({
      courseId,
      user,
      isTeacher: !!(user && user.roleCode === 'TEACHER')
    })
    if (!courseId) {
      wx.showToast({ title: '缺少课程ID', icon: 'none' })
      return
    }
    this.loadStudents()
  },

  async loadStudents() {
    if (!this.data.isTeacher) {
      wx.showToast({ title: '仅教师可查看学生列表', icon: 'none' })
      return
    }
    this.setData({ loading: true })
    try {
      const list = await request(`/api/teacher/courses/${this.data.courseId}/students`, 'GET')
      this.setData({ students: Array.isArray(list) ? list : [] })
    } catch (e) {
      wx.showToast({ title: (e && e.message) ? e.message : '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  async removeStudent(e) {
    const studentId = e && e.currentTarget ? e.currentTarget.dataset.studentId : null
    if (!this.data.isTeacher || !this.data.courseId || !studentId) return
    wx.showModal({
      title: '移除学生',
      content: '确认将该学生移出课程？',
      confirmColor: '#ff4d4f',
      success: async (res) => {
        if (!res.confirm) return
        try {
          await request(`/api/teacher/courses/${this.data.courseId}/students/${studentId}`, 'DELETE', {})
          wx.showToast({ title: '移除成功', icon: 'success' })
          await this.loadStudents()
        } catch (err) {
          wx.showToast({ title: (err && err.message) ? err.message : '移除失败', icon: 'none' })
        }
      }
    })
  }
})

