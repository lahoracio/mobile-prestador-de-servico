package com.exemple.facilita.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exemple.facilita.model.*
import com.exemple.facilita.util.ChatConfig
import com.exemple.facilita.webrtc.WebRTCManager
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import org.json.JSONObject
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.SessionDescription

class CallViewModel : ViewModel() {

    companion object {
        private const val TAG = "CallViewModel"
    }

    private var socket: Socket? = null
    private var webRTCManager: WebRTCManager? = null

    // Estados
    private val _callState = MutableStateFlow<CallState>(CallState.Idle)
    val callState: StateFlow<CallState> = _callState.asStateFlow()

    private val _localStream = MutableStateFlow<MediaStream?>(null)
    val localStream: StateFlow<MediaStream?> = _localStream.asStateFlow()

    private val _remoteStream = MutableStateFlow<MediaStream?>(null)
    val remoteStream: StateFlow<MediaStream?> = _remoteStream.asStateFlow()

    private val _isVideoEnabled = MutableStateFlow(true)
    val isVideoEnabled: StateFlow<Boolean> = _isVideoEnabled.asStateFlow()

    private val _isAudioEnabled = MutableStateFlow(true)
    val isAudioEnabled: StateFlow<Boolean> = _isAudioEnabled.asStateFlow()

    private val _connectionState = MutableStateFlow<PeerConnection.PeerConnectionState?>(null)
    val connectionState: StateFlow<PeerConnection.PeerConnectionState?> = _connectionState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // User info
    private var currentUserId: Int = 0
    private var currentUserName: String = ""
    private var currentServiceId: Int = 0

    /**
     * Inicializa o WebRTC e Socket.IO
     */
    fun initialize(
        context: Context,
        userId: Int,
        userName: String,
        servicoId: Int
    ) {
        try {
            currentUserId = userId
            currentUserName = userName
            currentServiceId = servicoId

            Log.d(TAG, "Inicializando CallViewModel: userId=$userId, servicoId=$servicoId")

            // Inicializar Socket.IO
            initializeSocket()

            // Inicializar WebRTC
            webRTCManager = WebRTCManager(
                context = context,
                socket = socket!!,
                servicoId = servicoId,
                userId = userId
            ).apply {
                initialize()

                // Callbacks do WebRTC
                onLocalStreamReady = { stream ->
                    _localStream.value = stream
                    Log.d(TAG, "Local stream pronto")
                }

                onRemoteStreamReady = { stream ->
                    _remoteStream.value = stream
                    Log.d(TAG, "Remote stream pronto")
                }

                onConnectionStateChange = { state ->
                    _connectionState.value = state
                    Log.d(TAG, "Connection state: $state")
                }

                onError = { error ->
                    _errorMessage.value = error
                    Log.e(TAG, "WebRTC Error: $error")
                }
            }

            Log.d(TAG, "✅ CallViewModel inicializado")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao inicializar CallViewModel: ${e.message}", e)
            _errorMessage.value = "Erro ao inicializar chamada: ${e.message}"
        }
    }

