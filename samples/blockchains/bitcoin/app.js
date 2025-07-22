// ================================================================
// COIN 미니앱 전역 설정 및 초기화
// ================================================================

// Bitcoin 네트워크 설정
const BITCOIN_NETWORKS = {
  testnet4: {
    name: 'testnet4',
    apiBaseUrl: 'https://mempool.space/testnet4/api',
    explorerUrl: 'https://mempool.space/testnet4',
    faucetUrl: 'https://bitcoinfaucet.uo1.net/', // testnet4 faucet
  },
  mainnet: {
    name: 'mainnet', 
    apiBaseUrl: 'https://mempool.space/api',
    explorerUrl: 'https://mempool.space',
    faucetUrl: null,
  }
};

// 현재 네트워크 설정
const CURRENT_NETWORK = 'testnet4'; // 'testnet4' 또는 'mainnet'

// Coin 설정
const CoinConfig = {
  // 기본 정보
  name: "Bitcoin",
  symbol: "BTC",
  decimals: 8, // Bitcoin은 8 decimals (1 BTC = 100,000,000 satoshi)

  // 네트워크 설정
  network: {
    networkName: CURRENT_NETWORK,
    // Bitcoin은 chainId 사용 안함
    network: CURRENT_NETWORK,
  },

  // UI 테마 설정
  theme: {
    primaryColor: "#F7931A", // 비트코인 오렌지
    secondaryColor: "#4D4D4D", // 비트코인 그레이
    logoText: "Bitcoin",
  },

  // 주소 설정
  address: {
    // 비트코인 주소 형식 정규식 (Legacy, SegWit, Native SegWit 지원)
    regex: /^(1[a-km-zA-HJ-NP-Z1-9]{25,34}|3[a-km-zA-HJ-NP-Z1-9]{25,34}|bc1[a-z0-9]{39,59}|tb1[a-z0-9]{39,59})$/,
    // 주소 표시 형식
    displayFormat: "bc1...", // Native SegWit 형식
  },

  // 트랜잭션 설정
  transaction: {
    // 기본 수수료 (BTC 단위)
    defaultFee: "0.0001",
    // 최소 전송 금액
    minAmount: "0.00001",
    // 확인 대기 시간 (ms)
    confirmationTime: 600000, // 10분 (평균 블록 시간)
    // 수수료율 (satoshi/byte)
    feeRate: 10,
  },

  // 기타 옵션
  options: {
    // 니모닉 지원 여부
    supportsMnemonic: true,
    // 토큰 지원 여부
    supportsTokens: false,
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
// Bitcoin Adapter 구현
// ================================================================

class BitcoinAdapter extends CoinAdapter {
  constructor(config) {
    super(config);
    // QuickNode RPC는 더 이상 사용하지 않음 - Mempool.space API 사용
    // this.rpcEndpoint = config.network.rpcEndpoint;
  }


  /**
   * 새 지갑 생성 (HD 지갑)
   */
  async generateWallet() {
    try {
      // bitcoinjs-lib 번들 글로벌 객체 확인
      const bitcoin = window.BitcoinJS;
      
      if (!bitcoin || !bitcoin.networks) {
        throw new Error("bitcoinjs-lib가 로드되지 않았습니다.");
      }
      
      const network = this.config.network.networkName === 'testnet4'
        ? bitcoin.networks.testnet 
        : bitcoin.networks.bitcoin;
      
      // 랜덤 니모닉 생성 (12 단어)
      const mnemonic = bitcoin.generateMnemonic();
      
      // HD 지갑 생성 (헬퍼 함수 사용)
      const hdWallet = bitcoin.hdWalletFromMnemonic(mnemonic, network);
      
      // 주소 생성 (헬퍼 함수 사용)
      const account = bitcoin.generateAddress(hdWallet, 0, network);
      
      return {
        address: account.address,
        privateKey: account.privateKey,
        mnemonic: mnemonic
      };
    } catch (error) {
      console.error("Bitcoin 지갑 생성 실패:", error);
      throw new Error("Bitcoin 지갑 생성에 실패했습니다. bitcoinjs-lib가 로드되었는지 확인하세요.");
    }
  }
  

  /**
   * 니모닉으로 지갑 복구
   */
  async importFromMnemonic(mnemonic) {
    try {
      const bitcoin = window.BitcoinJS;
      
      if (!bitcoin || !bitcoin.networks) {
        throw new Error("bitcoinjs-lib가 로드되지 않았습니다.");
      }
      
      const network = this.config.network.networkName === 'testnet4'
        ? bitcoin.networks.testnet 
        : bitcoin.networks.bitcoin;
      
      // HD 지갑 생성 (헬퍼 함수 사용)
      const hdWallet = bitcoin.hdWalletFromMnemonic(mnemonic, network);
      
      // 주소 생성 (헬퍼 함수 사용)
      const account = bitcoin.generateAddress(hdWallet, 0, network);
      
      return {
        address: account.address,
        privateKey: account.privateKey
      };
    } catch (error) {
      console.error("Bitcoin 니모닉 복구 실패:", error);
      throw new Error("Bitcoin 니모닉 복구에 실패했습니다: " + error.message);
    }
  }

  /**
   * 개인키로 지갑 복구
   */
  async importFromPrivateKey(privateKey) {
    try {
      const bitcoin = window.BitcoinJS;
      
      if (!bitcoin || !bitcoin.networks) {
        throw new Error("bitcoinjs-lib가 로드되지 않았습니다.");
      }
      
      const network = this.config.network.networkName === 'testnet4'
        ? bitcoin.networks.testnet 
        : bitcoin.networks.bitcoin;
      
      // WIF 형식의 개인키를 ECPair로 변환
      const keyPair = bitcoin.ECPair.fromWIF(privateKey, network);
      
      // P2WPKH 주소 생성 (bc1...)
      const { address } = bitcoin.payments.p2wpkh({ 
        pubkey: keyPair.publicKey, 
        network 
      });
      
      return {
        address: address
      };
    } catch (error) {
      console.error("Bitcoin 개인키 복구 실패:", error);
      throw new Error("Bitcoin 개인키 복구에 실패했습니다: " + error.message);
    }
  }

  /**
   * 주소 유효성 검증
   */
  isValidAddress(address) {
    return this.config.address.regex.test(address);
  }

  /**
   * 잔액 조회 (Mempool.space API 사용)
   */
  async getBalance(address) {
    try {
      const network = this.config.network.networkName || 'testnet4';
      const apiUrl = BITCOIN_NETWORKS[network].apiBaseUrl;
      
      const response = await fetch(`${apiUrl}/address/${address}`);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      const data = await response.json();
      
      // chain_stats: 확인된 거래
      // mempool_stats: 미확인 거래
      const confirmed = data.chain_stats.funded_txo_sum - data.chain_stats.spent_txo_sum;
      const unconfirmed = data.mempool_stats.funded_txo_sum - data.mempool_stats.spent_txo_sum;
      
      const totalSatoshi = confirmed + unconfirmed;
      return totalSatoshi.toString(); // satoshi 단위
    } catch (error) {
      console.error("잔액 조회 실패:", error);
      return "0";
    }
  }

  /**
   * UTXO 조회
   */
  async getUTXOs(address) {
    try {
      const network = this.config.network.networkName || 'testnet4';
      const apiUrl = BITCOIN_NETWORKS[network].apiBaseUrl;
        
      const response = await fetch(`${apiUrl}/address/${address}/utxo`);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return await response.json();
    } catch (error) {
      console.error('UTXO 조회 실패:', error);
      return [];
    }
  }

  /**
   * 권장 수수료 조회
   */
  async getFeeEstimates() {
    try {
      const network = this.config.network.networkName || 'testnet4';
      const apiUrl = BITCOIN_NETWORKS[network].apiBaseUrl;
        
      const response = await fetch(`${apiUrl}/v1/fees/recommended`);
      return await response.json();
      // 반환값: { fastestFee, halfHourFee, hourFee, economyFee, minimumFee }
    } catch (error) {
      console.error('수수료 조회 실패:', error);
      return { fastestFee: 20, halfHourFee: 10, hourFee: 5, economyFee: 2, minimumFee: 1 };
    }
  }

  /**
   * 트랜잭션 전송
   */
  async sendTransaction(params) {
    const { to, amount, privateKey, feeRate } = params;
    
    try {
      const bitcoin = window.BitcoinJS;
      const btcNetwork = this.config.network.networkName === 'testnet4'
        ? bitcoin.networks.testnet 
        : bitcoin.networks.bitcoin;
      
      // 현재 주소 가져오기
      const keyPair = bitcoin.ECPair.fromWIF(privateKey, btcNetwork);
      const { address: fromAddress } = bitcoin.payments.p2wpkh({ 
        pubkey: keyPair.publicKey, 
        network: btcNetwork 
      });
      
      // 1. UTXO 조회
      const utxos = await this.getUTXOs(fromAddress);
      if (utxos.length === 0) {
        throw new Error('사용 가능한 UTXO가 없습니다');
      }
      
      // 2. PSBT 생성
      const psbt = new bitcoin.Psbt({ network: btcNetwork });
      
      // 3. Input 추가 (UTXO)
      let totalInput = 0;
      const amountSatoshi = Math.floor(parseFloat(amount) * 100000000);
      
      for (const utxo of utxos) {
        if (totalInput >= amountSatoshi + 10000) break; // 수수료 여유분
        
        psbt.addInput({
          hash: utxo.txid,
          index: utxo.vout,
          witnessUtxo: {
            script: bitcoin.address.toOutputScript(fromAddress, btcNetwork),
            value: utxo.value
          }
        });
        
        totalInput += utxo.value;
      }
      
      if (totalInput < amountSatoshi) {
        throw new Error('잔액이 부족합니다');
      }
      
      // 4. Output 추가 (수신자)
      psbt.addOutput({
        address: to,
        value: amountSatoshi
      });
      
      // 5. 거스름돈 계산 및 추가
      const estimatedSize = 250; // 대략적인 트랜잭션 크기
      const fee = estimatedSize * (feeRate || 10);
      const change = totalInput - amountSatoshi - fee;
      
      if (change > 546) { // dust limit
        psbt.addOutput({
          address: fromAddress,
          value: change
        });
      }
      
      // 6. 서명
      psbt.signAllInputs(keyPair);
      psbt.finalizeAllInputs();
      
      // 7. 브로드캐스트
      const tx = psbt.extractTransaction();
      const rawTx = tx.toHex();
      
      const networkName = this.config.network.networkName || 'testnet4';
      const apiUrl = BITCOIN_NETWORKS[networkName].apiBaseUrl;
        
      const broadcastResponse = await fetch(`${apiUrl}/tx`, {
        method: 'POST',
        headers: { 'Content-Type': 'text/plain' },
        body: rawTx
      });
      
      if (!broadcastResponse.ok) {
        const errorText = await broadcastResponse.text();
        throw new Error(`트랜잭션 브로드캐스트 실패: ${errorText}`);
      }
      
      const txid = await broadcastResponse.text();
      return { hash: txid };
      
    } catch (error) {
      console.error('트랜잭션 전송 실패:', error);
      throw error;
    }
  }

  /**
   * 트랜잭션 상태 조회
   */
  async getTransactionStatus(txHash) {
    try {
      const network = this.config.network.networkName || 'testnet4';
      const apiUrl = BITCOIN_NETWORKS[network].apiBaseUrl;
        
      const response = await fetch(`${apiUrl}/tx/${txHash}/status`);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      const status = await response.json();
      
      return {
        status: status.confirmed ? 'confirmed' : 'pending',
        confirmations: status.confirmed ? 1 : 0, // Mempool API는 정확한 confirmations를 제공하지 않음
        blockHeight: status.block_height,
        blockTime: status.block_time
      };
    } catch (error) {
      console.error('트랜잭션 상태 조회 실패:', error);
      return {
        status: 'not_found',
        confirmations: 0
      };
    }
  }

  /**
   * 수수료율 조회
   */
  async getGasPrice() {
    try {
      const fees = await this.getFeeEstimates();
      
      return {
        low: String(fees.economyFee),      // 경제적 (1시간)
        medium: String(fees.halfHourFee),  // 중간 (30분)
        high: String(fees.fastestFee)      // 빠름 (10분)
      };
    } catch (error) {
      console.error("수수료율 조회 실패:", error);
      // 기본값 반환 (sat/vByte)
      return {
        low: "5",
        medium: "10",
        high: "20"
      };
    }
  }

  /**
   * 트랜잭션 수수료 예상
   */
  async estimateFee(txParams) {
    // Bitcoin 트랜잭션 크기는 일반적으로 250 bytes 정도
    const estimatedSize = 250;
    const feeRates = await this.getGasPrice();
    const feeRate = parseInt(feeRates.medium);
    
    const feeSatoshi = estimatedSize * feeRate;
    const feeBTC = feeSatoshi / 100000000;
    
    return feeBTC.toFixed(8);
  }
}

// ================================================================
// 앱 초기화
// ================================================================

// Bitcoin Adapter 인스턴스 생성 및 등록
const bitcoinAdapter = new BitcoinAdapter(CoinConfig);
window.setAdapter(bitcoinAdapter);

// 앱 시작 시 호출
if (window.App && window.App.onLaunch) {
  window.App.onLaunch({});
}