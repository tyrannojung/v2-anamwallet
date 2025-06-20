package com.anam145.wallet

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * ANAM Wallet Application 클래스
 * 
 * Hilt를 사용하여 의존성 주입을 설정합니다.
 * @HiltAndroidApp 어노테이션은 Hilt의 코드 생성을 트리거하고
 * 애플리케이션 레벨의 의존성 컨테이너를 생성합니다.
 */
@HiltAndroidApp
class AnamWalletApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
    }
}