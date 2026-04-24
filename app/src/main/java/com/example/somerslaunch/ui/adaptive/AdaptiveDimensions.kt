package com.example.somerslaunch.ui.adaptive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ScreenSizeClass {
    Compact,
    Medium,
    Expanded
}

@Immutable
data class AppAdaptiveMetrics(
    val sizeClass: ScreenSizeClass,
    val contentHorizontalPadding: Dp,
    val titleTopPadding: Dp,
    val topSectionSpacing: Dp,
    val listItemHorizontalPadding: Dp,
    val listContentBottomPadding: Dp,
    val inlineMessageSpacing: Dp,
    val stateMessagePadding: Dp,
    val primaryButtonHeight: Dp,
    val primaryButtonMinWidth: Dp,
    val primaryButtonMaxWidth: Dp,
    val primaryButtonWidthFraction: Float,
    val secondaryActionButtonHeight: Dp,
    val secondaryActionButtonMinWidth: Dp,
    val secondaryActionButtonMaxWidth: Dp,
    val iconButtonSize: Dp,
    val bottomAreaMinHeight: Dp,
    val bottomAreaVerticalPadding: Dp,
    val bottomButtonBottomPadding: Dp,
    val welcomeIllustrationSize: Dp,
    val activationIllustrationContainerSize: Dp,
    val activationIllustrationLogoSize: Dp,
    val setupIllustrationContainerSize: Dp,
    val setupIllustrationIconSize: Dp,
    val titleFontSize: TextUnit,
    val secondaryFontSize: TextUnit,
    val statusSectionBottomPadding: Dp,
    val statusToProgressSpacing: Dp,
    val progressWidthFraction: Float
)

