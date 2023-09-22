package com.pchi.socketpractice.adapters

import android.os.Handler
import android.widget.EditText
import com.pchi.socketpractice.Constants
import com.pchi.socketpractice.utilities.IOUtilities
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream
import java.net.*
import java.nio.ByteOrder

@Suppress("MemberVisibilityCanBePrivate")
class ConnectionAdapter : BaseAdapter() {
    companion object {
        const val SV_TYPE_SEND = "A001"
        const val SV_TYPE_PING = "0001"
        const val SV_TYPE_STOP = "0000"
        const val SV_TYPE_TARGET = "A003"
    }

    var mTokenFromHTTP = ""; private set
    var isSendingHTTP = false; private set

    private var mSocket = Socket()
    private var mIPS: InputStream? = null
    private var mOPS: OutputStream? = null

    private var mHandlerForAutoPing = Handler()
    private var isAutoPinging = false
    private var rAutoPing: Runnable? = null

    private var isManuallyDisconnected = false

    private var mState = State.Disconnected

    private var mCID = ""

    private var mTID = ""

    fun sendHTTP(urlPath: String, listener: OnConnectListener): Boolean {
        var result = false

        if (isSendingHTTP) {
            listener.onFail("Connecting, please wait.")

            return result
        }

        try {
            URL(urlPath)
        } catch (e: MalformedURLException) {
            e.printStackTrace()

            mTokenFromHTTP = ""

            listener.onFail("Invalid URL!!")

            return result
        } finally {
            printBoshLog("result in HTTP_1= $result")
            printBoshLog("token1= $mTokenFromHTTP")
        }

        (URL(urlPath).openConnection() as HttpURLConnection).apply {
            isSendingHTTP = true

            printBoshLog("URL in HTTP= $url")

            requestMethod = "GET"
            connectTimeout = 3000
            setRequestProperty("HttpKey", Constants.httpHeader)

            Thread {
                try {
                    connect()

                    printBoshLog("responseCode= $responseCode")
                    printBoshLog("responseMessage= $responseMessage")

                    inputStream.bufferedReader().readText().also { txt ->
                        mTokenFromHTTP = if (txt.contains("token")) {
                            txt.substringAfter("token\":\"").substringBefore("\"},\"code")
                        } else {
                            ""
                        }

                        if (mTokenFromHTTP.contains("tid")) {
                            mTokenFromHTTP = mTokenFromHTTP.substringBefore("\",\"tid")
                        }

                        listener.onComplete(txt)
                    }

                    disconnect()

                    result = true
                } catch (e: Exception) {
                    e.printStackTrace()

                    mTokenFromHTTP = ""

                    listener.onFail(when (e) {
                        is SocketTimeoutException -> "Failed to connect after timeout!!"
                        is ConnectException -> "Connected fail!!"
                        is UnknownHostException -> "Unknown host!!"
                        else -> "Unknown failed!!"
                    })

                    result = false
                } finally {
                    printBoshLog("result in HTTP_2= $result")
                    printBoshLog("token2= $mTokenFromHTTP")

                    isSendingHTTP = false
                }
            }.start()
        }

        return result
    }

    fun sendHTTP(etURL: EditText, vararg params: Pair<EditText, String>, listener: OnConnectListener) =
            sendHTTP(
                    StringBuilder(etURL.text).apply {
                        var isTheStart = false

                        for (p in params) {
                            if (p.first.text.isNullOrBlank()) continue

                            append(if (isTheStart) '&' else '?')
                            isTheStart = true

                            append("${p.second}=${p.first.text}")
                        }
                    }.toString(),
                    listener
            )

    fun isTokenBlank() = mTokenFromHTTP.isBlank()

