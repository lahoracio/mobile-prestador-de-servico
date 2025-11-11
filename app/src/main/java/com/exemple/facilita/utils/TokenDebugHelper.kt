package com.exemple.facilita.utils

import android.util.Log

/**
 * Helper para debug durante desenvolvimento
 * Use para verificar rapidamente o status do token
 */
object TokenDebugHelper {

    private const val TAG = "TokenDebug"

    /**
     * Imprime informações completas do token no Logcat
     * Execute isso antes de testar validações
     */
    fun logTokenStatus() {
        Log.d(TAG, "════════════════════════════════════════")
        Log.d(TAG, "🔍 STATUS DO TOKEN")
        Log.d(TAG, "════════════════════════════════════════")

        val hasToken = TokenManager.hasToken()
        Log.d(TAG, "📌 Tem token: $hasToken")

        if (hasToken) {
            val token = TokenManager.getToken()
            Log.d(TAG, "📝 Token (primeiros 50 chars): ${token.take(50)}...")
            Log.d(TAG, "📊 Tamanho: ${token.length} caracteres")

            val isExpired = TokenManager.isTokenLikelyExpired()
            Log.d(TAG, "⏰ Provavelmente expirado: $isExpired")

            if (isExpired) {
                Log.w(TAG, "⚠️ TOKEN EXPIRADO! Atualize no TokenManager.kt")
            } else {
                Log.i(TAG, "✅ Token parece válido")
            }

            Log.d(TAG, "🔐 Info decodificada:")
            Log.d(TAG, TokenManager.getTokenInfo())
        } else {
            Log.e(TAG, "❌ NENHUM TOKEN CONFIGURADO")
        }

        Log.d(TAG, "════════════════════════════════════════")
    }

    /**
     * Retorna um resumo rápido do status do token
     */
    fun getQuickStatus(): String {
        return when {
            !TokenManager.hasToken() -> "❌ Sem token"
            TokenManager.isTokenLikelyExpired() -> "⚠️ Token expirado"
            else -> "✅ Token OK"
        }
    }

    /**
     * Verifica se pode fazer requisições à API
     */
    fun canMakeApiCalls(): Boolean {
        return TokenManager.hasToken() && !TokenManager.isTokenLikelyExpired()
    }
}

