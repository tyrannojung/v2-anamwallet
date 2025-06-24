// IWebAppService.aidl
package com.anam145.wallet.feature.miniapp;

import com.anam145.wallet.feature.miniapp.IBlockchainCallback;

/**
 * WebApp 프로세스와 메인 프로세스 간 통신을 위한 AIDL 인터페이스
 * 
 * 이 인터페이스는 일반 웹앱(정부24 등)이 블록체인 서비스에 접근하거나
 * 결제 요청을 할 때 사용됩니다.
 */
interface IWebAppService {
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
     * 웹앱 서비스가 준비되었는지 확인
     * 
     * @return 준비 상태
     */
    boolean isReady();
}