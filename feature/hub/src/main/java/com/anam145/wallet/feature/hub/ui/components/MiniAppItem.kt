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
import com.anam145.wallet.core.ui.language.LocalStrings

@Composable
fun MiniAppItem(moduleName: String, installed: Boolean) {
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
                text = moduleName,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            if (!installed) {
                Button(onClick = { /*TODO: 설치 로직*/}) {
                    Text(strings.install)
                }
            }
            else{
                Button(onClick = {  /*TODO: 삭제 로직*/}) {
                    Text(strings.uninstall)
                }
            }
        }
    }
}
