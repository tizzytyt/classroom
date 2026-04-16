export function downloadBlob(blob, filename) {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename || 'export.xls'
  document.body.appendChild(a)
  a.click()
  a.remove()
  URL.revokeObjectURL(url)
}

export function filenameFromContentDisposition(contentDisposition) {
  if (!contentDisposition) return ''
  const cd = String(contentDisposition)
  const utf8 = cd.match(/filename\*\s*=\s*UTF-8''([^;]+)/i)
  if (utf8 && utf8[1]) {
    try {
      return decodeURIComponent(utf8[1].replace(/"/g, '').trim())
    } catch {
      return utf8[1].replace(/"/g, '').trim()
    }
  }
  const ascii = cd.match(/filename\s*=\s*("?)([^";]+)\1/i)
  if (ascii && ascii[2]) return ascii[2].trim()
  return ''
}

