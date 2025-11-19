package com.exemple.facilita.socket

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

class SocketManager {

    private var socket: Socket? = null

    fun connect(url: String) {
        if (socket != null) return

        socket = IO.socket(url)
        socket?.connect()

        socket?.on(Socket.EVENT_CONNECT) {
            Log.d("SOCKET", "Conectado ao servidor WS")
        }
    }

    fun on(event: String, callback: (JSONObject) -> Unit) {
        socket?.on(event) { args ->
            val data = args[0] as JSONObject
            callback(data)
        }
    }

    fun emit(event: String, data: JSONObject) {
        socket?.emit(event, data)
    }

    fun disconnect() {
        socket?.disconnect()
    }
}