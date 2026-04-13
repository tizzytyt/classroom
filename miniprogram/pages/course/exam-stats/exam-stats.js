const { request } = require('../../../utils/request')

function pct(n, d) {
  if (!d) return 0
  const v = Math.round((n / d) * 1000) / 10
  if (!Number.isFinite(v)) return 0
  return Math.max(0, Math.min(100, v))
}

Page({
  data: {
    courseId: '',
    paperId: '',
    paperTitle: '',
    loading: false,
    totalStudents: 0,
    joinedCount: 0,
    notJoinedCount: 0,
    participationRate: 0,
    scoreAvg: null,
    scoreMax: null,
    scoreMin: null,
    dist: {
      excellent: { label: '优秀 (≥90)', count: 0, percent: 0 },
      good: { label: '良好 (80-89)', count: 0, percent: 0 },
      pass: { label: '及格 (60-79)', count: 0, percent: 0 },
      fail: { label: '不及格 (<60)', count: 0, percent: 0 }
    },
    joinedList: [],
    notJoinedList: []
  },
  onLoad(options) {
    const cid = options.courseId != null ? String(options.courseId) : ''
    const pid = options.paperId != null ? String(options.paperId) : ''
    const title = options.paperTitle ? decodeURIComponent(options.paperTitle) : ''
    if (!cid || !pid) {
      wx.showToast({ title: '参数错误', icon: 'none' })
      return
    }
    this.setData({ courseId: cid, paperId: pid, paperTitle: title })
    if (title) wx.setNavigationBarTitle({ title: '考试统计' })
    this.loadStats()
  },
  async loadStats() {
    this.setData({ loading: true })
    try {
      const [studentsRaw, attemptsRaw] = await Promise.all([
        request(`/api/teacher/courses/${this.data.courseId}/students`, 'GET'),
        request(`/api/teacher/courses/${this.data.courseId}/exams/${encodeURIComponent(this.data.paperId)}/attempts`, 'GET')
      ])
      const students = Array.isArray(studentsRaw) ? studentsRaw : []
      const attempts = Array.isArray(attemptsRaw) ? attemptsRaw : []

      const joinedList = attempts.map((x) => ({
        ...x,
        attemptId: x.attemptId != null ? String(x.attemptId) : '',
        studentIdStr: x.studentId != null ? String(x.studentId) : ''
      }))

      // 成绩统计（以 totalScore 为准）
      const scores = joinedList
        .map((x) => (x && x.totalScore != null) ? Number(x.totalScore) : NaN)
        .filter((v) => Number.isFinite(v))
      let scoreAvg = null
      let scoreMax = null
      let scoreMin = null
      const dist = {
        excellent: { label: '优秀 (≥90)', count: 0, percent: 0 },
        good: { label: '良好 (80-89)', count: 0, percent: 0 },
        pass: { label: '及格 (60-79)', count: 0, percent: 0 },
        fail: { label: '不及格 (<60)', count: 0, percent: 0 }
      }
      if (scores.length) {
        let sum = 0
        scoreMax = scores[0]
        scoreMin = scores[0]
        for (const s of scores) {
          sum += s
          if (s > scoreMax) scoreMax = s
          if (s < scoreMin) scoreMin = s
          if (s >= 90) dist.excellent.count++
          else if (s >= 80) dist.good.count++
          else if (s >= 60) dist.pass.count++
          else dist.fail.count++
        }
        scoreAvg = Math.round((sum / scores.length) * 10) / 10
        dist.excellent.percent = pct(dist.excellent.count, scores.length)
        dist.good.percent = pct(dist.good.count, scores.length)
        dist.pass.percent = pct(dist.pass.count, scores.length)
        dist.fail.percent = pct(dist.fail.count, scores.length)
        scoreMax = Math.round(scoreMax * 10) / 10
        scoreMin = Math.round(scoreMin * 10) / 10
      }

      const joinedSet = {}
      joinedList.forEach((a) => {
        if (a.studentIdStr) joinedSet[a.studentIdStr] = true
      })

      const notJoinedList = students.filter((s) => {
        const sid = s && s.id != null ? String(s.id) : ''
        if (!sid) return false
        return !joinedSet[sid]
      })

      const total = students.length
      const joined = joinedList.length
      const notJoined = notJoinedList.length
      const rate = pct(joined, total)

      this.setData({
        totalStudents: total,
        joinedCount: joined,
        notJoinedCount: notJoined,
        participationRate: rate,
        scoreAvg,
        scoreMax,
        scoreMin,
        dist,
        joinedList,
        notJoinedList
      })
    } catch (e) {
      wx.showToast({ title: (e && e.message) ? e.message : '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  }
})

