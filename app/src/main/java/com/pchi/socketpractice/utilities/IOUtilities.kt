package com.pchi.socketpractice.utilities

import java.nio.ByteBuffer
import java.nio.ByteOrder

class IOUtilities : Utilities() {
    companion object {
        fun getIntFromBytes(byteArray: ByteArray, endian: ByteOrder = ByteOrder.BIG_ENDIAN) =
                ByteBuffer.wrap(byteArray).order(endian).int

        fun getBytesFromInt(iTarget: Int, capacity: Int = 4, endian: ByteOrder = ByteOrder.BIG_ENDIAN) =
                ByteBuffer.allocate(capacity).order(endian).putInt(iTarget).array()
                        ?: ByteArray(capacity)
    }
}
