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
import com.anam145.wallet.feature.miniapp.data.common.MiniAppFileManager
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
 */
@AndroidEntryPoint
class BlockchainActivity : ComponentActivity() {
    
    companion object {
        const val EXTRA_BLOCKCHAIN_ID = "blockchain_id"
        
        fun createIntent(context: Context, blockchainId: String): Intent {
            return Intent(context, BlockchainActivity::class.java).apply {
                putExtra(EXTRA_BLOCKCHAIN_ID, blockchainId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }
    }
    
    @Inject
    lateinit var fileManager: MiniAppFileManager
    
    private val viewModel: BlockchainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val blockchainId = intent.getStringExtra(EXTRA_BLOCKCHAIN_ID) ?: ""
        
        // Effect 처리
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effect
                    .onEach { effect ->
                        when (effect) {
                            is BlockchainContract.Effect.NavigateBack -> {
                                finish()
                            }
                            is BlockchainContract.Effect.ShowError -> {
                                Toast.makeText(this@BlockchainActivity, effect.message, Toast.LENGTH_SHORT).show()
                            }
                            is BlockchainContract.Effect.LoadUrl -> {
                                // WebView에서 처리
                            }
                        }
                    }
                    .launchIn(this)
            }
        }
        
        setContent {
            AnamWalletTheme {
                BlockchainScreen(
                    blockchainId = blockchainId,
                    viewModel = viewModel,
                    fileManager = fileManager
                )
            }
        }
    }
}