package com.ramaphosa.takasmart.ui.screens.shared

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import com.ramaphosa.takasmart.ui.theme.*

enum class BadgeType { SUCCESS, WARNING, INFO, ERROR, NEUTRAL, PURPLE }

@Composable
fun StatusBadge(
    label    : String,
    type     : BadgeType,
    modifier : Modifier = Modifier
) {
    val (bg, fg) = when (type) {
        BadgeType.SUCCESS -> GreenSurface  to GreenDark
        BadgeType.WARNING -> AmberSurface  to AmberDark
        BadgeType.INFO    -> TealSurface   to TealDark
        BadgeType.ERROR   -> ErrorSurface  to ErrorDark
        BadgeType.NEUTRAL -> GraySurface   to GrayMid
        BadgeType.PURPLE  -> PurpleSurface to PurpleDark
    }
    Surface(
        shape    = CircleShape,
        color    = bg,
        modifier = modifier
    ) {
        Text(
            text     = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            color    = fg,
            style    = androidx.compose.material3.MaterialTheme.typography.labelSmall
        )
    }
}