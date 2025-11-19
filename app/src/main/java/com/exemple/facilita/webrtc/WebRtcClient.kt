package com.exemple.facilita.webrtc

import org.webrtc.*

class WebRtcClient(
    private val factory: PeerConnectionFactory,
    private val iceCallback: (IceCandidate) -> Unit,
    private val sdpCallback: (SessionDescription) -> Unit
) {

    private var peer: PeerConnection? = null

    fun createPeer() {
        val config = PeerConnection.RTCConfiguration(
            listOf(
                PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
            )
        )

//       peer = factory.createPeerConnection(config, object : PeerConnection.Observer {
//          override fun onIceCandidate(candidate: IceCandidate) {
//               iceCallback(candidate)
//            }
//
//           override fun onAddStream(stream: MediaStream) {}
//
//           override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState) {}
//
//           override fun onConnectionChange(newState: PeerConnection.PeerConnectionState) {}
//       })
//   }

    fun createOffer() {
        peer?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription) {
                peer?.setLocalDescription(this, sdp)
                sdpCallback(sdp)
            }

            override fun onSetSuccess() {}
            override fun onCreateFailure(error: String) {}
            override fun onSetFailure(error: String) {}
        }, MediaConstraints())
    }

    fun createAnswer() {
        peer?.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription) {
                peer?.setLocalDescription(this, sdp)
                sdpCallback(sdp)
            }

            override fun onSetSuccess() {}
            override fun onCreateFailure(error: String) {}
            override fun onSetFailure(error: String) {}
        }, MediaConstraints())
    }

    fun setRemoteSdp(sdp: SessionDescription) {
        peer?.setRemoteDescription(object : SdpObserver {
            override fun onSetSuccess() {}
            override fun onSetFailure(p0: String?) {}
            override fun onCreateSuccess(p0: SessionDescription?) {}
            override fun onCreateFailure(p0: String?) {}
        }, sdp)
    }

    fun addIceCandidate(candidate: IceCandidate) {
        peer?.addIceCandidate(candidate)
    }
}