package com.exemple.facilita.utils

/**
 * Gerenciador simples de token de autenticação
 *
 * ⚠️ IMPORTANTE: Este token é temporário e expira!
 *
 * Para obter um novo token válido:
 * 1. Faça login na API: POST /auth/login
 * 2. Copie o token da resposta
 * 3. Cole aqui substituindo o currentToken
 *
 * Ou implemente um sistema de login que salve o token automaticamente.
 *
 * Verificar token em: https://jwt.io/
 * - tipo_conta deve ser "PRESTADOR"
 * - exp (expiração) deve ser no futuro
 */
object TokenManager {

    // ⚠️ TOKEN TEMPORÁRIO - Expira em 2025-11-06 ~16h
    // Para atualizar: substitua esta string pelo novo token JWT
    // Exemplo de resposta da API de login: { "token": "eyJ..." }
    private var currentToken: String = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MTE2LCJ0aXBvX2NvbnRhIjoiUFJFU1RBRE9SIiwiZW1haWwiOiJrYWlrZWRvZGVkYW9AZ21haWwuY29tIiwiaWF0IjoxNzYyNDU4MDM5LCJleHAiOjE3NjI0ODY4Mzl9.Z5g7sBzaH26GDBbS8T2MMho_cNL3D5_GmqW09UISKIs"

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
}

