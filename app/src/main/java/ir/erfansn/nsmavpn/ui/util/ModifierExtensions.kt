package ir.erfansn.nsmavpn.ui.util

import androidx.compose.ui.Modifier

inline fun Modifier.whenever(predicate: Boolean, block: Modifier.() -> Modifier): Modifier {
    return if (predicate) block() else this
}
