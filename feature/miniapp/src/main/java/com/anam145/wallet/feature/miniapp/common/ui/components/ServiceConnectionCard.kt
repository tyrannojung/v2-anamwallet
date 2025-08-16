package com.anam145.wallet.feature.miniapp.common.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anam145.wallet.core.ui.language.LocalStrings

@Composable
fun ServiceConnectionCard(
    onRetry: () -> Unit
) {
    val strings = LocalStrings.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings.serviceDisconnected,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            TextButton(onClick = onRetry) {
                Text(strings.retry)
            }
        }
    }
}