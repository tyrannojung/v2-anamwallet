// 이더리움 지갑 메인 페이지 로직

// Provider 설정 - 여기에 Infura/Alchemy 등의 RPC URL을 입력하세요
const PROVIDER_URL =
  "https://still-fluent-yard.ethereum-sepolia.quiknode.pro/ed1e699042dab42a0b3d7d6c7f059eaaef2cc930/"; // 예: 'https://mainnet.infura.io/v3/YOUR_PROJECT_ID'
let provider = null;

// 페이지 초기화
document.addEventListener("DOMContentLoaded", function () {
  console.log("이더리움 지갑 페이지 로드");
  
  // 디버깅: 페이지 로드 시 origin 확인
  console.log('Page load - Current origin:', window.location.origin);
  console.log('Page load - Current href:', window.location.href);
  console.log('Page load - localStorage keys:', Object.keys(localStorage));

  // ethers.js 로드 확인
  if (typeof ethers === "undefined") {
    console.error("ethers.js가 로드되지 않았습니다.");
    showToast("라이브러리 로드 실패");
    return;
  }

  // Provider 초기화
  if (PROVIDER_URL) {
    provider = new ethers.providers.JsonRpcProvider(PROVIDER_URL);
    console.log("Provider 연결됨:", PROVIDER_URL);
    
    // 네트워크 정보 가져오기
    provider.getNetwork().then(network => {
      console.log("Connected to network:", network);
      updateNetworkLabel(network);
    });
  } else {
    console.warn(
      "Provider URL이 설정되지 않았습니다. 읽기 전용 모드로 실행됩니다."
    );
  }

  // 지갑 존재 여부 확인
  checkWalletStatus();
});

