// 결제 페이지 로직
console.log('결제 페이지 로드됨');

// 현재 선택된 결제 수단
let selectedMethod = 'blockchain';

// 뒤로가기
function handleBack() {
    // 메인 페이지로 이동
    window.anam.navigateTo('pages/index/index');
}

// 결제 수단 선택
function selectPaymentMethod(method) {
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