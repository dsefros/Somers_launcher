package com.example.somerslaunch.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.somerslaunch.R
import com.example.somerslaunch.ui.adaptive.rememberAdaptiveMetrics
import com.example.somerslaunch.ui.theme.SomersLaunchTheme

@Composable
fun WelcomeScreen(
    onComplete: () -> Unit
) {
    val metrics = rememberAdaptiveMetrics()
    val centerComposition = rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.center_animation))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = metrics.contentHorizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.welcome),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = metrics.titleFontSize
                ),
                color = Color.Black,
                modifier = Modifier
                    .padding(top = metrics.titleTopPadding)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(R.string.welcome_subtitle),
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = metrics.secondaryFontSize),
                color = Color.Gray,
                modifier = Modifier.padding(top = metrics.topSectionSpacing),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(0.12f))

            if (centerComposition.value != null) {
                LottieAnimation(
                    composition = centerComposition.value,
                    modifier = Modifier.size(metrics.welcomeIllustrationSize),
                    iterations = LottieConstants.IterateForever
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(metrics.welcomeIllustrationSize)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF176FC6).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.loading), color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.weight(0.28f))

            Button(
                onClick = onComplete,
                modifier = Modifier
                    .fillMaxWidth(metrics.primaryButtonWidthFraction)
                    .widthIn(min = metrics.primaryButtonMinWidth, max = metrics.primaryButtonMaxWidth)
                    .height(metrics.primaryButtonHeight),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF176FC6)),
                shape = RoundedCornerShape(30.dp)
            ) {
                Text(
                    text = stringResource(R.string.start),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(metrics.bottomButtonBottomPadding))
        }
    }
}

@Preview(name = "Welcome Compact", widthDp = 320, heightDp = 568)
@Preview(name = "Welcome Medium", widthDp = 411, heightDp = 891)
@Preview(name = "Welcome Expanded", widthDp = 840, heightDp = 1280)
@Composable
private fun WelcomeScreenPreview() {
    SomersLaunchTheme {
        WelcomeScreen(onComplete = {})
    }
}
