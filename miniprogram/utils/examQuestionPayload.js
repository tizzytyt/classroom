/**
 * 教师端试卷题目：与后端 TeacherExamQuestionCreateRequest 一致
 * @returns {{ ok: true, payload: object } | { ok: false, msg: string }}
 */
function buildPayloadFromNewQ(n) {
  const stem = (n.stem || '').trim()
  if (!stem) {
    return { ok: false, msg: '请填写题干' }
  }
  const score = Number(n.score)
  const sortNo = n.sortNo === '' ? 0 : Number(n.sortNo)
  if (Number.isNaN(score) || score < 0) {
    return { ok: false, msg: '分值无效' }
  }
  const sn = Number.isNaN(sortNo) ? 0 : sortNo

  if (n.qType === 1) {
    const a = (n.optA || '').trim()
    const b = (n.optB || '').trim()
    const c = (n.optC || '').trim()
    const d = (n.optD || '').trim()
    if (!a || !b || !c || !d) {
      return { ok: false, msg: '请填写 A～D 四个选项' }
    }
    const ca = (n.correctAnswer || 'A').trim().toUpperCase()
    if (!['A', 'B', 'C', 'D'].includes(ca)) {
      return { ok: false, msg: '请选择正确答案' }
    }
    return {
      ok: true,
      payload: {
        qType: 1,
        stem,
        score,
        sortNo: sn,
        correctAnswer: ca,
        options: [
          { key: 'A', text: a },
          { key: 'B', text: b },
          { key: 'C', text: c },
          { key: 'D', text: d }
        ]
      }
    }
  }

  if (n.qType === 3) {
    const ca = (n.correctAnswer || 'T').toUpperCase() === 'F' ? 'F' : 'T'
    return {
      ok: true,
      payload: {
        qType: 3,
        stem,
        score,
        sortNo: sn,
        correctAnswer: ca,
        options: []
      }
    }
  }

  return { ok: false, msg: '题型错误' }
}

function qTypeLabel(t) {
  const m = { 1: '单选题', 2: '多选题', 3: '判断题', 4: '填空题', 5: '简答题' }
  return m[t] || '题目'
}

/** 从已保存的 payload 回填编辑表单 */
function payloadToNewQ(payload, defaults) {
  const p = payload || {}
  const base = {
    qType: p.qType === 3 ? 3 : 1,
    stem: p.stem || '',
    score: String(p.score != null ? p.score : (defaults && defaults.score) || '5'),
    sortNo: String(p.sortNo != null ? p.sortNo : (defaults && defaults.sortNo) || '1'),
    correctAnswer: p.correctAnswer || 'A',
    optA: '',
    optB: '',
    optC: '',
    optD: ''
  }
  if (p.qType === 1 && Array.isArray(p.options)) {
    p.options.forEach((o) => {
      if (!o || !o.key) return
      const k = String(o.key).toUpperCase()
      if (k === 'A') base.optA = o.text || ''
      if (k === 'B') base.optB = o.text || ''
      if (k === 'C') base.optC = o.text || ''
      if (k === 'D') base.optD = o.text || ''
    })
    base.correctAnswer = (p.correctAnswer || 'A').toUpperCase()
  }
  if (p.qType === 3) {
    base.correctAnswer = (p.correctAnswer || 'T').toUpperCase() === 'F' ? 'F' : 'T'
  }
  let correctIndex = ['A', 'B', 'C', 'D'].indexOf(base.correctAnswer)
  if (correctIndex < 0) correctIndex = 0
  const tfIndex = base.correctAnswer === 'F' ? 1 : 0
  return { newQ: base, correctIndex, tfIndex }
}

module.exports = {
  buildPayloadFromNewQ,
  qTypeLabel,
  payloadToNewQ
}
