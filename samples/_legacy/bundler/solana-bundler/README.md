# Solana Web3.js Bundler

이 프로젝트는 Solana Web3.js를 브라우저에서 사용할 수 있도록 번들링합니다.

## 설치 및 빌드

```bash
# 1. 의존성 설치
npm install

# 2. 번들 빌드
npm run build

# 3. 테스트 (선택사항)
npm run dev
```

## 빌드 결과

빌드가 완료되면 `dist/solana-bundle.umd.js` 파일이 생성됩니다.

## 사용 방법

1. 빌드된 파일을 blockchain 미니앱으로 복사:

```bash
cp dist/solana-bundle.umd.js ../../blockchains/solana/dist/
```

2. HTML에서 사용:

```html
<script src="../../dist/solana-bundle.umd.js"></script>
<script>
  // window.solanaWeb3로 접근
  const keypair = window.solanaWeb3.Keypair.generate();
</script>
```

## 포함된 모듈

- Connection
- Keypair
- PublicKey
- Transaction
- SystemProgram
- LAMPORTS_PER_SOL
- sendAndConfirmTransaction
- clusterApiUrl

## 특징

- Buffer, Process 등 Node.js polyfill 자동 포함
- Tree shaking으로 최적화
- UMD 형식으로 다양한 환경 지원
