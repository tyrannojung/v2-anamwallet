package com.anam145.wallet.feature.miniapp;

/**
 * 블록체인 서비스로부터 응답을 받기 위한 콜백 인터페이스
 */
interface IBlockchainCallback {
    /**
     * 요청이 성공적으로 처리되었을 때 호출됩니다.
     * @param responseJson 응답 데이터 (JSON 형식)
     */
    void onSuccess(String responseJson);
    
    /**
     * 요청 처리 중 오류가 발생했을 때 호출됩니다.
     * @param errorJson 오류 정보 (JSON 형식)
     */
    void onError(String errorJson);
}