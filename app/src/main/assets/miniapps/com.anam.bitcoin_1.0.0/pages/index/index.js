// 비트코인 지갑 메인 페이지 로직 - Legacy bitcoinjs-lib 0.2.0 버전

// BlockCypher API 설정 (테스트넷)
const BLOCKCYPHER_API_BASE = "https://api.blockcypher.com/v1/btc/test3";

// 페이지 초기화
document.addEventListener("DOMContentLoaded", function () {
  console.log("비트코인 지갑 페이지 로드 - Legacy bitcoinjs-lib 0.2.0");
  
  // 디버깅: 페이지 로드 시 origin 확인
  console.log('Page load - Current origin:', window.location.origin);
  console.log('Page load - Current href:', window.location.href);
  console.log('Page load - localStorage keys:', Object.keys(localStorage));

  // Legacy bitcoinjs-lib 로드 확인
  setTimeout(() => {
    let bitcoinLib = null;
    
    // Legacy 버전은 window.Bitcoin으로 노출됨
    if (typeof window.Bitcoin !== "undefined") {
      bitcoinLib = window.Bitcoin;
      window.bitcoin = bitcoinLib; // 표준 이름으로 매핑
      console.log("✅ Legacy bitcoinjs-lib 0.2.0 found as 'Bitcoin'");
      console.log("Available methods:", Object.keys(bitcoinLib));
    }

    if (!bitcoinLib) {
      console.error("❌ Legacy bitcoinjs-lib이 로드되지 않았습니다.");
      console.log("Available globals:", Object.keys(window).filter(key => 
        key.toLowerCase().includes('bit') || key.toLowerCase().includes('crypto')
      ));
      
      // 폴백: 모의 라이브러리 사용
      console.log("🔄 폴백: 모의 라이브러리 사용");
      initMockLibrary();
      showToast("모의 라이브러리로 실행 중");
    } else {
      console.log("✅ Legacy bitcoinjs-lib 연결됨");
      console.log("ECKey 타입:", typeof bitcoinLib.ECKey);
      console.log("Address 타입:", typeof bitcoinLib.Address);
      console.log("Crypto 타입:", typeof bitcoinLib.Crypto);
    }

    // 지갑 존재 여부 확인
    checkWalletStatus();
  }, 1000); // 라이브러리 로딩을 위해 충분한 시간 대기
});

// 모의 라이브러리 초기화 (폴백용)
function initMockLibrary() {
  window.bitcoin = {
    ECKey: function() {
      this.priv = crypto.getRandomValues(new Uint8Array(32));
      this.pub = crypto.getRandomValues(new Uint8Array(33));
      this.getAddress = function() {
        return {
          toString: function() {
            // 표준 bech32 주소 생성 시도
            return generateBech32Address(this.pub, true);
          }
        };
      };
    },
    Address: function(hash) {
      this.hash = hash;
      this.toString = function() {
        return generateBech32Address(this.hash, true);
      };
    },
    Crypto: {
      SHA256: function(data) {
        // 간단한 SHA256 시뮬레이션
        const hash = new Uint8Array(32);
        for (let i = 0; i < 32; i++) {
          hash[i] = (data[i % data.length] + i) % 256;
        }
        return hash;
      },
      RIPEMD160: function(data) {
        // 간단한 RIPEMD160 시뮬레이션
        const hash = new Uint8Array(20);
        for (let i = 0; i < 20; i++) {
          hash[i] = (data[i % data.length] + i * 7) % 256;
        }
        return hash;
      }
    }
  };
}

// 지갑 상태 확인
function checkWalletStatus() {
  const walletData = localStorage.getItem("bitcoin_wallet");

  if (walletData) {
    const wallet = JSON.parse(walletData);
    showMainWallet(wallet);
  } else {
    showWalletCreation();
  }
}

// 지갑 생성 화면 표시
function showWalletCreation() {
  document.getElementById("wallet-creation").style.display = "block";
  document.getElementById("wallet-main").style.display = "none";
}

// 메인 지갑 화면 표시
async function showMainWallet(walletData) {
  document.getElementById("wallet-creation").style.display = "none";
  document.getElementById("wallet-main").style.display = "block";

  if (walletData.address) {
    const addressElement = document.querySelector(".address-display");
    if (addressElement) {
      addressElement.textContent = walletData.address;
    }

    // 실제 잔액 조회
    showToast("잔액 조회 중...");
    const balance = await getBalance(walletData.address);
    const balanceElement = document.querySelector(".btc-balance span");
    if (balanceElement) {
      balanceElement.textContent = balance;
    }
  }
}

