package com.exemple.facilita.data.service

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketService @Inject constructor() {

    private var socket: Socket? = null
    private val url = "https://facilita-c6hhb9csgygudrdz.canadacentral-01.azurewebsites.net"

    // 🔵 Já existentes (chat, localização, etc.) — NÃO ALTERADOS
    var onMessageReceived: ((JSONObject) -> Unit)? = null
    var onUserStatusChanged: ((JSONObject) -> Unit)? = null
    var onLocationUpdated: ((JSONObject) -> Unit)? = null

    // 🔴 Novos callbacks exclusivamente para chamadas
    var onCallIncoming: ((JSONObject) -> Unit)? = null
    var onCallInitiated: ((JSONObject) -> Unit)? = null
    var onCallAccepted: ((JSONObject) -> Unit)? = null
    var onRemoteOffer: ((JSONObject) -> Unit)? = null
    var onRemoteAnswer: ((JSONObject) -> Unit)? = null
    var onRemoteIce: ((JSONObject) -> Unit)? = null

    fun connect() {
        if (socket == null) {
            socket = IO.socket(url)
        }

        socket?.on(Socket.EVENT_CONNECT) {
            Log.d("WEBSOCKET", "Conectado")
        }

        // 🔵 Eventos existentes no seu app (não removidos!)
        socket?.on("chat:message") { args -> onMessageReceived?.invoke(args[0] as JSONObject) }
        socket?.on("user:status") { args -> onUserStatusChanged?.invoke(args[0] as JSONObject) }
        socket?.on("location:update") { args -> onLocationUpdated?.invoke(args[0] as JSONObject) }

        // 🔴 Eventos de chamada — ADICIONADOS
        socket?.on("call:incoming") { args -> onCallIncoming?.invoke(args[0] as JSONObject) }
        socket?.on("call:initiated") { args -> onCallInitiated?.invoke(args[0] as JSONObject) }
        socket?.on("call:accepted") { args -> onCallAccepted?.invoke(args[0] as JSONObject) }

        socket?.on("call:offer") { args -> onRemoteOffer?.invoke(args[0] as JSONObject) }
        socket?.on("call:answer") { args -> onRemoteAnswer?.invoke(args[0] as JSONObject) }
        socket?.on("call:ice-candidate") { args -> onRemoteIce?.invoke(args[0] as JSONObject) }

        socket?.connect()
    }

    fun disconnect() = socket?.disconnect()

    // 🔴 Emissores de chamada
    fun initiateCall(servicoId: Int, callerId: Int, callerName: String, targetId: Int) {
        val data = JSONObject()
        data.put("servicoId", servicoId)
        data.put("callerId", callerId)
        data.put("callerName", callerName)
        data.put("targetUserId", targetId)
        data.put("callType", "video")

        socket?.emit("call:initiate", data)
    }

    fun acceptCall(servicoId: Int, userId: Int) {
        val data = JSONObject()
        data.put("servicoId", servicoId)
        data.put("userId", userId)

        socket?.emit("call:accept", data)
    }

    fun sendOffer(servicoId: Int, sdp: String) {
        socket?.emit("call:offer", JSONObject().apply {
            put("servicoId", servicoId)
            put("type", "offer")
            put("sdp", sdp)
        })
    }

    fun sendAnswer(servicoId: Int, sdp: String) {
        socket?.emit("call:answer", JSONObject().apply {
            put("servicoId", servicoId)
            put("type", "answer")
            put("sdp", sdp)
        })
    }

    fun sendIceCandidate(servicoId: Int, candidate: String, sdpMid: String, lineIndex: Int) {
        socket?.emit("call:ice-candidate", JSONObject().apply {
            put("servicoId", servicoId)
            put("candidate", candidate)
            put("sdpMid", sdpMid)
            put("sdpMLineIndex", lineIndex)
        })
    }
}
