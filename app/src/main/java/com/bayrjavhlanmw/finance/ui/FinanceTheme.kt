package com.bayrjavhlanmw.finance.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val IncomeColor = Color(0xFF176C55)
val ExpenseColor = Color(0xFFB5373B)

@Composable
fun FinanceTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = IncomeColor,
            secondary = Color(0xFF315A9A),
            background = Color(0xFFF5F7F6),
            surface = Color.White,
            surfaceVariant = Color(0xFFEAF0ED),
            onSurface = Color(0xFF17251F),
            onSurfaceVariant = Color(0xFF52665B),
            error = ExpenseColor
        ),
        shapes = Shapes(small = RoundedCornerShape(4.dp), medium = RoundedCornerShape(8.dp), large = RoundedCornerShape(8.dp)),
        content = content
    )
}
