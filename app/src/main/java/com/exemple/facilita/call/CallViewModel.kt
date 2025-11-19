package com.exemple.facilita.call

import androidx.lifecycle.ViewModel
import com.exemple.facilita.socket.SocketManager
import org.json.JSONObject

class CallViewModel : ViewModel() {

    var callState: CallState = CallState.Idle
        private set

    private val socket = SocketManager()

    fun connectSocket() {
        socket.connect("https://seu-servidor.com")
        listenSocketEvents()
    }

    private fun listenSocketEvents() {

        socket.on("call:incoming") { data ->
            callState = CallState.Incoming
        }

        socket.on("call:initiated") { data ->
            callState = CallState.Outgoing
        }

        socket.on("call:accepted") { data ->
            callState = CallState.Active
        }

        socket.on("call:ended") { data ->
            callState = CallState.Ended
        }
    }

    fun initiateCall(servicoId: Int, callerId: Int, targetUserId: Int) {
        val json = JSONObject().apply {
            put("servicoId", servicoId)
            put("callerId", callerId)
            put("targetUserId", targetUserId)
            put("callType", "video")
        }
        socket.emit("call:initiate", json)
    }

    fun acceptCall(servicoId: Int) {
        val json = JSONObject().apply {
            put("servicoId", servicoId)
        }
        socket.emit("call:accept", json)
    }

    fun endCall(servicoId: Int) {
        val json = JSONObject().apply {
            put("servicoId", servicoId)
        }
        socket.emit("call:end", json)
    }
}
