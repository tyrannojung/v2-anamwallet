// ================================================================
// COIN 미니앱 전역 설정 및 초기화
// ================================================================

// Coin 설정
const CoinConfig = {
  // 기본 정보
  name: "Solana",
  symbol: "SOL",
  decimals: 9,

  // 네트워크 설정
  network: {
    // QuickNode RPC 엔드포인트
    rpcEndpoint:
      "https://methodical-few-slug.solana-testnet.quiknode.pro/ced6f6658c56f53433e198c2124918a0e6dd6b0d",
    // 네트워크 이름
    networkName: "testnet", // 예: "mainnet", "testnet"
    // Solana는 chainId 대신 cluster 사용
    cluster: "testnet",
  },

  // UI 테마 설정
  theme: {
    primaryColor: "#14F195", // 메인 색상 (Solana Green)
    secondaryColor: "#9945FF", // 보조 색상 (Solana Purple)
    logoText: "Solana", // 로고 텍스트
  },

  // 주소 설정
  address: {
    // 주소 형식 정규식 (검증용)
    regex: /^[1-9A-HJ-NP-Za-km-z]{32,44}$/,
    // 주소 표시 형식
    displayFormat: "...", // Base58 형식
  },

  // 트랜잭션 설정
  transaction: {
    // 기본 가스비/수수료
    defaultFee: "0.000005", // 5000 lamports
    // 최소 전송 금액
    minAmount: "0.000001",
    // 확인 대기 시간 (ms)
    confirmationTime: 15000,
  },

  // 기타 옵션
  options: {
    // 니모닉 지원 여부
    supportsMnemonic: true,
    // 토큰 지원 여부 (SPL 토큰)
    supportsTokens: true,
    // QR 코드 지원
    supportsQRCode: true,
  },
};

