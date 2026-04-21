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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.somerslaunch.R
import com.example.somerslaunch.activation.ActivationStage
import com.example.somerslaunch.activation.ActivationUiState
import com.example.somerslaunch.activation.ActivationViewModel
import com.example.somerslaunch.utils.AppSettingsRepository

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

    val spinnerTransition = rememberInfiniteTransition(label = "activation_spinner")
    val logoRotation by spinnerTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "activation_logo_rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            text = stringResource(R.string.activation_wait_title),
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = Color(0xFF1A2233)
        )

        ActivationSpinner(
            modifier = Modifier
                .size(108.dp)
                .align(Alignment.Center)
                .rotate(logoRotation)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedContent(
                targetState = statusText(state),
                transitionSpec = { fadeIn(tween(280)) togetherWith fadeOut(tween(220)) },
                label = "activation_status"
            ) { text ->
                Text(
                    text = text,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (state is ActivationUiState.InProgress) {
                Text(
                    text = stringResource(
                        R.string.activation_step_counter,
                        (state as ActivationUiState.InProgress).progress.stepNumber,
                        (state as ActivationUiState.InProgress).progress.totalSteps
                    ),
                    color = Color(0xFF6A7384),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ActivationSpinner(modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.12f
        drawCircle(
            color = Color(0xFFE6EAF2),
            style = Stroke(width = strokeWidth)
        )
        drawArc(
            color = Color(0xFF1A2233),
            startAngle = -90f,
            sweepAngle = 120f,
            useCenter = false,
            style = Stroke(width = strokeWidth)
        )
        drawCircle(
            color = Color(0xFF1A2233),
            radius = size.minDimension * 0.14f
        )
    }
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
