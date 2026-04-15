/**
 * 后端 /api/files 需 JWT；video 组件与部分环境下的 downloadFile 无法可靠携带 Header，
 * 通过 URL 查询参数传递 token（与 JwtAuthInterceptor 约定一致）。
 */
function withFileAccessToken(url) {
  if (!url || String(url).indexOf('/api/files/') === -1) return url
  try {
    const raw = String(url)
    const basePart = raw.split('#')[0]
    const qMark = basePart.indexOf('?')
    const pathPart = qMark >= 0 ? basePart.slice(0, qMark) : basePart
    const fileName = fileNameFromUrl(pathPart)
    if (!fileName) return url
    const absolutePrefix = pathPart.startsWith('http')
      ? pathPart.slice(0, pathPart.indexOf('/api/files/'))
      : ''
    // /api/files 已放行，视频播放优先使用短路径，避免查询串在部分环境触发媒体加载异常
    return absolutePrefix + '/api/files/' + encodeURIComponent(fileName)
  } catch (e) {
    return url
  }
}

/** 从 /api/files/xxx.log 或完整 URL 取文件名，避免列表里 fileName 为空时无法判断类型 */
function fileNameFromUrl(url) {
  if (!url) return ''
  try {
    const path = String(url).split('#')[0].split('?')[0]
    const i = path.lastIndexOf('/')
    return (i >= 0 ? path.slice(i + 1) : path).trim()
  } catch (e) {
    return ''
  }
}

/** wx.openDocument 不支持 .txt/.log 等，按扩展名与 MIME 识别纯文本并走内置页预览 */
const TEXT_BY_EXT = ['.txt', '.log', '.md', '.csv', '.json', '.xml', '.cfg', '.ini',
  '.properties', '.yaml', '.yml', '.sh', '.bat', '.c', '.h', '.java', '.py',
  '.js', '.ts', '.css', '.html', '.htm', '.vue', '.sql', '.gitignore']

function isPlainTextFile(fileName, fileType) {
  const t = (fileType || '').toLowerCase().trim()
  if (t.startsWith('text/')) return true
  if (t === 'application/json' || t === 'application/xml' || t === 'application/javascript') return true
  const n = (fileName || '').toLowerCase()
  return TEXT_BY_EXT.some(ext => n.endsWith(ext))
}

/** 临时路径常无扩展名时，供 wx.openDocument 使用 */
function openDocumentFileTypeHint(displayName) {
  const lower = (displayName || '').toLowerCase()
  const dot = lower.lastIndexOf('.')
  if (dot < 0) return ''
  const ext = lower.slice(dot)
  const map = {
    '.pdf': 'pdf',
    '.doc': 'doc',
    '.docx': 'docx',
    '.xls': 'xls',
    '.xlsx': 'xlsx',
    '.ppt': 'ppt',
    '.pptx': 'pptx'
  }
  return map[ext] || ''
}

function openDocumentWithHint(tempFilePath, displayName) {
  const hint = openDocumentFileTypeHint(displayName)
  return new Promise((resolve, reject) => {
    const doc = {
      filePath: tempFilePath,
      showMenu: true,
      success: () => resolve(),
      fail: (err) => reject(err || new Error('openDocument 失败'))
    }
    if (hint) doc.fileType = hint
    wx.openDocument(doc)
  })
}

const MAX_PREVIEW_CHARS = 180000

/**
 * downloadFile 成功后的临时路径：纯文本读入内存并跳转 text-preview；其余 openDocument。
 * @param {string} [sourceUrl] 资源 fileUrl（可带域名），fileName 为空时用来解析扩展名
 */
function previewDownloadedFile(tempFilePath, fileName, fileType, navTitle, sourceUrl) {
  const displayName = (fileName || '').trim() || fileNameFromUrl(sourceUrl || '')
  return new Promise((resolve, reject) => {
    if (!tempFilePath) {
      reject(new Error('缺少临时文件'))
      return
    }
    if (!isPlainTextFile(displayName, fileType)) {
      openDocumentWithHint(tempFilePath, displayName).then(resolve).catch(reject)
      return
    }
    wx.getFileSystemManager().readFile({
      filePath: tempFilePath,
      encoding: 'utf8',
      success: (r) => {
        const text = typeof r.data === 'string' ? r.data : ''
        if (text.length > MAX_PREVIEW_CHARS) {
          const head = '（内容过长，仅显示前 ' + MAX_PREVIEW_CHARS + ' 个字符）\n\n'
          text = head + text.slice(0, MAX_PREVIEW_CHARS)
        }
        const storageKey = 'txt_pv_' + Date.now() + '_' + Math.random().toString(36).slice(2, 10)
        try {
          wx.setStorageSync(storageKey, text)
        } catch (e) {
          const cut = text.slice(0, 40000)
          try {
            wx.setStorageSync(storageKey, cut)
          } catch (e2) {
            openDocumentWithHint(tempFilePath, displayName).then(resolve).catch(() => reject(e2))
            return
          }
        }
        const rawTitle = (navTitle || displayName || '文本预览').trim() || '文本预览'
        const q = 'storageKey=' + encodeURIComponent(storageKey)
          + '&title=' + encodeURIComponent(rawTitle.slice(0, 200))
        const pageUrl = '/pages/course/text-preview/text-preview?' + q
        const go = (api) => {
          api({
            url: pageUrl,
            success: () => resolve(),
            fail: (err) => {
              try { wx.removeStorageSync(storageKey) } catch (x) {}
              reject(err || new Error('跳转失败'))
            }
          })
        }
        wx.navigateTo({ url: pageUrl, success: () => resolve(), fail: () => go(wx.redirectTo) })
      },
      fail: () => {
        openDocumentWithHint(tempFilePath, displayName).then(resolve).catch((err) => reject(err || new Error('无法读取或打开')))
      }
    })
  })
}

module.exports = {
  withFileAccessToken,
  previewDownloadedFile
}
