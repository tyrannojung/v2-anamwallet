package com.anam145.wallet.feature.hub.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.anam145.wallet.core.common.model.MiniApp
import com.anam145.wallet.core.ui.language.LocalStrings
import com.anam145.wallet.feature.hub.ui.HubContract
import com.anam145.wallet.feature.hub.ui.HubViewModel

@Composable
fun MiniAppItem(
    miniApp: MiniApp,
    installed: Boolean,
    viewModel: HubViewModel = hiltViewModel()
) {
    val strings = LocalStrings.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = if (installed) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        else CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = miniApp.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            if (!installed) {
                Button(onClick = {
                    viewModel.handleIntent(HubContract.HubIntent.InstallMiniApp(miniApp))
                }) {
                    Text(strings.install)
                }
            }
            else{
                Button(onClick = {
                    viewModel.handleIntent(HubContract.HubIntent.UninstallMiniApp(miniApp))
                }) {
                    Text(strings.uninstall)
                }
            }
        }
    }
}
