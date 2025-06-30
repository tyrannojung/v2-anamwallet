// Coin 지갑 메인 페이지 로직
// TODO: 각 코인에 맞게 CoinAdapter를 구현하고 import 경로를 수정하세요

// 전역 변수
let adapter = null; // 코인 어댑터 인스턴스
let currentWallet = null; // 현재 지갑 정보
let provider = null; // RPC 프로바이더 (필요한 경우)

// 페이지 초기화
document.addEventListener("DOMContentLoaded", function () {
  console.log(`${CoinConfig.name} 지갑 페이지 로드`);

  // 디버깅: 페이지 로드 시 origin 확인
  console.log("Page load - Current origin:", window.location.origin);
  console.log("Page load - Current href:", window.location.href);
  console.log("Page load - localStorage keys:", Object.keys(localStorage));

  // Bridge API 초기화
  if (window.anam) {
    console.log("Bridge API 사용 가능");
  }

  // TODO: 코인별 어댑터 구현 및 초기화
  // 예시:
  // adapter = new BitcoinAdapter(CoinConfig);
  // adapter = new EthereumAdapter(CoinConfig);

  // 임시로 에러 표시
  if (!adapter) {
    console.error(
      "CoinAdapter가 구현되지 않았습니다. 각 코인에 맞게 구현해주세요."
    );
    showToast("CoinAdapter 구현이 필요합니다");
  }

  // UI 테마 적용
  applyTheme();

  // 네트워크 상태 확인
  checkNetworkStatus();

  // 지갑 존재 여부 확인
  checkWalletStatus();

  // 주기적으로 잔액 업데이트 (30초마다)
  setInterval(() => {
    if (currentWallet) {
      updateBalance();
    }
  }, 30000);

  // 트랜잭션 요청 이벤트 리스너 등록
  window.addEventListener("transactionRequest", handleTransactionRequest);
});

// 테마 적용
function applyTheme() {
  const root = document.documentElement;
  root.style.setProperty("--coin-primary", CoinConfig.theme.primaryColor);
  root.style.setProperty("--coin-secondary", CoinConfig.theme.secondaryColor);

  // 로고 심볼 변경
  document.querySelectorAll(".coin-logo, .coin-logo-large").forEach((el) => {
    el.textContent = CoinConfig.theme.logoSymbol;
  });

  // 텍스트 변경
  document.querySelectorAll(".logo-text").forEach((el) => {
    el.textContent = CoinConfig.theme.logoText;
  });

  document.querySelectorAll(".coin-unit").forEach((el) => {
    el.textContent = CoinConfig.symbol;
  });

  // 타이틀 변경
  document.title = `${CoinConfig.name} 지갑`;
  document.querySelector(
    ".creation-title"
  ).textContent = `${CoinConfig.name} 지갑`;
  document.querySelector(
    ".creation-description"
  ).textContent = `안전한 ${CoinConfig.name} 지갑을 생성하세요`;
}

// 네트워크 상태 확인
async function checkNetworkStatus() {
  try {
    // TODO: 각 코인에 맞게 네트워크 상태 확인 구현
    document.getElementById("network-status").style.color = "#4cff4c";
  } catch (error) {
    console.error("네트워크 연결 실패:", error);
    document.getElementById("network-status").style.color = "#ff4444";
  }
}

// 지갑 상태 확인
function checkWalletStatus() {
  const walletKey = `${CoinConfig.symbol.toLowerCase()}_wallet`;
  const walletData = localStorage.getItem(walletKey);

  if (walletData) {
    // 지갑이 있으면 메인 화면 표시
    try {
      currentWallet = JSON.parse(walletData);

      document.getElementById("wallet-creation").style.display = "none";
      document.getElementById("wallet-main").style.display = "block";

      displayWalletInfo();
      updateBalance();
      loadTransactionHistory();
    } catch (error) {
      console.error("지갑 로드 실패:", error);
      showToast("지갑 로드 실패");
      resetWallet();
    }
  } else {
    // 지갑이 없으면 생성 화면 표시
    document.getElementById("wallet-creation").style.display = "block";
    document.getElementById("wallet-main").style.display = "none";
  }
}

