// 솔라나 지갑 메인 페이지 로직

// RPC 엔드포인트 설정 - QuickNode 사용
const RPC_ENDPOINT = 'https://cool-aged-snowflake.solana-mainnet.quiknode.pro/'; // QuickNode 메인넷

let connection = null;
let currentWallet = null;

// 페이지 초기화
document.addEventListener("DOMContentLoaded", function () {
  console.log("솔라나 지갑 페이지 로드");
  
  // 디버깅: 페이지 로드 시 origin 확인
  console.log('Page load - Current origin:', window.location.origin);
  console.log('Page load - Current href:', window.location.href);
  console.log('Page load - localStorage keys:', Object.keys(localStorage));

  // Solana Web3.js 로드 확인
  if (typeof solanaWeb3 === "undefined") {
    console.error("Solana Web3.js가 로드되지 않았습니다.");
    showToast("라이브러리 로드 실패");
    return;
  }

  // Bridge API 초기화
  if (window.anam) {
    console.log("Bridge API 사용 가능");
  }

  // Connection 초기화
  connection = new solanaWeb3.Connection(RPC_ENDPOINT);
  console.log("Solana 네트워크 연결됨:", RPC_ENDPOINT);

  // 네트워크 상태 확인
  checkNetworkStatus();

  // 새로고침 시 지갑 초기화 (개발용)
  localStorage.removeItem("solana_wallet");
  
  // 지갑 존재 여부 확인
  checkWalletStatus();

  // 주기적으로 잔액 업데이트 (30초마다)
  setInterval(() => {
    if (currentWallet) {
      updateBalance();
    }
  }, 30000);

  // 트랜잭션 요청 이벤트 리스너 등록
  window.addEventListener('transactionRequest', handleTransactionRequest);
});

// 네트워크 상태 확인
async function checkNetworkStatus() {
  try {
    const version = await connection.getVersion();
    console.log("Solana 버전:", version);
    document.getElementById('network-status').style.color = '#4cff4c';
  } catch (error) {
    console.error("네트워크 연결 실패:", error);
    document.getElementById('network-status').style.color = '#ff4444';
  }
}

