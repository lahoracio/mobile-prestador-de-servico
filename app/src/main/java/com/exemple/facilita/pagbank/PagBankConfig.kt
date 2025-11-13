package com.exemple.facilita.pagbank

/**
 * Configurações do PagBank Sandbox
 *
 * IMPORTANTE:
 * - Este é o ambiente de TESTES (Sandbox)
 * - Para produção, alterar para URLs de produção
 * - Obter credenciais em: https://pagseguro.uol.com.br/
 */
object PagBankConfig {

    // ========== AMBIENTE SANDBOX ==========
    const val BASE_URL_SANDBOX = "https://sandbox.api.pagseguro.com/"
    const val BASE_URL_PRODUCTION = "https://api.pagseguro.com/"

    // Usar SANDBOX por padrão
    const val BASE_URL = BASE_URL_SANDBOX
    const val IS_SANDBOX = true

    // ========== CREDENCIAIS ==========
    // ATENÇÃO: Em produção, use variáveis de ambiente ou BuildConfig
    // Obtenha suas credenciais em: https://dev.pagseguro.uol.com.br/
    const val TOKEN_SANDBOX = "SEU_TOKEN_SANDBOX_AQUI"
    const val PUBLIC_KEY_SANDBOX = "SUA_PUBLIC_KEY_AQUI"

    // ========== ENDPOINTS ==========
    const val ENDPOINT_ORDERS = "orders"
    const val ENDPOINT_CHARGES = "charges"
    const val ENDPOINT_ACCOUNTS = "accounts"
    const val ENDPOINT_TRANSFERS = "transfers"
    const val ENDPOINT_BALANCE = "accounts/balance"
    const val ENDPOINT_QR_CODE = "charges/qrcodes"
    const val ENDPOINT_WEBHOOKS = "webhooks"

    // ========== CONFIGURAÇÕES DE TRANSAÇÃO ==========
    const val DEFAULT_CURRENCY = "BRL"
    const val MIN_TRANSACTION_VALUE = 1.0 // R$ 1,00
    const val MAX_TRANSACTION_VALUE = 10000.0 // R$ 10.000,00

    // ========== TIMEOUT ==========
    const val CONNECT_TIMEOUT = 60L // segundos
    const val READ_TIMEOUT = 60L // segundos
    const val WRITE_TIMEOUT = 60L // segundos

    // ========== WEBHOOK ==========
    // URL do seu servidor para receber notificações
    const val WEBHOOK_URL = "https://seu-servidor.com/webhook/pagbank"

    // ========== STATUS DE TRANSAÇÃO ==========
    object TransactionStatus {
        const val PAID = "PAID"
        const val DECLINED = "DECLINED"
        const val CANCELED = "CANCELED"
        const val WAITING = "WAITING"
        const val IN_ANALYSIS = "IN_ANALYSIS"
        const val AUTHORIZED = "AUTHORIZED"
    }

    // ========== MÉTODOS DE PAGAMENTO ==========
    object PaymentMethod {
        const val PIX = "PIX"
        const val CREDIT_CARD = "CREDIT_CARD"
        const val DEBIT_CARD = "DEBIT_CARD"
        const val BOLETO = "BOLETO"
    }

    // ========== TIPOS DE CONTA ==========
    object AccountType {
        const val CHECKING = "CHECKING" // Conta Corrente
        const val SAVINGS = "SAVINGS"   // Poupança
    }

    /**
     * Retorna headers padrão para requisições
     */
    fun getDefaultHeaders(token: String = TOKEN_SANDBOX): Map<String, String> {
        return mapOf(
            "Authorization" to "Bearer $token",
            "Content-Type" to "application/json",
            "Accept" to "application/json",
            "x-api-version" to "4.0"
        )
    }

    /**
     * Valida se está configurado corretamente
     */
    fun isConfigured(): Boolean {
        return TOKEN_SANDBOX != "SEU_TOKEN_SANDBOX_AQUI" &&
               TOKEN_SANDBOX.isNotBlank()
    }

    /**
     * Retorna URL base conforme ambiente
     */
    fun getBaseUrl(): String {
        return if (IS_SANDBOX) BASE_URL_SANDBOX else BASE_URL_PRODUCTION
    }
}

