package org.mifos.feature.home

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
internal fun HomeScreen(modifier: Modifier = Modifier) {
    HomeScreenContent(
        modifier = modifier.fillMaxSize(),
    )
}

@Composable
internal fun HomeScreenContent(modifier: Modifier) {
    MifosScaffold(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // HomeScreenContent
            Text(text = "Home Screen", fontWeight = FontWeight.SemiBold)
        }
    }
}