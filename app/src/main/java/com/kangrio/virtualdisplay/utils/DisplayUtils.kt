package com.kangrio.virtualdisplay.utils

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.util.Log
import android.view.MotionEvent
import android.view.Surface
import com.kangrio.virtualdisplay.App
import com.kangrio.virtualdisplay.MainActivity
import com.kangrio.virtualdisplay.helper.ShizukuHelper

class DisplayUtils {
    private val TAG = "DisplayUtils"
    private val shizukuHelper = ShizukuHelper()
    private val handler = App.handler

    // Shizuku経由のシェルコマンド(input)でタッチイベントを送信する
    fun sendMotionEvent(motionEvent: MotionEvent) {
        val action = motionEvent.actionMasked
        val x = motionEvent.x
        val y = motionEvent.y
        // displayId が設定されている場合は、そのディスプレイに対して入力を送る（Android 10以降）
        val displayArg = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q && motionEvent.displayId != -1) {
            "-d ${motionEvent.displayId}"
        } else {
            ""
        }

        var cmd = ""
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                // シンプルなタップとして処理（スワイプなどの複雑なジェスチャーはシェルコマンドでは完全再現が難しいため簡易化）
                cmd = "input $displayArg tap $x $y"
            }
            // MotionEvent.ACTION_MOVE など他のイベントも必要に応じてコマンド化できますが、
            // コマンドのオーバーヘッドが大きいため、まずはACTION_DOWN(タップ)のみで動作確認を推奨します。
        }

        if (cmd.isNotEmpty()) {
             Log.d(TAG, "sendMotionEvent: $cmd")
             try {
                 // 別スレッドでコマンドを実行
                 Thread {
                     shizukuHelper.execInternal(cmd)
                 }.start()
             } catch (e: Throwable) {
                 e.printStackTrace()
             }
        }
    }


    private var virtualDisplay: VirtualDisplay? = null
    private var displayManager: DisplayManager? = null

    @SuppressLint("WrongConstant")
    fun createVirtualDisplay(
        context: Context,
        surface: Surface?,
        width: Int,
        height: Int
    ): VirtualDisplay {
        var displayId = 0
        var realDensity = context.resources.displayMetrics.densityDpi

        val density: Int =
            realDensity + (0.1f * realDensity).toInt()

        Log.d(TAG, "createVirtualDisplay: density = $density")

        // 隠しAPIのコンストラクタではなく、SystemServiceから取得する
        displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager

        if (virtualDisplay != null) virtualDisplay!!.release()

        virtualDisplay = displayManager!!.createVirtualDisplay(
            "MyVirtualDisplay",
            width,
            height,
            density,
            surface,
            2
        )

        displayId = virtualDisplay!!.display.displayId
        Log.d(TAG, "createVirtualDisplayTest: Succes: len(${displayManager!!.displays.size})")
        Log.d(
            TAG,
            "createVirtualDisplayTest: Succes: $displayId: ${virtualDisplay!!.display.flags}"
        )


        handler.post {
            MainActivity.tvDisplayId!!.text =
                "Display Size: ${displayManager!!.displays.size} Id: $displayId"
        }
        return virtualDisplay as VirtualDisplay
    }
}
