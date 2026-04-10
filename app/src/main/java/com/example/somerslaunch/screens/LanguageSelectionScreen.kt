package com.example.somerslaunch.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.somerslaunch.utils.LanguageManager
import com.example.somerslaunch.utils.SystemLanguage
import com.example.somerslaunch.R

@Composable
fun LanguageSelectionScreen(
    navController: NavController,
    languageManager: LanguageManager,
    onLanguageSelected: (String) -> Unit
) {
    val availableLanguages = remember {
        languageManager.getAvailableLanguages()
    }

    var selectedLanguage by remember {
        mutableStateOf(languageManager.getSystemLanguage())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Основной контент, который занимает всё доступное пространство
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Отступ сверху для status bar
            Spacer(modifier = Modifier.height(40.dp))

            // Заголовок
            Text(
                text = "Выберите язык",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(top = 8.dp, start = 24.dp, end = 24.dp)
            )

            // Список языков
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(availableLanguages) { language ->
                    LanguageItem(
                        language = language,
                        isSelected = language.code == selectedLanguage,
                        onClick = {
                            // Только отмечаем язык, без перехода
                            selectedLanguage = language.code
                        }
                    )
                }
            }
        }

        // Нижняя панель с кнопками
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .height(64.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Кнопка "Назад" слева
                IconButton(
                    onClick = {
                        navController.popBackStack()
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Назад",
                        tint = Color(0xFF176FC6)
                    )
                }

                // Кнопка "Далее" справа
                Button(
                    onClick = {
                        onLanguageSelected(selectedLanguage)
                        // Переход на следующий экран
                        navController.navigate("wifi_selection") // Замените на ваш роут
                    },
                    modifier = Modifier
                        .width(108.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF176FC6)
                    )
                ) {
                    Text(
                        text = "Далее",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun LanguageItem(
    language: SystemLanguage,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = language.displayName,
                    fontSize = 16.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color(0xFF176FC6) else Color.Black
                )
                Text(
                    text = language.nativeName,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Выбрано",
                    tint = Color(0xFF176FC6),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}