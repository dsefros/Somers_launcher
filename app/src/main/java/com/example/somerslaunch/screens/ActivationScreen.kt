package com.example.somerslaunch.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.somerslaunch.R
import com.example.somerslaunch.activation.ActivationStage
import com.example.somerslaunch.activation.ActivationUiState
import com.example.somerslaunch.activation.ActivationViewModel
import com.example.somerslaunch.ui.adaptive.AppAdaptiveMetrics
import com.example.somerslaunch.ui.adaptive.rememberAdaptiveMetrics
import com.example.somerslaunch.ui.theme.SomersLaunchTheme
import com.example.somerslaunch.utils.AppSettingsRepository
import kotlinx.coroutines.delay
import kotlin.math.min
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.graphics.drawscope.rotate

@Composable
fun ActivationScreen(
    appSettingsRepository: AppSettingsRepository,
    onActivationCompleted: () -> Unit
) {
    val metrics = rememberAdaptiveMetrics()
    val viewModel: ActivationViewModel = viewModel(
        factory = ActivationViewModel.provideFactory(appSettingsRepository)
    )
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.start()
    }

    LaunchedEffect(state) {
        if (state is ActivationUiState.Success) {
            onActivationCompleted()
        }
    }

    BackHandler(enabled = state is ActivationUiState.InProgress) {
        // Back is intentionally blocked while activation is in progress.
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = metrics.contentHorizontalPadding)
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = metrics.titleTopPadding),
            text = stringResource(R.string.activation_wait_title),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = metrics.titleFontSize
            ),
            textAlign = TextAlign.Center,
            color = Color.Black
        )

        ActivationIllustration(
            metrics = metrics,
            modifier = Modifier.align(Alignment.Center)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = metrics.statusSectionBottomPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedContent(
                targetState = statusText(state),
                transitionSpec = {
                    fadeIn(
                        animationSpec = tween(durationMillis = 260, delayMillis = 60)
                    ) togetherWith fadeOut(
                        animationSpec = tween(durationMillis = 180)
                    )
                },
                label = "activation_status"
            ) { text ->
                LuxuryStatusText(
                    text = text,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(metrics.statusToProgressSpacing))

            if (state is ActivationUiState.InProgress) {
                val progressState = state as ActivationUiState.InProgress
                val progress =
                    progressState.progress.stepNumber.toFloat() /
                            progressState.progress.totalSteps.toFloat()

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth(metrics.progressWidthFraction)
                        .height(8.dp)
                        .clip(RoundedCornerShape(100.dp)),
                    color = Color(0xFF176FC6),
                    trackColor = Color(0xFFE6EEF8)
                )
            }
        }
    }
}

@Composable
private fun ActivationIllustration(
    metrics: AppAdaptiveMetrics,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "activation_illustration")

    val outerArcRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outer_arc_rotation"
    )

    val innerArcRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "inner_arc_rotation"
    )

    val logoScale by infiniteTransition.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1800,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.10f,
        targetValue = 0.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1600,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = modifier.size(metrics.activationIllustrationContainerSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val minSide = min(size.width, size.height)

            val glowRadius = minSide * 0.23f
            drawCircle(
                color = Color(0xFF176FC6).copy(alpha = glowAlpha),
                radius = glowRadius,
                center = center
            )

            val outerRadius = minSide * 0.37f
            val innerRadius = minSide * 0.29f

            rotate(degrees = outerArcRotation, pivot = center) {
                drawArc(
                    color = Color(0xFF176FC6).copy(alpha = 0.95f),
                    startAngle = -90f,
                    sweepAngle = 84f,
                    useCenter = false,
                    topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                    size = androidx.compose.ui.geometry.Size(outerRadius * 2, outerRadius * 2),
                    style = Stroke(width = 4.dp.toPx())
                )

                drawArc(
                    color = Color(0xFF176FC6).copy(alpha = 0.28f),
                    startAngle = 50f,
                    sweepAngle = 34f,
                    useCenter = false,
                    topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                    size = androidx.compose.ui.geometry.Size(outerRadius * 2, outerRadius * 2),
                    style = Stroke(width = 4.dp.toPx())
                )
            }

            rotate(degrees = innerArcRotation, pivot = center) {
                drawArc(
                    color = Color(0xFF176FC6).copy(alpha = 0.65f),
                    startAngle = 120f,
                    sweepAngle = 62f,
                    useCenter = false,
                    topLeft = Offset(center.x - innerRadius, center.y - innerRadius),
                    size = androidx.compose.ui.geometry.Size(innerRadius * 2, innerRadius * 2),
                    style = Stroke(width = 3.dp.toPx())
                )

                drawArc(
                    color = Color(0xFF176FC6).copy(alpha = 0.18f),
                    startAngle = 230f,
                    sweepAngle = 24f,
                    useCenter = false,
                    topLeft = Offset(center.x - innerRadius, center.y - innerRadius),
                    size = androidx.compose.ui.geometry.Size(innerRadius * 2, innerRadius * 2),
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }

        Image(
            painter = painterResource(R.drawable.activation_logo),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(metrics.activationIllustrationLogoSize)
                .scale(logoScale)
                .alpha(0.99f)
        )
    }
}

