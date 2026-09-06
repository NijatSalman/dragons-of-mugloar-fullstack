/// <reference types="vitest/config" />
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Vite builds the app; in development it proxies /api to the Spring backend so the browser sees one origin.
// Override the backend address with BACKEND_URL when 8080 is taken, e.g. BACKEND_URL=http://localhost:8089 npm run dev
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': process.env.BACKEND_URL ?? 'http://localhost:8080',
    },
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: './src/test/setup.ts',
  },
})
