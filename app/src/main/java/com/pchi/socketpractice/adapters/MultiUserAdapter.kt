package com.pchi.socketpractice.adapters

import android.widget.EditText
import com.pchi.socketpractice.models.User

class MultiUserAdapter : BaseAdapter() {
    private val mUsers = sortedMapOf<Int, User>()

    fun startConnections(
            etURL: EditText, etUID1: EditText, etUID2: EditText, etName: EditText,
            etTarget: EditText, etServer: EditText, listener: ConnectionAdapter.OnConnectListener
    ) {
        if (etName.text.isBlank() || etTarget.text.isBlank()) {
            listener.onFail(when {
                etName.text.isBlank() -> "Name is blank!!"
                etTarget.text.isBlank() -> "Invalid target!!"
                else -> "靈異結果"
            })

            return
        }

        try {
            val uid1 = etUID1.text.toString().toInt()
            val uid2 = etUID2.text.toString().toInt()
            val range = if (uid1 > uid2) uid1 downTo uid2 else uid1..uid2

            range.forEach { uid ->
                if (mUsers[uid]?.uid != uid || mUsers[uid]?.name != etName.text.toString().trim()) {
                    mUsers[uid] = User(uid, etName.text.toString().trim(), ConnectionAdapter())
                }
            }
        } catch (e: NumberFormatException) {
            e.printStackTrace()

            listener.onFail("Invalid UID!!")
        }

        mUsers.forEach { entry ->
            Thread {
                val user = entry.value
                val url = StringBuilder(etURL.text).apply {
                    append("?uid=${user.uid}")
                    if (user.name.isNotBlank()) append("&name=${user.name}")
                    append("&token=${user.connectionAdapter.mTokenFromHTTP}")
                    append("&tid=${etTarget.text}")
                }.toString()

                user.connectionAdapter.apply {
                    sendHTTP(url, listener)

                    for (step in 0..5) {
                        if (!isTokenBlank()) break

                        Thread.sleep(500 + 10)
                    }

                    if (isTokenBlank()) return@apply

                    createNewSocket(etServer, listener)

                    Thread.sleep(1000)

                    if (!isSocketConnected()) return@apply

                    sendTargetToServer(user.uid.toString(), user.name, etTarget.text.toString(), listener)

                    Thread.sleep(1000)

                    if (!isSocketConnected()) return@apply

                    startAutoPing(user.uid.toString(), user.name, listener)
                }
            }.start()
        }
    }

    fun stopConnections(listener: ConnectionAdapter.OnConnectListener) {
        for ((_, user) in mUsers) {
            user.connectionAdapter.terminateConnection(user.uid.toString(), user.name, listener)
        }
    }

    fun stopConnection(etForcedDisconnect: EditText, listener: ConnectionAdapter.OnConnectListener) {
        try {
            mUsers[etForcedDisconnect.text.toString().toInt()]?.apply {
                connectionAdapter.terminateConnection(uid.toString(), name, listener)
            }
        } catch (e: NumberFormatException) {
            e.printStackTrace()

            listener.onFail("Invalid forced disconnect ID!!")
        }
    }
}
