// ================================================================
// COIN 미니앱 전역 설정 및 초기화
// ================================================================

// Coin 설정
const CoinConfig = {
  // 기본 정보
  name: "Ethereum",
  symbol: "ETH",
  decimals: 18,
  
  // 네트워크 설정
  network: {
    // QuickNode RPC 엔드포인트 (Sepolia 테스트넷)
    rpcEndpoint: "https://still-fluent-yard.ethereum-sepolia.quiknode.pro/ed1e699042dab42a0b3d7d6c7f059eaaef2cc930/",
    // 네트워크 이름
    networkName: "sepolia",
    // 체인 ID
    chainId: 11155111,  // Sepolia testnet
  },
  
  // UI 테마 설정
  theme: {
    primaryColor: "#4338CA",      // 이더리움 보라색
    secondaryColor: "#6366F1",    // 밝은 보라색
    logoText: "Ethereum",
  },
  
  // 주소 설정
  address: {
    // 주소 형식 정규식 (검증용)
    regex: /^0x[a-fA-F0-9]{40}$/,
    // 주소 표시 형식
    displayFormat: "0x...",
  },
  
  // 트랜잭션 설정
  transaction: {
    // 기본 가스비
    defaultGasLimit: 21000,
    // 기본 가스 가격 (gwei)
    defaultGasPrice: "20",
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
        "CoinAdapter is an abstract class. Cannot be instantiated directly."
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
    throw new Error("generateWallet() method must be implemented.");
  }

  /**
   * 니모닉으로 지갑 복구
   * @param {string} mnemonic - 니모닉 구문
   * @returns {Promise<{address: string, privateKey: string}>}
   */
  async importFromMnemonic(mnemonic) {
    throw new Error("importFromMnemonic() method must be implemented.");
  }

  /**
   * 개인키로 지갑 복구
   * @param {string} privateKey - 개인키
   * @returns {Promise<{address: string}>}
   */
  async importFromPrivateKey(privateKey) {
    throw new Error("importFromPrivateKey() method must be implemented.");
  }

  /**
   * 주소 유효성 검증
   * @param {string} address - 검증할 주소
   * @returns {boolean}
   */
  isValidAddress(address) {
    throw new Error("isValidAddress() method must be implemented.");
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
    throw new Error("getBalance() method must be implemented.");
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
    throw new Error("sendTransaction() method must be implemented.");
  }

  /**
   * 트랜잭션 상태 조회
   * @param {string} txHash - 트랜잭션 해시
   * @returns {Promise<{status: string, confirmations: number}>}
   */
  async getTransactionStatus(txHash) {
    throw new Error("getTransactionStatus() method must be implemented.");
  }

  /* ================================================================
   * 4. 수수료 관련
   * ================================================================ */

  /**
   * 현재 네트워크 수수료 조회
   * @returns {Promise<{low: string, medium: string, high: string}>}
   */
  async getGasPrice() {
    throw new Error("getGasPrice() method must be implemented.");
  }

  /**
   * 트랜잭션 수수료 예상
   * @param {Object} txParams - 트랜잭션 파라미터
   * @returns {Promise<string>} - 예상 수수료
   */
  async estimateFee(txParams) {
    throw new Error("estimateFee() method must be implemented.");
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
    console.log("Mini app started:", options);

    this.initializeApp();

    this.loadWalletData();

    this.startNetworkMonitoring();
  },

  // 앱이 포그라운드로 전환될 때
  onShow(options) {
    console.log("Mini app activated:", options);

    if (AppState.walletData?.address) {
      this.refreshBalance();
    }

    this.checkNetworkStatus();
  },

  // 앱이 백그라운드로 전환될 때
  onHide() {
    console.log("Mini app deactivated");
  },

  // 앱 오류 발생 시
  onError(error) {
    console.error("Mini app error:", error);
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
        throw new Error(`Required configuration missing: ${field}`);
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
      console.error("Failed to load wallet data:", e);
    }
  },

  saveWalletData(data) {
    try {
      AppState.walletData = data;
      localStorage.setItem("walletData", JSON.stringify(data));
    } catch (e) {
      console.error("Failed to save wallet data:", e);
    }
  },

  // ================================================================
  // 네트워크 관리
  // ================================================================

  startNetworkMonitoring() {
    console.log("Network monitoring started");
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
      console.log("Balance updated:", balance);
    } catch (e) {
      console.error("Failed to fetch balance:", e);
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
    throw new Error("Not a valid CoinAdapter instance.");
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
  try {
    // ethers v5 사용
    if (ethers.utils && ethers.utils.formatUnits) {
      return ethers.utils.formatUnits(balance, decimals);
    }
    // ethers v6 (미래 호환성)
    if (ethers.formatEther && decimals === 18) {
      return ethers.formatEther(balance);
    }
  } catch (error) {
    console.error("Error formatting balance:", error);
  }
  
  // Fallback: BigInt 사용 (정밀도 손실 최소화)
  try {
    const divisor = BigInt(10) ** BigInt(decimals);
    const balanceBigInt = BigInt(balance);
    const wholePart = balanceBigInt / divisor;
    const fractionalPart = balanceBigInt % divisor;
    
    // 소수점 4자리까지만 표시
    const fractionalStr = fractionalPart.toString().padStart(decimals, '0');
    const displayFraction = fractionalStr.substring(0, 4);
    
    return `${wholePart}.${displayFraction}`;
  } catch (fallbackError) {
    console.error("Fallback formatting error:", fallbackError);
    return "0.0000";
  }
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
// Ethereum Adapter 구현
// ================================================================

class EthereumAdapter extends CoinAdapter {
  constructor(config) {
    super(config);
    this.provider = null;
  }

  // Provider 초기화
  async initProvider() {
    if (!this.provider && typeof ethers !== 'undefined') {
      // 체인 ID를 명시하여 네트워크 자동 감지 지연 방지
      this.provider = new ethers.providers.JsonRpcProvider(
        this.config.network.rpcEndpoint,
        this.config.network.chainId
      );
    }
    return this.provider;
  }

  /* ================================================================
   * 1. 지갑 생성 및 관리
   * ================================================================ */

  async generateWallet() {
    const wallet = ethers.Wallet.createRandom();
    
    return {
      address: wallet.address,
      privateKey: wallet.privateKey,
      mnemonic: wallet.mnemonic.phrase
    };
  }

  async importFromMnemonic(mnemonic) {
    try {
      const wallet = ethers.Wallet.fromMnemonic(mnemonic);
      return {
        address: wallet.address,
        privateKey: wallet.privateKey
      };
    } catch (error) {
      throw new Error("Invalid mnemonic: " + error.message);
    }
  }

  async importFromPrivateKey(privateKey) {
    try {
      const wallet = new ethers.Wallet(privateKey);
      return {
        address: wallet.address
      };
    } catch (error) {
      throw new Error("Invalid private key: " + error.message);
    }
  }

  isValidAddress(address) {
    return ethers.utils.isAddress(address);
  }

  /* ================================================================
   * 2. 잔액 조회
   * ================================================================ */

  async getBalance(address) {
    await this.initProvider();
    const balance = await this.provider.getBalance(address);
    
    // 디버깅 로그
    console.log("Raw balance (BigNumber):", balance);
    console.log("Balance in Wei:", balance.toString());
    console.log("Balance in ETH:", ethers.utils.formatEther(balance));
    
    return balance.toString(); // Wei 단위 BigNumber를 문자열로
  }

  /* ================================================================
   * 3. 트랜잭션 처리
   * ================================================================ */

  async sendTransaction(params) {
    await this.initProvider();
    
    const wallet = new ethers.Wallet(params.privateKey, this.provider);
    
    const tx = {
      to: params.to,
      value: ethers.utils.parseEther(params.amount),
      gasLimit: params.gasLimit || this.config.transaction.defaultGasLimit,
      gasPrice: ethers.utils.parseUnits(params.gasPrice || this.config.transaction.defaultGasPrice, 'gwei')
    };
    
    if (params.data) {
      tx.data = params.data;
    }
    
    const transaction = await wallet.sendTransaction(tx);
    
    return {
      hash: transaction.hash
    };
  }

  async getTransactionStatus(txHash) {
    await this.initProvider();
    
    const receipt = await this.provider.getTransactionReceipt(txHash);
    
    if (!receipt) {
      return {
        status: 'pending',
        confirmations: 0
      };
    }
    
    const currentBlock = await this.provider.getBlockNumber();
    
    return {
      status: receipt.status === 1 ? 'confirmed' : 'failed',
      confirmations: currentBlock - receipt.blockNumber
    };
  }

  /* ================================================================
   * 4. 수수료 관련
   * ================================================================ */

  async getGasPrice() {
    await this.initProvider();
    
    const gasPrice = await this.provider.getGasPrice();
    const gasPriceGwei = ethers.utils.formatUnits(gasPrice, 'gwei');
    
    return {
      low: (parseFloat(gasPriceGwei) * 0.8).toFixed(2),
      medium: gasPriceGwei,
      high: (parseFloat(gasPriceGwei) * 1.5).toFixed(2)
    };
  }

  async estimateFee(txParams) {
    await this.initProvider();
    
    const gasLimit = txParams.gasLimit || this.config.transaction.defaultGasLimit;
    const gasPrice = await this.provider.getGasPrice();
    
    const fee = gasPrice.mul(gasLimit);
    return ethers.utils.formatEther(fee);
  }

  /* ================================================================
   * 5. 이더리움 특화 기능
   * ================================================================ */

  // 현재 블록 번호 조회
  async getBlockNumber() {
    await this.initProvider();
    return await this.provider.getBlockNumber();
  }

  // 네트워크 정보 조회
  async getNetwork() {
    await this.initProvider();
    return await this.provider.getNetwork();
  }

  // ENS 이름 해석
  async resolveENS(ensName) {
    await this.initProvider();
    try {
      const address = await this.provider.resolveName(ensName);
      return address;
    } catch (error) {
      return null;
    }
  }
}

// 내보내기
if (typeof module !== 'undefined' && module.exports) {
  module.exports = EthereumAdapter;
} else {
  window.EthereumAdapter = EthereumAdapter;
}

// ================================================================
// 앱 초기화
// ================================================================

// Ethereum Adapter 인스턴스 생성 및 등록
const ethereumAdapter = new EthereumAdapter(CoinConfig);
window.setAdapter(ethereumAdapter);

// 앱 시작 시 호출
if (window.App && window.App.onLaunch) {
  window.App.onLaunch({});
}