// Coin 설정 파일
// 이 파일을 복사한 후 각 코인에 맞게 수정하세요

const CoinConfig = {
  // 기본 정보
  name: "Solana",
  symbol: "SOL",
  decimals: 9,                    // Solana는 9 소수점 자릿수
  
  // 네트워크 설정
  network: {
    // QuickNode Solana Testnet RPC 엔드포인트
    rpcEndpoint: "https://methodical-few-slug.solana-testnet.quiknode.pro/ced6f6658c56f53433e198c2124918a0e6dd6b0d",
    // 네트워크 이름
    networkName: "testnet",
    // Solana는 chainId 대신 cluster 사용
    cluster: "testnet",
  },
  
  // UI 테마 설정
  theme: {
    primaryColor: "#14F195",      // Solana 그린
    secondaryColor: "#9945FF",    // Solana 퍼플
    logoSymbol: "◎",              // Solana 로고 심볼
    logoText: "Solana",
  },
  
  // 주소 설정
  address: {
    // Solana 주소 형식 (Base58)
    regex: /^[1-9A-HJ-NP-Za-km-z]{32,44}$/,
    // 주소 표시 형식
    displayFormat: "...xxx",
  },
  
  // 트랜잭션 설정
  transaction: {
    // 기본 트랜잭션 수수료 (SOL)
    defaultFee: "0.00001",
    // 최소 전송 금액
    minAmount: "0.000000001",
    // 확인 대기 시간 (ms)
    confirmationTime: 30000,
  },
  
  // 기타 옵션
  options: {
    // 니모닉 지원 여부
    supportsMnemonic: true,
    // 토큰 지원 여부 (SPL 토큰)
    supportsTokens: true,
    // QR 코드 지원
    supportsQRCode: true,
  }
};

// 설정 내보내기
if (typeof module !== 'undefined' && module.exports) {
  module.exports = CoinConfig;
} else {
  window.CoinConfig = CoinConfig;
}