@Composable
private fun LuxuryStatusText(
    text: String,
    modifier: Modifier = Modifier
) {
    var visibleChars by remember(text) { mutableIntStateOf(0) }

    LaunchedEffect(text) {
        visibleChars = 0
        while (visibleChars < text.length) {
            delay(26)
            visibleChars += 1
        }
    }

    Text(
        text = text.take(visibleChars),
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp,
            lineHeight = 24.sp
        ),
        color = Color(0xFF4F5D73),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun statusText(state: ActivationUiState): String {
    return when (state) {
        ActivationUiState.Idle -> stringResource(R.string.activation_preparing)
        is ActivationUiState.InProgress -> activationStageText(state.progress.stage)
        ActivationUiState.Success -> stringResource(R.string.activation_success)
        is ActivationUiState.Error -> stringResource(
            R.string.activation_error,
            state.cause?.localizedMessage ?: stringResource(R.string.activation_unknown_error)
        )
    }
}

@Composable
private fun activationStageText(stage: ActivationStage): String {
    return when (stage) {
        ActivationStage.DownloadApps -> stringResource(R.string.activation_stage_01)
        ActivationStage.RegisterWithOfd -> stringResource(R.string.activation_stage_02)
        ActivationStage.VerifyContracts -> stringResource(R.string.activation_stage_03)
        ActivationStage.FixTariffs -> stringResource(R.string.activation_stage_04)
        ActivationStage.ComputeBinRanges -> stringResource(R.string.activation_stage_05)
        ActivationStage.BuildLimits -> stringResource(R.string.activation_stage_06)
        ActivationStage.FetchConfiguration -> stringResource(R.string.activation_stage_07)
        ActivationStage.ActivatePaymentSoftware -> stringResource(R.string.activation_stage_08)
        ActivationStage.CheckBankConnectivity -> stringResource(R.string.activation_stage_09)
        ActivationStage.GenerateEncryptionKeys -> stringResource(R.string.activation_stage_10)
        ActivationStage.GenerateFiscalFeature -> stringResource(R.string.activation_stage_11)
        ActivationStage.InitializeServiceEnvironment -> stringResource(R.string.activation_stage_12)
        ActivationStage.SyncTerminalParameters -> stringResource(R.string.activation_stage_13)
        ActivationStage.VerifyEnvironmentIntegrity -> stringResource(R.string.activation_stage_14)
        ActivationStage.UpdateSystemDependencies -> stringResource(R.string.activation_stage_15)
        ActivationStage.ConfigurePaymentProfiles -> stringResource(R.string.activation_stage_16)
        ActivationStage.ValidateConnectionParameters -> stringResource(R.string.activation_stage_17)
        ActivationStage.RegisterDevice -> stringResource(R.string.activation_stage_18)
        ActivationStage.AlignExchangeProtocols -> stringResource(R.string.activation_stage_19)
        ActivationStage.VerifyServiceAvailability -> stringResource(R.string.activation_stage_20)
        ActivationStage.PreparePaymentContour -> stringResource(R.string.activation_stage_21)
        ActivationStage.InitializeCryptoModule -> stringResource(R.string.activation_stage_22)
        ActivationStage.VerifyConfigVersion -> stringResource(R.string.activation_stage_23)
        ActivationStage.UpdateReferenceRules -> stringResource(R.string.activation_stage_24)
        ActivationStage.ApplySecurityPolicies -> stringResource(R.string.activation_stage_25)
        ActivationStage.ActivateNetworkInterfaces -> stringResource(R.string.activation_stage_26)
        ActivationStage.VerifyLicenseStatus -> stringResource(R.string.activation_stage_27)
        ActivationStage.InitializeTransactionHandlers -> stringResource(R.string.activation_stage_28)
        ActivationStage.PrepareDataChannels -> stringResource(R.string.activation_stage_29)
        ActivationStage.RunSystemChecks -> stringResource(R.string.activation_stage_30)
        ActivationStage.CompleteEnvironmentSetup -> stringResource(R.string.activation_stage_31)
    }
}

@Preview(name = "Activation Illustration Compact", widthDp = 320, heightDp = 568)
@Preview(name = "Activation Illustration Medium", widthDp = 411, heightDp = 891)
@Preview(name = "Activation Illustration Expanded", widthDp = 840, heightDp = 1280)
@Composable
private fun ActivationIllustrationPreview() {
    SomersLaunchTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            ActivationIllustration(metrics = rememberAdaptiveMetrics())
        }
    }
}