// 지갑 상태 확인
function checkWalletStatus() {
  // localStorage를 사용하여 지갑 정보 확인
  const walletData = localStorage.getItem("ethereum_wallet");

  if (walletData) {
    // 지갑이 있으면 메인 화면 표시
    const wallet = JSON.parse(walletData);
    // 보안을 위해 개인키는 표시하지 않음
    showMainWallet(wallet);
  } else {
    // 지갑이 없으면 생성 화면 표시
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

  // 지갑 정보 업데이트
  if (walletData.address) {
    const addressElement = document.querySelector(".address-display");
    if (addressElement) {
      addressElement.textContent = walletData.address;
    }

    // 실제 잔액 조회
    if (provider) {
      showToast("잔액 조회 중...");
      const balance = await getBalance(walletData.address);
      const balanceElement = document.querySelector(".eth-balance span");
      if (balanceElement) {
        balanceElement.textContent = balance;
      }
    } else {
      // Provider가 없으면 저장된 잔액 표시
      const balanceElement = document.querySelector(".eth-balance span");
      if (balanceElement) {
        balanceElement.textContent = walletData.balance || "0.0000";
      }
    }
  }
}

// 실제 이더리움 지갑 생성 함수
async function createWallet() {
  try {
    showToast("지갑 생성 중...");

    // ethers.js를 사용하여 실제 지갑 생성
    const wallet = ethers.Wallet.createRandom();

    // 니모닉 문구 생성
    const mnemonic = wallet.mnemonic;

    // 지갑 정보 구성
    const walletInfo = {
      address: wallet.address,
      balance: "0.0000",
      createdAt: new Date().toISOString(),
      network: "mainnet",
      // 주의: 실제 앱에서는 개인키를 평문으로 저장하면 안됨!
      // 암호화하거나 안전한 키체인에 저장해야 함
      encryptedPrivateKey: await encryptPrivateKey(wallet.privateKey),
      // 니모닉도 안전하게 저장해야 함
      encryptedMnemonic: await encryptMnemonic(mnemonic.phrase),
    };

    // 디버깅: 저장 시 origin 확인
    console.log('Saving wallet - Current origin:', window.location.origin);
    console.log('Saving wallet - Current href:', window.location.href);
    
    // 지갑 정보 저장
    localStorage.setItem("ethereum_wallet", JSON.stringify(walletInfo));
    
    // 저장 확인
    console.log('Wallet saved:', localStorage.getItem("ethereum_wallet") ? "Success" : "Failed");

    // 생성 완료 메시지
    showToast("지갑이 성공적으로 생성되었습니다!");

    // 바로 메인 화면으로 전환
    showMainWallet(walletInfo);
  } catch (error) {
    console.error("지갑 생성 오류:", error);
    showToast("지갑 생성 실패: " + error.message);
  }
}

// 개인키 암호화 (간단한 예시 - 실제로는 더 강력한 암호화 필요)
async function encryptPrivateKey(privateKey) {
  // 실제로는 사용자 비밀번호나 디바이스 키로 암호화해야 함
  // 여기서는 Base64 인코딩만 수행 (데모용)
  return btoa(privateKey);
}

// 니모닉 암호화
async function encryptMnemonic(mnemonic) {
  // 실제로는 강력한 암호화 필요
  return btoa(mnemonic);
}

// 니모닉으로 지갑 가져오기
async function importWallet() {
  try {
    const mnemonicInput = document.getElementById('mnemonic-input');
    const mnemonic = mnemonicInput.value.trim();
    
    if (!mnemonic) {
      showToast('니모닉을 입력해주세요.');
      return;
    }
    
    // 니모닉 단어 개수 확인
    const words = mnemonic.split(/\s+/);
    if (words.length !== 12 && words.length !== 24) {
      showToast('니모닉은 12개 또는 24개 단어여야 합니다.');
      return;
    }
    
    showToast('지갑 복구 중...');
    
    // 니모닉으로 지갑 복구
    const wallet = ethers.Wallet.fromMnemonic(mnemonic);
    
    // 지갑 정보 구성
    const walletInfo = {
      address: wallet.address,
      balance: "0.0000",
      createdAt: new Date().toISOString(),
      network: "mainnet",
      encryptedPrivateKey: await encryptPrivateKey(wallet.privateKey),
      encryptedMnemonic: await encryptMnemonic(mnemonic),
    };
    
    // 지갑 정보 저장
    localStorage.setItem("ethereum_wallet", JSON.stringify(walletInfo));
    
    // 입력 필드 초기화
    mnemonicInput.value = '';
    
    // 성공 메시지
    showToast("지갑이 성공적으로 복구되었습니다!");
    
    // 메인 화면으로 전환
    showMainWallet(walletInfo);
    
  } catch (error) {
    console.error("지갑 복구 오류:", error);
    showToast("니모닉 오류: 올바른 니모닉인지 확인해주세요.");
  }
}

// 주소로 잔액 조회 (실제 네트워크 연결 시)
async function getBalance(address) {
  try {
    if (!provider) {
      console.warn("Provider가 설정되지 않았습니다.");
      return "0.0000";
    }

    const balance = await provider.getBalance(address);
    return ethers.utils.formatEther(balance);
  } catch (error) {
    console.error("잔액 조회 실패:", error);
    return "0.0000";
  }
}

// Toast 메시지 표시 함수 (브라우저용)
function showToast(message) {
  console.log("[Toast]", message);

  // 기존 토스트 제거
  const existingToast = document.querySelector(".toast-message");
  if (existingToast) {
    existingToast.remove();
  }

  // 새 토스트 생성
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

  // 3초 후 제거
  setTimeout(() => {
    toast.remove();
  }, 3000);
}

// 네트워크 레이블 업데이트
function updateNetworkLabel(network) {
  const networkNames = {
    1: "이더리움 메인넷",
    5: "Goerli 테스트넷",
    11155111: "Sepolia 테스트넷",
    137: "Polygon 메인넷",
    80001: "Mumbai 테스트넷"
  };
  
  const networkLabel = document.querySelector('.balance-label');
  if (networkLabel) {
    const networkName = networkNames[network.chainId] || `Chain ID: ${network.chainId}`;
    networkLabel.textContent = networkName;
  }
}

// 지갑 초기화 함수
function resetWallet() {
  try {
    // localStorage에서 지갑 데이터 삭제
    localStorage.removeItem('ethereum_wallet');
    
    // 성공 메시지
    showToast('지갑이 초기화되었습니다.');
    
    // 지갑 생성 화면으로 이동
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
    const { to, amount, data, requestId } = event.detail;
    
    // 디버깅: localStorage 상태 확인
    console.log('Current origin:', window.location.origin);
    console.log('localStorage keys:', Object.keys(localStorage));
    console.log('localStorage ethereum_wallet:', localStorage.getItem('ethereum_wallet'));
    
    // 지갑 정보 확인
    const walletData = localStorage.getItem('ethereum_wallet');
    if (!walletData) {
      throw new Error('No wallet found');
    }
    
    const walletInfo = JSON.parse(walletData);
    
    // 개인키 복호화 (실제로는 더 안전한 방법 필요)
    const privateKey = atob(walletInfo.encryptedPrivateKey);
    
    // Provider가 있는지 확인
    if (!provider) {
      throw new Error('No provider connected');
    }
    
    // 지갑 복구
    const wallet = new ethers.Wallet(privateKey, provider);
    
    // 잔액 확인
    const balance = await wallet.getBalance();
    const requiredAmount = ethers.utils.parseEther(amount);
    
    if (balance.lt(requiredAmount)) {
      throw new Error('Insufficient balance');
    }
    
    showToast(`트랜잭션 처리 중: ${amount} ETH`);
    
    // 트랜잭션 생성 및 전송
    const tx = await wallet.sendTransaction({
      to: to,
      value: requiredAmount,
      data: data || '0x'
    });
    
    console.log('Transaction sent:', tx.hash);
    showToast(`트랜잭션 전송됨: ${tx.hash.slice(0, 10)}...`);
    
    // 결과를 Bridge를 통해 전송
    const responseData = {
      txHash: tx.hash,
      from: wallet.address,
      to: to,
      amount: amount,
      chainId: (await provider.getNetwork()).chainId
    };
    
    // Bridge API를 통해 응답 전송
    if (window.anam && window.anam.sendTransactionResponse) {
      window.anam.sendTransactionResponse(requestId, JSON.stringify(responseData));
    }
    
    console.log('Transaction success response:', JSON.stringify(responseData, null, 2));
    
  } catch (error) {
    console.error('Transaction failed:', error);
    showToast(`트랜잭션 실패: ${error.message}`);
    
    // 에러 응답
    const errorResponse = {
      error: error.message
    };
    
    // Bridge API를 통해 에러 응답 전송
    if (window.anam && window.anam.sendTransactionResponse) {
      window.anam.sendTransactionResponse(event.detail.requestId, JSON.stringify(errorResponse));
    }
    
    console.log('Transaction error response:', JSON.stringify(errorResponse, null, 2));
  }
});

// 전역 함수로 등록 (HTML에서 호출 가능하도록)
window.createWallet = createWallet;
window.showToast = showToast;
window.resetWallet = resetWallet;
window.importWallet = importWallet;
