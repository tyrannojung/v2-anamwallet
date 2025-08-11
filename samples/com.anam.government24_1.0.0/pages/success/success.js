// 성공 페이지 로직
console.log('Payment success page loaded');

// URL 파라미터에서 트랜잭션 정보 가져오기
function getTransactionInfo() {
    const params = new URLSearchParams(window.location.search);
    return {
        txHash: params.get('txHash'),
        amount: params.get('amount') || '0.00000001',
        chainId: params.get('chainId') || '11155111'
    };
}

// 페이지 초기화
function initializePage() {
    const txInfo = getTransactionInfo();
    
    // 트랜잭션 해시 표시
    if (txInfo.txHash) {
        const txHashElement = document.getElementById('tx-hash');
        txHashElement.textContent = txInfo.txHash;
    }
    
    // 금액 표시
    if (txInfo.amount) {
        const amountElement = document.getElementById('payment-amount');
        amountElement.textContent = txInfo.amount + ' ETH';
    }
    
    // 네트워크 표시
    const networkElement = document.getElementById('network-name');
    if (txInfo.chainId === '11155111') {
        networkElement.textContent = 'Ethereum Sepolia';
    } else if (txInfo.chainId === '1') {
        networkElement.textContent = 'Ethereum Mainnet';
    }
}

// 트랜잭션 해시 복사
function copyTxHash() {
    const txHashElement = document.getElementById('tx-hash');
    const txHash = txHashElement.textContent;
    
    if (txHash && txHash !== '-') {
        // 클립보드에 복사
        navigator.clipboard.writeText(txHash).then(() => {
            showToast('Transaction hash copied');
        }).catch(() => {
            // 폴백: 구식 방법
            const textArea = document.createElement('textarea');
            textArea.value = txHash;
            document.body.appendChild(textArea);
            textArea.select();
            document.execCommand('copy');
            document.body.removeChild(textArea);
            showToast('Transaction hash copied');
        });
    }
}


// 홈으로 돌아가기
function handleHome() {
    // Government24 메인 페이지로 이동
    window.location.href = '../index/index.html';
}

// 토스트 메시지 표시
function showToast(message) {
    const existingToast = document.querySelector('.toast');
    if (existingToast) {
        existingToast.remove();
    }
    
    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.textContent = message;
    document.body.appendChild(toast);
    
    setTimeout(() => {
        toast.remove();
    }, 2000);
}

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', initializePage);