    /**
     * Inicializa conexão Socket.IO
     */
    private fun initializeSocket() {
        try {
            val url = ChatConfig.SOCKET_URL
            Log.d(TAG, "Conectando ao Socket.IO: $url")

            val opts = IO.Options().apply {
                transports = arrayOf("websocket")
                reconnection = true
                reconnectionAttempts = 5
                reconnectionDelay = 1000
                timeout = 20000
            }

            socket = IO.socket(url, opts)

            setupSocketListeners()

            socket?.connect()

            // Registrar usuário após conexão
            socket?.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "✅ Socket conectado")
                registerUser()
                joinService()
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao inicializar socket: ${e.message}", e)
            _errorMessage.value = "Erro ao conectar: ${e.message}"
        }
    }

    /**
     * Configura listeners do Socket.IO para chamadas
     */
    private fun setupSocketListeners() {
        socket?.apply {
            // Chamada recebida
            on("call:incoming") { args ->
                try {
                    val data = args[0] as JSONObject
                    Log.d(TAG, "📞 Chamada recebida: $data")

                    val callerId = data.optInt("callerId")
                    val currentState = _callState.value

                    // Verificar se já está em chamada
                    when (currentState) {
                        is CallState.OutgoingCall -> {
                            // Se está ligando E a chamada recebida é da MESMA pessoa
                            if (callerId == currentState.targetUserId) {
                                Log.d(TAG, "✅ Chamada recebida é da pessoa que estou ligando, aceitando automaticamente")
                                // Aceitar automaticamente (ambos ligaram ao mesmo tempo)
                                val incomingCall = IncomingCallData.fromJson(data)
                                acceptCall(incomingCall)
                            } else {
                                Log.d(TAG, "⚠️ Já ligando para outra pessoa, rejeitando")
                                socket?.emit("call:reject", JSONObject().apply {
                                    put("servicoId", data.optString("servicoId"))
                                    put("callId", data.optString("callId"))
                                    put("callerId", data.optString("callerId"))
                                    put("reason", "busy")
                                })
                            }
                            return@on
                        }
                        is CallState.ActiveCall -> {
                            // Já em chamada ativa, rejeitar
                            Log.d(TAG, "⚠️ Já em chamada ativa, rejeitando")
                            socket?.emit("call:reject", JSONObject().apply {
                                put("servicoId", data.optString("servicoId"))
                                put("callId", data.optString("callId"))
                                put("callerId", data.optString("callerId"))
                                put("reason", "busy")
                            })
                            return@on
                        }
                        else -> {
                            // Estado Idle ou outro: aceitar normalmente
                            val incomingCall = IncomingCallData.fromJson(data)
                            viewModelScope.launch {
                                _callState.value = CallState.IncomingCall(incomingCall)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao processar call:incoming", e)
                }
            }

            // Chamada iniciada (resposta para quem ligou)
            on("call:initiated") { args ->
                try {
                    val data = args[0] as JSONObject
                    Log.d(TAG, "✅ Chamada iniciada: $data")
                    val callId = data.optString("callId")
                    val targetOnline = data.optBoolean("targetOnline", false)

                    if (!targetOnline) {
                        viewModelScope.launch {
                            _callState.value = CallState.Error("Usuário offline")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao processar call:initiated", e)
                }
            }

            // Chamada aceita
            on("call:accepted") { args ->
                try {
                    val data = args[0] as JSONObject
                    Log.d(TAG, "✅ Chamada aceita: $data")
                    val acceptedData = CallAcceptedData.fromJson(data)

                    // Processar answer SDP
                    val answerSdp = acceptedData.answer.optString("sdp")
                    val answerType = acceptedData.answer.optString("type", "answer")

                    if (answerSdp.isNotEmpty()) {
                        val sdp = SessionDescription(
                            SessionDescription.Type.fromCanonicalForm(answerType),
                            answerSdp
                        )
                        webRTCManager?.setRemoteAnswer(sdp)
                    }

                    // Atualizar estado
                    val currentState = _callState.value
                    if (currentState is CallState.OutgoingCall) {
                        viewModelScope.launch {
                            _callState.value = CallState.ActiveCall(
                                callId = acceptedData.callId,
                                otherUserId = acceptedData.answererId,
                                otherUserName = acceptedData.answererName,
                                callType = currentState.callType,
                                startTime = System.currentTimeMillis()
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao processar call:accepted", e)
                }
            }

            // Oferta SDP recebida
            on("call:offer") { args ->
                try {
                    val data = args[0] as JSONObject
                    Log.d(TAG, "📩 Oferta SDP recebida: $data")

                    val sdpString = data.optString("sdp")
                    val sdpType = data.optString("type", "offer")

                    if (sdpString.isNotEmpty()) {
                        val sdp = SessionDescription(
                            SessionDescription.Type.fromCanonicalForm(sdpType),
                            sdpString
                        )
                        webRTCManager?.setRemoteOffer(sdp)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao processar call:offer", e)
                }
            }

            // Answer SDP recebida
            on("call:answer") { args ->
                try {
                    val data = args[0] as JSONObject
                    Log.d(TAG, "📩 Answer SDP recebida: $data")

                    val sdpString = data.optString("sdp")
                    val sdpType = data.optString("type", "answer")

                    if (sdpString.isNotEmpty()) {
                        val sdp = SessionDescription(
                            SessionDescription.Type.fromCanonicalForm(sdpType),
                            sdpString
                        )
                        webRTCManager?.setRemoteAnswer(sdp)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao processar call:answer", e)
                }
            }

            // ICE Candidate recebido
            on("call:ice-candidate") { args ->
                try {
                    val data = args[0] as JSONObject
                    Log.d(TAG, "🧊 ICE Candidate recebido")

                    val candidateData = data.optJSONObject("candidate")
                    candidateData?.let {
                        val sdp = it.optString("candidate")
                        val sdpMid = it.optString("sdpMid")
                        val sdpMLineIndex = it.optInt("sdpMLineIndex")

                        val iceCandidate = IceCandidate(sdpMid, sdpMLineIndex, sdp)
                        webRTCManager?.addRemoteIceCandidate(iceCandidate)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao processar ICE candidate", e)
                }
            }

            // Chamada finalizada
            on("call:ended") { args ->
                try {
                    val data = args[0] as JSONObject
                    Log.d(TAG, "❌ Chamada finalizada: $data")
                    val endedData = CallEndedData.fromJson(data)

                    viewModelScope.launch {
                        _callState.value = CallState.Ended(endedData.reason)
                        cleanup()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao processar call:ended", e)
                }
            }

            // Chamada rejeitada
            on("call:rejected") { args ->
                try {
                    val data = args[0] as JSONObject
                    Log.d(TAG, "❌ Chamada rejeitada: $data")
                    viewModelScope.launch {
                        _callState.value = CallState.Error("Chamada rejeitada")
                        cleanup()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao processar call:rejected", e)
                }
            }

            // Chamada falhou
            on("call:failed") { args ->
                try {
                    val data = args[0] as JSONObject
                    Log.d(TAG, "❌ Chamada falhou: $data")
                    val reason = data.optString("reason", "Erro desconhecido")
                    val message = data.optString("message", "")

                    // Se o usuário está offline, NÃO encerra a chamada
                    // Mantém a tela mostrando o vídeo local para o usuário decidir quando sair
                    if (reason == "user_offline") {
                        Log.d(TAG, "⚠️ Usuário offline - mantendo chamada ativa para o usuário cancelar")
                        // Mantém o estado atual (OutgoingCall) - não faz nada
                        // O usuário verá seu próprio vídeo e poderá cancelar manualmente
                    } else {
                        // Outros erros: mostra erro e limpa após 3 segundos
                        viewModelScope.launch {
                            _callState.value = CallState.Error(message.ifEmpty { reason })
                            delay(3000)
                            cleanup()
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao processar call:failed", e)
                }
            }

            // Media toggle (outro usuário desligou vídeo/áudio)
            on("call:media-toggled") { args ->
                try {
                    val data = args[0] as JSONObject
                    Log.d(TAG, "🎛️ Media toggled: $data")
                    // Você pode adicionar lógica para mostrar na UI que o outro usuário desligou vídeo/áudio
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao processar media-toggled", e)
                }
            }
        }
    }

    /**
     * Registra usuário no Socket.IO
     */
    private fun registerUser() {
        socket?.emit("user_connected", JSONObject().apply {
            put("userId", currentUserId)
            put("userType", "prestador")
            put("userName", currentUserName)
        })
        Log.d(TAG, "👤 Usuário registrado: $currentUserName")
    }

    /**
     * Entra na sala do serviço
     */
    private fun joinService() {
        socket?.emit("join_servico", currentServiceId.toString())
        Log.d(TAG, "🚪 Entrou na sala do serviço: $currentServiceId")
    }

    /**
     * Inicia uma chamada
     */
    fun startCall(targetUserId: Int, targetUserName: String, callType: CallType) {
        try {
            Log.d(TAG, "Iniciando chamada $callType para $targetUserName (ID: $targetUserId)")

            _callState.value = CallState.OutgoingCall(targetUserId, targetUserName, callType)

            // Emitir evento call:initiate
            socket?.emit("call:initiate", JSONObject().apply {
                put("servicoId", currentServiceId)
                put("callerId", currentUserId)
                put("callerName", currentUserName)
                put("targetUserId", targetUserId)
                put("callType", callType.value)
            })

            // Iniciar WebRTC
            val callId = "${currentServiceId}_${currentUserId}_${System.currentTimeMillis()}"
            webRTCManager?.startCall(targetUserId, callId, callType)

            Log.d(TAG, "✅ Chamada iniciada com callId: $callId")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao iniciar chamada: ${e.message}", e)
            _errorMessage.value = "Erro ao iniciar chamada: ${e.message}"
            _callState.value = CallState.Error(e.message ?: "Erro desconhecido")
        }
    }

    /**
     * Aceita uma chamada recebida
     */
    fun acceptCall(incomingCallData: IncomingCallData) {
        try {
            Log.d(TAG, "Aceitando chamada de ${incomingCallData.callerName}")

            _callState.value = CallState.ActiveCall(
                callId = incomingCallData.callId,
                otherUserId = incomingCallData.callerId,
                otherUserName = incomingCallData.callerName,
                callType = incomingCallData.callType,
                startTime = System.currentTimeMillis()
            )

            // Iniciar WebRTC
            webRTCManager?.acceptCall(
                callId = incomingCallData.callId,
                callerId = incomingCallData.callerId,
                callType = incomingCallData.callType
            )

            // Emitir evento call:accept será feito pelo WebRTCManager após criar o answer
            Log.d(TAG, "✅ Chamada aceita")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao aceitar chamada: ${e.message}", e)
            _errorMessage.value = "Erro ao aceitar chamada: ${e.message}"
            _callState.value = CallState.Error(e.message ?: "Erro desconhecido")
        }
    }

    /**
     * Rejeita uma chamada recebida
     */
    fun rejectCall(incomingCallData: IncomingCallData) {
        try {
            Log.d(TAG, "Rejeitando chamada de ${incomingCallData.callerName}")

            socket?.emit("call:reject", JSONObject().apply {
                put("servicoId", incomingCallData.servicoId)
                put("callId", incomingCallData.callId)
                put("callerId", incomingCallData.callerId)
                put("reason", "rejected_by_user")
            })

            _callState.value = CallState.Idle

        } catch (e: Exception) {
            Log.e(TAG, "Erro ao rejeitar chamada: ${e.message}", e)
        }
    }

    /**
     * Finaliza a chamada atual
     */
    fun endCall() {
        try {
            val currentState = _callState.value

            val callId = when (currentState) {
                is CallState.ActiveCall -> currentState.callId
                is CallState.OutgoingCall -> null // Pode não ter callId ainda
                else -> null
            }

            val targetId = when (currentState) {
                is CallState.ActiveCall -> currentState.otherUserId
                is CallState.OutgoingCall -> currentState.targetUserId
                else -> null
            }

            if (callId != null && targetId != null) {
                socket?.emit("call:end", JSONObject().apply {
                    put("servicoId", currentServiceId)
                    put("callId", callId)
                    put("targetUserId", targetId)
                    put("reason", "user_ended")
                })
            }

            cleanup()
            _callState.value = CallState.Ended("user_ended")

            Log.d(TAG, "✅ Chamada finalizada")

        } catch (e: Exception) {
            Log.e(TAG, "Erro ao finalizar chamada: ${e.message}", e)
        }
    }

    /**
     * Toggle vídeo
     */
    fun toggleVideo() {
        val enabled = webRTCManager?.toggleVideo() ?: false
        _isVideoEnabled.value = enabled
    }

    /**
     * Toggle áudio
     */
    fun toggleAudio() {
        val enabled = webRTCManager?.toggleAudio() ?: false
        _isAudioEnabled.value = enabled
    }

    /**
     * Troca de câmera
     */
    fun switchCamera() {
        webRTCManager?.switchCamera()
    }

    /**
     * Limpa recursos
     */
    private fun cleanup() {
        webRTCManager?.endCall()
        _localStream.value = null
        _remoteStream.value = null
        _isVideoEnabled.value = true
        _isAudioEnabled.value = true
    }

    /**
     * Cleanup ao destruir ViewModel
     */
    override fun onCleared() {
        super.onCleared()
        cleanup()
        webRTCManager?.dispose()
        socket?.disconnect()
        socket?.off()
        Log.d(TAG, "CallViewModel cleared")
    }
}

