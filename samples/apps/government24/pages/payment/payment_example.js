// 활성화된 블록체인 확인 예시
function checkActiveBlockchain() {
    // window.anam.getActiveBlockchain() 호출
    const blockchainInfo = JSON.parse(window.anam.getActiveBlockchain());
    
    console.log('Active blockchain info:', blockchainInfo);
    // 결과 예시:
    // {
    //   "blockchainId": "com.anam.ethereum",
    //   "name": "Ethereum",
    //   "isActive": true
    // }
    // 또는 활성화된 블록체인이 없는 경우:
    // {
    //   "blockchainId": "",
    //   "name": "",
    //   "isActive": false
    // }
    
    return blockchainInfo;
}

// 결제 처리 전 블록체인 확인
function processTransactionWithCheck() {
    const blockchain = checkActiveBlockchain();
    
    if (!blockchain.isActive) {
        // 활성화된 블록체인이 없음
        showError('블록체인을 먼저 활성화해주세요.');
        return;
    }
    
    if (blockchain.blockchainId !== 'com.anam.ethereum') {
        // 이더리움이 아닌 다른 블록체인이 활성화됨
        showError(`현재 ${blockchain.name}이 활성화되어 있습니다. 이더리움으로 전환해주세요.`);
        return;
    }
    
    // 이더리움이 활성화되어 있음 - 결제 진행
    processTransaction();
}

// UI 업데이트 예시
function updatePaymentUI() {
    const blockchain = checkActiveBlockchain();
    const payButton = document.getElementById('pay-button');
    const statusMessage = document.getElementById('status-message');
    
    if (!blockchain.isActive) {
        payButton.disabled = true;
        statusMessage.textContent = '블록체인을 활성화해주세요';
        statusMessage.className = 'error';
    } else if (blockchain.blockchainId !== 'com.anam.ethereum') {
        payButton.disabled = true;
        statusMessage.textContent = `${blockchain.name}이 활성화되어 있습니다. 이더리움으로 전환해주세요.`;
        statusMessage.className = 'warning';
    } else {
        payButton.disabled = false;
        statusMessage.textContent = '이더리움으로 결제 가능합니다';
        statusMessage.className = 'success';
    }
}

// 페이지 로드 시 상태 확인
document.addEventListener('DOMContentLoaded', function() {
    updatePaymentUI();
});