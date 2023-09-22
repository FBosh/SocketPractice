package com.pchi.socketpractice.fragments

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.pchi.socketpractice.App
import com.pchi.socketpractice.R
import com.pchi.socketpractice.utilities.Utilities

abstract class BaseFragment : Fragment() {
    protected val appCtx = App.shared().applicationContext ?: App.shared()

    abstract val layoutRes: Int

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(layoutRes, container, false)
    }

    abstract fun initUI()

    override fun onStart() {
        super.onStart()

        initUI()
    }

    protected fun scrollToBottom(view: View) = Utilities.scrollToBottom(view)

    protected fun setupTVLog(tv: TextView) {
        tv.apply {
            setBackgroundResource(R.color.color_black)
            text = StringBuilder("Log:")
            setTextColor(Utilities.getColor(R.color.color_white))
            movementMethod = ScrollingMovementMethod()
        }
    }

    protected fun logOnTV(tv: TextView?, msg: String) {
        App.shared().appHandler.post {
            tv?.also {
                it.text = StringBuilder(it.text).append(msg).append("\n")
                scrollToBottom(it)
            }
        }
    }

    protected fun printBoshLog(msg: String) = Utilities.printBoshLog(msg)
}
