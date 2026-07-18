package com.jayanth.jmoney.wear.presentation.form

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.wear.compose.material3.Icon

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.scale

private val KEY_SIZE = 32.dp
private val SPACING = 3.dp

@Composable
fun NumericKeypad(
    value: Double,
    onValueChange: (Double) -> Unit,
    onBack: () -> Unit,
    onConfirm: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var displayValue by remember { mutableStateOf(if (value == 0.0) "" else value.toString()) }

    BackHandler(onBack = onBack)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 40.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SPACING)
        ) {
            // Top action row: Close | DEL | OK — icon buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ActionButton(
                    icon = Icons.Filled.Close,
                    contentDescription = "Close",
                    onClick = onBack,
                    color = MaterialTheme.colorScheme.errorContainer,
                    textColor = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.weight(1f)
                )
                ActionButton(
                    icon = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Backspace",
                    onClick = {
                        val strValue = displayValue
                        if (strValue.isNotEmpty()) {
                            val newValue = strValue.dropLast(1)
                            displayValue = newValue
                            onValueChange(newValue.toDoubleOrNull() ?: 0.0)
                        }
                    },
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    textColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                ActionButton(
                    icon = Icons.Filled.Check,
                    contentDescription = "Confirm",
                    onClick = onConfirm,
                    color = MaterialTheme.colorScheme.primary,
                    textColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.weight(1f)
                )
            }

            // Display current value
            Text(
                text = if (displayValue.isEmpty()) "0" else displayValue,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 1.dp)
            )

            // Keypad number grid (1-2-3 / 4-5-6 / 7-8-9)
            val numberRows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9")
            )

            numberRows.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(SPACING),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    row.forEach { label ->
                        KeypadButton(
                            label = label,
                            onClick = {
                                val newValue = if (displayValue.isEmpty()) {
                                    label
                                } else {
                                    "$displayValue$label"
                                }
                                displayValue = newValue
                                onValueChange(newValue.toDoubleOrNull() ?: value)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Bottom row: C (clear), 0, . (decimal)
            Row(
                horizontalArrangement = Arrangement.spacedBy(SPACING),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("C", "0", ".").forEach { label ->
                    KeypadButton(
                        label = label,
                        onClick = {
                            when (label) {
                                "C" -> {
                                    displayValue = ""
                                    onValueChange(0.0)
                                }
                                "." -> {
                                    if (!displayValue.contains(".")) {
                                        val newValue = if (displayValue.isEmpty()) {
                                            "0."
                                        } else {
                                            "$displayValue."
                                        }
                                        displayValue = newValue
                                        onValueChange(newValue.toDoubleOrNull() ?: value)
                                    }
                                }
                                else -> { // "0"
                                    val newValue = if (displayValue.isEmpty()) {
                                        "0"
                                    } else {
                                        "$displayValue$label"
                                    }
                                    displayValue = newValue
                                    onValueChange(newValue.toDoubleOrNull() ?: value)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    color: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 6.dp else 13.dp,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "ActionShapeMorph"
    )
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "ActionPressScale"
    )

    Button(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(cornerRadius),
        modifier = modifier
            .height(26.dp)
            .scale(scale),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = textColor
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.scale(0.85f)
        )
    }
}

@Composable
private fun KeypadButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 6.dp else 16.dp, // Morphs from circle (16dp radius for 32dp size) to rounded rectangle (6dp radius)!
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "KeypadShapeMorph"
    )
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "KeypadPressScale"
    )

    Button(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(cornerRadius),
        modifier = modifier
            .height(KEY_SIZE)
            .width(KEY_SIZE)
            .scale(scale),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}
