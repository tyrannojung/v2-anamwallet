import { defineConfig } from 'vite';
import { nodePolyfills } from 'vite-plugin-node-polyfills';

export default defineConfig({
  define: {
    global: 'globalThis',
  },
  plugins: [
    nodePolyfills({
      // Bitcoin에 필요한 Node.js polyfill
      include: ['buffer', 'process', 'util', 'stream', 'events', 'crypto'],
      globals: {
        Buffer: true,
        process: true,
        global: true
      },
      protocolImports: true
    })
  ],
  build: {
    lib: {
      entry: './src/index.js',
      name: 'BitcoinJS',
      fileName: 'bitcoin-bundle',
      formats: ['iife']
    },
    rollupOptions: {
      // 외부 의존성 제외 (번들에 포함시킬 것)
      external: [],
      output: {
        // 전역 변수 설정
        globals: {},
        // 번들 출력 디렉토리
        dir: 'dist'
      }
    },
    // 압축 설정
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: false  // console.log 유지 (디버깅용)
      }
    },
    // 출력 디렉토리
    outDir: 'dist',
    // 기존 파일 정리
    emptyOutDir: true
  },
  // 개발 서버 설정 (테스트용)
  server: {
    open: '/test.html'
  }
});