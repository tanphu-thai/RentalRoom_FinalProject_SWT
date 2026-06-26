const API_BASE = '/api'

export async function api(path, { method = 'GET', body, token } = {}) {
  const headers = { 'Content-Type': 'application/json' }
  const authToken = token ?? localStorage.getItem('rrms_token')
  if (authToken) headers.Authorization = `Bearer ${authToken}`

  const response = await fetch(`${API_BASE}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined
  })
  const data = await response.json().catch(() => ({}))
  if (!response.ok || data.success === false) {
    throw new Error(data.message || 'Request failed.')
  }
  return data.data
}

export const get = (path) => api(path)
export const post = (path, body) => api(path, { method: 'POST', body })
export const put = (path, body) => api(path, { method: 'PUT', body })
export const del = (path) => api(path, { method: 'DELETE' })
