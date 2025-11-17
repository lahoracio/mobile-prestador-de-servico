package com.exemple.facilita.utils

import android.content.Context

/**
 * Classe utilitária para gerenciar o token JWT do usuário
 * Centraliza o acesso ao SharedPreferences para evitar inconsistências
 */
object TokenManager {

    private const val PREFS_NAME = "user_prefs"
    private const val TOKEN_KEY = "auth_token"
    private const val TIPO_CONTA_KEY = "tipo_conta"
    private const val USER_ID_KEY = "user_id"
    private const val USER_NAME_KEY = "user_name"

    /**
     * Salva o token JWT e informações do usuário no SharedPreferences
     */
    fun salvarToken(context: Context, token: String, tipoConta: String? = null, userId: Int? = null, nomeUsuario: String? = null) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(TOKEN_KEY, token)
            tipoConta?.let { putString(TIPO_CONTA_KEY, it) }
            userId?.let { putInt(USER_ID_KEY, it) }
            nomeUsuario?.let { putString(USER_NAME_KEY, it) }
            apply()
        }

        // Também salva no FacilitaPrefs para compatibilidade com código legado
        val legacyPrefs = context.getSharedPreferences("FacilitaPrefs", Context.MODE_PRIVATE)
        legacyPrefs.edit().apply {
            putString("token", token)
            nomeUsuario?.let { putString("nomeUsuario", it) }
            apply()
        }
    }

    /**
     * Recupera o token JWT do SharedPreferences
     * @return Token JWT ou null se não existir
     */
    fun obterToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var token = prefs.getString(TOKEN_KEY, null)

        // Se não encontrar, tenta buscar no FacilitaPrefs (compatibilidade)
        if (token == null) {
            val legacyPrefs = context.getSharedPreferences("FacilitaPrefs", Context.MODE_PRIVATE)
            token = legacyPrefs.getString("token", null)

            // Se encontrou no legado, migra para o novo
            if (token != null) {
                salvarToken(context, token)
            }
        }

        return token
    }

    /**
     * Remove o token JWT do SharedPreferences (logout)
     */
    fun limparToken(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(TOKEN_KEY).apply()

        val legacyPrefs = context.getSharedPreferences("FacilitaPrefs", Context.MODE_PRIVATE)
        legacyPrefs.edit().remove("token").apply()
    }

    /**
     * Verifica se existe um token salvo
     */
    fun temToken(context: Context): Boolean {
        return obterToken(context) != null
    }

    /**
     * Retorna o token com o prefixo Bearer para uso em requisições
     */
    fun obterTokenComBearer(context: Context): String? {
        val token = obterToken(context)
        return if (token != null) "Bearer $token" else null
    }

    /**
     * Obtém o ID do usuário salvo
     */
    fun obterUsuarioId(context: Context): Int? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val userId = prefs.getInt(USER_ID_KEY, -1)
        return if (userId != -1) userId else null
    }

    /**
     * Obtém o nome do usuário salvo
     */
    fun obterNomeUsuario(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var nome = prefs.getString(USER_NAME_KEY, null)

        // Se não encontrar, tenta buscar no FacilitaPrefs (compatibilidade)
        if (nome == null) {
            val legacyPrefs = context.getSharedPreferences("FacilitaPrefs", Context.MODE_PRIVATE)
            nome = legacyPrefs.getString("nomeUsuario", null)
        }

        return nome
    }

    /**
     * Obtém o tipo de conta do usuário
     */
    fun obterTipoConta(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(TIPO_CONTA_KEY, null)
    }

    /**
     * Verifica se o usuário é CONTRATANTE
     */
    fun isContratante(context: Context): Boolean {
        return obterTipoConta(context) == "CONTRATANTE"
    }
}

