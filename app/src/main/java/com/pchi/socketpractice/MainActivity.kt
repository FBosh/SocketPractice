package com.pchi.socketpractice

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.pchi.socketpractice.fragments.MultiUserFragment
import com.pchi.socketpractice.fragments.SingleUserFragment
import com.pchi.socketpractice.utilities.Utilities
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()

        initUI()
    }

    override fun init() {
        //
    }

    override fun initUI() {
        Utilities.handleFragment(
                supportFragmentManager, Constants.FRAG_REPLACE, layout_frag_container.id, SingleUserFragment()
        )

        lockScreenPortrait()

        layout_activity_main.keepScreenOn = true

        findViewById<Toolbar>(resources.getIdentifier("action_bar", "id", packageName)).also { bar ->
            if (bar == null) return@also

            var isSingleUserFrag = true

            bar.setOnClickListener {
                printBoshLog("Clicked action bar.")

                Utilities.handleFragment(
                        supportFragmentManager,
                        Constants.FRAG_REPLACE,
                        layout_frag_container.id,
                        if (isSingleUserFrag) MultiUserFragment() else SingleUserFragment()
                )

                isSingleUserFrag = !isSingleUserFrag
            }
        }
    }
}
