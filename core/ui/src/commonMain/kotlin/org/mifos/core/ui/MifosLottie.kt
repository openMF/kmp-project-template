package org.mifos.core.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import org.mifos.core.ui.generated.resources.Res


@Composable
fun MifosLottieAnimation(
    modifier: Modifier = Modifier,
    size: Dp = 50.dp,
    path: String,
) {
    val composition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes(path).decodeToString(),
        )
    }

    val progress by animateLottieCompositionAsState(
        composition,
        iterations = Int.MAX_VALUE,
    )

    Image(
        modifier = modifier.size(size),
        painter = rememberLottiePainter(
            composition = composition,
            progress = { progress },
        ),
        contentDescription = "Lottie animation",
    )
}