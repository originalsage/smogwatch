import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'

/**
 * H5 下若 __UNI_FEATURE_PAGES__ 被误判为 false，则不会 initRouter，
 * navigateTo / navigator 会报 Cannot read properties of undefined (reading 'push')。
 * 在 uni 插件之后强制开启多页特性（与 pages.json 中多页面配置一致）。
 */
function forceUniMultiPageRouter() {
  return {
    name: 'smogwatch-force-uni-multi-page',
    enforce: 'post',
    config() {
      return {
        define: {
          __UNI_FEATURE_PAGES__: JSON.stringify(true),
        },
      }
    },
  }
}

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [uni(), forceUniMultiPageRouter()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
      },
    },
  },
})
