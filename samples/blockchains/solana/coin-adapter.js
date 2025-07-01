// Coin Adapter 추상 클래스
// 모든 블록체인 지갑이 구현해야 하는 공통 인터페이스

class CoinAdapter {
  constructor(config) {
    if (this.constructor === CoinAdapter) {
      throw new Error("CoinAdapter는 추상 클래스입니다. 직접 인스턴스화할 수 없습니다.");
    }
    this.config = config;
  }

  /* ================================================================
   * 1. 지갑 생성 및 관리
   * ================================================================ */
  
  /**
   * 새 지갑 생성
   * @returns {Promise<{address: string, privateKey: string, mnemonic?: string}>}
   */
  async generateWallet() {
    throw new Error("generateWallet() 메서드를 구현해야 합니다.");
  }

  /**
   * 니모닉으로 지갑 복구
   * @param {string} mnemonic - 니모닉 구문
   * @returns {Promise<{address: string, privateKey: string}>}
   */
  async importFromMnemonic(mnemonic) {
    throw new Error("importFromMnemonic() 메서드를 구현해야 합니다.");
  }

  /**
   * 개인키로 지갑 복구
   * @param {string} privateKey - 개인키
   * @returns {Promise<{address: string}>}
   */
  async importFromPrivateKey(privateKey) {
    throw new Error("importFromPrivateKey() 메서드를 구현해야 합니다.");
  }

  /**
   * 주소 유효성 검증
   * @param {string} address - 검증할 주소
   * @returns {boolean}
   */
  isValidAddress(address) {
    throw new Error("isValidAddress() 메서드를 구현해야 합니다.");
  }

  /* ================================================================
   * 2. 잔액 조회
   * ================================================================ */
  
  /**
   * 주소의 잔액 조회
   * @param {string} address - 조회할 주소
   * @returns {Promise<string>} - 잔액 (최소 단위)
   */
  async getBalance(address) {
    throw new Error("getBalance() 메서드를 구현해야 합니다.");
  }

  /**
   * 잔액을 사람이 읽기 쉬운 형식으로 변환
   * @param {string} balance - 최소 단위 잔액
   * @returns {string} - 변환된 잔액
   */
  formatBalance(balance) {
    const decimals = this.config.decimals || 18;
    const value = Number(balance) / Math.pow(10, decimals);
    return value.toFixed(4);
  }

  /* ================================================================
   * 3. 트랜잭션 처리
   * ================================================================ */
  
  /**
   * 트랜잭션 전송
   * @param {Object} params - 트랜잭션 파라미터 (코인별로 다를 수 있음)
   * @returns {Promise<{hash: string}>}
   * 
   * @example
   * // Bitcoin (UTXO)
   * sendTransaction({
   *   from: "bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh",
   *   to: "bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh", 
   *   amount: "0.001",
   *   privateKey: "L1234...",
   *   utxos: [...],  // UTXO 목록
   *   feeRate: 10    // sat/byte
   * })
   * 
   * @example
   * // Ethereum (Account)
   * sendTransaction({
   *   from: "0x123...",
   *   to: "0x456...",
   *   amount: "1.5",
   *   privateKey: "0xabc...",
   *   gasPrice: "20000000000",  // wei
   *   gasLimit: 21000,
   *   nonce: 5,
   *   data: "0x"
   * })
   * 
   * @example
   * // Solana
   * sendTransaction({
   *   from: publicKey,
   *   to: "recipientPublicKey",
   *   amount: "0.1",
   *   keypair: Keypair,  // Solana는 Keypair 객체 사용
   *   recentBlockhash: "...",
   *   memo: "Payment for coffee"
   * })
   */
  async sendTransaction(params) {
    throw new Error("sendTransaction() 메서드를 구현해야 합니다.");
  }

  /**
   * 트랜잭션 상태 조회
   * @param {string} txHash - 트랜잭션 해시
   * @returns {Promise<{status: string, confirmations: number}>}
   */
  async getTransactionStatus(txHash) {
    throw new Error("getTransactionStatus() 메서드를 구현해야 합니다.");
  }


  /* ================================================================
   * 4. 수수료 관련
   * ================================================================ */
  
  /**
   * 현재 네트워크 수수료 조회
   * @returns {Promise<{low: string, medium: string, high: string}>}
   */
  async getGasPrice() {
    throw new Error("getGasPrice() 메서드를 구현해야 합니다.");
  }

  /**
   * 트랜잭션 수수료 예상
   * @param {Object} txParams - 트랜잭션 파라미터
   * @returns {Promise<string>} - 예상 수수료
   */
  async estimateFee(txParams) {
    throw new Error("estimateFee() 메서드를 구현해야 합니다.");
  }

  /* ================================================================
   * 5. 유틸리티 메서드
   * ================================================================ */
  
  /**
   * 금액을 최소 단위로 변환
   * @param {string} amount - 사람이 읽는 단위
   * @returns {string} - 최소 단위
   */
  parseAmount(amount) {
    const decimals = this.config.decimals || 18;
    const value = parseFloat(amount) * Math.pow(10, decimals);
    return value.toString();
  }

  /**
   * 주소 축약 표시
   * @param {string} address - 전체 주소
   * @param {number} [chars=4] - 표시할 문자 수
   * @returns {string} - 축약된 주소
   */
  shortenAddress(address, chars = 4) {
    if (!address) return '';
    return `${address.slice(0, chars + 2)}...${address.slice(-chars)}`;
  }

  /**
   * 현재 시간 타임스탬프
   * @returns {number}
   */
  getCurrentTimestamp() {
    return Math.floor(Date.now() / 1000);
  }
}

// 내보내기
if (typeof module !== 'undefined' && module.exports) {
  module.exports = CoinAdapter;
} else {
  window.CoinAdapter = CoinAdapter;
}