// 새 지갑 생성
async function createWallet() {
  if (!adapter) {
    showToast("CoinAdapter가 구현되지 않았습니다");
    return;
  }

  try {
    console.log("새 지갑 생성 시작");
    showToast("지갑 생성 중...");

    // 어댑터를 통해 지갑 생성
    const wallet = await adapter.generateWallet();

    // localStorage에 저장
    const walletData = {
      address: wallet.address,
      privateKey: wallet.privateKey, // 실제로는 암호화 필요
      mnemonic: wallet.mnemonic, // 실제로는 암호화 필요
      createdAt: new Date().toISOString(),
    };

    const walletKey = `${CoinConfig.symbol.toLowerCase()}_wallet`;
    localStorage.setItem(walletKey, JSON.stringify(walletData));
    currentWallet = walletData;

    console.log("지갑 생성 완료:", wallet.address);
    showToast("지갑이 생성되었습니다!");

    // 화면 전환
    document.getElementById("wallet-creation").style.display = "none";
    document.getElementById("wallet-main").style.display = "block";

    displayWalletInfo();
    updateBalance();
  } catch (error) {
    console.error("지갑 생성 실패:", error);
    showToast("지갑 생성에 실패했습니다: " + error.message);
  }
}

// 니모닉으로 지갑 가져오기
async function importFromMnemonic() {
  if (!adapter) {
    showToast("CoinAdapter가 구현되지 않았습니다");
    return;
  }

  const mnemonicInput = document.getElementById("mnemonic-input").value.trim();

  if (!mnemonicInput) {
    showToast("니모닉을 입력해주세요");
    return;
  }

  try {
    showToast("지갑 가져오는 중...");

    const wallet = await adapter.importFromMnemonic(mnemonicInput);

    // localStorage에 저장
    const walletData = {
      address: wallet.address,
      privateKey: wallet.privateKey,
      mnemonic: mnemonicInput,
      createdAt: new Date().toISOString(),
    };

    const walletKey = `${CoinConfig.symbol.toLowerCase()}_wallet`;
    localStorage.setItem(walletKey, JSON.stringify(walletData));
    currentWallet = walletData;

    showToast("지갑을 가져왔습니다!");

    // 화면 전환
    document.getElementById("wallet-creation").style.display = "none";
    document.getElementById("wallet-main").style.display = "block";

    displayWalletInfo();
    updateBalance();
  } catch (error) {
    console.error("지갑 가져오기 실패:", error);
    showToast("올바른 니모닉을 입력해주세요");
  }
}

// 개인키로 지갑 가져오기
async function importFromPrivateKey() {
  if (!adapter) {
    showToast("CoinAdapter가 구현되지 않았습니다");
    return;
  }

  const privateKeyInput = document
    .getElementById("privatekey-input")
    .value.trim();

  if (!privateKeyInput) {
    showToast("개인키를 입력해주세요");
    return;
  }

  try {
    showToast("지갑 가져오는 중...");

    const wallet = await adapter.importFromPrivateKey(privateKeyInput);

    // localStorage에 저장
    const walletData = {
      address: wallet.address,
      privateKey: privateKeyInput,
      mnemonic: null,
      createdAt: new Date().toISOString(),
    };

    const walletKey = `${CoinConfig.symbol.toLowerCase()}_wallet`;
    localStorage.setItem(walletKey, JSON.stringify(walletData));
    currentWallet = walletData;

    showToast("지갑을 가져왔습니다!");

    // 화면 전환
    document.getElementById("wallet-creation").style.display = "none";
    document.getElementById("wallet-main").style.display = "block";

    displayWalletInfo();
    updateBalance();
  } catch (error) {
    console.error("지갑 가져오기 실패:", error);
    showToast("올바른 개인키를 입력해주세요");
  }
}

// 지갑 정보 표시
function displayWalletInfo() {
  if (!currentWallet || !adapter) return;

  const address = currentWallet.address;
  const addressDisplay = document.getElementById("address-display");

  // 주소 축약 표시
  const shortAddress = adapter.shortenAddress(address);
  addressDisplay.textContent = shortAddress;
  addressDisplay.title = address; // 전체 주소는 툴팁으로

  // 클릭 시 전체 주소 복사
  addressDisplay.style.cursor = "pointer";
  addressDisplay.onclick = () => {
    navigator.clipboard.writeText(address);
    showToast("주소가 복사되었습니다");
  };

  // Receive 모달에도 주소 설정
  document.getElementById("receive-address").textContent = address;
  
  // Receive 정보 업데이트
  const receiveInfo = document.querySelector('.receive-info');
  if (receiveInfo) {
    receiveInfo.innerHTML = `
      <p>이 주소로 <strong>${CoinConfig.name}</strong>을(를) 전송할 수 있습니다.</p>
      <p class="receive-warning">다른 코인을 전송하면 자산을 잃을 수 있습니다.</p>
    `;
  }
}