@Composable
fun rememberAdaptiveMetrics(): AppAdaptiveMetrics {
    val configuration = LocalConfiguration.current
    val widthDp = configuration.screenWidthDp
    val heightDp = configuration.screenHeightDp

    return remember(widthDp, heightDp) {
        val sizeClass = when {
            widthDp < 360 || (widthDp < 392 && heightDp < 760) -> ScreenSizeClass.Compact
            widthDp >= 600 && heightDp >= 840 -> ScreenSizeClass.Expanded
            else -> ScreenSizeClass.Medium
        }

        val verticalBaseDp = minOf(heightDp, (widthDp * 2.1f).toInt())
        val titleTopPadding = (verticalBaseDp * 0.11f).dp.coerceIn(52.dp, 116.dp)
        val statusBottomPadding = (verticalBaseDp * 0.12f).dp.coerceIn(54.dp, 122.dp)

        // Базовый размер иллюстраций считаем в первую очередь от ширины экрана,
        // чтобы анимации не были слишком маленькими на длинных/узких дисплеях.
        val illustrationBaseFromWidth = (widthDp * 0.78f).dp.coerceIn(196.dp, 420.dp)

        when (sizeClass) {
            ScreenSizeClass.Compact -> AppAdaptiveMetrics(
                sizeClass = sizeClass,
                contentHorizontalPadding = 16.dp,
                titleTopPadding = titleTopPadding,
                topSectionSpacing = 8.dp,
                listItemHorizontalPadding = 16.dp,
                listContentBottomPadding = 12.dp,
                inlineMessageSpacing = 8.dp,
                stateMessagePadding = 20.dp,
                primaryButtonHeight = 52.dp,
                primaryButtonMinWidth = 172.dp,
                primaryButtonMaxWidth = 268.dp,
                primaryButtonWidthFraction = 0.78f,
                secondaryActionButtonHeight = 44.dp,
                secondaryActionButtonMinWidth = 98.dp,
                secondaryActionButtonMaxWidth = 150.dp,
                iconButtonSize = 44.dp,
                bottomAreaMinHeight = 60.dp,
                bottomAreaVerticalPadding = 6.dp,
                bottomButtonBottomPadding = 28.dp,
                welcomeIllustrationSize = illustrationBaseFromWidth.coerceIn(196.dp, 280.dp),
                activationIllustrationContainerSize = illustrationBaseFromWidth.coerceIn(176.dp, 244.dp),
                activationIllustrationLogoSize = 52.dp,
                setupIllustrationContainerSize = illustrationBaseFromWidth.coerceIn(172.dp, 236.dp),
                setupIllustrationIconSize = 98.dp,
                titleFontSize = 22.sp,
                secondaryFontSize = 14.sp,
                statusSectionBottomPadding = statusBottomPadding,
                statusToProgressSpacing = 16.dp,
                progressWidthFraction = 0.82f
            )

            ScreenSizeClass.Medium -> AppAdaptiveMetrics(
                sizeClass = sizeClass,
                contentHorizontalPadding = 20.dp,
                titleTopPadding = titleTopPadding,
                topSectionSpacing = 10.dp,
                listItemHorizontalPadding = 24.dp,
                listContentBottomPadding = 16.dp,
                inlineMessageSpacing = 10.dp,
                stateMessagePadding = 24.dp,
                primaryButtonHeight = 56.dp,
                primaryButtonMinWidth = 196.dp,
                primaryButtonMaxWidth = 304.dp,
                primaryButtonWidthFraction = 0.70f,
                secondaryActionButtonHeight = 48.dp,
                secondaryActionButtonMinWidth = 108.dp,
                secondaryActionButtonMaxWidth = 172.dp,
                iconButtonSize = 48.dp,
                bottomAreaMinHeight = 64.dp,
                bottomAreaVerticalPadding = 8.dp,
                bottomButtonBottomPadding = 36.dp,
                welcomeIllustrationSize = illustrationBaseFromWidth.coerceIn(228.dp, 330.dp),
                activationIllustrationContainerSize = illustrationBaseFromWidth.coerceIn(198.dp, 264.dp),
                activationIllustrationLogoSize = 64.dp,
                setupIllustrationContainerSize = illustrationBaseFromWidth.coerceIn(194.dp, 256.dp),
                setupIllustrationIconSize = 112.dp,
                titleFontSize = 24.sp,
                secondaryFontSize = 14.sp,
                statusSectionBottomPadding = statusBottomPadding,
                statusToProgressSpacing = 22.dp,
                progressWidthFraction = 0.72f
            )

            ScreenSizeClass.Expanded -> AppAdaptiveMetrics(
                sizeClass = sizeClass,
                contentHorizontalPadding = 28.dp,
                titleTopPadding = titleTopPadding,
                topSectionSpacing = 12.dp,
                listItemHorizontalPadding = 32.dp,
                listContentBottomPadding = 20.dp,
                inlineMessageSpacing = 12.dp,
                stateMessagePadding = 28.dp,
                primaryButtonHeight = 60.dp,
                primaryButtonMinWidth = 232.dp,
                primaryButtonMaxWidth = 360.dp,
                primaryButtonWidthFraction = 0.58f,
                secondaryActionButtonHeight = 50.dp,
                secondaryActionButtonMinWidth = 118.dp,
                secondaryActionButtonMaxWidth = 196.dp,
                iconButtonSize = 52.dp,
                bottomAreaMinHeight = 72.dp,
                bottomAreaVerticalPadding = 10.dp,
                bottomButtonBottomPadding = 46.dp,
                welcomeIllustrationSize = illustrationBaseFromWidth.coerceIn(280.dp, 400.dp),
                activationIllustrationContainerSize = illustrationBaseFromWidth.coerceIn(222.dp, 300.dp),
                activationIllustrationLogoSize = 74.dp,
                setupIllustrationContainerSize = illustrationBaseFromWidth.coerceIn(216.dp, 292.dp),
                setupIllustrationIconSize = 126.dp,
                titleFontSize = 26.sp,
                secondaryFontSize = 15.sp,
                statusSectionBottomPadding = statusBottomPadding,
                statusToProgressSpacing = 26.dp,
                progressWidthFraction = 0.62f
            )
        }
    }
}
