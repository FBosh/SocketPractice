package com.pchi.socketpractice

import android.content.pm.ActivityInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pchi.socketpractice.utilities.Utilities

abstract class BaseActivity : AppCompatActivity() {
    private var canCloseApp = false

    protected val appCtx = App.shared().applicationContext ?: App.shared()

    override fun onBackPressed() {
        if (canCloseApp) {
            finish()

            return
        }

        canCloseApp = true
        Toast.makeText(this, "Tap again to close App", Toast.LENGTH_SHORT).show()
        App.shared().appHandler.postDelayed({ canCloseApp = false }, 2500)
    }

    protected abstract fun init()

    protected abstract fun initUI()

    protected fun lockScreenPortrait() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    protected fun printBoshLog(msg: String) = Utilities.printBoshLog(msg)
}
