// Coin 지갑 메인 페이지 로직
// TODO: 각 코인에 맞게 CoinAdapter를 구현하고 import 경로를 수정하세요

// 전역 변수
let adapter = null; // 코인 어댑터 인스턴스
let currentWallet = null; // 현재 지갑 정보

// 페이지 초기화
document.addEventListener("DOMContentLoaded", function () {
  console.log(`${CoinConfig.name} wallet page loaded`);

  // Bridge API 초기화
  if (window.anam) {
    console.log("Bridge API available");
  }

  // Ethereum 어댑터 초기화
  adapter = window.getAdapter();

  if (!adapter) {
    console.error("EthereumAdapter not initialized");
    showToast("Failed to initialize Ethereum adapter");
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

  // 트랜잭션 요청 이벤트 리스너 등록 (기존 방식 지원)
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
  document.title = `${CoinConfig.name} Wallet`;
  document.querySelector(
    ".creation-title"
  ).textContent = `${CoinConfig.name} Wallet`;
  document.querySelector(
    ".creation-description"
  ).textContent = `Create a secure ${CoinConfig.name} wallet`;
}

// 네트워크 상태 확인
async function checkNetworkStatus() {
  try {
    // Ethereum 네트워크 상태 확인
    await adapter.initProvider();
    const blockNumber = await adapter.getBlockNumber();
    console.log("Current block number:", blockNumber);
    document.getElementById("network-status").style.color = "#4cff4c";
  } catch (error) {
    console.error("Network connection failed:", error);
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
      console.error("Failed to load wallet:", error);
      showToast("Failed to load wallet");
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
    showToast("CoinAdapter not implemented");
    return;
  }

  try {
    console.log("Starting new wallet creation");
    showToast("Creating wallet...");

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

    console.log("Wallet created:", wallet.address);
    showToast("Wallet created successfully!");

    // 화면 전환
    document.getElementById("wallet-creation").style.display = "none";
    document.getElementById("wallet-main").style.display = "block";

    displayWalletInfo();
    updateBalance();
  } catch (error) {
    console.error("Failed to create wallet:", error);
    showToast("Failed to create wallet: " + error.message);
  }
}

// 니모닉으로 지갑 가져오기
async function importFromMnemonic() {
  if (!adapter) {
    showToast("CoinAdapter not implemented");
    return;
  }

  const mnemonicInput = document.getElementById("mnemonic-input").value.trim();

  if (!mnemonicInput) {
    showToast("Please enter the mnemonic");
    return;
  }

  try {
    showToast("Importing wallet...");

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

    showToast("Wallet imported successfully!");

    // 화면 전환
    document.getElementById("wallet-creation").style.display = "none";
    document.getElementById("wallet-main").style.display = "block";

    displayWalletInfo();
    updateBalance();
  } catch (error) {
    console.error("Failed to import wallet:", error);
    showToast("Please enter a valid mnemonic");
  }
}

// 개인키로 지갑 가져오기
async function importFromPrivateKey() {
  if (!adapter) {
    showToast("CoinAdapter not implemented");
    return;
  }

  const privateKeyInput = document
    .getElementById("privatekey-input")
    .value.trim();

  if (!privateKeyInput) {
    showToast("Please enter the private key");
    return;
  }

  try {
    showToast("Importing wallet...");

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

    showToast("Wallet imported successfully!");

    // 화면 전환
    document.getElementById("wallet-creation").style.display = "none";
    document.getElementById("wallet-main").style.display = "block";

    displayWalletInfo();
    updateBalance();
  } catch (error) {
    console.error("Failed to import wallet:", error);
    showToast("Please enter a valid private key");
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
    showToast("Address copied to clipboard");
  };
}

// 잔액 업데이트
async function updateBalance() {
  if (!currentWallet || !adapter) return;

  try {
    const balance = await adapter.getBalance(currentWallet.address);
    
    // 디버깅 로그 추가
    console.log("Wallet address:", currentWallet.address);
    console.log("Raw balance from adapter:", balance);
    console.log("Type of balance:", typeof balance);
    
    const formattedBalance = window.formatBalance(balance);
    
    console.log("Formatted balance:", formattedBalance);

    document.getElementById("balance-display").textContent = formattedBalance;

    // TODO: 실시간 가격 API 연동 필요
    document.getElementById("fiat-value").textContent = "";
  } catch (error) {
    console.error("Failed to fetch balance:", error);
  }
}

// Send 페이지로 이동
function navigateToSend() {
  if (!currentWallet) {
    showToast("No wallet found");
    return;
  }
  // blockchain miniapp은 anamUI 네임스페이스 사용
  if (window.anamUI && window.anamUI.navigateTo) {
    window.anamUI.navigateTo("pages/send/send");
  } else if (window.anam && window.anam.navigateTo) {
    window.anam.navigateTo("pages/send/send");
  } else {
    // 개발 환경: 일반 HTML 페이지 이동
    window.location.href = "../send/send.html";
  }
}

// Receive 페이지로 이동
function navigateToReceive() {
  if (!currentWallet) {
    showToast("No wallet found");
    return;
  }
  // blockchain miniapp은 anamUI 네임스페이스 사용
  if (window.anamUI && window.anamUI.navigateTo) {
    window.anamUI.navigateTo("pages/receive/receive");
  } else if (window.anam && window.anam.navigateTo) {
    window.anam.navigateTo("pages/receive/receive");
  } else {
    // 개발 환경: 일반 HTML 페이지 이동
    window.location.href = "../receive/receive.html";
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

  showToast("Wallet has been reset");
}

// 트랜잭션 요청 처리 (Bridge API)
async function handleTransactionRequest(event) {
  console.log("Transaction request received:", event.detail);

  // 지갑 정보 다시 로드 (BlockchainService 환경에서 실행될 때를 위해)
  if (!currentWallet) {
    const walletKey = `${CoinConfig.symbol.toLowerCase()}_wallet`;
    const walletData = localStorage.getItem(walletKey);
    if (walletData) {
      try {
        currentWallet = JSON.parse(walletData);
        console.log("Wallet info reloaded");
      } catch (e) {
        console.error("Failed to load wallet:", e);
      }
    }
  }

  if (!currentWallet || !adapter) {
    console.error("No wallet found");
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
      console.log("Transaction response sent:", responseData);
    }

    // UI 업데이트
    setTimeout(updateBalance, 3000);
  } catch (error) {
    console.error("Transaction failed:", error);

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

// 트랜잭션 요청 처리 (기존 방식 - WebApp에서 직접 호출)
async function handleTransactionRequest(event) {
  console.log("Transaction request received (legacy):", event.detail);

  // 지갑 정보 다시 로드 (BlockchainService 환경에서 실행될 때를 위해)
  if (!currentWallet) {
    const walletKey = `${CoinConfig.symbol.toLowerCase()}_wallet`;
    const walletData = localStorage.getItem(walletKey);
    if (walletData) {
      try {
        currentWallet = JSON.parse(walletData);
        console.log("Wallet info reloaded");
      } catch (e) {
        console.error("Failed to load wallet:", e);
      }
    }
  }

  if (!currentWallet || !adapter) {
    console.error("No wallet found");
    // 에러 응답 전송
    if (window.anam && window.anam.sendTransactionResponse) {
      const requestId = event.detail.requestId;
      const errorResponse = {
        error: "No wallet found",
        status: "error"
      };
      window.anam.sendTransactionResponse(
        requestId,
        JSON.stringify(errorResponse)
      );
    }
    return;
  }

  const requestData = event.detail;
  const requestId = requestData.requestId;

  try {
    // 트랜잭션 데이터 파싱
    let transactionData;
    if (typeof requestData.transactionData === 'string') {
      transactionData = JSON.parse(requestData.transactionData);
    } else {
      transactionData = requestData;
    }

    // 트랜잭션 파라미터 구성
    const txParams = {
      from: currentWallet.address,
      to: transactionData.to,
      amount: transactionData.amount || transactionData.value || "0",
      privateKey: currentWallet.privateKey,
    };

    // Ethereum 추가 파라미터 처리
    if (transactionData.data) {
      txParams.data = transactionData.data;
    }
    if (transactionData.gasPrice) {
      txParams.gasPrice = transactionData.gasPrice;
    }
    if (transactionData.gasLimit) {
      txParams.gasLimit = transactionData.gasLimit;
    }

    console.log("Sending transaction with params:", txParams);
    const result = await adapter.sendTransaction(txParams);

    // 응답 데이터 구성
    const responseData = {
      requestId: requestId,
      status: "success",
      txHash: result.hash || result.txid || result.signature,
      from: currentWallet.address,
      to: txParams.to,
      amount: txParams.amount,
      chainId: CoinConfig.network.chainId,
      network: CoinConfig.network.networkName,
      symbol: CoinConfig.symbol,
    };

    // Bridge API를 통해 응답 전송
    if (window.anam && window.anam.sendTransactionResponse) {
      window.anam.sendTransactionResponse(
        requestId,
        JSON.stringify(responseData)
      );
      console.log("Transaction response sent:", responseData);
    }

    // UI 업데이트
    setTimeout(updateBalance, 3000);
  } catch (error) {
    console.error("Transaction failed:", error);

    // 에러 응답 전송
    if (window.anam && window.anam.sendTransactionResponse) {
      const errorResponse = {
        requestId: requestId,
        status: "error",
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
window.createWallet = createWallet;
window.importFromMnemonic = importFromMnemonic;
window.importFromPrivateKey = importFromPrivateKey;
window.navigateToSend = navigateToSend;
window.navigateToReceive = navigateToReceive;
window.resetWallet = resetWallet;

// ================================================================
// Universal Bridge 요청 처리
// ================================================================

// Universal Bridge 요청 이벤트 리스너
window.addEventListener("universalRequest", async (event) => {
  console.log("Universal request received:", event.detail);
  
  const { requestId, payload } = event.detail;
  
  try {
    const request = JSON.parse(payload);
    
    // Ethereum RPC 요청인지 확인
    if (request.type === 'ethereum_rpc') {
      handleDAppRequest(requestId, request.method, request.params);
      return;
    }
    
    // 기존 트랜잭션 요청 처리 (하위 호환성)
    if (request.to && request.amount) {
      const transactionEvent = {
        detail: {
          requestId: requestId,
          ...request
        }
      };
      handleTransactionRequest(transactionEvent);
      return;
    }
    
    // 알 수 없는 요청 타입
    sendUniversalError(requestId, -32000, "Unknown request type");
    
  } catch (error) {
    console.error("Failed to parse universal request:", error);
    sendUniversalError(requestId, -32700, "Parse error");
  }
});

// DApp 요청 처리
async function handleDAppRequest(requestId, method, params) {
  console.log(`DApp request - method: ${method}, params:`, params);
  console.log(`Network: ${CoinConfig.network.networkName} (chainId: ${CoinConfig.network.chainId})`);
  
  // 지갑 정보 다시 로드 (BlockchainService 환경에서 실행될 때를 위해)
  if (!currentWallet) {
    const walletKey = `${CoinConfig.symbol.toLowerCase()}_wallet`;
    const walletData = localStorage.getItem(walletKey);
    if (walletData) {
      try {
        currentWallet = JSON.parse(walletData);
        console.log("Wallet info reloaded for DApp request");
      } catch (e) {
        console.error("Failed to load wallet:", e);
        sendDAppError(requestId, -32000, "No wallet found");
        return;
      }
    } else {
      sendDAppError(requestId, -32000, "No wallet found");
      return;
    }
  }
  
  if (!adapter) {
    adapter = window.getAdapter();
    if (!adapter) {
      sendDAppError(requestId, -32603, "Adapter not initialized");
      return;
    }
  }
  
  try {
    // EIP-1193 메서드 처리
    switch (method) {
      case "wallet_requestPermissions":
        // 권한 요청 - eth_accounts 권한 반환
        sendDAppResponse(requestId, [{ parentCapability: "eth_accounts" }]);
        break;
        
      case "eth_requestAccounts":
      case "eth_accounts":
        // 현재 계정 반환
        if (currentWallet && currentWallet.address) {
          sendDAppResponse(requestId, [currentWallet.address]);
        } else {
          // 지갑이 없으면 빈 배열 반환
          sendDAppResponse(requestId, []);
        }
        break;
        
      case "eth_chainId":
        // 체인 ID 반환 (Sepolia: 11155111 = 0xaa36a7)
        sendDAppResponse(requestId, "0xaa36a7");
        break;
        
      case "eth_sendTransaction":
        // 트랜잭션 전송
        await handleDAppSendTransaction(requestId, params);
        break;
        
      case "personal_sign":
        // 메시지 서명
        await handleDAppPersonalSign(requestId, params);
        break;
        
      case "eth_signTypedData_v4":
        // 구조화된 데이터 서명
        await handleDAppSignTypedData(requestId, params);
        break;
        
      case "wallet_switchEthereumChain":
        // 네트워크 전환 (현재는 Sepolia만 지원)
        const chainId = params[0]?.chainId;
        if (chainId === "0xaa36a7") {
          sendDAppResponse(requestId, null); // 성공
        } else {
          sendDAppError(requestId, 4902, "Unrecognized chain ID");
        }
        break;
        
      case "eth_getBalance":
        // 잔액 조회
        const balance = await adapter.getBalance(currentWallet.address);
        // Wei 단위를 16진수로 변환
        const hexBalance = "0x" + BigInt(balance).toString(16);
        sendDAppResponse(requestId, hexBalance);
        break;
        
      case "eth_blockNumber":
        // 현재 블록 번호
        const blockNumber = await adapter.getBlockNumber();
        const hexBlockNumber = "0x" + blockNumber.toString(16);
        sendDAppResponse(requestId, hexBlockNumber);
        break;
        
      case "net_version":
        // 네트워크 버전 (Sepolia: 11155111)
        sendDAppResponse(requestId, "11155111");
        break;
        
      case "wallet_getCapabilities":
        // 지갑 기능 목록 반환 (EIP-5792)
        sendDAppResponse(requestId, {
          "0xaa36a7": { // Sepolia chainId
            atomicBatch: {
              supported: false
            },
            switchChain: {
              supported: true  // 체인 전환 지원
            },
            signTypedDataV4: {
              supported: true  // EIP-712 서명 지원
            }
          }
        });
        break;
        
      case "wallet_disconnect":
        // 지갑 연결 해제
        handleDAppDisconnect(requestId);
        break;
        
      default:
        // 미지원 메서드
        sendDAppError(requestId, -32601, `Method not supported: ${method}`);
    }
  } catch (error) {
    console.error("Error handling DApp request:", error);
    sendDAppError(requestId, -32603, error.message);
  }
}

// DApp 트랜잭션 전송 처리
async function handleDAppSendTransaction(requestId, params) {
  try {
    const txParams = params[0]; // eth_sendTransaction의 첫 번째 파라미터
    
    console.log("DApp transaction params:", txParams);
    
    // 트랜잭션 파라미터 구성
    const txRequest = {
      from: currentWallet.address,
      to: txParams.to,
      amount: txParams.value ? ethers.utils.formatEther(txParams.value) : "0",
      privateKey: currentWallet.privateKey,
      data: txParams.data || "0x"
    };
    
    // 가스 설정
    if (txParams.gas) {
      txRequest.gasLimit = parseInt(txParams.gas, 16);
    }
    if (txParams.gasPrice) {
      txRequest.gasPrice = txParams.gasPrice;
    }
    
    // 트랜잭션 전송
    const result = await adapter.sendTransaction(txRequest);
    
    // 트랜잭션 해시 반환
    sendDAppResponse(requestId, result.hash);
    
    // UI 업데이트
    setTimeout(updateBalance, 3000);
    
  } catch (error) {
    console.error("DApp transaction failed:", error);
    sendDAppError(requestId, -32000, error.message);
  }
}

// DApp 메시지 서명 처리
async function handleDAppPersonalSign(requestId, params) {
  try {
    const message = params[0]; // 서명할 메시지
    const address = params[1]; // 주소 (검증용)
    
    // 주소 확인
    if (address.toLowerCase() !== currentWallet.address.toLowerCase()) {
      sendDAppError(requestId, -32000, "Address mismatch");
      return;
    }
    
    // ethers.js를 사용한 서명
    const wallet = new ethers.Wallet(currentWallet.privateKey);
    const signature = await wallet.signMessage(
      ethers.utils.isHexString(message) 
        ? ethers.utils.arrayify(message)
        : message
    );
    
    sendDAppResponse(requestId, signature);
    
  } catch (error) {
    console.error("DApp signing failed:", error);
    sendDAppError(requestId, -32000, error.message);
  }
}

// DApp 구조화된 데이터 서명 처리
async function handleDAppSignTypedData(requestId, params) {
  try {
    const address = params[0];
    const typedData = typeof params[1] === 'string' ? JSON.parse(params[1]) : params[1];
    
    // 주소 확인
    if (address.toLowerCase() !== currentWallet.address.toLowerCase()) {
      sendDAppError(requestId, -32000, "Address mismatch");
      return;
    }
    
    // ethers.js를 사용한 EIP-712 서명
    const wallet = new ethers.Wallet(currentWallet.privateKey);
    const signature = await wallet._signTypedData(
      typedData.domain,
      typedData.types,
      typedData.message
    );
    
    sendDAppResponse(requestId, signature);
    
  } catch (error) {
    console.error("DApp typed data signing failed:", error);
    sendDAppError(requestId, -32000, error.message);
  }
}

// Universal Bridge 응답 전송
function sendDAppResponse(requestId, result) {
  const response = {
    jsonrpc: "2.0",
    id: requestId,
    result: result
  };
  
  if (window.anam && window.anam.sendUniversalResponse) {
    window.anam.sendUniversalResponse(
      requestId,
      JSON.stringify(response)
    );
    console.log("Universal response sent:", response);
  } else {
    console.error("Universal Bridge not available for response");
  }
}

// Universal Bridge 에러 응답 전송
function sendDAppError(requestId, code, message) {
  const errorResponse = {
    jsonrpc: "2.0",
    id: requestId,
    error: {
      code: code,
      message: message
    }
  };
  
  if (window.anam && window.anam.sendUniversalResponse) {
    window.anam.sendUniversalResponse(
      requestId,
      JSON.stringify(errorResponse)
    );
    console.log("Universal error sent:", errorResponse);
  }
}

// Universal Bridge 에러 응답 전송 (호환성)
function sendUniversalError(requestId, code, message) {
  sendDAppError(requestId, code, message);
}

// DApp disconnect 처리
function handleDAppDisconnect(requestId) {
  console.log("DApp disconnect requested");
  
  // 성공 응답 보내기 - null 반환 (대부분의 라이브러리가 기대하는 값)
  sendDAppResponse(requestId, null);
  
  // 참고: disconnect 이벤트는 BrowserWebView의 provider.disconnect() 메서드가 호출될 때 
  // 직접 발생합니다. Ethereum 미니앱에서는 응답만 보내면 됩니다.
}
