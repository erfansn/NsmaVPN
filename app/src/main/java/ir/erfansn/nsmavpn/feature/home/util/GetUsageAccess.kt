package ir.erfansn.nsmavpn.feature.home.util

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.getSystemService

class GetUsageAccess : ActivityResultContract<Unit, Context.() -> Boolean>() {

    override fun createIntent(context: Context, input: Unit): Intent {
        return Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Context.() -> Boolean {
        return { isGrantedGetUsageStatsPermission }
    }

    override fun getSynchronousResult(context: Context, input: Unit): SynchronousResult<Context.() -> Boolean>? {
        return if (context.isGrantedGetUsageStatsPermission) SynchronousResult { true } else null
    }
}

val Context.isGrantedGetUsageStatsPermission: Boolean
    get() {
        val appOps = getSystemService<AppOpsManager>()!!
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), packageName)
        } else {
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }
