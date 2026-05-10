// #ifdef H5
const defaultApiBase = ''
// #endif
// #ifndef H5
const defaultApiBase = 'http://127.0.0.1:8080'
// #endif

const envBase = import.meta.env.VITE_API_BASE
export const API_BASE =
  envBase !== undefined && envBase !== null && envBase !== '' ? envBase : defaultApiBase

/**
 * @param {{ url: string, method?: string, data?: object, header?: object }} opts
 * @returns {Promise<{ statusCode: number, data: any }>}
 */
export function request(opts) {
  const url = opts.url.startsWith('http') ? opts.url : `${API_BASE}${opts.url}`
  return new Promise((resolve, reject) => {
    uni.request({
      url,
      method: opts.method || 'GET',
      data: opts.data,
      header: {
        'Content-Type': 'application/json',
        ...opts.header,
      },
      success: (res) => resolve(res),
      fail: (err) => reject(err),
    })
  })
}

export async function getJson(url, query) {
  const qs =
    query && Object.keys(query).length
      ? `?${Object.entries(query)
          .filter(([, v]) => v !== undefined && v !== null && v !== '')
          .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(String(v))}`)
          .join('&')}`
      : ''
  const res = await request({ url: `${url}${qs}`, method: 'GET' })
  if (res.statusCode >= 400) {
    throw new Error(`HTTP ${res.statusCode}`)
  }
  return res.data
}

export async function postJson(url, body) {
  const res = await request({ url, method: 'POST', data: body })
  if (res.statusCode >= 400) {
    throw new Error(`HTTP ${res.statusCode}`)
  }
  return res.data
}
