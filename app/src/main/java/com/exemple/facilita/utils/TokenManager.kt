package com.exemple.facilita.utils

/**
 * Gerenciador de token de autenticação
 *
 * 📝 MODO DESENVOLVIMENTO:
 * Este app está em desenvolvimento modular. A tela de login está em outra aplicação
 * e será integrada depois. Por enquanto, atualize o token manualmente aqui.
 *
 * 🔧 PARA ATUALIZAR O TOKEN (quando expirar):
 *
 * 1. Faça login no outro app ou use Postman:
 *    POST https://servidor-facilita.onrender.com/v1/facilita/auth/login
 *    Body: {"email": "seu_email", "senha": "sua_senha"}
 *
 * 2. Copie o token da resposta
 *
 * 3. Cole abaixo em currentToken (substitua tudo entre as aspas)
 *
 * 4. Salve este arquivo e execute o app novamente
 *
 * ℹ️ Verificar validade em: https://jwt.io/
 *    - Procure por "exp" no payload (deve ser data futura)
 *    - "tipo_conta" deve ser "PRESTADOR"
 */
object TokenManager {

    // ══════════════════════════════════════════════════════════════════════
    // 🔑 COLE SEU TOKEN AQUI (entre as aspas):
    // ══════════════════════════════════════════════════════════════════════
    private var currentToken: String = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MTE4LCJ0aXBvX2NvbnRhIjpudWxsLCJlbWFpbCI6ImxhcmExQGdtYWlsLmNvbSIsImlhdCI6MTc2Mjg2NDQ3OSwiZXhwIjoxNzYyODkzMjc5LCJpc3MiOiJmYWNpbGl0YS1hcGkiLCJzdWIiOiIxMTgifQ.4hoaa4XUy203q3GGItrpfKvTHgPVkkXZS5HfL91uX7U"
    // ══════════════════════════════════════════════════════════════════════
    // ⚠️ Status: TOKEN EXPIRADO (válido até 06/11/2025)
    // 🔄 Atualize acima quando vir erro "Token expirado ou inválido"
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Retorna o token atual
     */
    fun getToken(): String {
        return currentToken
    }

    /**
     * Atualiza o token (ex: após login)
     */
    fun setToken(token: String) {
        currentToken = token
    }

    /**
     * Retorna o token formatado para o header Bearer
     */
    fun getBearerToken(): String {
        return "Bearer $currentToken"
    }

    /**
     * Limpa o token (logout)
     */
    fun clearToken() {
        currentToken = ""
    }

    /**
     * Verifica se tem token válido
     */
    fun hasToken(): Boolean {
        return currentToken.isNotEmpty()
    }

    /**
     * Retorna informações sobre o token para debug
     * Use em logs durante desenvolvimento
     */
    fun getTokenInfo(): String {
        if (currentToken.isEmpty()) return "Token vazio"

        return try {
            // Decodifica o payload do JWT (parte do meio)
            val parts = currentToken.split(".")
            if (parts.size != 3) return "Token inválido (formato incorreto)"

            val payload = parts[1]
            val decodedBytes = android.util.Base64.decode(payload, android.util.Base64.URL_SAFE)
            val decodedString = String(decodedBytes)

            "Token Info: $decodedString"
        } catch (e: Exception) {
            "Erro ao decodificar token: ${e.message}"
        }
    }

    /**
     * Verifica se o token parece estar expirado (verificação local, não 100% precisa)
     * Útil para desenvolvimento
     */
    fun isTokenLikelyExpired(): Boolean {
        if (currentToken.isEmpty()) return true

        return try {
            val parts = currentToken.split(".")
            if (parts.size != 3) return true

            val payload = parts[1]
            val decodedBytes = android.util.Base64.decode(payload, android.util.Base64.URL_SAFE)
            val decodedString = String(decodedBytes)

            // Extrai timestamp de expiração (exp)
            val expMatch = Regex(""""exp":(\d+)""").find(decodedString)
            val exp = expMatch?.groupValues?.get(1)?.toLongOrNull() ?: return true

            // Compara com timestamp atual
            val now = System.currentTimeMillis() / 1000
            exp < now
        } catch (e: Exception) {
            true
        }
    }
}

