// 충전 페이지 로직
console.log('부산카드 충전 페이지 로드');

// 환율 (예시)
const EXCHANGE_RATE = 1450; // 1 USD = 1450 KRW
const ETH_PRICE = 2500; // 1 ETH = 2500 USD
const GAS_FEE = 0.002; // ETH

// 블록체인 정보 로드
async function loadBlockchainBalance() {
  try {
    // 항상 이더리움을 활성 블록체인으로 표시
    const balanceDiv = document.getElementById('blockchain-balance');
    
    // 이더리움 데이터 하드코딩
    const data = { 
      name: 'Ethereum', 
      symbol: 'ETH', 
      balance: '0.5',
      icon: 'ETH',
      color: '#627eea'
    };
    
    balanceDiv.innerHTML = `
      <div class="blockchain-balance">
        <img src="../../assets/images/ethereum.png" alt="Ethereum" class="chain-icon">
        <div class="chain-info">
          <div class="chain-name">${data.name}</div>
          <div class="chain-balance">${data.balance} ${data.symbol}</div>
        </div>
      </div>
    `;
  } catch (error) {
    console.error('블록체인 잔액 로드 실패:', error);
    document.getElementById('blockchain-balance').innerHTML = `
      <p style="text-align: center; color: #e74c3c;">잔액 정보를 불러올 수 없습니다.</p>
    `;
  }
}

// 충전 금액 계산
function calculateCharge() {
  const amountInput = document.getElementById('charge-amount');
  const amount = parseFloat(amountInput.value) || 0;
  
  if (amount >= 1) {
    // 충전 정보 표시
    document.getElementById('charge-info').style.display = 'block';
    
    // 필요 ETH 계산
    const ethAmount = amount / ETH_PRICE;
    document.getElementById('estimated-eth').textContent = `${ethAmount.toFixed(6)} ETH`;
    
    // 네트워크 수수료
    document.getElementById('blockchain-fee').textContent = `${GAS_FEE} ETH`;
    
    // 총 필요 ETH
    const totalEth = ethAmount + GAS_FEE;
    document.getElementById('total-amount').textContent = `${totalEth.toFixed(6)} ETH`;
    
    // 충전 버튼 활성화
    document.getElementById('charge-btn').disabled = false;
  } else {
    // 충전 정보 숨기기
    document.getElementById('charge-info').style.display = 'none';
    document.getElementById('charge-btn').disabled = true;
  }
}

// 충전 처리
async function processCharge() {
  const amount = parseFloat(document.getElementById('charge-amount').value);
  
  if (amount < 1 || amount > 1000) {
    alert('충전 금액은 $1 ~ $1,000 사이여야 합니다.');
    return;
  }
  
  // 충전 버튼 비활성화
  const chargeBtn = document.getElementById('charge-btn');
  chargeBtn.disabled = true;
  chargeBtn.textContent = '처리 중...';
  
  // 실제로는 여기서 블록체인 트랜잭션을 실행하겠지만
  // 데모이므로 시뮬레이션만 진행
  setTimeout(() => {
    // 성공 메시지 표시
    showSuccessMessage();
    
    // 2초 후 메인 페이지로 이동
    setTimeout(() => {
      window.location.href = '../index/index.html';
    }, 2000);
  }, 1500);
}

// 성공 메시지 표시
function showSuccessMessage() {
  const successDiv = document.createElement('div');
  successDiv.className = 'success-message';
  successDiv.innerHTML = `
    <div class="success-icon">✅</div>
    <div class="success-text">충전이 완료되었습니다!</div>
  `;
  document.body.appendChild(successDiv);
}

// 뒤로가기
function goBack() {
  window.location.href = '../index/index.html';
}

// 페이지 로드 시 실행
window.addEventListener('DOMContentLoaded', () => {
  loadBlockchainBalance();
});