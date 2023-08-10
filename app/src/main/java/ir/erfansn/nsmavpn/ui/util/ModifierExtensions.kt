package ir.erfansn.nsmavpn.ui.util

import androidx.compose.ui.Modifier
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
inline fun Modifier.whenever(predicate: Boolean, block: Modifier.() -> Modifier): Modifier {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    return if (predicate) block() else this
}
