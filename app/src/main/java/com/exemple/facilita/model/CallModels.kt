package com.exemple.facilita.model

import org.json.JSONObject

/**
 * Estados possíveis de uma chamada
 */
sealed class CallState {
    object Idle : CallState()
    object Connecting : CallState()
    data class OutgoingCall(
        val targetUserId: Int,
        val targetUserName: String,
        val callType: CallType
    ) : CallState()
    data class IncomingCall(
        val callData: IncomingCallData
    ) : CallState()
    data class ActiveCall(
        val callId: String,
        val otherUserId: Int,
        val otherUserName: String,
        val callType: CallType,
        val startTime: Long
    ) : CallState()
    data class Ended(val reason: String) : CallState()
    data class Error(val message: String) : CallState()
}

/**
 * Tipo de chamada
 */
enum class CallType(val value: String) {
    VIDEO("video"),
    AUDIO("audio");

    companion object {
        fun fromString(value: String): CallType {
            return values().find { it.value == value } ?: VIDEO
        }
    }
}

/**
 * Dados de chamada recebida
 */
data class IncomingCallData(
    val servicoId: Int,
    val callerId: Int,
    val callerName: String,
    val callType: CallType,
    val callId: String,
    val timestamp: Long
) {
    companion object {
        fun fromJson(json: JSONObject): IncomingCallData {
            return IncomingCallData(
                servicoId = json.optInt("servicoId", 0),
                callerId = json.optInt("callerId", 0),
                callerName = json.optString("callerName", "Usuário"),
                callType = CallType.fromString(json.optString("callType", "video")),
                callId = json.optString("callId", ""),
                timestamp = json.optLong("timestamp", System.currentTimeMillis())
            )
        }
    }
}

/**
 * Dados de chamada iniciada
 */
data class CallInitiatedData(
    val callId: String,
    val targetUserId: Int,
    val targetOnline: Boolean
)

/**
 * Dados de chamada aceita
 */
data class CallAcceptedData(
    val servicoId: Int,
    val callId: String,
    val answererId: Int,
    val answererName: String,
    val answer: JSONObject,
    val timestamp: Long
) {
    companion object {
        fun fromJson(json: JSONObject): CallAcceptedData {
            return CallAcceptedData(
                servicoId = json.optInt("servicoId", 0),
                callId = json.optString("callId", ""),
                answererId = json.optInt("answererId", 0),
                answererName = json.optString("answererName", "Usuário"),
                answer = json.optJSONObject("answer") ?: JSONObject(),
                timestamp = json.optLong("timestamp", System.currentTimeMillis())
            )
        }
    }
}

/**
 * Dados de chamada finalizada
 */
data class CallEndedData(
    val servicoId: Int,
    val callId: String,
    val endedBy: Int,
    val reason: String,
    val duration: Long?,
    val timestamp: Long
) {
    companion object {
        fun fromJson(json: JSONObject): CallEndedData {
            return CallEndedData(
                servicoId = json.optInt("servicoId", 0),
                callId = json.optString("callId", ""),
                endedBy = json.optInt("endedBy", 0),
                reason = json.optString("reason", "unknown"),
                duration = if (json.has("duration")) json.optLong("duration") else null,
                timestamp = json.optLong("timestamp", System.currentTimeMillis())
            )
        }
    }
}

/**
 * ICE Candidate data
 */
data class IceCandidateData(
    val servicoId: Int,
    val candidate: JSONObject,
    val callId: String,
    val timestamp: Long
) {
    companion object {
        fun fromJson(json: JSONObject): IceCandidateData {
            return IceCandidateData(
                servicoId = json.optInt("servicoId", 0),
                candidate = json.optJSONObject("candidate") ?: JSONObject(),
                callId = json.optString("callId", ""),
                timestamp = json.optLong("timestamp", System.currentTimeMillis())
            )
        }
    }
}

/**
 * Media toggle data (mute/unmute video/audio)
 */
data class MediaToggleData(
    val servicoId: Int,
    val callId: String,
    val mediaType: MediaType,
    val enabled: Boolean,
    val timestamp: Long
) {
    companion object {
        fun fromJson(json: JSONObject): MediaToggleData {
            return MediaToggleData(
                servicoId = json.optInt("servicoId", 0),
                callId = json.optString("callId", ""),
                mediaType = MediaType.fromString(json.optString("mediaType", "video")),
                enabled = json.optBoolean("enabled", true),
                timestamp = json.optLong("timestamp", System.currentTimeMillis())
            )
        }
    }
}

enum class MediaType(val value: String) {
    VIDEO("video"),
    AUDIO("audio");

    companion object {
        fun fromString(value: String): MediaType {
            return values().find { it.value == value } ?: VIDEO
        }
    }
}

