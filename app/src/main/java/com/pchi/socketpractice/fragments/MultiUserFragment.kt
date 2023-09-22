package com.pchi.socketpractice.fragments

import android.text.InputType
import com.pchi.socketpractice.Constants
import com.pchi.socketpractice.R
import com.pchi.socketpractice.adapters.ConnectionAdapter
import com.pchi.socketpractice.adapters.MultiUserAdapter
import kotlinx.android.synthetic.main.frag_multiple_user.*

class MultiUserFragment : BaseFragment() {
    private val mAdapter = MultiUserAdapter()

    override val layoutRes = R.layout.frag_multiple_user

    override fun initUI() {
        setupTVLog(tv_log)

        arrayOf(et_url, et_uid, et_uid_2, et_name, et_target, et_server, et_forced_disconnect).forEach { et ->
            if (et == null) return@forEach

            et.isSingleLine = true
            et.nextFocusDownId = when (et) {
                et_url -> et_uid.id
                et_uid -> et_uid_2.id
                et_uid_2 -> et_name.id
                et_name -> et_target.id
                et_target -> et_server.id
                et_server -> et_forced_disconnect.id
                else -> 0
            }

            et.inputType = when (et) {
                et_uid, et_uid_2, et_target, et_forced_disconnect -> InputType.TYPE_CLASS_NUMBER
                else -> InputType.TYPE_CLASS_TEXT
            }

            et.setText(when (et) {
                et_url -> Constants.userUrl
                et_server -> Constants.svUrl
                else -> null
            })
        }

        arrayOf(btn_start, btn_stop, btn_forced_disconnect).forEach { btn ->
            if (btn == null) return@forEach

            btn.setOnClickListener {
                val cListener = object : ConnectionAdapter.OnConnectListener {
                    override fun onFail(msg: String) {
                        logOnTV(tv_log, msg)
                    }

                    override fun onComplete(msg: String) {
                        logOnTV(tv_log, msg)
                    }
                }

                when (it) {
                    btn_start -> {
                        mAdapter.startConnections(
                                et_url, et_uid, et_uid_2, et_name, et_target, et_server, cListener
                        )
                    }

                    btn_stop -> {
                        mAdapter.stopConnections(cListener)
                    }

                    btn_forced_disconnect -> {
                        mAdapter.stopConnection(et_forced_disconnect, cListener)
                    }
                }
            }
        }
    }
}