    fun createNewSocket(strIP: String, port: Int, listener: OnConnectListener): Boolean {
        var result = false

        isManuallyDisconnected = false

        when (mState) {
            State.Connecting -> {
                listener.onFail("Socket connecting, please wait.")

                return false
            }

            State.Connected -> {
                listener.onFail("Socket is already connected.")

                return false
            }

            else -> {
                //
            }
        }

        mState = State.Connecting

        Thread {
            try {
                Socket(strIP, port).also { sk ->
                    printBoshLog("Socket= $sk")
                    printBoshLog("Socket.isConnected= ${sk.isConnected}")
                    printBoshLog("Socket.isClosed= ${sk.isClosed}")

                    if (!sk.isConnected || sk.isClosed) {
                        result = false

                        listener.onFail("No socket connection!!")

                        return@also
                    }

                    mSocket = sk
                    mIPS = sk.inputStream
                    mOPS = sk.outputStream

                    sk.keepAlive = true

                    mState = State.Connected

                    mCID = ""
                }

                val bytes = ByteArray(DEFAULT_BUFFER_SIZE)

                while (true) {
                    if (!mSocket.isConnected || isManuallyDisconnected
                            || mState == State.Disconnected || mState == State.Disconnecting) {
                        break
                    }

                    val strFromSV = String(bytes, 0, mIPS?.read(bytes) ?: -87)
                    printBoshLog("strFromSV= $strFromSV")

                    val strHeader = Constants.socketHeader
                    val strData = strFromSV.substring(strHeader.length + 4, strFromSV.length)
                    val lengthOfBAData = strData.toByteArray(Charsets.UTF_8).size

                    printBoshLog("header= $strHeader")
                    printBoshLog("strData= $strData")
                    printBoshLog("lengthOfBAData= $lengthOfBAData")

                    if (strData.contains("\"result\":0")) {
                        listener.onComplete(strData)

                        mState = State.Disconnected

                        if (isAutoPinging) stopAutoPing()

                        mTokenFromHTTP = ""

                        break
                    }

                    if (strData.contains("\"type\":\"A001\"") || strData.contains("\"type\":\"A003\"")) {
                        mCID = strData.substringAfter("\"data\":\"").substringBefore("\",\"code")
                    }

                    listener.onComplete(strData)
                }

                result = true
            } catch (e: Exception) {
                e.printStackTrace()

                listener.onFail(when (e) {
                    is ConnectException -> "Failed to connect to server!!"
                    is UnknownHostException -> "Unknown host!!"
                    is NoRouteToHostException -> "Invalid route!!"
                    is SocketException -> "Connection lost!!"
                    else -> "Unknown fail!!"
                })

                mState = State.Disconnected

                if (isAutoPinging) stopAutoPing()

                result = false
            } finally {
                printBoshLog("result in create_socket= $result")
                printBoshLog("mSocket= $mSocket")
                printBoshLog("mIPS= $mIPS")
                printBoshLog("mOPS= $mOPS")

                printBoshLog("mState= $mState")
            }
        }.start()

        return result
    }

    fun createNewSocket(etServer: EditText, listener: OnConnectListener) =
            try {
                createNewSocket(
                        etServer.text.toString().substringBefore(':'),
                        etServer.text.toString().substringAfter(":").toInt(),
                        listener
                )
            } catch (e: NumberFormatException) {
                e.printStackTrace()

                listener.onFail("Invalid server port!!")

                false
            }

    fun isSocketConnected() = mState == State.Connected

    fun sendDataThroughSocket(etUID: EditText, etName: EditText, listener: OnConnectListener) =
            sendSomethingToServer(SV_TYPE_SEND, etUID, etName, listener)

//    fun sendDataThroughSocket(userID: String, userName: String, listener: OnConnectListener) =
//            sendSomethingToServer(SV_TYPE_SEND, userID, userName, listener)

    fun pingServer(etUID: EditText, etName: EditText, listener: OnConnectListener) =
            sendSomethingToServer(SV_TYPE_PING, etUID, etName, listener)

    fun pingServer(userID: String, userName: String, listener: OnConnectListener) =
            sendSomethingToServer(SV_TYPE_PING, userID, userName, listener)

    fun startAutoPing(etUID: EditText, etName: EditText, listener: OnConnectListener) =
            startAutoPing(etUID.text.toString(), etName.text.toString(), listener)

