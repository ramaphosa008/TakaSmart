package com.ramaphosa.takasmart.ui.screens.onboarding.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ramaphosa.takasmart.R
import com.ramaphosa.takasmart.navigation.ROUT_ROLE_SELECT
import kotlinx.coroutines.delay

// ── Brand colours ──────────────────────────────────────────────────────────────
private val BrandDark     = Color(0xFF0A2E22)
private val BrandDeep     = Color(0xFF061D16)
private val BrandGreen    = Color(0xFF1D9E75)
private val BrandGreenDim = Color(0x401D9E75)
private val White40       = Color(0x66FFFFFF)
private val White20       = Color(0x33FFFFFF)

@Composable
fun SplashScreen(navController: NavController) {

    // ── Single LaunchedEffect handles both navigate + visible toggle ───────────
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        delay(5_000L)
        navController.navigate(ROUT_ROLE_SELECT) {
            popUpTo(0) { inclusive = true }
        }
    }

    // ── Fade + slide-up ────────────────────────────────────────────────────────
    val contentAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "contentAlpha"
    )
    val contentOffset by animateFloatAsState(
        targetValue = if (visible) 0f else 24f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "contentOffset"
    )

    // ── Logo pulse ─────────────────────────────────────────────────────────────
    val logoPulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoPulse"
    )

    // ── Dot shimmer ────────────────────────────────────────────────────────────
    val dotAlpha by rememberInfiniteTransition(label = "dots").animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    // ── Root ───────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            // ✅ Use explicit pixel end point instead of Float.MAX_VALUE
            //    which can crash the Preview renderer
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF0D3D2E), BrandDark, BrandDeep),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 2000f)
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        GlowCanvas()

        // Top edge highlight
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            BrandGreen.copy(alpha = 0.45f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Main content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(horizontal = 40.dp)
                .graphicsLayer {
                    alpha = contentAlpha
                    translationY = contentOffset
                }
        ) {
            Box(
                modifier = Modifier
                    .scale(logoPulse)
                    .size(88.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(BrandGreenDim),
                contentAlignment = Alignment.Center
            ) {
                // ✅ Explicit import at top: import androidx.compose.foundation.Image
                Image(
                    painter = painterResource(id = R.drawable.arrow),
                    contentDescription = "TakaSmart logo",
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text = "TakaSmart",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = (-0.5).sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Recycle. Earn. Impact.",
                fontSize = 11.sp,
                color = White40,
                letterSpacing = 2.5.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(52.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LoadingDot(BrandGreen, dotAlpha)
                LoadingDot(BrandGreen, dotAlpha * 0.55f)
                LoadingDot(BrandGreen, dotAlpha * 0.25f)
            }
        }

        // Footer
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
                .graphicsLayer { alpha = contentAlpha }
        ) {
            Text(
                text = "POWERED BY",
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                color = White20,
                fontWeight = FontWeight.Normal
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = "RAMAPHOSA TECH",
                fontSize = 11.sp,
                letterSpacing = 0.5.sp,
                color = White40,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ── Helpers ────────────────────────────────────────────────────────────────────

@Composable
private fun GlowCanvas() {
    Canvas(Modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x2E1D9E75), Color.Transparent),
                center = Offset(0f, 0f),
                radius = 480f
            ),
            radius = 480f,
            center = Offset(0f, 0f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x1E1D9E75), Color.Transparent),
                center = Offset(size.width, size.height),
                radius = 380f
            ),
            radius = 380f,
            center = Offset(size.width, size.height)
        )
    }
}

@Composable
private fun LoadingDot(color: Color, alpha: Float) {
    Box(
        Modifier
            .size(6.dp)
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = alpha.coerceIn(0f, 1f)))
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF061D16)
@Composable
fun SplashScreenPreview() {
    SplashScreen(rememberNavController())
}