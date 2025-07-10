// Coin 설정 파일
// 이 파일을 복사한 후 각 코인에 맞게 수정하세요

const CoinConfig = {
  // 기본 정보
  name: "COIN_NAME",              // 예: "Bitcoin"
  symbol: "SYMBOL",               // 예: "BTC"
  decimals: 18,                   // 소수점 자릿수
  
  // 네트워크 설정
  network: {
    // QuickNode RPC 엔드포인트
    rpcEndpoint: "https://YOUR-ENDPOINT.NETWORK.quiknode.pro/YOUR-TOKEN/",
    // 네트워크 이름
    networkName: "testnet",       // 예: "mainnet", "testnet"
    // 체인 ID (EVM 체인의 경우)
    chainId: 1,
  },
  
  // UI 테마 설정
  theme: {
    primaryColor: "#4338CA",      // 메인 색상
    secondaryColor: "#6366F1",    // 보조 색상
    logoSymbol: "Ω",              // 로고 심볼
    logoText: "COIN",             // 로고 텍스트
  },
  
  // 주소 설정
  address: {
    // 주소 형식 정규식 (검증용)
    regex: /^0x[a-fA-F0-9]{40}$/,
    // 주소 표시 형식
    displayFormat: "0x...",       // 예: "0x...", "bc1...", etc.
  },
  
  // 트랜잭션 설정
  transaction: {
    // 기본 가스비/수수료
    defaultFee: "0.0001",
    // 최소 전송 금액
    minAmount: "0.000001",
    // 확인 대기 시간 (ms)
    confirmationTime: 15000,
  },
  
  // 기타 옵션
  options: {
    // 니모닉 지원 여부
    supportsMnemonic: true,
    // 토큰 지원 여부
    supportsTokens: false,
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