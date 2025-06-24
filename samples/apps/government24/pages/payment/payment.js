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
        const paymentData = {
            to: '0x8091C2fD8a79a9EF812d487052496243f6825B02', // 정부24 수신 주소
            amount: '0.00000001', // ETH
            data: '0x' // 빈 데이터 (16진수 형식)
        };
        
        console.log('결제 요청:', JSON.stringify(paymentData, null, 2));
        
        // 결제 응답 이벤트 리스너 등록
        window.addEventListener('paymentResponse', handlePaymentResponse, { once: true });
        
        // JavaScript Bridge를 통해 결제 요청
        if (window.anam && window.anam.requestPayment) {
            window.anam.requestPayment(JSON.stringify(paymentData));
        } else {
            alert('결제 기능을 사용할 수 없습니다.');
            window.removeEventListener('paymentResponse', handlePaymentResponse);
        }
    } else {
        // 다른 결제 수단은 아직 미구현
        alert(`${selectedMethod} 결제는 아직 구현되지 않았습니다.`);
    }
}

// 결제 응답 처리
function handlePaymentResponse(event) {
    console.log('결제 응답:', JSON.stringify(event.detail, null, 2));
    
    const response = event.detail;
    
    if (response.error) {
        // Toast로만 표시하므로 alert 제거
        console.log('결제 실패:', response.error);
    } else if (response.status === 'success' && (response.transactionId || response.data?.txHash)) {
        // 성공 페이지로 이동
        const txHash = response.transactionId || response.data.txHash;
        const params = new URLSearchParams({
            txHash: txHash,
            amount: response.data?.amount || '0.00000001',
            chainId: response.data?.chainId || '11155111'
        });
        
        // navigateTo API 사용 (manifest 검증 포함)
        window.anam.navigateTo('pages/success/success?' + params.toString());
    }
}