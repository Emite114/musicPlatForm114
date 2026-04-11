import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'node:path'

const target = process.env.VITE_API_TARGET ?? 'http://localhost:8080'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/user': target,
      '/song': target,
      '/post': target,
      '/message': target,
      '/report': target,
      '/admin': target,
      '/superAdmin': target,
      '/connect': target,
      '/media': target,
      '/default': target
    }
  }
})
