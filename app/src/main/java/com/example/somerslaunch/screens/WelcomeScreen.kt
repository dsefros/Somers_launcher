package com.example.somerslaunch.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.somerslaunch.R

@Composable
fun WelcomeScreen(
    onComplete: () -> Unit
) {
    val centerComposition = rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.center_animation))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.welcome),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 24.sp),
                color = Color.Black,
                modifier = Modifier.padding(top = 124.dp)
            )

            Text(
                text = stringResource(R.string.welcome_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp, start = 32.dp, end = 32.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(0.1f))

            if (centerComposition.value != null) {
                LottieAnimation(
                    composition = centerComposition.value,
                    modifier = Modifier.size(350.dp),
                    iterations = LottieConstants.IterateForever
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(350.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF176FC6).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.loading), color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.weight(0.3f))

            Button(
                onClick = onComplete,
                modifier = Modifier
                    .width(232.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF176FC6)),
                shape = RoundedCornerShape(30.dp)
            ) {
                Text(
                    text = stringResource(R.string.start),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
