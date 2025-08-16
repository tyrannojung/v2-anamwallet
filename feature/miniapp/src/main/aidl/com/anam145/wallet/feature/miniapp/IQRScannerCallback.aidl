// IQRScannerCallback.aidl
package com.anam145.wallet.feature.miniapp;

/**
 * QR 스캐너 결과 콜백 인터페이스
 */
interface IQRScannerCallback {
    /**
     * QR 코드 스캔 성공
     * 
     * @param qrData 스캔된 QR 코드의 원본 데이터
     */
    void onSuccess(String qrData);
    
    /**
     * QR 코드 스캔 실패 또는 취소
     * 
     * @param errorMessage 에러 메시지
     */
    void onError(String errorMessage);
}