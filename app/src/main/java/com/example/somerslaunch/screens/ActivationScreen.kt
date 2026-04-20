package com.example.somerslaunch.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F8FB))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.activation_title),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                when (val currentState = state) {
                    ActivationUiState.Idle -> {
                        CircularProgressIndicator(color = Color(0xFF176FC6))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.activation_preparing),
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }

                    is ActivationUiState.InProgress -> {
                        CircularProgressIndicator(color = Color(0xFF176FC6))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(
                                R.string.activation_step_counter,
                                currentState.progress.stepNumber,
                                currentState.progress.totalSteps
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = activationStageText(currentState.progress.stage),
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }

                    ActivationUiState.Success -> {
                        Text(
                            text = stringResource(R.string.activation_success),
                            color = Color(0xFF176FC6),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    is ActivationUiState.Error -> {
                        Text(
                            text = stringResource(R.string.activation_error, currentState.cause?.localizedMessage ?: stringResource(R.string.activation_unknown_error)),
                            color = Color.Red,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
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
