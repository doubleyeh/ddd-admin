import { useUserStore } from '@/store/user'
import router from '@/router'

const BASE_URL = '/api'

interface RestResponse<T> {
  code: number
  state: boolean
  message: string
  data: T
}

function handleResponse<T>(res: Response): Promise<T> {
  const store = useUserStore()

  if (res.status === 401 || res.status === 403) {
    store.logout()
    router.push('/login')
    throw new Error('认证失败或会话过期，请重新登录')
  }
  
  if (!res.ok) {
    throw new Error(`网络请求失败，状态码: ${res.status}`)
  }

  return res.json().then((json: RestResponse<T>) => {
    if (json.state) {
      return json.data
    } else {
      throw new Error(json.message || '操作失败')
    }
  }).catch(e => {
    throw new Error('API 响应解析失败，后端可能返回了非 JSON 格式')
  })
}

function createRequestOptions(method: string, body?: any): RequestInit {
  const store = useUserStore()
  const headers = new Headers({
    'Content-Type': 'application/json',
  })

  if (store.token) {
    headers.set('Authorization', `Bearer ${store.token}`)
  }
  if (store.tenantId) {
    headers.set('X-Tenant-Id', store.tenantId)
  }

  const options: RequestInit = {
    method,
    headers,
  }

  if (body) {
    options.body = JSON.stringify(body)
  }

  return options
}

const http = {
  get<T>(url: string): Promise<T> {
    const options = createRequestOptions('GET')
    return fetch(`${BASE_URL}${url}`, options).then(handleResponse<T>)
  },

  post<T>(url: string, body: any): Promise<T> {
    const options = createRequestOptions('POST', body)
    return fetch(`${BASE_URL}${url}`, options).then(handleResponse<T>)
  },

  put<T>(url: string, body: any): Promise<T> {
    const options = createRequestOptions('PUT', body)
    return fetch(`${BASE_URL}${url}`, options).then(handleResponse<T>)
  },

  delete<T>(url: string): Promise<T> {
    const options = createRequestOptions('DELETE')
    return fetch(`${BASE_URL}${url}`, options).then(handleResponse<T>)
  },
}

export { http }