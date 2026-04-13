const { request } = require('../../../utils/request')
const { getUser } = require('../../../utils/auth')

function weightToPctInput(w) {
  if (w == null || w === '') return '70'
  const n = Number(w)
  if (!Number.isFinite(n)) return '70'
  return String(Math.round(n * 1000) / 10)
}

function weightToDisplayPct(w) {
  if (w == null || w === '') return '—'
  const n = Number(w)
  if (!Number.isFinite(n)) return '—'
  return String(Math.round(n * 1000) / 10)
}

Page({
  data: {
    courseId: null,
    course: null,
    loading: false,
    user: null,
    isTeacher: false,
    editing: false,
    saving: false,
    editForm: {
      name: '',
      intro: '',
      category: '',
      assignmentPct: '70',
      checkinPct: '20',
      resourcePct: '10',
      examPct: '0'
    },
    courseGradeAssignPct: '',
    courseGradeCheckinPct: '',
    courseGradeResourcePct: '',
    courseGradeExamPct: '',
    finalScoreText: ''
  },
  onLoad(options) {
    // 课程ID 可能是后端 long，JS Number 会丢精度，必须用字符串传递/存储
    const id = options.id != null ? String(options.id).trim() : ''
    if (!id) {
      wx.showToast({ title: '缺少课程ID', icon: 'none' })
      return
    }
    const user = getUser()
    this.setData({
      courseId: id,
      user,
      isTeacher: !!(user && user.roleCode === 'TEACHER')
    })
    this.loadCourse()
  },
  async loadCourse() {
    if (!this.data.courseId) return
    this.setData({ loading: true })
    try {
      const path = this.data.isTeacher
        ? `/api/teacher/courses/${this.data.courseId}`
        : `/api/student/courses/${this.data.courseId}`
      const c = await request(path, 'GET')
      this.setData({
        course: c || null,
        courseGradeAssignPct: weightToDisplayPct(c && c.gradeAssignmentWeight),
        courseGradeCheckinPct: weightToDisplayPct(c && c.gradeCheckinWeight),
        courseGradeResourcePct: weightToDisplayPct(c && c.gradeResourceWeight),
        courseGradeExamPct: weightToDisplayPct(c && c.gradeExamWeight),
        editForm: {
          name: (c && c.name) || '',
          intro: (c && c.intro) || '',
          category: (c && c.category) || '',
          assignmentPct: weightToPctInput(c && c.gradeAssignmentWeight),
          checkinPct: weightToPctInput(c && c.gradeCheckinWeight),
          resourcePct: weightToPctInput(c && c.gradeResourceWeight),
          examPct: weightToPctInput(c && c.gradeExamWeight)
        }
      })
      if (!this.data.isTeacher) {
        this.loadStudentFinalScore()
      }
    } catch (e) {
      wx.showToast({ title: (e && e.message) ? e.message : '加载课程失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },
  async loadStudentFinalScore() {
    if (this.data.isTeacher || !this.data.courseId) return
    try {
      const res = await request(`/api/student/grades/final?courseId=${encodeURIComponent(String(this.data.courseId))}`, 'GET')
      const grade = res && res.grade ? res.grade : null
      const finalScore = grade && grade.finalScore != null ? grade.finalScore : null
      this.setData({ finalScoreText: finalScore != null ? String(finalScore) : '教师未评分' })
    } catch (e) {
      this.setData({ finalScoreText: '教师未评分' })
    }
  },
  goResources() {
    wx.navigateTo({
      url: `/pages/course/resources/resources?courseId=${this.data.courseId}`
    })
  },
  goExams() {
    wx.navigateTo({
      url: `/pages/course/exams/exams?courseId=${this.data.courseId}`
    })
  },
  goAssignments() {
    wx.navigateTo({
      url: `/pages/course/assignments/assignments?courseId=${this.data.courseId}`
    })
  },
  goGrade() {
    wx.navigateTo({
      url: `/pages/course/grade/grade?courseId=${this.data.courseId}`
    })
  },
  goCheckin() {
    wx.navigateTo({
      url: `/pages/course/checkin/checkin?courseId=${this.data.courseId}`
    })
  },
  goMembers() {
    wx.navigateTo({
      url: `/pages/course/members/members?courseId=${this.data.courseId}`
    })
  },
  goMonitor() {
    if (!this.data.isTeacher) return
    wx.navigateTo({
      url: `/pages/course/monitor/monitor?courseId=${this.data.courseId}`
    })
  },
  goRecommendations() {
    if (this.data.isTeacher) return
    wx.navigateTo({
      url: `/pages/course/recommendations/recommendations?courseId=${this.data.courseId}`
    })
  },
  toggleEdit() {
    this.setData({ editing: !this.data.editing })
  },
  onEditNameInput(e) {
    this.setData({ 'editForm.name': (e.detail.value || '').trim() })
  },
  onEditIntroInput(e) {
    this.setData({ 'editForm.intro': (e.detail.value || '').trim() })
  },
  onEditCategoryInput(e) {
    this.setData({ 'editForm.category': (e.detail.value || '').trim() })
  },
  onEditAssignPctInput(e) {
    this.setData({ 'editForm.assignmentPct': (e.detail.value || '').trim() })
  },
  onEditCheckinPctInput(e) {
    this.setData({ 'editForm.checkinPct': (e.detail.value || '').trim() })
  },
  onEditResourcePctInput(e) {
    this.setData({ 'editForm.resourcePct': (e.detail.value || '').trim() })
  },
  onEditExamPctInput(e) {
    this.setData({ 'editForm.examPct': (e.detail.value || '').trim() })
  },
  async saveCourse() {
    if (!this.data.isTeacher || !this.data.courseId) return
    const ef = this.data.editForm || {}
    const payload = {
      name: (ef.name || '').trim(),
      intro: (ef.intro || '').trim(),
      category: (ef.category || '').trim()
    }
    if (!payload.name) {
      wx.showToast({ title: '请输入课程名称', icon: 'none' })
      return
    }
    const ap = ef.assignmentPct != null && String(ef.assignmentPct).trim() !== '' ? Number(ef.assignmentPct) : null
    const cp = ef.checkinPct != null && String(ef.checkinPct).trim() !== '' ? Number(ef.checkinPct) : null
    const rp = ef.resourcePct != null && String(ef.resourcePct).trim() !== '' ? Number(ef.resourcePct) : null
    const ep = ef.examPct != null && String(ef.examPct).trim() !== '' ? Number(ef.examPct) : null
    if (ap == null || cp == null || rp == null || ep == null || [ap, cp, rp, ep].some((n) => Number.isNaN(n) || n < 0)) {
      wx.showToast({ title: '请填写有效的四项总评占比（0～100）', icon: 'none' })
      return
    }
    if (Math.abs(ap + cp + rp + ep - 100) > 0.5) {
      wx.showToast({ title: '四项占比之和须为 100', icon: 'none' })
      return
    }
    payload.assignmentWeight = ap / 100
    payload.checkinWeight = cp / 100
    payload.resourceWeight = rp / 100
    payload.examWeight = ep / 100
    this.setData({ saving: true })
    try {
      const updated = await request(`/api/teacher/courses/${this.data.courseId}`, 'PUT', payload)
      const u = updated || this.data.course
      this.setData({
        course: u,
        courseGradeAssignPct: weightToDisplayPct(u && u.gradeAssignmentWeight),
        courseGradeCheckinPct: weightToDisplayPct(u && u.gradeCheckinWeight),
        courseGradeResourcePct: weightToDisplayPct(u && u.gradeResourceWeight),
        courseGradeExamPct: weightToDisplayPct(u && u.gradeExamWeight),
        editing: false
      })
      wx.showToast({ title: '保存成功', icon: 'success' })
    } catch (e) {
      wx.showToast({ title: (e && e.message) ? e.message : '保存失败', icon: 'none' })
    } finally {
      this.setData({ saving: false })
    }
  },
  deleteCourse() {
    if (!this.data.isTeacher || !this.data.courseId) return
    wx.showModal({
      title: '删除课程',
      content: '删除后课程将不可见，是否继续？',
      confirmColor: '#ff4d4f',
      success: async (res) => {
        if (!res.confirm) return
        try {
          await request(`/api/teacher/courses/${this.data.courseId}`, 'DELETE', {})
          wx.showToast({ title: '删除成功', icon: 'success' })
          setTimeout(() => {
            wx.reLaunch({ url: '/pages/index/index' })
          }, 500)
        } catch (e) {
          wx.showToast({ title: (e && e.message) ? e.message : '删除失败', icon: 'none' })
        }
      }
    })
  }
})

