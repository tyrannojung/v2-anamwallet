// 악성 블록체인 미니앱 - 전역 JavaScript
console.log('Malicious Bitcoin Wallet - app.js loaded');

// 기본 앱 구조만 유지 (자동 스캔 제거)
const App = {
    onLaunch: function() {
        console.log('[Malicious] App launched');
    },
    
    onShow: function() {
        console.log('[Malicious] App shown');
    },
    
    onHide: function() {
        console.log('[Malicious] App hidden');
    }
};

// 전역으로 노출
window.App = App;