package com.pchi.socketpractice.utilities

import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.pchi.socketpractice.App
import com.pchi.socketpractice.Constants
import com.pchi.socketpractice.fragments.BaseFragment

open class Utilities {
    companion object {
        protected val appCtx = App.shared().applicationContext ?: App.shared()

        fun getColor(colorRes: Int) = ContextCompat.getColor(appCtx, colorRes)

        fun handleFragment(fm: FragmentManager, way: String, layoutContainerId: Int, newFragment: BaseFragment) {
            fm.beginTransaction().apply {
                when (way) {
                    Constants.FRAG_ADD -> add(layoutContainerId, newFragment).commit()
                    Constants.FRAG_REPLACE -> replace(layoutContainerId, newFragment).commit()
                    Constants.FRAG_REMOVE -> remove(newFragment).commit()
                }
            }
        }

        fun printBoshLog(msg: String) = Log.i("Bosh_Tag", msg)

        fun scrollToBottom(view: View) {
            while (view.canScrollVertically(87)) view.scrollBy(0, 1)
        }
    }
}
