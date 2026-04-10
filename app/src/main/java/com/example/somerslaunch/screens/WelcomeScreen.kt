package com.example.somerslaunch.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.somerslaunch.R

@Composable
fun WelcomeScreen(
    navController: NavController,
    onComplete: () -> Unit
) {
    val centerComposition = rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.center_animation)
    )

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
                text = "Добро пожаловать!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(top = 124.dp)
            )

            // Новый подзаголовок
            Text(
                text = "У вас в руках POS-терминал от компании \n Сомерс, нажмите \"Начать\" для продолжения настройки",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp, start = 32.dp, end = 32.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
                    Text("Загрузка...", color = Color.Black)
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
                    text = "Начать",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}