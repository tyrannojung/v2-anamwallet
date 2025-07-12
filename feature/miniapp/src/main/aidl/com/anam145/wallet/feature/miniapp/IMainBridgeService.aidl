// IMainBridgeService.aidl
package com.anam145.wallet.feature.miniapp;

import com.anam145.wallet.feature.miniapp.IBlockchainCallback;
import com.anam145.wallet.feature.miniapp.IKeystoreCallback;
import com.anam145.wallet.feature.miniapp.IKeystoreDecryptCallback;

/**
 * 메인 브릿지 서비스와 통신하기 위한 AIDL 인터페이스
 * 
 * 웹앱 프로세스(:app)와 블록체인 프로세스(:blockchain) 간의
 * 통신을 중개하는 메인 프로세스의 브릿지 서비스 인터페이스입니다.
 */
interface IMainBridgeService {
    /**
     * 웹앱에서 블록체인 트랜잭션 요청
     * 
     * @param requestJson 트랜잭션 요청 정보 (JSON 형식)
     *                    - requestId: 요청 ID
     *                    - blockchainId: 사용할 블록체인 ID
     *                    - transactionData: 트랜잭션 데이터
     * @param callback 결과 콜백
     */
    void requestTransaction(String requestJson, IBlockchainCallback callback);
    
    /**
     * 활성화된 블록체인 ID 조회
     * 
     * @return 현재 활성화된 블록체인 ID
     */
    String getActiveBlockchainId();
    
    /**
     * 메인 브릿지 서비스가 준비되었는지 확인
     * 
     * @return 준비 상태
     */
    boolean isReady();
    
    /**
     * 키스토어 생성 요청
     * 
     * @param privateKey 개인키 (16진수 문자열)
     * @param address 지갑 주소
     * @param callback 결과 콜백
     */
    void createKeystore(String privateKey, String address, IKeystoreCallback callback);
    
    /**
     * 키스토어 복호화 요청
     * 
     * @param keystoreJson 암호화된 키스토어 (JSON 문자열)
     * @param callback 결과 콜백
     */
    void decryptKeystore(String keystoreJson, IKeystoreDecryptCallback callback);
}