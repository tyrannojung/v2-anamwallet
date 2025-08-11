// Send 페이지 로직

// 전역 변수
let adapter = null;
let currentWallet = null;

// 페이지 초기화
document.addEventListener("DOMContentLoaded", function () {
  console.log("Send page loaded");

  // 지갑 정보 로드
  loadWalletInfo();

  // Ethereum 어댑터 초기화
  adapter = window.getAdapter();
  
  if (!adapter) {
    console.error("EthereumAdapter not initialized");
    showToast("Failed to initialize Ethereum adapter");
  }

  // UI 초기화
  updateUI();
});

// 지갑 정보 로드
function loadWalletInfo() {
  const walletKey = `${CoinConfig.symbol.toLowerCase()}_wallet`;
  const walletData = localStorage.getItem(walletKey);

  if (walletData) {
    currentWallet = JSON.parse(walletData);
    console.log("Wallet loaded:", currentWallet.address);
  } else {
    showToast("No wallet found");
    goBack();
  }
}

// UI 업데이트
async function updateUI() {
  // 코인 심볼 업데이트
  document.querySelectorAll('.coin-symbol').forEach(el => {
    el.textContent = CoinConfig.symbol;
  });

  // 타이틀 업데이트
  document.title = `Send ${CoinConfig.name}`;

  // 잔액 업데이트
  if (currentWallet && adapter) {
    try {
      const balance = await adapter.getBalance(currentWallet.address);
      const formattedBalance = window.formatBalance(balance);
      document.getElementById('available-balance').textContent = formattedBalance;
    } catch (error) {
      console.error("Failed to fetch balance:", error);
    }
  }
}

// 뒤로 가기
function goBack() {
  // blockchain miniapp은 anamUI 네임스페이스 사용
  if (window.anamUI && window.anamUI.navigateTo) {
    window.anamUI.navigateTo('pages/index/index');
  } else if (window.anam && window.anam.navigateTo) {
    window.anam.navigateTo('pages/index/index');
  } else {
    // 개발 환경: 일반 HTML 페이지 이동
    window.location.href = '../index/index.html';
  }
}

// 전송 확인
async function confirmSend() {
  if (!currentWallet || !adapter) {
    showToast("No wallet found");
    return;
  }

  const recipient = document.getElementById("recipient-address").value.trim();
  const amount = document.getElementById("send-amount").value.trim();
  const feeLevel = document.getElementById("tx-fee").value;

  // 유효성 검증
  if (!recipient || !amount) {
    showToast("Please enter recipient address and amount");
    return;
  }

  if (!adapter.isValidAddress(recipient)) {
    showToast("Invalid address format");
    return;
  }

  if (parseFloat(amount) <= 0) {
    showToast("Please enter an amount greater than 0");
    return;
  }

  try {
    showToast("Sending transaction...");

    // 수수료 가져오기
    const gasPrice = await adapter.getGasPrice();
    const fee = gasPrice[feeLevel];

    // 트랜잭션 전송
    const txParams = {
      from: currentWallet.address,
      to: recipient,
      amount: amount,
      privateKey: currentWallet.privateKey,
    };

    // Ethereum 특화 파라미터 추가
    if (feeLevel && fee) {
      txParams.gasPrice = fee; // Gwei 단위
      txParams.gasLimit = 21000; // 기본 ETH 전송 가스 한도
    }

    const result = await adapter.sendTransaction(txParams);

    showToast(`Transaction sent successfully!`);
    console.log("Transaction hash:", result.hash);

    // 메인 페이지로 돌아가기
    setTimeout(() => {
      goBack();
    }, 2000);

  } catch (error) {
    console.error("Transaction failed:", error);
    showToast("Transaction failed: " + error.message);
  }
}

// HTML onclick을 위한 전역 함수 등록
window.goBack = goBack;
window.confirmSend = confirmSend;