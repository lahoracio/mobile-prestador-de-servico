package com.exemple.facilita.webrtc

import android.content.Context
import org.webrtc.*

object WebRtcModule {

    private var initialized = false
    lateinit var factory: PeerConnectionFactory

    fun initialize(context: Context) {
        if (initialized) return

        // Inicializa o WebRTC nativo
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)

        // Cria opções do encoder/decoder
        val encoderFactory = DefaultVideoEncoderFactory(
            EglBase.create().eglBaseContext,
            true,
            true
        )

        val decoderFactory = DefaultVideoDecoderFactory(EglBase.create().eglBaseContext)

        // Cria a fábrica principal
        factory = PeerConnectionFactory.builder()
            .setVideoEncoderFactory(encoderFactory)
            .setVideoDecoderFactory(decoderFactory)
            .createPeerConnectionFactory()

        initialized = true
    }
}