// 지갑 상태 확인
function checkWalletStatus() {
  const walletData = localStorage.getItem("solana_wallet");

  if (walletData) {
    // 지갑이 있으면 메인 화면 표시
    try {
      const parsed = JSON.parse(walletData);
      const secretKey = Uint8Array.from(parsed.secretKey);
      currentWallet = solanaWeb3.Keypair.fromSecretKey(secretKey);
      
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
  try {
    console.log("새 지갑 생성 시작");
    
    // 새로운 키페어 생성
    const newKeypair = solanaWeb3.Keypair.generate();
    
    // localStorage에 저장 (프로덕션에서는 보안 고려 필요)
    const walletData = {
      publicKey: newKeypair.publicKey.toString(),
      secretKey: Array.from(newKeypair.secretKey),
      createdAt: new Date().toISOString()
    };
    
    localStorage.setItem("solana_wallet", JSON.stringify(walletData));
    currentWallet = newKeypair;
    
    console.log("지갑 생성 완료:", newKeypair.publicKey.toString());
    showToast("지갑이 생성되었습니다!");
    
    // 화면 전환
    document.getElementById("wallet-creation").style.display = "none";
    document.getElementById("wallet-main").style.display = "block";
    
    displayWalletInfo();
    updateBalance();
    
    // 에어드랍 제거 - 실제 SOL 사용
  } catch (error) {
    console.error("지갑 생성 실패:", error);
    showToast("지갑 생성에 실패했습니다");
  }
}

// 지갑 가져오기 (시드 구문)
async function importWallet() {
  const mnemonicInput = document.getElementById("mnemonic-input").value.trim();
  
  if (!mnemonicInput) {
    showToast("시드 구문을 입력해주세요");
    return;
  }
  
  try {
    // 시드 구문에서 지갑 복구
    // 참고: 실제 구현에서는 BIP39 라이브러리 사용 필요
    showToast("시드 구문 기능은 추가 라이브러리가 필요합니다");
    
    // 임시로 새 지갑 생성
    // createWallet();
  } catch (error) {
    console.error("지갑 가져오기 실패:", error);
    showToast("올바른 시드 구문을 입력해주세요");
  }
}

// 지갑 정보 표시
function displayWalletInfo() {
  if (!currentWallet) return;
  
  const publicKey = currentWallet.publicKey.toString();
  const addressDisplay = document.getElementById("address-display");
  
  // 주소 축약 표시 (처음 6자...마지막 4자)
  const shortAddress = publicKey.slice(0, 6) + "..." + publicKey.slice(-4);
  addressDisplay.textContent = shortAddress;
  addressDisplay.title = publicKey; // 전체 주소는 툴팁으로
  
  // 클릭 시 전체 주소 복사
  addressDisplay.style.cursor = "pointer";
  addressDisplay.onclick = () => {
    navigator.clipboard.writeText(publicKey);
    showToast("주소가 복사되었습니다");
  };
}

// 잔액 업데이트
async function updateBalance() {
  if (!currentWallet || !connection) return;
  
  try {
    const balance = await connection.getBalance(currentWallet.publicKey);
    const solBalance = balance / solanaWeb3.LAMPORTS_PER_SOL;
    
    document.getElementById("balance-display").textContent = solBalance.toFixed(4);
    
    // USD 가격 계산 (실제로는 API 호출 필요)
    const solPrice = 100; // 임시 가격
    const usdValue = (solBalance * solPrice).toFixed(2);
    document.getElementById("usd-value").textContent = `≈ $${usdValue}`;
    
  } catch (error) {
    console.error("잔액 조회 실패:", error);
  }
}


// Send 폼 표시
function showSendForm() {
  showToast("Send 기능은 준비 중입니다");
}

// Receive 모달 표시
function showReceiveModal() {
  if (!currentWallet) return;
  
  const publicKey = currentWallet.publicKey.toString();
  
  // 주소를 클립보드에 복사
  navigator.clipboard.writeText(publicKey);
  showToast("주소가 클립보드에 복사되었습니다");
}

// 지갑 초기화
function resetWallet() {
  if (confirm("정말로 지갑을 초기화하시겠습니까?\n복구할 수 없습니다.")) {
    localStorage.removeItem("solana_wallet");
    currentWallet = null;
    
    // 화면 전환
    document.getElementById("wallet-main").style.display = "none";
    document.getElementById("wallet-creation").style.display = "block";
    
    showToast("지갑이 초기화되었습니다");
  }
}

// 토스트 메시지 표시
function showToast(message) {
  const existing = document.querySelector('.toast');
  if (existing) {
    existing.remove();
  }
  
  const toast = document.createElement('div');
  toast.className = 'toast';
  toast.textContent = message;
  document.body.appendChild(toast);
  
  setTimeout(() => {
    toast.remove();
  }, 3000);
}

// 트랜잭션 요청 처리
async function handleTransactionRequest(event) {
  console.log('트랜잭션 요청 받음:', event.detail);
  
  const requestData = event.detail;
  const requestId = requestData.requestId;
  
  try {
    // 지갑 확인
    if (!currentWallet) {
      throw new Error('지갑이 없습니다. 먼저 지갑을 생성하세요.');
    }
    
    // Solana 트랜잭션 형식으로 변환
    let recipientAddress;
    let amountInSol;
    
    // 이더리움 형식의 요청 처리
    if (requestData.to && requestData.amount) {
      // 이더리움 주소를 Solana 주소로 변환 (실제로는 불가능하므로 임시 처리)
      recipientAddress = 'So11111111111111111111111111111111111111112'; // 시스템 프로그램 주소
      amountInSol = parseFloat(requestData.amount);
    } 
    // Solana 네이티브 형식
    else if (requestData.recipient && requestData.lamports) {
      recipientAddress = requestData.recipient;
      amountInSol = requestData.lamports / solanaWeb3.LAMPORTS_PER_SOL;
    } else {
      throw new Error('지원하지 않는 트랜잭션 형식입니다.');
    }
    
    console.log('트랜잭션 준비:', {
      from: currentWallet.publicKey.toString(),
      to: recipientAddress,
      amount: amountInSol + ' SOL'
    });
    
    // 트랜잭션 생성
    const transaction = new solanaWeb3.Transaction();
    
    // 시스템 프로그램 전송 명령 추가
    transaction.add(
      solanaWeb3.SystemProgram.transfer({
        fromPubkey: currentWallet.publicKey,
        toPubkey: new solanaWeb3.PublicKey(recipientAddress),
        lamports: Math.floor(amountInSol * solanaWeb3.LAMPORTS_PER_SOL)
      })
    );
    
    // 최신 블록해시 가져오기
    const { blockhash } = await connection.getLatestBlockhash();
    transaction.recentBlockhash = blockhash;
    transaction.feePayer = currentWallet.publicKey;
    
    // 트랜잭션 서명
    transaction.sign(currentWallet);
    
    // 트랜잭션 전송
    showToast('트랜잭션 전송 중...');
    const signature = await connection.sendRawTransaction(transaction.serialize());
    
    // 트랜잭션 확인
    await connection.confirmTransaction(signature);
    
    console.log('트랜잭션 성공:', signature);
    showToast('트랜잭션 성공!');
    
    // 응답 데이터 구성
    const responseData = {
      signature: signature,
      from: currentWallet.publicKey.toString(),
      to: recipientAddress,
      amount: amountInSol.toString(),
      network: 'solana-mainnet'
    };
    
    // Bridge API를 통해 응답 전송
    if (window.anam && window.anam.sendTransactionResponse) {
      window.anam.sendTransactionResponse(requestId, JSON.stringify(responseData));
      console.log('트랜잭션 응답 전송됨:', responseData);
    }
    
    // 잔액 업데이트
    setTimeout(updateBalance, 2000);
    
  } catch (error) {
    console.error('트랜잭션 실패:', error);
    showToast('트랜잭션 실패: ' + error.message);
    
    // 에러 응답 전송
    if (window.anam && window.anam.sendTransactionResponse) {
      const errorResponse = {
        error: error.message,
        from: currentWallet ? currentWallet.publicKey.toString() : null
      };
      window.anam.sendTransactionResponse(requestId, JSON.stringify(errorResponse));
    }
  }
}