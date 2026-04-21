package com.example.somerslaunch.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.somerslaunch.R
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
    var showIcon by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showIcon = true
    }

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
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Text(
            text = stringResource(R.string.setup_complete_title),
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 24.dp)
        )

        SuccessPulse(
            visible = showIcon,
            modifier = Modifier.align(Alignment.Center)
        )

        Button(
            onClick = viewModel::onStartWorkClicked,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF176FC6))
        ) {
            Text(
                text = stringResource(R.string.start_work),
                color = Color.White,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun SuccessPulse(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "success-pulse")
    val waveProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave-progress"
    )

    val iconScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon-scale"
    )

    Box(modifier = modifier.size(260.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = min(size.width, size.height) * 0.18f

            repeat(3) { ring ->
                val ringOffset = ring / 3f
                val progress = (waveProgress + ringOffset) % 1f
                val radius = baseRadius + (size.minDimension * 0.34f * progress)
                val alpha = (1f - progress) * 0.35f

                drawCircle(
                    color = Color(0xFF176FC6).copy(alpha = alpha),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(450)) +
                scaleIn(
                    initialScale = 0.55f,
                    animationSpec = spring(
                        dampingRatio = 0.45f,
                        stiffness = 240f
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .size(118.dp)
                    .scale(iconScale)
                    .background(Color(0xFF176FC6), CircleShape)
                    .alpha(0.98f),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(62.dp)
                )
            }
        }
    }
}
