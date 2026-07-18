package com.jayanth.jmoney.wear.presentation.common.buttons

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonColors
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text

@Composable
fun ExpressiveButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    text: String? = null,
    colors: ButtonColors? = null,
    isPrimary: Boolean = true,
    isError: Boolean = false,
    enabled: Boolean = true,
    size: Dp = 52.dp,
    width: Dp = size,
    height: Dp = size,
    shape: Shape = RoundedCornerShape(24.dp), // Expressive large rounded shape
    content: @Composable (RowScope.() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Dynamic corner radius morphing on press
    val dynamicCornerRadius by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isPressed) 8.dp else 24.dp,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "ButtonShapeMorph"
    )

    // Smooth press-scaling animation
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "ButtonPressScale"
    )

    val resolvedColors = colors ?: when {
        isError -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
        isPrimary -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
        else -> ButtonDefaults.filledTonalButtonColors()
    }

    val isDefaultShape = shape == RoundedCornerShape(24.dp)
    val finalShape = if (isDefaultShape) {
        RoundedCornerShape(dynamicCornerRadius)
    } else {
        shape
    }

    Button(
        onClick = onClick,
        colors = resolvedColors,
        shape = finalShape,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = modifier
            .then(if (width != Dp.Unspecified) Modifier.width(width) else Modifier)
            .then(if (height != Dp.Unspecified) Modifier.height(height) else Modifier)
            .scale(scale)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            if (content != null) {
                content()
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    modifier = Modifier.size(24.dp)
                )
            } else if (text != null) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}
