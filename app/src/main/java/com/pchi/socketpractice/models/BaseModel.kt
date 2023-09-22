package com.pchi.socketpractice.models

import com.pchi.socketpractice.App

open class BaseModel {
    protected val appCtx = App.shared().applicationContext ?: App.shared()
}
