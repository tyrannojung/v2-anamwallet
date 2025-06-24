package com.anam145.wallet.feature.miniapp;

import com.anam145.wallet.feature.miniapp.IBlockchainCallback;

/**
 * 블록체인 서비스와 통신하기 위한 AIDL 인터페이스
 * 
 * 프로세스 간 통신을 위해 모든 데이터는 JSON String으로 전달됩니다.
 */
interface IBlockchainService {
    /**
     * 블록체인을 전환합니다.
     * @param blockchainId 활성화할 블록체인 ID (예: "com.anam.ethereum")
     */
    void switchBlockchain(String blockchainId);
    
    /**
     * 현재 활성화된 블록체인 ID를 반환합니다.
     */
    String getActiveBlockchainId();
    
    /**
     * 블록체인에 요청을 전송합니다. (결제, 서명 등)
     * @param requestJson 요청 데이터 (JSON 형식)
     * @param callback 응답을 받을 콜백
     */
    void processRequest(String requestJson, IBlockchainCallback callback);
    
    /**
     * 블록체인 서비스가 준비되었는지 확인합니다.
     */
    boolean isReady();
}