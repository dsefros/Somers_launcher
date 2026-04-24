package com.example.somerslaunch.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.somerslaunch.R
import com.example.somerslaunch.ui.adaptive.AppAdaptiveMetrics
import com.example.somerslaunch.ui.adaptive.rememberAdaptiveMetrics
import com.example.somerslaunch.ui.theme.SomersLaunchTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.math.min

interface AppCloser {
    fun closeApp()
}

class ActivityAppCloser(
    private val finishAction: () -> Unit
) : AppCloser {
    override fun closeApp() = finishAction()
}

class SetupCompletionViewModel : ViewModel() {
    private val _events = MutableSharedFlow<SetupCompletionEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun onStartWorkClicked() {
        _events.tryEmit(SetupCompletionEvent.CloseApp)
    }
}

sealed interface SetupCompletionEvent {
    data object CloseApp : SetupCompletionEvent
}

@Composable
fun SetupCompletionScreen(
    appCloser: AppCloser,
    viewModel: SetupCompletionViewModel = viewModel()
) {
    val metrics = rememberAdaptiveMetrics()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            if (event is SetupCompletionEvent.CloseApp) {
                appCloser.closeApp()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = metrics.contentHorizontalPadding)
    ) {
        Text(
            text = stringResource(R.string.setup_complete_title),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = metrics.titleFontSize
            ),
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = metrics.heroTitleTopPadding)
        )

        SuccessIllustration(
            metrics = metrics,
            modifier = Modifier.align(Alignment.Center)
        )

        Button(
            onClick = viewModel::onStartWorkClicked,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = metrics.bottomButtonBottomPadding)
                .fillMaxWidth(metrics.primaryButtonWidthFraction)
                .widthIn(min = metrics.primaryButtonMinWidth, max = metrics.primaryButtonMaxWidth)
                .height(metrics.primaryButtonHeight),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF176FC6)
            )
        ) {
            Text(
                text = stringResource(R.string.start_work),
                color = Color.White,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
private fun SuccessIllustration(
    metrics: AppAdaptiveMetrics,
    modifier: Modifier = Modifier
) {
    val circleScale = remember { Animatable(0.72f) }
    val circleAlpha = remember { Animatable(0f) }
    val shortStrokeProgress = remember { Animatable(0f) }
    val longStrokeProgress = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "success_pulse")

    val waveProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_progress"
    )

    val iconScale by infiniteTransition.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_scale"
    )

    LaunchedEffect(Unit) {
        circleAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 260)
        )
        circleScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = 0.62f,
                stiffness = 420f
            )
        )

        delay(120)

        shortStrokeProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
        )

        longStrokeProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier.size(metrics.setupIllustrationContainerSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val minSide = min(size.width, size.height)

            repeat(3) { ring ->
                val ringOffset = ring / 3f
                val progress = (waveProgress + ringOffset) % 1f
                val radius = minSide * (0.22f + 0.16f * progress)
                val alpha = (1f - progress) * 0.22f

                drawCircle(
                    color = Color(0xFF176FC6).copy(alpha = alpha),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 2.5.dp.toPx())
                )
            }
        }

        Canvas(
            modifier = Modifier
                .size(metrics.setupIllustrationIconSize)
                .scale(circleScale.value * iconScale)
                .alpha(circleAlpha.value)
                .background(
                    color = Color(0xFF176FC6),
                    shape = CircleShape
                )
        ) {
            val w = size.width
            val h = size.height

            val p1 = Offset(w * 0.30f, h * 0.53f)
            val p2 = Offset(w * 0.46f, h * 0.67f)
            val p3 = Offset(w * 0.73f, h * 0.39f)

            val shortCurrent = Offset(
                x = p1.x + (p2.x - p1.x) * shortStrokeProgress.value,
                y = p1.y + (p2.y - p1.y) * shortStrokeProgress.value
            )

            val longCurrent = Offset(
                x = p2.x + (p3.x - p2.x) * longStrokeProgress.value,
                y = p2.y + (p3.y - p2.y) * longStrokeProgress.value
            )

            val strokeWidth = 8.dp.toPx()

            if (shortStrokeProgress.value > 0f) {
                drawLine(
                    color = Color.White,
                    start = p1,
                    end = shortCurrent,
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }

            if (longStrokeProgress.value > 0f) {
                drawLine(
                    color = Color.White,
                    start = p2,
                    end = longCurrent,
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Preview(name = "Setup Compact", widthDp = 320, heightDp = 568)
@Preview(name = "Setup Medium", widthDp = 411, heightDp = 891)
@Preview(name = "Setup Expanded", widthDp = 840, heightDp = 1280)
@Composable
private fun SetupCompletionScreenPreview() {
    SomersLaunchTheme {
        SetupCompletionScreen(appCloser = object : AppCloser {
            override fun closeApp() = Unit
        })
    }
}
