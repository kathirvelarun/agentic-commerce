// Safely parse dates from Spring Boot LocalDateTime.
// Handles: ISO strings, arrays [y,m,d,h,min,s], null/undefined.
function safeDate(val) {
  if (!val) return null
  if (Array.isArray(val)) {
    const [y, m, d, h = 0, min = 0, s = 0] = val
    const dt = new Date(y, m - 1, d, h, min, s)
    return isNaN(dt.getTime()) ? null : dt
  }
  const dt = new Date(val)
  return isNaN(dt.getTime()) ? null : dt
}

export function fmtTime(val) {
  const d = safeDate(val)
  return d ? d.toLocaleTimeString() : '—'
}

export function fmtDateTime(val) {
  const d = safeDate(val)
  return d ? d.toLocaleString() : '—'
}

export function fmtDate(val, opts = { day: '2-digit', month: 'short', year: 'numeric' }) {
  const d = safeDate(val)
  return d ? d.toLocaleDateString(undefined, opts) : '—'
}