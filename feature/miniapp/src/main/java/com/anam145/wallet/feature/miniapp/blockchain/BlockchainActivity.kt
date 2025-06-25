package com.anam145.wallet.feature.miniapp.blockchain

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import com.anam145.wallet.core.ui.theme.AnamWalletTheme
import com.anam145.wallet.feature.miniapp.common.data.common.MiniAppFileManager
import com.anam145.wallet.feature.miniapp.blockchain.ui.BlockchainContract
import com.anam145.wallet.feature.miniapp.blockchain.ui.BlockchainScreen
import com.anam145.wallet.feature.miniapp.blockchain.ui.BlockchainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 블록체인 미니앱을 표시하는 Activity
 * 
 * 블록체인 프로세스(:blockchain)에서 실행되며,
 * BlockchainService와 통신하여 블록체인 상태를 동기화합니다.
 * 
 * 프로세스 구조:
 * - Main 프로세스: MainActivity가 실행되는 주 프로세스
 * - :app 프로세스: WebAppActivity가 실행되는 일반 앱 프로세스
 * - :blockchain 프로세스: BlockchainActivity가 실행되는 블록체인 전용 프로세스
 * 
 * 이렇게 프로세스를 분리하는 이유:
 * 1. 보안: 각 미니앱의 WebView가 격리된 환경에서 실행됨
 * 2. 안정성: 한 프로세스가 크래시해도 다른 프로세스에 영향 없음
 * 3. 리소스 관리: 각 프로세스의 메모리를 독립적으로 관리
 */
@AndroidEntryPoint  // Hilt가 의존성 주입을 수행할 수 있도록 표시
class BlockchainActivity : ComponentActivity() {
    
    companion object {
        // Intent에 블록체인 ID를 전달하기 위한 키
        const val EXTRA_BLOCKCHAIN_ID = "blockchain_id"
        
        /**
         * BlockchainActivity를 시작하기 위한 Intent 생성
         * 
         * @param context 시작하는 컨텍스트
         * @param blockchainId 표시할 블록체인의 ID (예: "com.anam.ethereum")
         * @return 설정된 Intent
         */
        fun createIntent(context: Context, blockchainId: String): Intent {
            return Intent(context, BlockchainActivity::class.java).apply {
                putExtra(EXTRA_BLOCKCHAIN_ID, blockchainId)
                // FLAG_ACTIVITY_NEW_TASK: 새로운 태스크에서 실행 (다른 프로세스이므로 필수)
                // FLAG_ACTIVITY_CLEAR_TOP: 이미 실행 중이면 기존 것을 재사용
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }
    }
    
    // Hilt로 주입받는 파일 매니저 (미니앱 파일 접근용)
    @Inject
    lateinit var fileManager: MiniAppFileManager
    
    // ViewModel은 Hilt가 자동으로 생성하고 관리
    // by viewModels()는 Activity 스코프의 ViewModel을 생성
    private val viewModel: BlockchainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Intent에서 블록체인 ID 추출
        val blockchainId = intent.getStringExtra(EXTRA_BLOCKCHAIN_ID) ?: ""
        
        // Effect(부수효과) 처리를 위한 코루틴 설정
        // Effect는 UI에 직접적으로 영향을 주지 않는 일회성 이벤트
        // 예: 네비게이션, 토스트 메시지, 외부 앱 실행 등
        lifecycleScope.launch {
            // repeatOnLifecycle: 생명주기에 맞춰 코루틴을 자동으로 시작/중지
            // STARTED 상태: Activity가 사용자에게 보이는 상태
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effect
                    .onEach { effect ->
                        // ViewModel에서 발생한 Effect를 처리
                        when (effect) {
                            // 뒤로가기 효과: Activity 종료
                            is BlockchainContract.Effect.NavigateBack -> {
                                finish()
                            }
                            // 에러 표시 효과: 토스트 메시지
                            is BlockchainContract.Effect.ShowError -> {
                                Toast.makeText(this@BlockchainActivity, effect.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .launchIn(this)  // 현재 코루틴 스코프에서 Flow 수집 시작
            }
        }
        
        // Compose UI 설정
        setContent {
            // 앱 전체 테마 적용
            AnamWalletTheme {
                // 블록체인 화면 컴포저블
                BlockchainScreen(
                    blockchainId = blockchainId,
                    viewModel = viewModel,
                    fileManager = fileManager  // WebView가 미니앱 파일에 접근하기 위해 필요
                )
            }
        }
    }
}