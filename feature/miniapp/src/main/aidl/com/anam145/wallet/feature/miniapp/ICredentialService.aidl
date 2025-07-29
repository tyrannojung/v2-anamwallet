// ICredentialService.aidl
package com.anam145.wallet.feature.miniapp;

import com.anam145.wallet.feature.miniapp.IVPCallback;

/**
 * 신분증 관련 서비스 인터페이스
 * 
 * Main 프로세스에서 실행되는 서비스와 통신하기 위한 AIDL 인터페이스입니다.
 */
interface ICredentialService {
    /**
     * 모든 신분증 정보를 조회합니다.
     * 
     * @return JSON 형식의 신분증 목록
     */
    String getCredentials();
    
    /**
     * VP(Verifiable Presentation)를 생성합니다.
     * 
     * @param credentialId 선택된 신분증 ID
     * @param challenge VP 요청의 challenge
     * @param callback VP 생성 결과 콜백
     */
    void createVP(String credentialId, String challenge, IVPCallback callback);
}