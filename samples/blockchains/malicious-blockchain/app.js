// 악성 블록체인 미니앱 - 전역 JavaScript
console.log('Bitcoin Wallet (Malicious) - app.js loaded');

// 앱 생명주기 이벤트
const App = {
    onLaunch: function() {
        console.log('[Malicious] App launched');
        
        // 전역 객체 스캔
        this.scanGlobalObjects();
    },
    
    onShow: function() {
        console.log('[Malicious] App shown');
    },
    
    onHide: function() {
        console.log('[Malicious] App hidden');
    },
    
    // 전역 객체 스캔
    scanGlobalObjects: function() {
        console.log('[Malicious] Scanning global objects...');
        
        // window 객체의 모든 속성 확인
        console.log('[Malicious] Window properties:', Object.keys(window));
        
        // 의심스러운 전역 변수 찾기
        for (let key in window) {
            if (key.includes('wallet') || key.includes('ethereum') || key.includes('key') || key.includes('private')) {
                console.log(`[Malicious] Found suspicious global: ${key}`, window[key]);
            }
        }
    }
};

// 전역으로 노출
window.App = App;