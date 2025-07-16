// 부산카드 미니앱 생명주기 정의

window.App = {
  onLaunch() {
    console.log('부산카드 미니앱 시작');
    // 초기화 로직
    this.initializeApp();
  },
  
  onShow() {
    console.log('부산카드 미니앱 표시');
    // 앱이 포그라운드로 돌아올 때
    this.refreshData();
  },
  
  onHide() {
    console.log('부산카드 미니앱 숨김');
    // 앱이 백그라운드로 갈 때
    this.saveState();
  },
  
  // 앱 초기화
  initializeApp() {
    // 로컬 스토리지에서 데이터 로드
    const savedData = localStorage.getItem('busancard_data');
    if (savedData) {
      window.busanCardData = JSON.parse(savedData);
    } else {
      window.busanCardData = {
        balanceUSD: 850.00,
        balanceKRW: 1234567,
        lastUpdated: new Date().toISOString()
      };
    }
  },
  
  // 데이터 새로고침
  refreshData() {
    // 실제로는 서버에서 최신 데이터를 가져올 것
    console.log('데이터 새로고침');
  },
  
  // 상태 저장
  saveState() {
    if (window.busanCardData) {
      localStorage.setItem('busancard_data', JSON.stringify(window.busanCardData));
    }
  }
};

// ANAM Bridge API 정의
window.anam = window.anam || {
  // 활성 블록체인 ID 가져오기
  getActiveBlockchainId: async function() {
    return new Promise((resolve) => {
      // 항상 이더리움을 반환하도록 하드코딩
      setTimeout(() => {
        resolve('com.anam.ethereum');
      }, 100);
    });
  },
  
  // 트랜잭션 요청
  requestTransaction: async function(data) {
    return new Promise((resolve, reject) => {
      console.log('트랜잭션 요청:', data);
      // 실제로는 네이티브 브리지를 통해 처리
      setTimeout(() => {
        resolve({
          status: 'success',
          txHash: '0x' + Math.random().toString(36).substr(2, 9)
        });
      }, 1000);
    });
  }
};