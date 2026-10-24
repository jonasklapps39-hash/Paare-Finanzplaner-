package de.paarfinanz.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Runder Avatar mit Initiale in der Akzentfarbe der Person. */
@Composable
fun PartnerAvatar(
    name: String,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
) {
    Surface(
        shape = CircleShape,
        color = color,
        modifier = modifier.size(size),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = name.trim().firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
            )
        }
    }
}