// 잔액 업데이트
async function updateBalance() {
  if (!currentWallet || !adapter) return;

  try {
    const balance = await adapter.getBalance(currentWallet.address);
    const formattedBalance = adapter.formatBalance(balance);

    document.getElementById("balance-display").textContent = formattedBalance;
    document.getElementById("available-balance").textContent = formattedBalance;

    // TODO: 실제 환율 API 호출
    const fiatPrice = 100; // 임시 가격
    const fiatValue = (parseFloat(formattedBalance) * fiatPrice).toFixed(2);
    document.getElementById("fiat-value").textContent = `≈ $${fiatValue}`;
  } catch (error) {
    console.error("잔액 조회 실패:", error);
  }
}

// 거래 내역 로드
async function loadTransactionHistory() {
  if (!currentWallet || !adapter) return;

  try {
    const transactions = await adapter.getTransactionHistory(
      currentWallet.address,
      10
    );
    displayTransactions(transactions);
  } catch (error) {
    console.error("거래 내역 조회 실패:", error);
  }
}

// 거래 내역 표시
function displayTransactions(transactions) {
  const listElement = document.getElementById("transaction-list");

  if (!transactions || transactions.length === 0) {
    listElement.innerHTML =
      '<div class="empty-state"><span>거래 내역이 없습니다</span></div>';
    return;
  }

  listElement.innerHTML = transactions
    .map((tx) => {
      const isReceived =
        tx.to.toLowerCase() === currentWallet.address.toLowerCase();
      const amount = adapter.formatBalance(tx.amount);
      const time = new Date(tx.timestamp * 1000).toLocaleString("ko-KR");

      return `
      <div class="transaction-item">
        <div class="transaction-info">
          <div class="transaction-type">${isReceived ? "받기" : "보내기"}</div>
          <div class="transaction-time">${time}</div>
        </div>
        <div class="transaction-amount ${isReceived ? "received" : "sent"}">
          ${isReceived ? "+" : "-"}${amount} ${CoinConfig.symbol}
        </div>
      </div>
    `;
    })
    .join("");
}

// Send 모달 표시
function showSendForm() {
  document.getElementById("send-modal").style.display = "flex";
}

// Send 모달 닫기
function closeSendModal() {
  document.getElementById("send-modal").style.display = "none";
  // 입력값 초기화
  document.getElementById("recipient-address").value = "";
  document.getElementById("send-amount").value = "";
  document.getElementById("tx-fee").value = "medium";
}

// 전송 확인
async function confirmSend() {
  if (!currentWallet || !adapter) {
    showToast("지갑이 없습니다");
    return;
  }

  const recipient = document.getElementById("recipient-address").value.trim();
  const amount = document.getElementById("send-amount").value.trim();
  const feeLevel = document.getElementById("tx-fee").value;

  // 유효성 검증
  if (!recipient || !amount) {
    showToast("받는 주소와 금액을 입력해주세요");
    return;
  }

  if (!adapter.isValidAddress(recipient)) {
    showToast("올바른 주소 형식이 아닙니다");
    return;
  }

  if (parseFloat(amount) <= 0) {
    showToast("0보다 큰 금액을 입력해주세요");
    return;
  }

  try {
    showToast("트랜잭션 전송 중...");

    // 수수료 가져오기
    const gasPrice = await adapter.getGasPrice();
    const fee = gasPrice[feeLevel];

    // 트랜잭션 전송 (코인별로 파라미터가 다를 수 있음)
    // TODO: 각 코인 어댑터에 맞는 파라미터 전달
    const txParams = {
      from: currentWallet.address,
      to: recipient,
      amount: amount,
      privateKey: currentWallet.privateKey,
      // 코인별 추가 파라미터 예시:
      // - Bitcoin: utxos, feeRate
      // - Ethereum: gasPrice, gasLimit, nonce
      // - Solana: keypair, recentBlockhash
    };

    // 수수료 관련 파라미터 추가
    // 코인 별로 다를 수 있음
    if (feeLevel && fee) {
      txParams.fee = fee;
      txParams.feePreference = feeLevel;
    }

    const result = await adapter.sendTransaction(txParams);

    showToast(`트랜잭션 전송 성공!`);
    console.log("트랜잭션 해시:", result.hash);

    // 모달 닫기
    closeSendModal();

    // 잔액 업데이트
    setTimeout(updateBalance, 3000);

    // 거래 내역 업데이트
    setTimeout(loadTransactionHistory, 5000);
  } catch (error) {
    console.error("트랜잭션 실패:", error);
    showToast("트랜잭션 실패: " + error.message);
  }
}

