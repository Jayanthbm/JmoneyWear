package com.jayanth.jmoney.wear.presentation.common.cards

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PayDayCard(
    nextPayDate: String,
    remainingDays: Int,
    daysInMonth: Int,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val passedDays = maxOf(0, daysInMonth - remainingDays)
    val dotColorPassed = MaterialTheme.colorScheme.primary
    val dotColorRemaining = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)

    ExpressiveCard(
        onClick = onClick ?: {},
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "PAY DAY",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = nextPayDate,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(54.dp)
            ) {
                Canvas(modifier = Modifier.size(54.dp)) {
                    val radius = size.width / 2f - 4.dp.toPx()
                    val center = Offset(size.width / 2f, size.height / 2f)
                    
                    for (i in 0 until daysInMonth) {
                        val angleDegrees = (i * (360f / daysInMonth)) - 90f
                        val angleRad = Math.toRadians(angleDegrees.toDouble())
                        val dotCenter = Offset(
                            x = center.x + radius * cos(angleRad).toFloat(),
                            y = center.y + radius * sin(angleRad).toFloat()
                        )
                        val isPassed = i < passedDays
                        drawCircle(
                            color = if (isPassed) dotColorPassed else dotColorRemaining,
                            radius = if (isPassed) 2.2.dp.toPx() else 1.8.dp.toPx(),
                            center = dotCenter
                        )
                    }
                }
                Text(
                    text = remainingDays.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
