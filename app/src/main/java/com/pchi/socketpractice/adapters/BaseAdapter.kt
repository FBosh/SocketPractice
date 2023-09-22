package com.pchi.socketpractice.adapters

import com.pchi.socketpractice.App
import com.pchi.socketpractice.utilities.Utilities

open class BaseAdapter {
    protected val appCtx = App.shared().applicationContext ?: App.shared()

    protected fun printBoshLog(msg: String) = Utilities.printBoshLog(msg)
}
