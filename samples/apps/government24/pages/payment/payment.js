// 결제 페이지 로직
console.log('결제 페이지 로드됨');

// 현재 선택된 결제 수단
let selectedMethod = 'blockchain';

// 이더리움 활성화 상태
let isEthereumActive = false;

// 페이지 로드 시 블록체인 상태 확인
document.addEventListener('DOMContentLoaded', function() {
    checkBlockchainStatus();
});

// 블록체인 상태 확인
// TODO: 이더리움 활성화 체크는 임시 하드코딩입니다. 
// 실제로는 정부24가 지원하는 블록체인 목록을 서버에서 받아와 확인해야 합니다.
function checkBlockchainStatus() {
    try {
        // 활성화된 블록체인 정보 가져오기
        const blockchainInfo = JSON.parse(window.anam.getActiveBlockchain());
        console.log('활성 블록체인 정보:', blockchainInfo);
        
        // 이더리움 활성화 여부 확인 (임시 하드코딩 - 이더리움만 지원)
        isEthereumActive = blockchainInfo.isActive && blockchainInfo.blockchainId === 'com.anam.ethereum';
        
        // UI 업데이트
        updateBlockchainPaymentUI(blockchainInfo);
    } catch (error) {
        console.error('블록체인 상태 확인 실패:', error);
        isEthereumActive = false;
        updateBlockchainPaymentUI({ isActive: false });
    }
}

// 블록체인 결제 UI 업데이트
// TODO: 이더리움만 지원하는 것은 임시 하드코딩입니다.
// 실제로는 정부24가 지원하는 여러 블록체인에 대한 처리가 필요합니다.
function updateBlockchainPaymentUI(blockchainInfo) {
    const blockchainMethod = document.querySelector('[data-method="blockchain"]');
    const methodInfo = blockchainMethod.querySelector('.method-info');
    const methodStatus = blockchainMethod.querySelector('.method-status');
    
    if (!blockchainInfo.isActive) {
        // 블록체인이 활성화되지 않음
        methodInfo.innerHTML = `
            <div class="method-name">블록체인 결제</div>
            <div class="method-amount" style="color: #e74c3c;">블록체인을 활성화해주세요</div>
        `;
        methodStatus.innerHTML = '';
        blockchainMethod.style.opacity = '0.6';
        blockchainMethod.style.cursor = 'not-allowed';
        
        // 다른 결제 수단으로 변경
        if (selectedMethod === 'blockchain') {
            selectPaymentMethod('phone');
        }
    } else if (blockchainInfo.blockchainId !== 'com.anam.ethereum') {
        // 이더리움이 아닌 다른 블록체인이 활성화됨
        // TODO: 임시 하드코딩 - 현재는 이더리움만 지원하지만, 
        // 향후 다른 블록체인도 지원할 수 있도록 확장 필요
        methodInfo.innerHTML = `
            <div class="method-name">블록체인 결제</div>
            <div class="method-amount" style="color: #f39c12;">이더리움으로 전환해주세요</div>
        `;
        methodStatus.innerHTML = `<span class="inactive-badge">${blockchainInfo.name} 활성</span>`;
        blockchainMethod.style.opacity = '0.6';
        blockchainMethod.style.cursor = 'not-allowed';
        
        // 다른 결제 수단으로 변경
        if (selectedMethod === 'blockchain') {
            selectPaymentMethod('phone');
        }
    } else {
        // 이더리움이 활성화됨
        methodInfo.innerHTML = `
            <div class="method-name">블록체인 결제</div>
            <div class="method-amount">Ethereum - 0.00000001 ETH</div>
        `;
        methodStatus.innerHTML = '<span class="active-badge">활성화</span>';
        blockchainMethod.style.opacity = '1';
        blockchainMethod.style.cursor = 'pointer';
    }
}

// 뒤로가기
function handleBack() {
    // 메인 페이지로 이동
    window.anam.navigateTo('pages/index/index');
}

// 결제 수단 선택
function selectPaymentMethod(method) {
    // 블록체인 결제를 선택하려는 경우 이더리움 활성화 확인
    // TODO: 임시 하드코딩 - 이더리움만 확인하고 있음
    if (method === 'blockchain' && !isEthereumActive) {
        console.log('이더리움이 활성화되지 않아 선택할 수 없습니다.');
        return;
    }
    
    // 이전 선택 제거 (border만 제거, 활성화 뱃지는 유지)
    document.querySelectorAll('.payment-method').forEach(methodEl => {
        methodEl.classList.remove('active');
    });
    
    // 새로운 선택 추가
    const selectedMethodEl = document.querySelector(`[data-method="${method}"]`);
    selectedMethodEl.classList.add('active');
    
    selectedMethod = method;
    console.log(`선택된 결제 수단: ${method}`);
}

// 결제 처리
function processPayment() {
    console.log(`${selectedMethod}으로 결제 처리 시작`);
    
    if (selectedMethod === 'blockchain') {
        // 이더리움 활성화 상태 재확인
        // TODO: 임시 하드코딩 - 현재는 이더리움만 지원
        if (!isEthereumActive) {
            alert('이더리움으로 전환 후 결제해주세요.');
            return;
        }
        
        // 블록체인 결제 처리
        const transactionData = {
            to: '0x8091C2fD8a79a9EF812d487052496243f6825B02', // 정부24 수신 주소
            amount: '0.00000001', // ETH
            data: '0x' // 빈 데이터 (16진수 형식)
        };
        
        console.log('트랜잭션 요청:', JSON.stringify(transactionData, null, 2));
        
        // 트랜잭션 응답 이벤트 리스너 등록
        window.addEventListener('transactionResponse', handleTransactionResponse, { once: true });
        
        // JavaScript Bridge를 통해 트랜잭션 요청
        if (window.anam && window.anam.requestTransaction) {
            window.anam.requestTransaction(JSON.stringify(transactionData));
        } else {
            alert('결제 기능을 사용할 수 없습니다.');
            window.removeEventListener('transactionResponse', handleTransactionResponse);
        }
    } else {
        // 다른 결제 수단은 아직 미구현
        alert(`${selectedMethod} 결제는 아직 구현되지 않았습니다.`);
    }
}

// 트랜잭션 응답 처리
function handleTransactionResponse(event) {
    console.log('트랜잭션 응답:', JSON.stringify(event.detail, null, 2));
    
    const response = event.detail;
    
    if (response.error) {
        // Toast로만 표시하므로 alert 제거
        console.log('트랜잭션 실패:', response.error);
    } else if (response.status === 'success' && response.txHash) {
        // 성공 페이지로 이동
        const txHash = response.txHash;
        const params = new URLSearchParams({
            txHash: txHash,
            amount: response.amount || '0.00000001',
            chainId: response.chainId || '11155111'
        });
        
        // navigateTo API 사용 (manifest 검증 포함)
        window.anam.navigateTo('pages/success/success?' + params.toString());
    }
}