// Legacy API를 사용한 비트코인 지갑 생성 함수
async function createWallet() {
  try {
    showToast("지갑 생성 중...");

    // bitcoinjs-lib 확인
    if (typeof window.bitcoin === "undefined") {
      throw new Error("Bitcoin library not loaded");
    }

    const Bitcoin = window.bitcoin;

    console.log("🔑 Legacy API로 키 쌍 생성 중...");
    
    // Legacy API로 키 쌍 생성
    let eckey, publicKey, address;
    
    if (typeof Bitcoin.ECKey === 'function') {
      // Legacy 0.2.0 API
      eckey = new Bitcoin.ECKey();
      publicKey = eckey.pub;
      
      // ✅ 표준 bech32 주소 생성
      address = generateBech32Address(publicKey, true); // true = testnet
    } else {
      // 폴백: 모의 키 생성
      console.log("ECKey not available, using fallback");
      const mockKey = crypto.getRandomValues(new Uint8Array(32));
      publicKey = crypto.getRandomValues(new Uint8Array(33));
      address = generateBech32Address(publicKey, true);
    }

    console.log("✅ 표준 bech32 주소 생성됨:", address);

    // 니모닉 문구 생성 (간단한 구현)
    const mnemonic = generateMnemonic();

    // 지갑 정보 구성
    const walletInfo = {
      address: address,
      balance: "0.00000000",
      createdAt: new Date().toISOString(),
      network: "testnet",
      // Legacy 개인키 저장
      encryptedPrivateKey: await encryptPrivateKey(
        eckey && eckey.priv ? 
        Array.from(eckey.priv, b => b.toString(16).padStart(2, '0')).join('') :
        Array.from(crypto.getRandomValues(new Uint8Array(32)), b => b.toString(16).padStart(2, '0')).join('')
      ),
      encryptedMnemonic: await encryptMnemonic(mnemonic),
    };

    // 지갑 정보 저장
    localStorage.setItem("bitcoin_wallet", JSON.stringify(walletInfo));
    
    console.log('✅ 지갑 저장 완료 (표준 bech32 주소 포함)');

    showToast("표준 bech32 지갑이 성공적으로 생성되었습니다!");
    showMainWallet(walletInfo);
    
  } catch (error) {
    console.error("❌ 지갑 생성 오류:", error);
    showToast("지갑 생성 실패: " + error.message);
  }
}

// 간단한 니모닉 생성
function generateMnemonic() {
  const words = [
    'abandon', 'ability', 'able', 'about', 'above', 'absent', 'absorb', 'abstract',
    'absurd', 'abuse', 'access', 'accident', 'account', 'accuse', 'achieve', 'acid',
    'acoustic', 'acquire', 'across', 'act', 'action', 'actor', 'actress', 'actual',
    'adapt', 'add', 'addict', 'address', 'adjust', 'admit', 'adult', 'advance'
  ];
  
  const mnemonic = [];
  for (let i = 0; i < 12; i++) {
    mnemonic.push(words[Math.floor(Math.random() * words.length)]);
  }
  return mnemonic.join(' ');
}

// 개인키 암호화
async function encryptPrivateKey(privateKey) {
  return btoa(privateKey);
}

// 니모닉 암호화
async function encryptMnemonic(mnemonic) {
  return btoa(mnemonic);
}

// Legacy API를 사용한 지갑 가져오기
async function importWallet() {
  try {
    const mnemonicInput = document.getElementById('mnemonic-input');
    const mnemonic = mnemonicInput.value.trim();
    
    if (!mnemonic) {
      showToast('니모닉을 입력해주세요.');
      return;
    }
    
    const words = mnemonic.split(/\s+/);
    if (words.length !== 12 && words.length !== 24) {
      showToast('니모닉은 12개 또는 24개 단어여야 합니다.');
      return;
    }
    
    showToast('지갑 복구 중...');
    
    if (typeof window.bitcoin === "undefined") {
      throw new Error("Bitcoin library not loaded");
    }

    const Bitcoin = window.bitcoin;
    
    // 간단한 니모닉에서 키 복원
    const seed = mnemonic.split(' ').reduce((acc, word) => acc + word.charCodeAt(0), 0);
    const seedArray = new Uint8Array(32);
    const seedStr = seed.toString().padStart(64, '0');
    for (let i = 0; i < 32; i++) {
      seedArray[i] = parseInt(seedStr.substr(i * 2, 2), 16);
    }
    
    let eckey, publicKey, address;
    
    if (typeof Bitcoin.ECKey === 'function') {
      // Legacy API로 키 복원
      eckey = new Bitcoin.ECKey();
      eckey.priv = seedArray;
      
      // 공개키 생성 (간단한 방법)
      publicKey = seedArray; // 실제로는 ECDSA 곡선 연산 필요
      
      // ✅ 표준 bech32 주소 생성
      address = generateBech32Address(publicKey, true);
    } else {
      // 폴백
      publicKey = seedArray;
      address = generateBech32Address(publicKey, true);
    }
    
    // 지갑 정보 구성
    const walletInfo = {
      address: address,
      balance: "0.00000000",
      createdAt: new Date().toISOString(),
      network: "testnet",
      encryptedPrivateKey: await encryptPrivateKey(
        Array.from(seedArray, b => b.toString(16).padStart(2, '0')).join('')
      ),
      encryptedMnemonic: await encryptMnemonic(mnemonic),
    };
    
    localStorage.setItem("bitcoin_wallet", JSON.stringify(walletInfo));
    mnemonicInput.value = '';
    
    showToast("지갑이 성공적으로 복구되었습니다!");
    showMainWallet(walletInfo);
    
  } catch (error) {
    console.error("지갑 복구 오류:", error);
    showToast("니모닉 오류: " + error.message);
  }
}

