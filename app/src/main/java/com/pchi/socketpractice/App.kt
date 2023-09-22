package com.pchi.socketpractice

import android.app.Application
import android.os.Handler

class App : Application() {
    companion object {
        private lateinit var instance: App

        fun shared() = instance
    }

    val appHandler = Handler()

    override fun onCreate() {
        super.onCreate()

        instance = this
    }
}
