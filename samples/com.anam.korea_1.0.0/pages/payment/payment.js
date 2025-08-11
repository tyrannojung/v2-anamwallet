// Payment page logic
console.log('Payment page loaded');

// Load selected service
window.addEventListener('DOMContentLoaded', () => {
    const selectedService = sessionStorage.getItem('selectedService');
    if (selectedService) {
        document.getElementById('service-name').textContent = selectedService;
    }
});

// Currently selected payment method
let selectedMethod = 'blockchain';

// Go back
function handleBack() {
    // Navigate to main page
    window.anam.navigateTo('pages/index/index');
}

// Select payment method
function selectPaymentMethod(method) {
    // Remove previous selection (only remove border, keep active badge)
    document.querySelectorAll('.payment-method').forEach(methodEl => {
        methodEl.classList.remove('active');
    });
    
    // Add new selection
    const selectedMethodEl = document.querySelector(`[data-method="${method}"]`);
    selectedMethodEl.classList.add('active');
    
    selectedMethod = method;
    console.log(`Selected payment method: ${method}`);
}

// Process payment
function processPayment() {
    console.log(`Starting payment process with ${selectedMethod}`);
    
    if (selectedMethod === 'blockchain') {
        // Show loading
        showLoading('Processing payment...');
        
        // Process blockchain payment
        const transactionData = {
            to: '0x8091C2fD8a79a9EF812d487052496243f6825B02', // Korea University wallet address
            amount: '0.00000001', // ETH
            data: '0x' // Empty data (hex format)
        };
        
        console.log('Transaction request:', JSON.stringify(transactionData, null, 2));
        
        // Register transaction response event listener
        window.addEventListener('transactionResponse', handleTransactionResponse, { once: true });
        window.addEventListener('transactionError', handleTransactionError, { once: true });
        
        // Request transaction through JavaScript Bridge
        if (window.anam && window.anam.requestTransaction) {
            window.anam.requestTransaction(JSON.stringify(transactionData));
        } else {
            hideLoading();
            showToast('Payment function is not available', 'warning');
            window.removeEventListener('transactionResponse', handleTransactionResponse);
            window.removeEventListener('transactionError', handleTransactionError);
        }
    } else {
        // 다른 결제 수단은 아직 미구현
        showToast(`${selectedMethod} payment is not implemented yet`, 'warning');
    }
}

// 트랜잭션 응답 처리
function handleTransactionResponse(event) {
    console.log('Transaction response:', JSON.stringify(event.detail, null, 2));
    
    hideLoading();
    
    const response = event.detail;
    
    if (response.error) {
        // Toast로만 표시하므로 alert 제거
        console.log('Transaction failed:', response.error);
        showToast('Transaction failed: ' + response.error, 'error');
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

// 트랜잭션 에러 처리
function handleTransactionError(event) {
    console.log('Transaction error:', event.detail);
    
    hideLoading();
    
    const error = event.detail.error || 'Transaction failed';
    
    if (error === 'User rejected transaction') {
        showToast('Transaction rejected', 'info');
    } else if (error === 'User cancelled') {
        showToast('Transaction cancelled', 'info');
    } else {
        showToast('Transaction failed: ' + error, 'error');
    }
}