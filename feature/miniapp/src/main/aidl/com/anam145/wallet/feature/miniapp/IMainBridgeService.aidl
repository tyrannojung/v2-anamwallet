// IMainBridgeService.aidl
package com.anam145.wallet.feature.miniapp;

import com.anam145.wallet.feature.miniapp.IBlockchainCallback;

/**
 * 메인 브릿지 서비스와 통신하기 위한 AIDL 인터페이스
 * 
 * 웹앱 프로세스(:app)와 블록체인 프로세스(:blockchain) 간의
 * 통신을 중개하는 메인 프로세스의 브릿지 서비스 인터페이스입니다.
 */
interface IMainBridgeService {
    /**
     * 웹앱에서 블록체인 결제 요청
     * 
     * @param requestJson 결제 요청 정보 (JSON 형식)
     *                    - blockchainId: 사용할 블록체인 ID
     *                    - amount: 결제 금액
     *                    - recipient: 수신자 주소
     *                    - memo: 메모 (선택)
     * @param callback 결과 콜백
     */
    void requestPayment(String requestJson, IBlockchainCallback callback);
    
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
}