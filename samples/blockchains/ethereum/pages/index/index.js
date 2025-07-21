// Coin 지갑 메인 페이지 로직
// TODO: 각 코인에 맞게 CoinAdapter를 구현하고 import 경로를 수정하세요

// 전역 변수
let adapter = null; // 코인 어댑터 인스턴스
let currentWallet = null; // 현재 지갑 정보

// 페이지 초기화
document.addEventListener("DOMContentLoaded", function () {
  console.log(`${CoinConfig.name} 지갑 페이지 로드`);

  // Bridge API 초기화
  if (window.anam) {
    console.log("Bridge API 사용 가능");
  }

  // Ethereum 어댑터 초기화
  adapter = new EthereumAdapter(CoinConfig);
  console.log("Ethereum adapter 초기화 완료");

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
    // Ethereum 네트워크 상태 확인
    await adapter.initProvider();
    const blockNumber = await adapter.getBlockNumber();
    console.log("현재 블록 번호:", blockNumber);
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
  const shortAddress = window.shortenAddress(address);
  addressDisplay.textContent = shortAddress;
  addressDisplay.title = address; // 전체 주소는 툴팁으로

  // 클릭 시 전체 주소 복사
  addressDisplay.style.cursor = "pointer";
  addressDisplay.onclick = () => {
    navigator.clipboard.writeText(address);
    showToast("주소가 복사되었습니다");
  };
}

// 잔액 업데이트
async function updateBalance() {
  if (!currentWallet || !adapter) return;

  try {
    const balance = await adapter.getBalance(currentWallet.address);
    const formattedBalance = window.formatBalance(balance);

    document.getElementById("balance-display").textContent = formattedBalance;

    // TODO: 실제 환율 API 호출
    const fiatPrice = 100; // 임시 가격
    const fiatValue = (parseFloat(formattedBalance) * fiatPrice).toFixed(2);
    document.getElementById("fiat-value").textContent = `≈ $${fiatValue}`;
  } catch (error) {
    console.error("잔액 조회 실패:", error);
  }
}

// Send 페이지로 이동
function navigateToSend() {
  if (!currentWallet) {
    showToast("지갑이 없습니다");
    return;
  }
  // blockchain miniapp은 anamUI 네임스페이스 사용
  if (window.anamUI && window.anamUI.navigateTo) {
    window.anamUI.navigateTo("pages/send/send");
  } else if (window.anam && window.anam.navigateTo) {
    window.anam.navigateTo("pages/send/send");
  } else {
    console.error("navigateTo API not available");
  }
}

// Receive 페이지로 이동
function navigateToReceive() {
  if (!currentWallet) {
    showToast("지갑이 없습니다");
    return;
  }
  // blockchain miniapp은 anamUI 네임스페이스 사용
  if (window.anamUI && window.anamUI.navigateTo) {
    window.anamUI.navigateTo("pages/receive/receive");
  } else if (window.anam && window.anam.navigateTo) {
    window.anam.navigateTo("pages/receive/receive");
  } else {
    console.error("navigateTo API not available");
  }
}

// 지갑 초기화
function resetWallet() {
  const walletKey = `${CoinConfig.symbol.toLowerCase()}_wallet`;
  localStorage.removeItem(walletKey);
  currentWallet = null;

  // 화면 전환
  document.getElementById("wallet-main").style.display = "none";
  document.getElementById("wallet-creation").style.display = "block";

  // 입력 필드 초기화
  const mnemonicInput = document.getElementById("mnemonic-input");
  const privateKeyInput = document.getElementById("privatekey-input");
  if (mnemonicInput) mnemonicInput.value = "";
  if (privateKeyInput) privateKeyInput.value = "";

  showToast("지갑이 초기화되었습니다");
}

// 트랜잭션 요청 처리 (Bridge API)
async function handleTransactionRequest(event) {
  console.log("트랜잭션 요청 받음:", event.detail);

  // 지갑 정보 다시 로드 (BlockchainService 환경에서 실행될 때를 위해)
  if (!currentWallet) {
    const walletKey = `${CoinConfig.symbol.toLowerCase()}_wallet`;
    const walletData = localStorage.getItem(walletKey);
    if (walletData) {
      try {
        currentWallet = JSON.parse(walletData);
        console.log("지갑 정보 재로드 완료");
      } catch (e) {
        console.error("지갑 로드 실패:", e);
      }
    }
  }

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

    // Ethereum 추가 파라미터 처리
    if (requestData.data) {
      txParams.data = requestData.data;
    }
    if (requestData.gasPrice) {
      txParams.gasPrice = requestData.gasPrice;
    }
    if (requestData.gasLimit) {
      txParams.gasLimit = requestData.gasLimit;
    }

    const result = await adapter.sendTransaction(txParams);

    // 응답 데이터 구성
    const responseData = {
      txHash: result.hash || result.txid || result.signature, // government24 호환성을 위해 txHash 사용
      from: currentWallet.address,
      to: txParams.to,
      amount: txParams.amount,
      chainId: CoinConfig.network.chainId, // government24 호환성을 위해 chainId 사용
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

// HTML onclick을 위한 전역 함수 등록
window.resetWallet = resetWallet;
