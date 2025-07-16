// 사용 페이지 로직
console.log('부산카드 사용 페이지 로드');

// 카드 이미지 로드 실패 시 placeholder 표시
function showCardPlaceholder() {
  document.querySelector('.card-image').style.display = 'none';
  document.getElementById('card-placeholder').style.display = 'flex';
}

// QR 이미지 로드 실패 시 placeholder 표시
function showQRPlaceholder() {
  document.querySelector('.qr-image').style.display = 'none';
  document.getElementById('qr-placeholder').style.display = 'flex';
}

// QR 코드 새로고침
function refreshQR() {
  const qrWrapper = document.querySelector('.qr-wrapper');
  const refreshBtn = document.querySelector('.refresh-btn');
  
  // 버튼 비활성화
  refreshBtn.disabled = true;
  
  // 회전 애니메이션
  const refreshIcon = document.querySelector('.refresh-icon');
  refreshIcon.style.transform = 'rotate(360deg)';
  
  // QR 코드에 페이드 효과
  qrWrapper.style.opacity = '0.5';
  
  // 시뮬레이션: 1초 후 새로고침 완료
  setTimeout(() => {
    // QR 코드 복원
    qrWrapper.style.opacity = '1';
    
    // 버튼 활성화
    refreshBtn.disabled = false;
    
    // 아이콘 회전 초기화
    refreshIcon.style.transform = 'rotate(0deg)';
    
    // 성공 메시지
    showToast('QR 코드가 새로고침되었습니다');
    
    // 타이머 리셋
    resetTimer();
  }, 1000);
}

// 토스트 메시지 표시
function showToast(message) {
  // 기존 토스트 제거
  const existingToast = document.querySelector('.toast');
  if (existingToast) {
    existingToast.remove();
  }
  
  // 새 토스트 생성
  const toast = document.createElement('div');
  toast.className = 'toast';
  toast.textContent = message;
  toast.style.cssText = `
    position: fixed;
    bottom: 30px;
    left: 50%;
    transform: translateX(-50%);
    background: #2c3e50;
    color: white;
    padding: 15px 25px;
    border-radius: 30px;
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
    font-size: 14px;
    z-index: 1000;
    animation: toast-slide 0.3s ease;
  `;
  
  document.body.appendChild(toast);
  
  // 3초 후 제거
  setTimeout(() => {
    toast.style.opacity = '0';
    setTimeout(() => toast.remove(), 300);
  }, 3000);
}

// 타이머 리셋
let timerInterval;
function resetTimer() {
  // 기존 타이머 정리
  if (timerInterval) {
    clearInterval(timerInterval);
  }
  
  // 5분 타이머 시작
  let remainingTime = 300; // 5분 = 300초
  
  timerInterval = setInterval(() => {
    remainingTime--;
    
    if (remainingTime <= 0) {
      clearInterval(timerInterval);
      showToast('QR 코드가 만료되었습니다. 새로고침하세요.');
      
      // QR 코드 흐리게 표시
      document.querySelector('.qr-wrapper').style.opacity = '0.3';
    }
    
    // 남은 시간 업데이트
    const minutes = Math.floor(remainingTime / 60);
    const seconds = remainingTime % 60;
    document.querySelector('.info-subtext').textContent = 
      `유효시간: ${minutes}분 ${seconds.toString().padStart(2, '0')}초`;
  }, 1000);
}

// 뒤로가기
function goBack() {
  window.location.href = '../index/index.html';
}

// 페이지 로드 시 타이머 시작
window.addEventListener('DOMContentLoaded', () => {
  resetTimer();
});

// 토스트 애니메이션 CSS 추가
const style = document.createElement('style');
style.textContent = `
  @keyframes toast-slide {
    from {
      transform: translateX(-50%) translateY(100%);
      opacity: 0;
    }
    to {
      transform: translateX(-50%) translateY(0);
      opacity: 1;
    }
  }
`;
document.head.appendChild(style);