    fun startAutoPing(userID: String, userName: String, listener: OnConnectListener) {
        isAutoPinging = true

        if (rAutoPing == null) {
            rAutoPing = object : Runnable {
                override fun run() {
                    if (isAutoPinging) {
                        if (mState != State.Connected) {
                            listener.onFail("Current state is not connected!!")
                            mHandlerForAutoPing.post { stopAutoPing() }

                            return
                        }

                        pingServer(userID, userName, listener)
                        mHandlerForAutoPing.postDelayed(this, 10000)
                    }
                }
            }.apply { run() }
        }
    }

    fun stopAutoPing() {
        isAutoPinging = false
        mHandlerForAutoPing.removeCallbacks(rAutoPing ?: return)
        rAutoPing = null
    }

    fun terminateConnection(etUID: EditText, etName: EditText, listener: OnConnectListener) =
            terminateConnection(etUID.text.toString(), etName.text.toString(), listener)

    fun terminateConnection(userID: String, userName: String, listener: OnConnectListener): Boolean {
        if (mState == State.Disconnected || mState == State.Disconnecting) {
            listener.onFail(when (mState) {
                State.Disconnected -> "Already disconnected!!"
                State.Disconnecting -> "The connection is disconnecting."
                else -> "靈異結果"
            })

            return false
        }

        isManuallyDisconnected = true

        mState = State.Disconnecting

        if (isAutoPinging) mHandlerForAutoPing.post { stopAutoPing() }

        return sendSomethingToServer(SV_TYPE_STOP, userID, userName, listener)
    }

    fun sendTargetToServer(
            userID: String,
            userName: String,
            targetID: String,
            listener: OnConnectListener
    ): Boolean {
        mTID = targetID
        return sendSomethingToServer(SV_TYPE_TARGET, userID, userName, listener)
    }

    private fun sendSomethingToServer(
            strSVType: String,
            etUID: EditText,
            etName: EditText,
            listener: OnConnectListener
    ) = sendSomethingToServer(strSVType, etUID.text.toString(), etName.text.toString(), listener)

    private fun sendSomethingToServer(
            strSVType: String,
            userID: String,
            userName: String,
            listener: OnConnectListener
    ): Boolean {
        if (mTokenFromHTTP.isBlank() || mOPS == null || mSocket.isClosed || mState == State.Disconnected) {
            listener.onFail(when {
                mTokenFromHTTP.isBlank() -> "No token!!"
                mOPS == null -> "outputStream is null!!"
                mSocket.isClosed -> "The socket is closed!!"
                mState == State.Disconnected -> "Current state is disconnected!!"
                else -> "Unknown failure!!"
            })

            return false
        }

        var result = false

        Thread {
            try {
                mOPS?.buffered()?.also { bos ->
                    val baData = JSONObject().apply {
                        put("type", strSVType)
                        put("data", JSONObject().apply {
                            put("uid", userID)

                            when (strSVType) {
                                SV_TYPE_SEND -> {
                                    put("name", userName)
                                    put("token", mTokenFromHTTP)
                                }

                                SV_TYPE_TARGET -> {
                                    put("tid", mTID)
                                    put("token", mTokenFromHTTP)
                                }

                                else -> {
                                    put("cid", mCID)
                                }
                            }
                        })
                    }.toString().toByteArray(Charsets.UTF_8)

                    bos.write(Constants.socketHeader.toByteArray(Charsets.US_ASCII)
                            + IOUtilities.getBytesFromInt(baData.size, endian = ByteOrder.LITTLE_ENDIAN)
                            + baData)
                    bos.flush()
                }

                result = true
            } catch (e: Exception) {
                e.printStackTrace()

                listener.onFail("Error!!")

                result = false
            } finally {
                printBoshLog("result in send_data= $result")

                printBoshLog("mState= $mState")
            }
        }.start()

        return result
    }

    interface OnConnectListener {
        fun onFail(msg: String)
        fun onComplete(msg: String)
    }

    private enum class State {
        Disconnecting, Disconnected, Connecting, Connected
    }
}
