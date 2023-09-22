package com.pchi.socketpractice.fragments

import android.text.method.DigitsKeyListener
import com.pchi.socketpractice.Constants
import com.pchi.socketpractice.R
import com.pchi.socketpractice.adapters.ConnectionAdapter
import kotlinx.android.synthetic.main.frag_single_user.*
import java.net.*

class SingleUserFragment : BaseFragment() {
    private val mCAdapter = ConnectionAdapter()

//    private val sp by lazy { activity?.getSharedPreferences("Bosh_sp", Context.MODE_PRIVATE) }

    override val layoutRes = R.layout.frag_single_user

    override fun initUI() {
        setupTVLog(tv_log)

        arrayOf(et_url, et_uid, et_name, et_server).forEach { et ->
            if (et == null) return@forEach

            et.setSingleLine()

            when (et) {
                et_url -> et.setText(Constants.streamerUrl)
//                et_url -> et.setText(sp?.getString("str_streamer_url", Constants.streamerUrl)
//                        ?: Constants.streamerUrl)

                et_uid -> et.keyListener = DigitsKeyListener.getInstance("0123456789")

                et_server -> et.setText(Constants.svUrl)
            }

            et.nextFocusDownId = when (et) {
                et_url -> et_uid.id
                et_uid -> et_name.id
                et_name -> et_server.id
                else -> 0
            }
        }

        arrayOf(btn_user, btn_create, btn_send, btn_ping,
                btn_start_auto_ping, btn_stop_auto_ping, btn_stop
        ).forEach { btn ->
            if (btn == null) return@forEach

            btn.setOnClickListener { v ->
                val cListener = object : ConnectionAdapter.OnConnectListener {
                    override fun onFail(msg: String) {
                        logOnTV(tv_log, msg)
                    }

                    override fun onComplete(msg: String) {
                        logOnTV(tv_log, msg)
                    }
                }

                when (v) {
                    btn_user -> {
                        mCAdapter.sendHTTP(et_url, et_uid to "uid", et_name to "name", listener = cListener)
                    }

                    btn_create -> {
                        mCAdapter.createNewSocket(et_server, cListener)
                    }

                    btn_send -> {
                        mCAdapter.sendDataThroughSocket(et_uid, et_name, cListener)
                    }

                    btn_ping -> {
                        mCAdapter.pingServer(et_uid, et_name, cListener)
                    }

                    btn_start_auto_ping -> {
                        mCAdapter.startAutoPing(et_uid, et_name, cListener)
                    }

                    btn_stop_auto_ping -> {
                        mCAdapter.stopAutoPing()
                    }

                    btn_stop -> {
                        mCAdapter.terminateConnection(et_uid, et_name, cListener)
                    }
                }
            }
        }
    }

//    override fun onDestroyView() {
//        sp?.edit()?.apply {
//            putString("str_streamer_url", et_url.text.toString())
//            apply()
//        }
//
//        super.onDestroyView()
//    }
}
