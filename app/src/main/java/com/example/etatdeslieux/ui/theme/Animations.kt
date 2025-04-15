package com.example.etatdeslieux.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.dp

object AppAnimations {
    // Animation pour le bouton FAB
    @Composable
    fun rotateFabAnimation(expanded: Boolean): State<Float> {
        val angle by animateFloatAsState(
            targetValue = if (expanded) 45f else 0f,
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            ),
            label = "fab_rotation"
        )
        return rememberUpdatedState(angle)
    }

    // Animation pour le menu qui slide
    fun menuSlideSpec(): SpringSpec<Int> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    // Animation pour les boutons
    @Composable
    fun buttonClickAnimation(): State<Float> {
        val scale = remember { Animatable(1f) }
        LaunchedEffect(Unit) {
            scale.animateTo(
                targetValue = 0.95f,
                animationSpec = tween(100)
            )
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        return rememberUpdatedState(scale.value)
    }

    // Animation pour les transitions de page
    fun pageTransitionSpec(): SpringSpec<Int> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
}
