package com.kangrio.virtualdisplay.utils

import android.util.Log
import com.kangrio.virtualdisplay.helper.ShizukuHelper

class AppsUtils {
    val TAG = "AppsUtils"
    val shizukuHelper: ShizukuHelper = ShizukuHelper()

    fun launchAppTargetDisplay(packageName: String, componentClassName: String, displayId: Int) {
        Log.d(TAG, "launchAppTargetDisplay: $displayId")

        // 隠しAPIの代わりに、Shizukuのシェルコマンド(am start)を使って指定ディスプレイでアプリを起動します
        // --windowingMode 1 をつけることで activityOptions.launchWindowingMode = 1 と同じ効果になります
        val cmd = "am start -n $packageName/$componentClassName --display $displayId --windowingMode 1"
        Log.d(TAG, "launchAppTargetDisplay: $cmd")
        try {
            shizukuHelper.execInternal(cmd)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun killApp(packageName: String) {
        val cmd = "am force-stop $packageName"
        Log.d(TAG, "killApp: $cmd")
        try {
            shizukuHelper.execInternal(cmd)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}
