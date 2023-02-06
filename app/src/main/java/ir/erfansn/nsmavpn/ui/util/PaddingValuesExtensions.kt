package ir.erfansn.nsmavpn.ui.util

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection

@Composable
operator fun PaddingValues.plus(values: PaddingValues): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current

    return PaddingValues(
        top = calculateTopPadding() + values.calculateTopPadding(),
        start = calculateStartPadding(layoutDirection) + values.calculateStartPadding(layoutDirection),
        bottom = calculateBottomPadding() + values.calculateBottomPadding(),
        end = calculateEndPadding(layoutDirection) + values.calculateEndPadding(layoutDirection),
    )
}
