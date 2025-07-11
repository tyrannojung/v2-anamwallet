// IKeystoreCallback.aidl
package com.anam145.wallet.feature.miniapp;

/**
 * 키스토어 생성 결과를 전달하기 위한 콜백 인터페이스
 * 
 * 블록체인 프로세스(:blockchain)에서 메인 프로세스로
 * 키스토어 생성 결과를 전달할 때 사용됩니다.
 */
interface IKeystoreCallback {
    /**
     * 키스토어 생성 성공
     * 
     * @param keystoreJson 생성된 키스토어 (JSON 형식)
     *                     Web3 호환 키스토어 v3 형식
     */
    void onSuccess(String keystoreJson);
    
    /**
     * 키스토어 생성 실패
     * 
     * @param errorMessage 에러 메시지
     */
    void onError(String errorMessage);
}