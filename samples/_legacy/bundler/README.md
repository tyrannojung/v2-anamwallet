# Blockchain Library Bundlers

이 디렉토리는 브라우저 환경에서 블록체인 라이브러리를 사용하기 위한 번들러들을 포함합니다.

## 왜 번들링이 필요한가?

대부분의 블록체인 라이브러리는 Node.js 환경을 위해 설계되었습니다. 이들은 다음과 같은 이유로 브라우저에서 직접 사용할 수 없습니다:

1. **Node.js 전용 모듈 의존성**: `crypto`, `buffer`, `stream` 등 Node.js 내장 모듈 사용
2. **CommonJS 모듈 시스템**: 브라우저는 ES 모듈만 지원
3. **웹 호환성 문제**: WebAssembly, Worker 등 특수한 브라우저 API 필요
4. **파일 크기**: 수많은 의존성으로 인한 큰 파일 크기

## 번들링 프로세스

### 1. 설치
```bash
cd [coin]-bundler
npm install
```

### 2. 빌드
```bash
# 프로덕션 번들 생성
npm run build

# 개발 모드 (실시간 재빌드 + 테스트 페이지)
npm run dev
```

### 3. 배포
빌드된 번들 파일을 blockchain 미니앱의 assets 디렉토리로 복사:
```bash
cp dist/[coin]-bundle.[format].js ../../blockchains/[coin]/assets/
```

## 번들 형식

각 번들러는 다음 형식 중 하나로 빌드됩니다:

- **UMD (Universal Module Definition)**: 다양한 환경 지원
- **IIFE (Immediately Invoked Function Expression)**: 전역 변수로 노출

## 공통 설정

### Vite 설정 (`vite.config.js`)
```javascript
{
  build: {
    lib: {
      entry: './src/index.js',
      name: 'LibraryName',
      fileName: 'bundle-name',
      formats: ['umd'] // 또는 ['iife']
    },
    rollupOptions: {
      output: {
        globals: {
          // 외부 라이브러리 전역 변수 매핑
        }
      }
    }
  },
  resolve: {
    alias: {
      // Node.js polyfill 설정
    }
  },
  define: {
    global: 'globalThis',
    'process.env.NODE_ENV': '"production"'
  }
}
```

### Polyfill 처리
브라우저에서 Node.js API를 사용하기 위한 polyfill:
- `buffer`: 바이너리 데이터 처리
- `crypto`: 암호화 함수
- `stream`: 스트림 처리
- `process`: 환경 변수 등

## 현재 번들러 목록

### Bitcoin (`bitcoin-bundler/`)
- 라이브러리: bitcoinjs-lib, bip39, bip32, ecpair
- 전역 변수: `window.BitcoinJS`
- 주요 기능: HD 지갑, 니모닉, 트랜잭션 생성

### Solana (`solana-bundler/`)
- 라이브러리: @solana/web3.js
- 전역 변수: `window.solanaWeb3`
- 주요 기능: 키페어 생성, 트랜잭션, RPC 연결

## 문제 해결

### 번들이 로드되지 않을 때
1. 브라우저 콘솔에서 에러 확인
2. 파일 경로가 올바른지 확인
3. 번들 형식(UMD/IIFE)이 맞는지 확인

### 함수를 찾을 수 없을 때
1. 전역 변수명 확인 (예: `window.BitcoinJS` vs `window.bitcoin`)
2. 번들에 해당 기능이 포함되었는지 확인
3. 번들 빌드가 최신인지 확인

### 브라우저 호환성
- 최신 브라우저 필요 (Chrome, Safari, Firefox)
- WebAssembly 지원 필요 (일부 라이브러리)

## 새로운 번들러 추가하기

1. 새 디렉토리 생성: `[coin]-bundler/`
2. `package.json` 생성 및 의존성 추가
3. `vite.config.js` 설정 (위 공통 설정 참고)
4. `src/index.js`에 번들링할 내용 정의
5. 테스트 페이지 작성 (`test.html`)
6. 빌드 및 테스트

## 참고사항

- 번들 크기를 최소화하기 위해 필요한 모듈만 포함
- Tree shaking을 활용하여 사용하지 않는 코드 제거
- 민감한 정보(개인키 등)는 절대 번들에 포함하지 않음
- 각 코인별 특수한 요구사항은 개별 번들러에서 처리