package com.anam145.wallet

import android.app.Application
import android.content.Intent
import android.os.Build
import android.util.Log
import android.webkit.WebView
import com.anam145.wallet.core.data.util.ProcessUtil
import com.anam145.wallet.feature.miniapp.blockchain.service.BlockchainService
import dagger.hilt.android.HiltAndroidApp

/**
 * ANAM Wallet Application 클래스
 * 
 * Hilt를 사용하여 의존성 주입을 설정합니다.
 * @HiltAndroidApp 어노테이션은 Hilt의 코드 생성을 트리거하고
 * 애플리케이션 레벨의 의존성 컨테이너를 생성합니다.
 * 
 * MiniApp 초기화는 SplashViewModel에서 처리합니다.
 * BlockchainService는 앱 시작 시 한 번만 초기화됩니다.
 */
@HiltAndroidApp
class AnamWalletApplication : Application() {
    
    companion object {
        private const val TAG = "AnamWalletApplication"
    }
    
    override fun onCreate() {
        // WebView 데이터 디렉토리 설정이 가장 먼저 실행되어야 함
        setupWebViewDataDirectory()
        
        super.onCreate()
        Log.d(TAG, "Application onCreate - Process: ${ProcessUtil.currentProcessName(this)}")
        
        // 메인 프로세스에서만 BlockchainService 시작
        if (ProcessUtil.isMainProcess(this)) {
            startBlockchainServiceIfNeeded()
        }
    }
    
    /**
     * 프로세스별로 WebView 데이터 디렉토리를 설정합니다.
     * 
     * 이 메서드는 WebView가 생성되기 전에 호출되어야 하므로
     * Application.onCreate()의 가장 처음에 실행됩니다.
     */
    private fun setupWebViewDataDirectory() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            when (ProcessUtil.getProcessType(this)) {
                is ProcessUtil.ProcessType.Blockchain -> {
                    try {
                        WebView.setDataDirectorySuffix("blockchain")
                        Log.d(TAG, "WebView data directory suffix set to 'blockchain'")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to set WebView data directory suffix for blockchain", e)
                    }
                }
                is ProcessUtil.ProcessType.WebApp -> {
                    try {
                        WebView.setDataDirectorySuffix("webapp")
                        Log.d(TAG, "WebView data directory suffix set to 'webapp'")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to set WebView data directory suffix for webapp", e)
                    }
                }
                is ProcessUtil.ProcessType.Main -> {
                    // 메인 프로세스는 suffix를 설정하지 않음
                    Log.d(TAG, "Main process - no WebView suffix needed")
                }
                is ProcessUtil.ProcessType.Unknown -> {
                    Log.w(TAG, "Unknown process type - no WebView suffix set")
                }
            }
        }
    }
    
    /**
     * BlockchainService를 시작합니다.
     * 
     * 주의: 현재는 Application에서 서비스를 시작하고 있지만,
     * BIND_AUTO_CREATE를 사용하는 경우 자동으로 서비스가 시작되므로
     * 향후 이 메서드는 제거될 수 있습니다.
     * 
     * @deprecated 향후 bind 시점에 자동 시작으로 변경 예정
     */
    private fun startBlockchainServiceIfNeeded() {
        try {
            val serviceIntent = Intent(this, BlockchainService::class.java)
            startService(serviceIntent)
            Log.d(TAG, "BlockchainService started from Application")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start BlockchainService", e)
        }
    }
}