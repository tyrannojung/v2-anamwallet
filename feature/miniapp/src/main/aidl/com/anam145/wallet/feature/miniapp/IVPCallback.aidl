// IVPCallback.aidl
package com.anam145.wallet.feature.miniapp;

/**
 * VP 생성 결과 콜백 인터페이스
 */
interface IVPCallback {
    /**
     * VP 생성 성공
     * @param vpJson VP JSON 문자열
     */
    void onSuccess(String vpJson);
    
    /**
     * VP 생성 실패
     * @param error 에러 메시지
     */
    void onError(String error);
}