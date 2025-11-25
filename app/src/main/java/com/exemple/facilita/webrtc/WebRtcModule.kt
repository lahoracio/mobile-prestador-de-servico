// WebRtcModule.kt - ATUALIZADO
package com.exemple.facilita.webrtc

import android.content.Context
import com.exemple.facilita.data.service.WebSocketService
import com.exemple.facilita.call.CallViewModel
import org.json.JSONObject
import org.webrtc.*

object WebRtcModule {

    private var initialized = false
    lateinit var factory: PeerConnectionFactory
    lateinit var localView: SurfaceViewRenderer
    lateinit var remoteView: SurfaceViewRenderer
    val eglBase: EglBase = EglBase.create()

    private lateinit var webSocketService: WebSocketService
    private lateinit var callViewModel: CallViewModel

    // Callbacks
    var onIncomingCall: ((String) -> Unit)? = null
    var onCallAccepted: (() -> Unit)? = null
    var onCallRejected: (() -> Unit)? = null
    var onCallEnded: (() -> Unit)? = null

    fun initialize(context: Context, socketService: WebSocketService, viewModel: CallViewModel) {
        if (initialized) return

        this.webSocketService = socketService
        this.callViewModel = viewModel

        // Initialize WebRTC
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)

        val encoderFactory = DefaultVideoEncoderFactory(
            eglBase.eglBaseContext,
            true,
            true
        )

        val decoderFactory = DefaultVideoDecoderFactory(eglBase.eglBaseContext)

        factory = PeerConnectionFactory.builder()
            .setVideoEncoderFactory(encoderFactory)
            .setVideoDecoderFactory(decoderFactory)
            .createPeerConnectionFactory()

        // Create views
        localView = SurfaceViewRenderer(context)
        localView.init(eglBase.eglBaseContext, null)

        remoteView = SurfaceViewRenderer(context)
        remoteView.init(eglBase.eglBaseContext, null)

        setupSocketListeners()
        initialized = true
    }

    private fun setupSocketListeners() {
        webSocketService.onCallIncoming = { data ->
            val callerName = data.getString("callerName")
            onIncomingCall?.invoke(callerName)
        }

        webSocketService.onCallAccepted = {
            onCallAccepted?.invoke()
        }

        webSocketService.onRemoteOffer = { data ->
            val sdp = data.getString("sdp")
            callViewModel.onRemoteOffer(sdp)
        }

        webSocketService.onRemoteAnswer = { data ->
            val sdp = data.getString("sdp")
            callViewModel.onRemoteAnswer(sdp)
        }

        webSocketService.onRemoteIce = { data ->
            val candidate = IceCandidate(
                data.getString("sdpMid"),
                data.getInt("sdpMLineIndex"),
                data.getString("candidate")
            )
            callViewModel.onRemoteIce(candidate)
        }
    }

    fun startCall(
        servicoId: Int,
        callerId: Int,
        callerName: String,
        targetId: Int,
        callType: String
    ) {
        webSocketService.initiateCall(servicoId, callerId, callerName, targetId)
    }

    fun acceptCall(servicoId: Int, userId: Int) {
        webSocketService.acceptCall(servicoId, userId)
    }

    fun endCall() {
        // Implementar lógica para finalizar chamada
        onCallEnded?.invoke()
    }

    fun cleanup() {
        localView.release()
        remoteView.release()
    }
}