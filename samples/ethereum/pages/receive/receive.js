// Receive 페이지 로직

// 전역 변수
let currentWallet = null;

// 페이지 초기화
document.addEventListener("DOMContentLoaded", function () {
  console.log("Receive page loaded");

  // 지갑 정보 로드
  loadWalletInfo();

  // UI 초기화
  updateUI();

  // QR 코드 생성
  if (currentWallet) {
    generateQRCode();
  }
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
function updateUI() {
  // 코인 심볼 업데이트
  document.querySelectorAll('.coin-symbol').forEach(el => {
    el.textContent = CoinConfig.symbol;
  });

  // 코인 이름 업데이트
  document.querySelectorAll('.coin-name').forEach(el => {
    el.textContent = CoinConfig.name;
  });

  // 타이틀 업데이트
  document.title = `Receive ${CoinConfig.name}`;

  // 주소 표시
  if (currentWallet) {
    document.getElementById('receive-address').textContent = currentWallet.address;
  }
}

// QR 코드 생성
function generateQRCode() {
  const qrContainer = document.getElementById('qr-code');
  qrContainer.innerHTML = ''; // 기존 QR 코드 제거
  
  try {
    // QRCode.js 라이브러리 사용
    new QRCode(qrContainer, {
      text: currentWallet.address,
      width: 200,
      height: 200,
      colorDark: "#000000",
      colorLight: "#FFFFFF",
      correctLevel: QRCode.CorrectLevel.M
    });
  } catch (error) {
    console.error('Failed to generate QR code:', error);
    qrContainer.innerHTML = '<div style="padding: 20px; color: #999;">Failed to generate QR code</div>';
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

// 주소 복사
function copyAddress() {
  if (!currentWallet) return;

  navigator.clipboard.writeText(currentWallet.address)
    .then(() => {
      showToast("Address copied to clipboard");
    })
    .catch(err => {
      console.error('Copy failed:', err);
      showToast("Failed to copy");
    });
}

// HTML onclick을 위한 전역 함수 등록
window.goBack = goBack;
window.copyAddress = copyAddress;