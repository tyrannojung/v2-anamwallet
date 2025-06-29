package com.anam145.wallet.feature.hub.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anam145.wallet.core.common.model.MiniApp
import com.anam145.wallet.core.ui.language.LocalStrings
import com.anam145.wallet.feature.hub.ui.components.MiniAppItem

/**
 * 허브 화면
 * 
 * 미니앱 허브 화면입니다.
 * 사용 가능한 모든 미니앱 목록을 표시합니다.
 */
@Composable
fun HubScreen(
    modifier: Modifier = Modifier,
    viewModel: HubViewModel = hiltViewModel()
) {
    val strings = LocalStrings.current
//    val installedModules: List<String> = listOf("installcoin1", "installcoin2")
//    val notInstalledModules: List<String> = listOf("uninstallcoin1", "uninstallcoin2")
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val installedMiniApp: List<MiniApp> = uiState.installedMiniApp
    val notInstalledModules: List<String> = listOf("uninstallcoin1", "uninstallcoin2")

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Text(
                        text = strings.installedModule,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(installedMiniApp) { module ->
                    MiniAppItem(moduleName = module.name, installed = true)
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = strings.notInstalledModule,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(notInstalledModules) { module ->
                    MiniAppItem(moduleName = module, installed = false)
                }
            }
        }
    }
}

