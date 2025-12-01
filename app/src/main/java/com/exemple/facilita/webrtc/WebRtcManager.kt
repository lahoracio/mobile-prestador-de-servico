package com.exemple.facilita.webrtc

import android.content.Context
import android.util.Log
import com.exemple.facilita.model.CallType
import io.socket.client.Socket
import org.json.JSONObject
import org.webrtc.*

/**
 * Gerenciador WebRTC para chamadas de voz e vídeo
 * Implementa a conexão peer-to-peer usando WebRTC
 */
class WebRTCManager(
    private val context: Context,
    private val socket: Socket,
    private val servicoId: Int,
    private val userId: Int
) {
    companion object {
        private const val TAG = "WebRTCManager"

        // STUN servers do Google (gratuitos)
        private val ICE_SERVERS = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun2.l.google.com:19302").createIceServer()
        )
    }

    // WebRTC Components
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var localVideoTrack: VideoTrack? = null
    private var localAudioTrack: AudioTrack? = null
    private var videoCapturer: VideoCapturer? = null
    private var localVideoSource: VideoSource? = null
    private var localAudioSource: AudioSource? = null

    // Streams
    var localStream: MediaStream? = null
        private set
    var remoteStream: MediaStream? = null
        private set

    // Current call info
    private var currentCallId: String? = null
    private var targetUserId: Int? = null

    // Callbacks
    var onLocalStreamReady: ((MediaStream) -> Unit)? = null
    var onRemoteStreamReady: ((MediaStream) -> Unit)? = null
    var onIceCandidate: ((IceCandidate) -> Unit)? = null
    var onConnectionStateChange: ((PeerConnection.PeerConnectionState) -> Unit)? = null
    var onError: ((String) -> Unit)? = null

    // Media state
    var isVideoEnabled = true
        private set
    var isAudioEnabled = true
        private set

    // Cleanup state
    private var isDisposed = false

    /**
     * Inicializa o PeerConnectionFactory
     */
    fun initialize() {
        try {
            Log.d(TAG, "Inicializando WebRTC...")

            // Opções de inicialização
            val options = PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(true)
                .setFieldTrials("WebRTC-H264HighProfile/Enabled/")
                .createInitializationOptions()

            PeerConnectionFactory.initialize(options)

            // Builder do PeerConnectionFactory
            val encoderFactory = DefaultVideoEncoderFactory(
                EglBase.create().eglBaseContext,
                true,
                true
            )

            val decoderFactory = DefaultVideoDecoderFactory(
                EglBase.create().eglBaseContext
            )

            peerConnectionFactory = PeerConnectionFactory.builder()
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .setOptions(PeerConnectionFactory.Options().apply {
                    disableEncryption = false
                    disableNetworkMonitor = false
                })
                .createPeerConnectionFactory()

            Log.d(TAG, "✅ WebRTC inicializado com sucesso")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao inicializar WebRTC: ${e.message}", e)
            onError?.invoke("Erro ao inicializar WebRTC: ${e.message}")
        }
    }

    /**
     * Inicia uma chamada (criar oferta)
     */
    fun startCall(
        targetUserId: Int,
        callId: String,
        callType: CallType
    ) {
        try {
            this.currentCallId = callId
            this.targetUserId = targetUserId

            Log.d(TAG, "Iniciando chamada $callType para usuário $targetUserId")

            // Criar streams locais
            createLocalMediaStream(callType)

            // Criar PeerConnection
            createPeerConnection()

            // Adicionar tracks ao PeerConnection
            localStream?.let { stream ->
                stream.audioTracks.forEach { track ->
                    peerConnection?.addTrack(track, listOf(stream.id))
                }
                if (callType == CallType.VIDEO) {
                    stream.videoTracks.forEach { track ->
                        peerConnection?.addTrack(track, listOf(stream.id))
                    }
                }
            }

            // Criar oferta SDP
            createOffer()

        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao iniciar chamada: ${e.message}", e)
            onError?.invoke("Erro ao iniciar chamada: ${e.message}")
        }
    }

    /**
     * Aceita uma chamada recebida
     */
    fun acceptCall(
        callId: String,
        callerId: Int,
        callType: CallType
    ) {
        try {
            this.currentCallId = callId
            this.targetUserId = callerId

            Log.d(TAG, "Aceitando chamada $callType de usuário $callerId")

            // Criar streams locais
            createLocalMediaStream(callType)

            // Criar PeerConnection
            createPeerConnection()

            // Adicionar tracks ao PeerConnection
            localStream?.let { stream ->
                stream.audioTracks.forEach { track ->
                    peerConnection?.addTrack(track, listOf(stream.id))
                }
                if (callType == CallType.VIDEO) {
                    stream.videoTracks.forEach { track ->
                        peerConnection?.addTrack(track, listOf(stream.id))
                    }
                }
            }

            Log.d(TAG, "✅ Chamada aceita, aguardando oferta SDP do caller")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao aceitar chamada: ${e.message}", e)
            onError?.invoke("Erro ao aceitar chamada: ${e.message}")
        }
    }

    /**
     * Cria o stream de mídia local (câmera e microfone)
     */
    private fun createLocalMediaStream(callType: CallType) {
        try {
            Log.d(TAG, "Criando stream local de mídia...")

            val streamId = "local_stream_$userId"
            localStream = peerConnectionFactory?.createLocalMediaStream(streamId)

            // Áudio (sempre necessário)
            val audioConstraints = MediaConstraints()
            localAudioSource = peerConnectionFactory?.createAudioSource(audioConstraints)
            localAudioTrack = peerConnectionFactory?.createAudioTrack("audio_track", localAudioSource)
            localAudioTrack?.setEnabled(true)
            localStream?.addTrack(localAudioTrack)

            // Vídeo (apenas se for videochamada)
            if (callType == CallType.VIDEO) {
                videoCapturer = createVideoCapturer()
                val surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", EglBase.create().eglBaseContext)
                localVideoSource = peerConnectionFactory?.createVideoSource(videoCapturer?.isScreencast ?: false)
                videoCapturer?.initialize(surfaceTextureHelper, context, localVideoSource?.capturerObserver)

                videoCapturer?.startCapture(1280, 720, 30)

                localVideoTrack = peerConnectionFactory?.createVideoTrack("video_track", localVideoSource)
                localVideoTrack?.setEnabled(true)
                localStream?.addTrack(localVideoTrack)
            }

            // Notificar que stream local está pronto
            localStream?.let { stream ->
                onLocalStreamReady?.invoke(stream)
                Log.d(TAG, "✅ Stream local criado: ${stream.audioTracks.size} audio, ${stream.videoTracks.size} video")
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao criar stream local: ${e.message}", e)
            onError?.invoke("Erro ao acessar câmera/microfone: ${e.message}")
        }
    }

    /**
     * Cria o VideoCapturer (câmera)
     */
    private fun createVideoCapturer(): VideoCapturer? {
        val enumerator = Camera2Enumerator(context)

        // Tentar câmera frontal primeiro
        val deviceNames = enumerator.deviceNames
        for (deviceName in deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                val capturer = enumerator.createCapturer(deviceName, null)
                if (capturer != null) {
                    Log.d(TAG, "Usando câmera frontal: $deviceName")
                    return capturer
                }
            }
        }

        // Se não encontrar frontal, usar traseira
        for (deviceName in deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                val capturer = enumerator.createCapturer(deviceName, null)
                if (capturer != null) {
                    Log.d(TAG, "Usando câmera traseira: $deviceName")
                    return capturer
                }
            }
        }

        return null
    }

    /**
     * Cria a PeerConnection
     */
    private fun createPeerConnection() {
        try {
            val rtcConfig = PeerConnection.RTCConfiguration(ICE_SERVERS).apply {
                bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
                rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
                tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED
                continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
                keyType = PeerConnection.KeyType.ECDSA
                sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            }

            val observer = object : PeerConnection.Observer {
                override fun onIceCandidate(candidate: IceCandidate?) {
                    candidate?.let {
                        Log.d(TAG, "🧊 ICE Candidate gerado: ${it.sdp}")
                        onIceCandidate?.invoke(it)
                        sendIceCandidate(it)
                    }
                }

                override fun onAddStream(stream: MediaStream?) {
                    Log.d(TAG, "📺 Stream remoto adicionado")
                    stream?.let {
                        remoteStream = it
                        onRemoteStreamReady?.invoke(it)
                    }
                }

                override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                    Log.d(TAG, "🔗 Estado da conexão: $newState")
                    newState?.let { onConnectionStateChange?.invoke(it) }
                }

                override fun onSignalingChange(state: PeerConnection.SignalingState?) {
                    Log.d(TAG, "📡 Estado de sinalização: $state")
                }

                override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
                    Log.d(TAG, "🧊 Estado ICE: $state")
                }

                override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {
                    Log.d(TAG, "🧊 ICE Gathering: $state")
                }

                override fun onRemoveStream(stream: MediaStream?) {
                    Log.d(TAG, "Stream remoto removido")
                }

                override fun onDataChannel(channel: DataChannel?) {}
                override fun onRenegotiationNeeded() {
                    Log.d(TAG, "Renegociação necessária")
                }
                override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {}
                override fun onAddTrack(receiver: RtpReceiver?, streams: Array<out MediaStream>?) {
                    Log.d(TAG, "Track adicionado")
                }
                override fun onIceConnectionReceivingChange(receiving: Boolean) {
                    Log.d(TAG, "🧊 ICE Connection Receiving: $receiving")
                }
            }

            peerConnection = peerConnectionFactory?.createPeerConnection(rtcConfig, observer)
            Log.d(TAG, "✅ PeerConnection criada")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao criar PeerConnection: ${e.message}", e)
            onError?.invoke("Erro ao criar conexão: ${e.message}")
        }
    }

    /**
     * Cria oferta SDP
     */
    private fun createOffer() {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        }

        peerConnection?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                sdp?.let {
                    Log.d(TAG, "✅ Oferta SDP criada")
                    peerConnection?.setLocalDescription(object : SdpObserver {
                        override fun onSetSuccess() {
                            Log.d(TAG, "✅ Local Description definida")
                            sendOffer(it)
                        }
                        override fun onSetFailure(error: String?) {
                            Log.e(TAG, "❌ Erro ao definir Local Description: $error")
                        }
                        override fun onCreateSuccess(p0: SessionDescription?) {}
                        override fun onCreateFailure(p0: String?) {}
                    }, it)
                }
            }

            override fun onCreateFailure(error: String?) {
                Log.e(TAG, "❌ Erro ao criar oferta SDP: $error")
                onError?.invoke("Erro ao criar oferta: $error")
            }

            override fun onSetSuccess() {}
            override fun onSetFailure(error: String?) {}
        }, constraints)
    }

    /**
     * Define a oferta SDP remota (quando recebe a oferta)
     */
    fun setRemoteOffer(sdp: SessionDescription) {
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onSetSuccess() {
                Log.d(TAG, "✅ Remote offer definida, criando answer...")
                createAnswer()
            }

            override fun onSetFailure(error: String?) {
                Log.e(TAG, "❌ Erro ao definir remote offer: $error")
            }

            override fun onCreateSuccess(p0: SessionDescription?) {}
            override fun onCreateFailure(p0: String?) {}
        }, sdp)
    }

    /**
     * Cria resposta SDP (answer)
     */
    private fun createAnswer() {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        }

        peerConnection?.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                sdp?.let {
                    Log.d(TAG, "✅ Answer SDP criada")
                    peerConnection?.setLocalDescription(object : SdpObserver {
                        override fun onSetSuccess() {
                            Log.d(TAG, "✅ Local Description (answer) definida")
                            sendAnswer(it)
                        }
                        override fun onSetFailure(error: String?) {
                            Log.e(TAG, "❌ Erro ao definir Local Description: $error")
                        }
                        override fun onCreateSuccess(p0: SessionDescription?) {}
                        override fun onCreateFailure(p0: String?) {}
                    }, it)
                }
            }

            override fun onCreateFailure(error: String?) {
                Log.e(TAG, "❌ Erro ao criar answer SDP: $error")
            }

            override fun onSetSuccess() {}
            override fun onSetFailure(error: String?) {}
        }, constraints)
    }

    /**
     * Define a resposta SDP remota (quando recebe o answer)
     */
    fun setRemoteAnswer(sdp: SessionDescription) {
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onSetSuccess() {
                Log.d(TAG, "✅ Remote answer definida, conexão estabelecida!")
            }

            override fun onSetFailure(error: String?) {
                Log.e(TAG, "❌ Erro ao definir remote answer: $error")
            }

            override fun onCreateSuccess(p0: SessionDescription?) {}
            override fun onCreateFailure(p0: String?) {}
        }, sdp)
    }

    /**
     * Adiciona ICE candidate remoto
     */
    fun addRemoteIceCandidate(candidate: IceCandidate) {
        peerConnection?.addIceCandidate(candidate)
        Log.d(TAG, "🧊 ICE Candidate remoto adicionado")
    }

    /**
     * Envia oferta SDP via Socket.IO
     */
    private fun sendOffer(sdp: SessionDescription) {
        try {
            socket.emit("call:offer", JSONObject().apply {
                put("servicoId", servicoId)
                put("targetUserId", targetUserId)
                put("callId", currentCallId)
                put("type", sdp.type.canonicalForm())
                put("sdp", sdp.description)
            })
            Log.d(TAG, "📤 Oferta SDP enviada")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao enviar oferta: ${e.message}", e)
        }
    }

    /**
     * Envia resposta SDP via Socket.IO
     */
    private fun sendAnswer(sdp: SessionDescription) {
        try {
            socket.emit("call:answer", JSONObject().apply {
                put("servicoId", servicoId)
                put("callId", currentCallId)
                put("type", sdp.type.canonicalForm())
                put("sdp", sdp.description)
            })
            Log.d(TAG, "📤 Answer SDP enviada")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao enviar answer: ${e.message}", e)
        }
    }

    /**
     * Envia ICE candidate via Socket.IO
     */
    private fun sendIceCandidate(candidate: IceCandidate) {
        try {
            socket.emit("call:ice-candidate", JSONObject().apply {
                put("servicoId", servicoId)
                put("targetUserId", targetUserId)
                put("callId", currentCallId)
                put("candidate", JSONObject().apply {
                    put("candidate", candidate.sdp)
                    put("sdpMid", candidate.sdpMid)
                    put("sdpMLineIndex", candidate.sdpMLineIndex)
                })
            })
            Log.d(TAG, "📤 ICE Candidate enviado")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao enviar ICE candidate: ${e.message}", e)
        }
    }

    /**
     * Toggle vídeo (on/off)
     */
    fun toggleVideo(): Boolean {
        isVideoEnabled = !isVideoEnabled
        localVideoTrack?.setEnabled(isVideoEnabled)

        // Notificar outro usuário
        sendMediaToggle("video", isVideoEnabled)

        Log.d(TAG, "📹 Vídeo ${if (isVideoEnabled) "ligado" else "desligado"}")
        return isVideoEnabled
    }

    /**
     * Toggle áudio (on/off)
     */
    fun toggleAudio(): Boolean {
        isAudioEnabled = !isAudioEnabled
        localAudioTrack?.setEnabled(isAudioEnabled)

        // Notificar outro usuário
        sendMediaToggle("audio", isAudioEnabled)

        Log.d(TAG, "🎤 Áudio ${if (isAudioEnabled) "ligado" else "desligado"}")
        return isAudioEnabled
    }

    /**
     * Envia notificação de toggle de mídia
     */
    private fun sendMediaToggle(mediaType: String, enabled: Boolean) {
        try {
            socket.emit("call:toggle-media", JSONObject().apply {
                put("servicoId", servicoId)
                put("targetUserId", targetUserId)
                put("callId", currentCallId)
                put("mediaType", mediaType)
                put("enabled", enabled)
            })
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao enviar media toggle: ${e.message}", e)
        }
    }

    /**
     * Troca de câmera (frontal/traseira)
     */
    fun switchCamera() {
        try {
            (videoCapturer as? Camera2Capturer)?.switchCamera(null)
            Log.d(TAG, "📷 Câmera trocada")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao trocar câmera: ${e.message}", e)
        }
    }

    /**
     * Finaliza a chamada e limpa recursos
     */
    fun endCall() {
        if (isDisposed) {
            Log.d(TAG, "WebRTC já foi disposed, ignorando endCall")
            return
        }

        try {
            Log.d(TAG, "Finalizando chamada e limpando recursos...")

            // Parar captura de vídeo
            try {
                videoCapturer?.stopCapture()
            } catch (e: Exception) {
                Log.w(TAG, "Erro ao parar captura: ${e.message}")
            }
            try {
                videoCapturer?.dispose()
            } catch (e: Exception) {
                Log.w(TAG, "Erro ao liberar capturer: ${e.message}")
            }
            videoCapturer = null

            // Fechar tracks (com try-catch individual)
            try {
                localAudioTrack?.setEnabled(false)
            } catch (e: IllegalStateException) {
                Log.w(TAG, "Audio track já foi liberado")
            } catch (e: Exception) {
                Log.w(TAG, "Erro ao desabilitar audio: ${e.message}")
            }

            try {
                localVideoTrack?.setEnabled(false)
            } catch (e: IllegalStateException) {
                Log.w(TAG, "Video track já foi liberado")
            } catch (e: Exception) {
                Log.w(TAG, "Erro ao desabilitar video: ${e.message}")
            }

            try {
                localAudioTrack?.dispose()
            } catch (e: Exception) {
                Log.w(TAG, "Erro ao liberar audio track: ${e.message}")
            }

            try {
                localVideoTrack?.dispose()
            } catch (e: Exception) {
                Log.w(TAG, "Erro ao liberar video track: ${e.message}")
            }

            localAudioTrack = null
            localVideoTrack = null

            // Fechar sources
            try {
                localAudioSource?.dispose()
            } catch (e: Exception) {
                Log.w(TAG, "Erro ao liberar audio source: ${e.message}")
            }

            try {
                localVideoSource?.dispose()
            } catch (e: Exception) {
                Log.w(TAG, "Erro ao liberar video source: ${e.message}")
            }

            localAudioSource = null
            localVideoSource = null

            // Fechar PeerConnection
            try {
                peerConnection?.close()
            } catch (e: Exception) {
                Log.w(TAG, "Erro ao fechar PeerConnection: ${e.message}")
            }
            try {
                peerConnection?.dispose()
            } catch (e: Exception) {
                Log.w(TAG, "Erro ao liberar PeerConnection: ${e.message}")
            }
            peerConnection = null

            // Limpar streams
            localStream = null
            remoteStream = null

            // Limpar IDs
            currentCallId = null
            targetUserId = null

            // Reset estados
            isVideoEnabled = true
            isAudioEnabled = true

            Log.d(TAG, "✅ Chamada finalizada e recursos liberados")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro geral ao finalizar chamada: ${e.message}", e)
        }
    }

    /**
     * Libera todos os recursos do WebRTC
     */
    fun dispose() {
        if (isDisposed) {
            Log.d(TAG, "WebRTC já foi disposed, ignorando")
            return
        }

        isDisposed = true
        endCall()

        try {
            peerConnectionFactory?.dispose()
        } catch (e: Exception) {
            Log.w(TAG, "Erro ao liberar factory: ${e.message}")
        }
        peerConnectionFactory = null

        Log.d(TAG, "WebRTC Manager disposed")
    }
}