// Receive 모달 표시
function showReceiveModal() {
  if (!currentWallet) {
    showToast("지갑이 없습니다");
    return;
  }
  
  document.getElementById("receive-modal").style.display = "flex";

  // QR 코드 생성
  const qrContainer = document.getElementById('qr-code');
  qrContainer.innerHTML = ''; // 기존 QR 코드 제거
  
  // Canvas 엘리먼트 생성
  const canvas = document.createElement('canvas');
  qrContainer.appendChild(canvas);
  
  // QR 코드 옵션
  const qrOptions = {
    width: 200,
    height: 200,
    margin: 2,
    color: {
      dark: '#000000',
      light: '#FFFFFF'
    },
    errorCorrectionLevel: 'M'
  };
  
  // QR 코드 생성
  QRCode.toCanvas(canvas, currentWallet.address, qrOptions, (error) => {
    if (error) {
      console.error('QR 코드 생성 실패:', error);
      qrContainer.innerHTML = '<div style="padding: 20px; color: #999;">QR 코드 생성 실패</div>';
    }
  });
  
  // 주소 표시 업데이트
  document.getElementById("receive-address").textContent = currentWallet.address;
}

// Receive 모달 닫기
function closeReceiveModal() {
  document.getElementById("receive-modal").style.display = "none";
}

// 주소 복사
function copyAddress() {
  if (!currentWallet) return;

  navigator.clipboard.writeText(currentWallet.address);
  showToast("주소가 클립보드에 복사되었습니다");
}

// 지갑 초기화
function resetWallet() {
  if (confirm("정말로 지갑을 초기화하시겠습니까?\n복구할 수 없습니다.")) {
    const walletKey = `${CoinConfig.symbol.toLowerCase()}_wallet`;
    localStorage.removeItem(walletKey);
    currentWallet = null;

    // 화면 전환
    document.getElementById("wallet-main").style.display = "none";
    document.getElementById("wallet-creation").style.display = "block";

    showToast("지갑이 초기화되었습니다");
  }
}

// 트랜잭션 요청 처리 (Bridge API)
async function handleTransactionRequest(event) {
  console.log("트랜잭션 요청 받음:", event.detail);

  if (!currentWallet || !adapter) {
    console.error("지갑이 없습니다");
    return;
  }

  const requestData = event.detail;
  const requestId = requestData.requestId;

  try {
    // TODO: 각 코인별로 요청 데이터 파싱 로직을 커스터마이징하세요
    // 예시:
    // - Ethereum 형식: {to, amount, data}
    // - Bitcoin 형식: {recipient, satoshis, memo}
    // - Solana 형식: {destination, lamports}

    // 기본 트랜잭션 파라미터 (공통)
    const txParams = {
      from: currentWallet.address,
      to: requestData.to || requestData.recipient || requestData.destination,
      amount: requestData.amount || requestData.value,
      privateKey: currentWallet.privateKey,
    };

    // TODO: 코인별 추가 파라미터 처리
    // if (CoinConfig.symbol === 'BTC') {
    //   txParams.satoshis = requestData.satoshis;
    //   txParams.feeRate = requestData.feeRate || 10;
    // } else if (CoinConfig.symbol === 'ETH') {
    //   txParams.data = requestData.data || '0x';
    //   txParams.gasPrice = requestData.gasPrice;
    // }

    const result = await adapter.sendTransaction(txParams);

    // 응답 데이터 구성
    const responseData = {
      hash: result.hash || result.txid || result.signature, // 코인별 다른 이름
      from: currentWallet.address,
      to: txParams.to,
      amount: txParams.amount,
      network: CoinConfig.network.networkName,
      symbol: CoinConfig.symbol,
      // TODO: 코인별 추가 응답 데이터
    };

    // Bridge API를 통해 응답 전송
    if (window.anam && window.anam.sendTransactionResponse) {
      window.anam.sendTransactionResponse(
        requestId,
        JSON.stringify(responseData)
      );
      console.log("트랜잭션 응답 전송됨:", responseData);
    }

    // UI 업데이트
    setTimeout(updateBalance, 3000);
    setTimeout(loadTransactionHistory, 5000);
  } catch (error) {
    console.error("트랜잭션 실패:", error);

    // 에러 응답 전송
    if (window.anam && window.anam.sendTransactionResponse) {
      const errorResponse = {
        error: error.message,
        from: currentWallet.address,
        symbol: CoinConfig.symbol,
      };
      window.anam.sendTransactionResponse(
        requestId,
        JSON.stringify(errorResponse)
      );
    }
  }
}

// 토스트 메시지 표시
function showToast(message) {
  const existing = document.querySelector(".toast");
  if (existing) {
    existing.remove();
  }

  const toast = document.createElement("div");
  toast.className = "toast";
  toast.textContent = message;
  document.body.appendChild(toast);

  setTimeout(() => {
    toast.remove();
  }, 3000);
}

// 모달 외부 클릭 시 닫기
window.onclick = function (event) {
  if (event.target.classList.contains("modal")) {
    event.target.style.display = "none";
  }
};
