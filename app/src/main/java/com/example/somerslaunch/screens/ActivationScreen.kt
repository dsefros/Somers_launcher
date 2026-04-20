package com.example.somerslaunch.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.example.somerslaunch.utils.AppSettingsRepository
import kotlinx.coroutines.delay

@Composable
fun ActivationScreen(
    appSettingsRepository: AppSettingsRepository,
    onActivationCompleted: () -> Unit
) {
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

    val statusText = activationStatusText(state)

    ActivationScreenContent(statusText = statusText)
}

@Composable
private fun ActivationScreenContent(statusText: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 28.dp, vertical = 42.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.activation_please_wait),
            fontSize = 34.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        RotatingActivationLogo()

        Spacer(modifier = Modifier.weight(1.05f))

        AnimatedStatusText(
            statusText = statusText,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp)
        )
    }
}

@Composable
private fun RotatingActivationLogo() {
    val infiniteTransition = rememberInfiniteTransition(label = "logo-rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier.size(182.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(182.dp)
                .graphicsLayer { rotationZ = rotation }
        ) {
            val stroke = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
            val radius = size.minDimension / 2f - 18.dp.toPx()
            val center = Offset(size.width / 2f, size.height / 2f)

            drawArc(
                color = Color(0xFF176FC6),
                startAngle = 0f,
                sweepAngle = 238f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                style = stroke
            )

            drawArc(
                color = Color(0xFF176FC6).copy(alpha = 0.32f),
                startAngle = 250f,
                sweepAngle = 82f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                style = stroke
            )

            drawCircle(
                color = Color(0xFF176FC6),
                radius = 9.dp.toPx(),
                center = Offset(center.x + radius * 0.92f, center.y)
            )
        }
    }
}

@Composable
private fun AnimatedStatusText(statusText: String, modifier: Modifier = Modifier) {
    AnimatedContent(
        targetState = statusText,
        transitionSpec = {
            ContentTransform(
                targetContentEnter = fadeIn(animationSpec = tween(220, delayMillis = 80)),
                initialContentExit = fadeOut(animationSpec = tween(180))
            )
        },
        label = "status-change",
        modifier = modifier
    ) { text ->
        TypingStatusText(
            text = text,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun TypingStatusText(text: String, modifier: Modifier = Modifier) {
    var visibleChars by remember(text) { mutableIntStateOf(0) }

    LaunchedEffect(text) {
        visibleChars = 0
        while (visibleChars < text.length) {
            delay(18)
            visibleChars += 1
        }
    }

    Text(
        text = text.take(visibleChars),
        fontSize = 20.sp,
        fontWeight = FontWeight.Normal,
        color = Color(0xFF1B1B1B),
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

@Composable
private fun activationStatusText(state: ActivationUiState): String {
    val baseText = when (state) {
        ActivationUiState.Idle -> stringResource(R.string.activation_preparing)
        is ActivationUiState.InProgress -> activationStageText(state.progress.stage)
        ActivationUiState.Success -> stringResource(R.string.activation_success)
        is ActivationUiState.Error -> stringResource(
            R.string.activation_error,
            state.cause?.localizedMessage ?: stringResource(R.string.activation_unknown_error)
        )
    }

    return if (baseText.endsWith("…") || baseText.endsWith("...")) baseText else "$baseText..."
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

@Preview(showBackground = true)
@Composable
private fun ActivationScreenContentPreview() {
    ActivationScreenContent(
        statusText = stringResource(R.string.activation_stage_07) + "..."
    )
}
