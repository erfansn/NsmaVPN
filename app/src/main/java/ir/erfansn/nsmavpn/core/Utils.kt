package ir.erfansn.nsmavpn.core

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import ir.erfansn.nsmavpn.R

val Context.hasRationaleShownPreferences: SharedPreferences
    get() = getSharedPreferences(getString(R.string.runtime_permissions_rationale_shown_state), Context.MODE_PRIVATE)

operator fun SharedPreferences.set(key: String, value: Boolean) = edit {
    putBoolean(key, value)
}

operator fun SharedPreferences.get(key: String, default: Boolean = false) =
    getBoolean(key, default)
