import axios from 'axios'

const http = axios.create({
  baseURL: '/',
  timeout: 20000
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('musicplatform_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export default http
