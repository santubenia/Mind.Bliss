package com.mhss.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mhss.app.ui.theme.Blue
import com.mhss.app.ui.theme.MyBrainTheme
import com.mhss.app.ui.theme.DarkOrange
import com.mhss.app.ui.theme.Purple


fun gradientBrushColor(
    vararg colorStops: Pair<Float, Color> = arrayOf(
        0f to Blue,
        0.4f to Purple,
        1f to DarkOrange,
    )
) = Brush.linearGradient(
    colorStops = colorStops,
    start = Offset.Zero,
    end = Offset.Infinite
)

@Preview
@Composable
private fun GradientColorPreview() {
    MyBrainTheme(useDynamicColors = false) {
        Box(
            Modifier
                .size(100.dp)
                .drawBehind {
                    drawRect(gradientBrushColor())
                }

        )
    }
}