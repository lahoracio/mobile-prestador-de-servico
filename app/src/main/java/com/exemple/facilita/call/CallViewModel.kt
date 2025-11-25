package com.exemple.facilita.call

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.exemple.facilita.data.service.WebSocketService
import com.exemple.facilita.webrtc.WebRtcClient
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.SurfaceViewRenderer
import org.webrtc.SessionDescription

class CallViewModel(
    app: Application
) : AndroidViewModel(app) {

    private val socket: WebSocketService by lazy {
        WebSocketService()
    }

    val callState = MutableLiveData("idle")
    val servicoId = MutableLiveData<Int>()
    val incomingCallerName = MutableLiveData("")

    private val egl = EglBase.create()
    private lateinit var rtc: WebRtcClient

    private lateinit var localView: SurfaceViewRenderer
    private lateinit var remoteView: SurfaceViewRenderer

    fun initWebRTC(local: SurfaceViewRenderer, remote: SurfaceViewRenderer) {
        localView = local
        remoteView = remote

        rtc = WebRtcClient(
            getApplication(),
            egl,
            onLocalSdp = { sdp ->
                if (callState.value == "outgoing") {
                    socket.sendOffer(servicoId.value!!, sdp.description)
                } else {
                    socket.sendAnswer(servicoId.value!!, sdp.description)
                }
            },
            onIceCandidate = { cand ->
                socket.sendIceCandidate(
                    servicoId.value!!,
                    cand.sdp,
                    cand.sdpMid,
                    cand.sdpMLineIndex
                )
            }
        )

        rtc.init(localView)
        rtc.createPeer(remoteView)
    }

    fun startCall(servico: Int, callerId: Int, callerName: String, target: Int) {
        servicoId.value = servico
        callState.value = "outgoing"
        socket.initiateCall(servico, callerId, callerName, target)
    }

    fun acceptCall(servico: Int, userId: Int) {
        servicoId.value = servico
        callState.value = "in-call"
        socket.acceptCall(servico, userId)
        rtc.createAnswer()
    }

    fun onRemoteOffer(sdp: String) {
        callState.value = "incoming"
        rtc.setRemoteDescription("offer", sdp)
    }

    fun onRemoteAnswer(sdp: String) {
        rtc.setRemoteDescription("answer", sdp)
    }

    fun onRemoteIce(candidate: IceCandidate) {
        rtc.addIceCandidate(candidate)
    }

    fun generateOffer() {
        rtc.createOffer()
    }
}