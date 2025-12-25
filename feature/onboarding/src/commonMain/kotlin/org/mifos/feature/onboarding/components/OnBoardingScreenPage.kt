package org.mifos.feature.onboarding.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.mifos.feature.onboarding.Total_Pages


@Composable
fun OnBoardingScreenPage(
    onNext: () -> Unit,
    currentPage: Int,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        Box(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = description,
                    style = MaterialTheme.typography.bodyLarge
                        .copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        OnboardingTextBlock(
            modifier = Modifier,
            onNext = onNext,
            currentPage = currentPage,
            totalPages = Total_Pages
        )
    }
}

@Composable
fun OnboardingTextBlock(
    onNext: () -> Unit,
    currentPage: Int,
    modifier: Modifier = Modifier,
    totalPages: Int = Total_Pages,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row {
            repeat(totalPages) { index ->
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = if (index == currentPage - 1) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            },
                            shape = CircleShape,
                        ),
                )
                if (index < totalPages - 1) Spacer(Modifier.width(8.dp))
            }
        }

        Button(
            modifier = Modifier,
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(
                text="Next"
            )
        }
    }
}

@Preview
@Composable
fun PreviewOnboardingTextBlock() {
    OnboardingTextBlock(
        onNext = {},
        currentPage = 2,
        totalPages = 5,
    )
}

@Preview
@Composable
fun PreviewOnBoardingScreenPage() {
    OnBoardingScreenPage(
        onNext = { /* ... */ },
        currentPage = 1,
        title = "Welcome",
        description = "This is onboarding",
    )
}