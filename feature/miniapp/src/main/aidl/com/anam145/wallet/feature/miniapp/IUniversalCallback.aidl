// IUniversalCallback.aidl
package com.anam145.wallet.feature.miniapp;

/**
 * Universal Bridge 콜백 인터페이스
 * 
 * Universal Bridge 요청에 대한 응답을 처리합니다.
 */
interface IUniversalCallback {
    /**
     * 요청 성공 시 호출
     * 
     * @param requestId 요청 ID
     * @param result JSON 형태의 응답 데이터
     */
    void onSuccess(String requestId, String result);
    
    /**
     * 요청 실패 시 호출
     * 
     * @param requestId 요청 ID
     * @param error 에러 메시지
     */
    void onError(String requestId, String error);
}