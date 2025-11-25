package com.exemple.facilita.webrtc

import android.content.Context
import org.webrtc.*

class WebRtcClient(
    private val context: Context,
    private val eglBase: EglBase,
    private val onLocalSdp: (SessionDescription) -> Unit,
    private val onIceCandidate: (IceCandidate) -> Unit
) {

    private val iceServers = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
    )

    private val factory by lazy {
        PeerConnectionFactory.builder()
            .setVideoEncoderFactory(
                DefaultVideoEncoderFactory(
                    eglBase.eglBaseContext,
                    true,
                    true
                )
            )
            .setVideoDecoderFactory(
                DefaultVideoDecoderFactory(eglBase.eglBaseContext)
            )
            .createPeerConnectionFactory()
    }

    private var peer: PeerConnection? = null
    private lateinit var videoSource: VideoSource
    private lateinit var videoTrack: VideoTrack
    private lateinit var audioTrack: AudioTrack
    private var capturer: CameraVideoCapturer? = null

    private open class SimpleSdpObserver : SdpObserver {
        override fun onCreateSuccess(sessionDescription: SessionDescription) = Unit
        override fun onSetSuccess() = Unit
        override fun onCreateFailure(s: String) = Unit
        override fun onSetFailure(s: String) = Unit
    }

    fun init(localView: SurfaceViewRenderer) {
        localView.init(eglBase.eglBaseContext, null)

        capturer = createCameraCapturer()

        videoSource = factory.createVideoSource(false)
        val surfaceHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.eglBaseContext)
        capturer?.initialize(surfaceHelper, context, videoSource.capturerObserver)
        capturer?.startCapture(720, 1280, 30)

        videoTrack = factory.createVideoTrack("localVideo", videoSource)
        videoTrack.addSink(localView)

        val audioSource = factory.createAudioSource(MediaConstraints())
        audioTrack = factory.createAudioTrack("localAudio", audioSource)
    }

    fun createPeer(remoteView: SurfaceViewRenderer) {
        remoteView.init(eglBase.eglBaseContext, null)

        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN

        peer = factory.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
            override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {}
            override fun onIceConnectionReceivingChange(p0: Boolean) {}
            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}
            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
            override fun onAddStream(p0: MediaStream?) {}
            override fun onRemoveStream(p0: MediaStream?) {}
            override fun onDataChannel(p0: DataChannel?) {}
            override fun onRenegotiationNeeded() {}
            override fun onIceCandidate(candidate: IceCandidate) {
                onIceCandidate(candidate)
            }

            override fun onAddTrack(receiver: RtpReceiver?, streams: Array<out MediaStream>?) {
                streams?.firstOrNull()?.videoTracks?.firstOrNull()?.addSink(remoteView)
            }
        })

        peer?.addTrack(videoTrack)
        peer?.addTrack(audioTrack)
    }

    fun createOffer() {
        peer?.createOffer(object : SimpleSdpObserver() {
            override fun onCreateSuccess(sdp: SessionDescription) {
                peer?.setLocalDescription(object : SimpleSdpObserver() {
                    override fun onSetSuccess() {
                        onLocalSdp(sdp)
                    }
                }, sdp)
            }
        }, MediaConstraints())
    }

    fun createAnswer() {
        peer?.createAnswer(object : SimpleSdpObserver() {
            override fun onCreateSuccess(sdp: SessionDescription) {
                peer?.setLocalDescription(object : SimpleSdpObserver() {
                    override fun onSetSuccess() {
                        onLocalSdp(sdp)
                    }
                }, sdp)
            }
        }, MediaConstraints())
    }

    fun setRemoteDescription(type: String, sdp: String) {
        val desc = SessionDescription(
            if (type == "offer") SessionDescription.Type.OFFER else SessionDescription.Type.ANSWER,
            sdp
        )
        peer?.setRemoteDescription(SimpleSdpObserver(), desc)
    }

    fun addIceCandidate(candidate: IceCandidate) {
        peer?.addIceCandidate(candidate)
    }

    private fun createCameraCapturer(): CameraVideoCapturer? {
        val enumerator = Camera2Enumerator(context)
        val names = enumerator.deviceNames

        names.forEach {
            if (enumerator.isFrontFacing(it)) {
                return enumerator.createCapturer(it, null)
            }
        }
        return enumerator.createCapturer(names[0], null)
    }
}
