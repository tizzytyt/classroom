function pad2(n) {
  return String(n).padStart(2, '0')
}

/** 与后端 LocalDateTime 序列化兼容：字符串 / 数组 [y,m,d,h,mi,s] */
function isPastDueAt(dueAt) {
  if (dueAt == null || dueAt === '') return false
  if (Array.isArray(dueAt) && dueAt.length >= 6) {
    const y = dueAt[0]
    const mo = dueAt[1]
    const d = dueAt[2]
    const h = dueAt[3]
    const mi = dueAt[4]
    const se = dueAt[5] != null ? dueAt[5] : 0
    const s = `${y}-${pad2(mo)}-${pad2(d)} ${pad2(h)}:${pad2(mi)}:${pad2(se)}`
    const ms = new Date(s.replace(/-/g, '/')).getTime()
    return Number.isFinite(ms) && ms <= Date.now()
  }
  let s = String(dueAt)
  s = s.includes('T') ? s.replace('T', ' ').split('.')[0] : s
  const ms = new Date(s.replace(/-/g, '/')).getTime()
  return Number.isFinite(ms) && ms <= Date.now()
}

module.exports = {
  isPastDueAt
}
