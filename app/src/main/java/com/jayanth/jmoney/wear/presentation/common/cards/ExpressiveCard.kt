package com.jayanth.jmoney.wear.presentation.common.cards

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CardColors
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.MaterialTheme

import androidx.compose.animation.core.animateDpAsState

@Composable
fun ExpressiveCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: CardColors? = null,
    shape: Shape = RoundedCornerShape(24.dp), // Large M3 Expressive corner radius
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Dynamic corner radius morphing on press
    val dynamicCornerRadius by animateDpAsState(
        targetValue = if (isPressed) 12.dp else 24.dp,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
        label = "CardShapeMorph"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
        label = "CardPressScale"
    )

    val resolvedColors = colors ?: CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface
    )

    val isDefaultShape = shape == RoundedCornerShape(24.dp)
    val finalShape = if (isDefaultShape) {
        RoundedCornerShape(dynamicCornerRadius)
    } else {
        shape
    }

    Card(
        onClick = onClick,
        colors = resolvedColors,
        shape = finalShape,
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
            .scale(scale)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}
