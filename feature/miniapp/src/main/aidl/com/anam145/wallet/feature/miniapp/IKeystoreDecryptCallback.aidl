// IKeystoreDecryptCallback.aidl
package com.anam145.wallet.feature.miniapp;

/**
 * 키스토어 복호화 결과를 전달하기 위한 콜백 인터페이스
 * 
 * 블록체인 프로세스(:blockchain)에서 메인 프로세스로
 * 키스토어 복호화 결과를 전달할 때 사용됩니다.
 */
interface IKeystoreDecryptCallback {
    /**
     * 키스토어 복호화 성공
     * 
     * @param address 지갑 주소
     * @param privateKey 복호화된 개인키 (16진수 문자열)
     */
    void onSuccess(String address, String privateKey);
    
    /**
     * 키스토어 복호화 실패
     * 
     * @param errorMessage 에러 메시지
     */
    void onError(String errorMessage);
}