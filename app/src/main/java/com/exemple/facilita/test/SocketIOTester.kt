package com.exemple.facilita.test

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

/**
 * Teste simples de conexão Socket.IO
 * Use este código para testar a conexão básica
 */
object SocketIOTester {
    private const val TAG = "SocketIOTester"
    private const val SERVER_URL = "https://servidor-facilita.onrender.com"

    fun testarConexao() {
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "🧪 INICIANDO TESTE DE SOCKET.IO")
        Log.d(TAG, "🌐 URL: $SERVER_URL")
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

        try {
            val opts = IO.Options().apply {
                transports = arrayOf("websocket")
                reconnection = true
            }

            val socket = IO.socket(SERVER_URL, opts)

            socket.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "✅ CONECTADO COM SUCESSO!")

                // Testar user_connected
                val userData = JSONObject().apply {
                    put("userId", 999)
                    put("userType", "prestador")
                    put("userName", "Teste")
                }
                Log.d(TAG, "📤 Enviando user_connected: $userData")
                socket.emit("user_connected", userData)

                // Testar join_servico
                Log.d(TAG, "📤 Enviando join_servico: 1")
                socket.emit("join_servico", "1")

                // Testar envio de mensagem após 2 segundos
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    val msg = JSONObject().apply {
                        put("servicoId", 1)
                        put("mensagem", "MENSAGEM DE TESTE")
                        put("sender", "prestador")
                        put("targetUserId", 1)
                    }
                    Log.d(TAG, "📤 Enviando send_message: $msg")
                    socket.emit("send_message", msg)
                }, 2000)
            }

            socket.on(Socket.EVENT_CONNECT_ERROR) { args ->
                Log.e(TAG, "❌ ERRO DE CONEXÃO: ${args.contentToString()}")
            }

            socket.on(Socket.EVENT_DISCONNECT) {
                Log.d(TAG, "🔌 DESCONECTADO")
            }

            socket.on("receive_message") { args ->
                Log.d(TAG, "📥 MENSAGEM RECEBIDA: ${args[0]}")
            }

            socket.on("user_connected") { args ->
                Log.d(TAG, "👤 USER_CONNECTED RESPOSTA: ${args[0]}")
            }

            Log.d(TAG, "🔌 Chamando socket.connect()...")
            socket.connect()

        } catch (e: Exception) {
            Log.e(TAG, "❌ EXCEÇÃO: ${e.message}", e)
        }
    }
}

