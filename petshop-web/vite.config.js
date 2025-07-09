// vite.config.js
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver({
        importStyle: 'css'
      })],
      imports: ['vue', 'vue-router']
    }),
    Components({
      resolvers: [
        ElementPlusResolver({
          components:['ElLoading'],
          importStyle: 'css',
          directives: true
        })
      ]
    })
  ],
  resolve: {
    alias: {
      // 路径别名配置（Windows 系统需要处理路径分隔符）
      '@': path.resolve(__dirname, 'src').replace(/\\/g, '/'),
    }
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // 后端地址
        changeOrigin: true,              // 允许跨域
        rewrite: (path) => path
      },
      // 新增图片路径代理
      '/uploads': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      }
    }
  },


})