// Coin Adapter 추상 클래스
// 모든 블록체인 지갑이 구현해야 하는 공통 인터페이스
class CoinAdapter {
  constructor(config) {
    if (this.constructor === CoinAdapter) {
      throw new Error(
        "CoinAdapter는 추상 클래스입니다. 직접 인스턴스화할 수 없습니다."
      );
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
}

// ================================================================
// 미니앱 생명주기 정의
// ================================================================

// 전역 앱 상태 관리
const AppState = {
  isInitialized: false,
  walletData: null,
  config: CoinConfig,
  adapter: null, // 실제 구현체에서 설정
};

// 미니앱 생명주기 핸들러
window.App = {
  // 앱 시작 시 호출 (최초 1회)
  onLaunch(options) {
    console.log("미니앱 시작:", options);

    this.initializeApp();

    this.loadWalletData();

    this.startNetworkMonitoring();
  },

  // 앱이 포그라운드로 전환될 때
  onShow(options) {
    console.log("미니앱 활성화:", options);

    if (AppState.walletData?.address) {
      this.refreshBalance();
    }

    this.checkNetworkStatus();
  },

  // 앱이 백그라운드로 전환될 때
  onHide() {
    console.log("미니앱 비활성화");
  },

  // 앱 오류 발생 시
  onError(error) {
    console.error("미니앱 오류:", error);
  },

  // ================================================================
  // 초기화 메서드
  // ================================================================

  initializeApp() {
    if (AppState.isInitialized) return;

    this.validateConfig();

    AppState.isInitialized = true;
  },

  validateConfig() {
    const required = ["name", "symbol", "network"];
    for (const field of required) {
      if (!CoinConfig[field]) {
        throw new Error(`필수 설정 누락: ${field}`);
      }
    }
  },

  // ================================================================
  // 데이터 관리
  // ================================================================

  loadWalletData() {
    try {
      const stored = localStorage.getItem("walletData");
      if (stored) {
        AppState.walletData = JSON.parse(stored);
      }
    } catch (e) {
      console.error("지갑 데이터 로드 실패:", e);
    }
  },

  saveWalletData(data) {
    try {
      AppState.walletData = data;
      localStorage.setItem("walletData", JSON.stringify(data));
    } catch (e) {
      console.error("지갑 데이터 저장 실패:", e);
    }
  },

  // ================================================================
  // 네트워크 관리
  // ================================================================

  startNetworkMonitoring() {
    console.log("네트워크 모니터링 시작");
  },

  checkNetworkStatus() {
    return true;
  },

  // ================================================================
  // 비즈니스 로직
  // ================================================================

  async refreshBalance() {
    if (!AppState.adapter || !AppState.walletData?.address) return;

    try {
      const balance = await AppState.adapter.getBalance(
        AppState.walletData.address
      );
      console.log("잔액 업데이트:", balance);
    } catch (e) {
      console.error("잔액 조회 실패:", e);
    }
  },
};

// ================================================================
// 전역 유틸리티 함수
// ================================================================

// 설정 접근자
window.getConfig = () => AppState.config;

// 어댑터 접근자
window.getAdapter = () => AppState.adapter;

// 어댑터 설정 (각 코인 구현체에서 호출)
window.setAdapter = (adapter) => {
  if (!(adapter instanceof CoinAdapter)) {
    throw new Error("올바른 CoinAdapter 인스턴스가 아닙니다.");
  }
  AppState.adapter = adapter;
};

// ================================================================
// 공통 유틸리티 함수
// ================================================================

// Toast 메시지 표시
window.showToast = (message, type = "info") => {
  const toast = document.createElement("div");
  toast.className = `toast toast-${type}`;
  toast.textContent = message;
  document.body.appendChild(toast);

  setTimeout(() => {
    toast.classList.add("show");
  }, 100);

  setTimeout(() => {
    toast.classList.remove("show");
    setTimeout(() => toast.remove(), 300);
  }, 3000);
};

// 잔액을 사람이 읽기 쉬운 형식으로 변환
window.formatBalance = (balance, decimals = 18) => {
  const value = Number(balance) / Math.pow(10, decimals);
  return value.toFixed(4);
};

// 금액을 최소 단위로 변환
window.parseAmount = (amount, decimals = 18) => {
  const value = parseFloat(amount) * Math.pow(10, decimals);
  return value.toString();
};

// 주소 축약 표시
window.shortenAddress = (address, chars = 4) => {
  if (!address) return "";
  return `${address.slice(0, chars + 2)}...${address.slice(-chars)}`;
};

// ================================================================
// Solana Adapter 구현
// ================================================================

// Solana 어댑터 구현
class SolanaAdapter extends CoinAdapter {
  constructor(config) {
    super(config);
    this.connection = null;
    this.solanaWeb3 = window.solanaWeb3;
  }

  // RPC 연결 초기화
  async initProvider() {
    if (!this.connection) {
      this.connection = new this.solanaWeb3.Connection(
        this.config.network.rpcEndpoint,
        "confirmed"
      );
    }
    return this.connection;
  }

  // 새 지갑 생성
  async generateWallet() {
    // 니모닉 생성
    const mnemonic = this.solanaWeb3.generateMnemonic();

    // 니모닉으로부터 키페어 생성
    const keypair = this.solanaWeb3.keypairFromMnemonic(mnemonic);

    return {
      address: keypair.publicKey.toString(),
      privateKey: Array.from(keypair.secretKey, (byte) =>
        byte.toString(16).padStart(2, "0")
      ).join(""),
      mnemonic: mnemonic,
      publicKey: keypair.publicKey.toString(),
    };
  }

  // 니모닉으로 지갑 복구
  async importFromMnemonic(mnemonic) {
    try {
      // 니모닉 유효성 검사
      if (!this.solanaWeb3.bip39.validateMnemonic(mnemonic)) {
        throw new Error("유효하지 않은 니모닉입니다");
      }

      // 니모닉으로부터 키페어 복구
      const keypair = this.solanaWeb3.keypairFromMnemonic(mnemonic);

      return {
        address: keypair.publicKey.toString(),
        privateKey: Array.from(keypair.secretKey, (byte) =>
          byte.toString(16).padStart(2, "0")
        ).join(""),
        mnemonic: mnemonic,
        publicKey: keypair.publicKey.toString(),
      };
    } catch (error) {
      throw new Error(error.message || "니모닉 복구에 실패했습니다");
    }
  }

  // 개인키로 지갑 가져오기
  async importFromPrivateKey(privateKey) {
    try {
      // Hex string을 Uint8Array로 변환
      const secretKey = Uint8Array.from(
        privateKey.match(/.{1,2}/g).map((byte) => parseInt(byte, 16))
      );

      const keypair = this.solanaWeb3.Keypair.fromSecretKey(secretKey);

      return {
        address: keypair.publicKey.toString(),
        privateKey: privateKey,
        publicKey: keypair.publicKey.toString(),
      };
    } catch (error) {
      throw new Error("유효하지 않은 개인키입니다");
    }
  }

  // 잔액 조회
  async getBalance(address) {
    try {
      await this.initProvider();
      const publicKey = new this.solanaWeb3.PublicKey(address);
      const balance = await this.connection.getBalance(publicKey);
      return balance.toString();
    } catch (error) {
      console.error("잔액 조회 실패:", error);
      return "0";
    }
  }

  // 주소 유효성 검사
  isValidAddress(address) {
    try {
      new this.solanaWeb3.PublicKey(address);
      return true;
    } catch (error) {
      return false;
    }
  }

  // 트랜잭션 전송
  async sendTransaction(params) {
    const { to, amount, privateKey } = params;

    if (!this.isValidAddress(to)) {
      throw new Error("유효하지 않은 주소입니다");
    }

    try {
      await this.initProvider();

      // 개인키로 키페어 복구
      const secretKey = Uint8Array.from(
        privateKey.match(/.{1,2}/g).map((byte) => parseInt(byte, 16))
      );
      const fromKeypair = this.solanaWeb3.Keypair.fromSecretKey(secretKey);

      // 트랜잭션 생성
      const transaction = new this.solanaWeb3.Transaction().add(
        this.solanaWeb3.SystemProgram.transfer({
          fromPubkey: fromKeypair.publicKey,
          toPubkey: new this.solanaWeb3.PublicKey(to),
          lamports: Math.floor(
            parseFloat(amount) * this.solanaWeb3.LAMPORTS_PER_SOL
          ),
        })
      );

      // 트랜잭션 전송
      const signature = await this.solanaWeb3.sendAndConfirmTransaction(
        this.connection,
        transaction,
        [fromKeypair]
      );

      console.log("Solana 트랜잭션 전송 완료:", signature);

      return {
        hash: signature,
        signature: signature,
      };
    } catch (error) {
      console.error("트랜잭션 전송 실패:", error);
      throw new Error(error.message || "트랜잭션 전송에 실패했습니다");
    }
  }

  // 블록 번호 조회 (Solana는 slot 사용)
  async getBlockNumber() {
    try {
      await this.initProvider();
      const slot = await this.connection.getSlot();
      return slot;
    } catch (error) {
      console.error("Slot 조회 실패:", error);
      return 0;
    }
  }

  // ================================================================
  // 미구현 메서드 (Abstract)
  // ================================================================

  /**
   * 트랜잭션 상태 조회 - 미구현
   * @param {string} txHash - 트랜잭션 해시
   * @returns {Promise<{status: string, confirmations: number}>}
   */
  async getTransactionStatus(txHash) {
    throw new Error(
      "getTransactionStatus() 메서드는 아직 구현되지 않았습니다."
    );
  }

  /**
   * 현재 네트워크 수수료 조회 - 미구현
   * @returns {Promise<{low: string, medium: string, high: string}>}
   */
  async getGasPrice() {
    throw new Error("getGasPrice() 메서드는 아직 구현되지 않았습니다.");
  }

  /**
   * 트랜잭션 수수료 예상 - 미구현
   * @param {Object} txParams - 트랜잭션 파라미터
   * @returns {Promise<string>} - 예상 수수료
   */
  async estimateFee(txParams) {
    throw new Error("estimateFee() 메서드는 아직 구현되지 않았습니다.");
  }
}

// 어댑터 내보내기
if (typeof module !== "undefined" && module.exports) {
  module.exports = SolanaAdapter;
} else {
  window.SolanaAdapter = SolanaAdapter;
}

// ================================================================
// 앱 초기화
// ================================================================

// 앱 시작 시 호출
if (window.App && window.App.onLaunch) {
  window.App.onLaunch({});
}
