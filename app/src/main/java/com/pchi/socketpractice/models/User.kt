package com.pchi.socketpractice.models

import com.pchi.socketpractice.adapters.ConnectionAdapter

data class User(
        val uid: Int,
        var name: String,
        val connectionAdapter: ConnectionAdapter
) : BaseModel() {
//    override fun toString() = name
}