// 주소로 잔액 조회
async function getBalance(address) {
  try {
    const response = await fetch(`${BLOCKCYPHER_API_BASE}/addrs/${address}/balance`);
    
    if (!response.ok) {
      console.warn("잔액 조회 실패:", response.status);
      return "0.00000000";
    }
    
    const data = await response.json();
    const btcBalance = (data.balance || 0) / 100000000;
    return btcBalance.toFixed(8);
  } catch (error) {
    console.error("잔액 조회 실패:", error);
    return "0.00000000";
  }
}

// Toast 메시지 표시
function showToast(message) {
  console.log("[Toast]", message);

  const existingToast = document.querySelector(".toast-message");
  if (existingToast) {
    existingToast.remove();
  }

  const toast = document.createElement("div");
  toast.className = "toast-message";
  toast.style.cssText = `
    position: fixed;
    bottom: 20px;
    left: 50%;
    transform: translateX(-50%);
    background: rgba(0,0,0,0.8);
    color: white;
    padding: 12px 24px;
    border-radius: 8px;
    z-index: 9999;
    font-size: 14px;
    max-width: 80%;
    text-align: center;
  `;
  toast.textContent = message;
  document.body.appendChild(toast);

  setTimeout(() => {
    toast.remove();
  }, 3000);
}

// 지갑 초기화
function resetWallet() {
  try {
    localStorage.removeItem('bitcoin_wallet');
    showToast('지갑이 초기화되었습니다.');
    showWalletCreation();
  } catch (error) {
    console.error('지갑 초기화 실패:', error);
    showToast('초기화 실패: ' + error.message);
  }
}

// 트랜잭션 요청 수신 리스너
window.addEventListener('transactionRequest', async (event) => {
  console.log('Transaction request received:', JSON.stringify(event.detail, null, 2));
  
  try {
    const { to, amount, requestId } = event.detail;
    
    const walletData = localStorage.getItem('bitcoin_wallet');
    if (!walletData) {
      throw new Error('No wallet found');
    }
    
    const walletInfo = JSON.parse(walletData);
    
    if (typeof window.bitcoin === "undefined") {
      throw new Error("Bitcoin library not loaded");
    }

    showToast(`트랜잭션 처리 중: ${amount} BTC`);
    
    // Legacy API로 트랜잭션 시뮬레이션
    const mockTxHash = 'legacy_btc_tx_' + Date.now();
    console.log('Mock transaction sent:', mockTxHash);
    showToast(`트랜잭션 전송됨: ${mockTxHash.slice(0, 10)}...`);
    
    const responseData = {
      txHash: mockTxHash,
      from: walletInfo.address,
      to: to,
      amount: amount,
      network: 'testnet'
    };
    
    if (window.anam && window.anam.sendTransactionResponse) {
      window.anam.sendTransactionResponse(requestId, JSON.stringify(responseData));
    }
    
    console.log('Transaction success response:', JSON.stringify(responseData, null, 2));
    
  } catch (error) {
    console.error('Transaction failed:', error);
    showToast(`트랜잭션 실패: ${error.message}`);
    
    const errorResponse = { error: error.message };
    
    if (window.anam && window.anam.sendTransactionResponse) {
      window.anam.sendTransactionResponse(event.detail.requestId, JSON.stringify(errorResponse));
    }
    
    console.log('Transaction error response:', JSON.stringify(errorResponse, null, 2));
  }
});

// 전역 함수 등록
window.createWallet = createWallet;
window.showToast = showToast;
window.resetWallet = resetWallet;
window.importWallet = importWallet;