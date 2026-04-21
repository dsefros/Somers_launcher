package com.example.somerslaunch.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.somerslaunch.R

private val Montserrat = FontFamily(
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_medium, FontWeight.Medium),
    Font(R.font.montserrat_semibold, FontWeight.SemiBold),
    Font(R.font.montserrat_bold, FontWeight.Bold)
)

private fun TextStyle.withMontserrat(): TextStyle = copy(fontFamily = Montserrat)

private val DefaultTypography = Typography()

val AppTypography = DefaultTypography.copy(
    displayLarge = DefaultTypography.displayLarge.withMontserrat(),
    displayMedium = DefaultTypography.displayMedium.withMontserrat(),
    displaySmall = DefaultTypography.displaySmall.withMontserrat(),
    headlineLarge = DefaultTypography.headlineLarge.withMontserrat(),
    headlineMedium = DefaultTypography.headlineMedium.withMontserrat(),
    headlineSmall = DefaultTypography.headlineSmall.withMontserrat(),
    titleLarge = DefaultTypography.titleLarge.withMontserrat(),
    titleMedium = DefaultTypography.titleMedium.withMontserrat(),
    titleSmall = DefaultTypography.titleSmall.withMontserrat(),
    bodyLarge = DefaultTypography.bodyLarge.withMontserrat(),
    bodyMedium = DefaultTypography.bodyMedium.withMontserrat(),
    bodySmall = DefaultTypography.bodySmall.withMontserrat(),
    labelLarge = DefaultTypography.labelLarge.withMontserrat(),
    labelMedium = DefaultTypography.labelMedium.withMontserrat(),
    labelSmall = DefaultTypography.labelSmall.withMontserrat()
)
