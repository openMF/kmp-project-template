package org.mifos.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import org.mifos.core.designsystem.component.MifosScaffold

@Composable
internal fun ProfileScreen(modifier: Modifier = Modifier) {
    ProfileScreenContent(
        modifier = modifier.fillMaxSize(),
    )
}

@Composable
internal fun ProfileScreenContent(modifier: Modifier) {
    MifosScaffold(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // ProfileScreenContent
            Text(text = "Profile Screen", fontWeight = FontWeight.SemiBold)
        }
    }
}