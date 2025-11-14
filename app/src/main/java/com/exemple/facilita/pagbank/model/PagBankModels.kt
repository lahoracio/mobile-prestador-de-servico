package com.exemple.facilita.pagbank.model

import com.google.gson.annotations.SerializedName

/**
 * Modelos de dados para integração com PagBank
 */

// ========== PEDIDO (ORDER) ==========
data class PagBankOrder(
    @SerializedName("id") val id: String? = null,
    @SerializedName("reference_id") val referenceId: String,
    @SerializedName("customer") val customer: PagBankCustomer,
    @SerializedName("items") val items: List<PagBankItem>,
    @SerializedName("charges") val charges: List<PagBankCharge>? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("links") val links: List<PagBankLink>? = null
)

// ========== CLIENTE ==========
data class PagBankCustomer(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("tax_id") val taxId: String, // CPF/CNPJ
    @SerializedName("phones") val phones: List<PagBankPhone>
)

data class PagBankPhone(
    @SerializedName("country") val country: String = "55",
    @SerializedName("area") val area: String,
    @SerializedName("number") val number: String,
    @SerializedName("type") val type: String = "MOBILE"
)

// ========== ITEM DO PEDIDO ==========
data class PagBankItem(
    @SerializedName("reference_id") val referenceId: String,
    @SerializedName("name") val name: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("unit_amount") val unitAmount: Int // Valor em centavos
)

// ========== COBRANÇA (CHARGE) ==========
data class PagBankCharge(
    @SerializedName("id") val id: String? = null,
    @SerializedName("reference_id") val referenceId: String,
    @SerializedName("status") val status: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("paid_at") val paidAt: String? = null,
    @SerializedName("description") val description: String,
    @SerializedName("amount") val amount: PagBankAmount,
    @SerializedName("payment_method") val paymentMethod: PagBankPaymentMethod,
    @SerializedName("links") val links: List<PagBankLink>? = null,
    @SerializedName("notification_urls") val notificationUrls: List<String>? = null
)

data class PagBankAmount(
    @SerializedName("value") val value: Int, // Valor em centavos
    @SerializedName("currency") val currency: String = "BRL"
)

// ========== MÉTODO DE PAGAMENTO ==========
data class PagBankPaymentMethod(
    @SerializedName("type") val type: String, // PIX, CREDIT_CARD, DEBIT_CARD, BOLETO
    @SerializedName("installments") val installments: Int? = null,
    @SerializedName("capture") val capture: Boolean? = true,
    @SerializedName("soft_descriptor") val softDescriptor: String? = null,
    @SerializedName("card") val card: PagBankCard? = null,
    @SerializedName("pix") val pix: PagBankPix? = null
)

// ========== PIX ==========
data class PagBankPix(
    @SerializedName("qr_code") val qrCode: String? = null,
    @SerializedName("qr_code_base64") val qrCodeBase64: String? = null,
    @SerializedName("expiration_date") val expirationDate: String? = null,
    @SerializedName("holder") val holder: PagBankPixHolder? = null
)

data class PagBankPixHolder(
    @SerializedName("name") val name: String? = null,
    @SerializedName("tax_id") val taxId: String? = null
)

data class PagBankCard(
    @SerializedName("encrypted") val encrypted: String? = null,
    @SerializedName("number") val number: String? = null,
    @SerializedName("exp_month") val expMonth: String? = null,
    @SerializedName("exp_year") val expYear: String? = null,
    @SerializedName("security_code") val securityCode: String? = null,
    @SerializedName("holder") val holder: PagBankCardHolder? = null
)

data class PagBankCardHolder(
    @SerializedName("name") val name: String,
    @SerializedName("tax_id") val taxId: String? = null
)

// ========== QR CODE PIX ==========
data class PagBankQRCode(
    @SerializedName("id") val id: String? = null,
    @SerializedName("reference_id") val referenceId: String,
    @SerializedName("expiration_date") val expirationDate: String,
    @SerializedName("amount") val amount: PagBankAmount,
    @SerializedName("text") val text: String? = null, // Código PIX copia e cola
    @SerializedName("links") val links: List<PagBankLink>? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

// ========== TRANSFERÊNCIA ==========
data class PagBankTransfer(
    @SerializedName("id") val id: String? = null,
    @SerializedName("reference_id") val referenceId: String,
    @SerializedName("amount") val amount: PagBankAmount,
    @SerializedName("source") val source: PagBankAccount,
    @SerializedName("destination") val destination: PagBankAccount,
    @SerializedName("status") val status: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

// ========== CONTA BANCÁRIA ==========
data class PagBankAccount(
    @SerializedName("id") val id: String? = null,
    @SerializedName("holder") val holder: PagBankAccountHolder,
    @SerializedName("bank") val bank: PagBankBank,
    @SerializedName("type") val type: String // CHECKING ou SAVINGS
)

data class PagBankAccountHolder(
    @SerializedName("name") val name: String,
    @SerializedName("tax_id") val taxId: String
)

data class PagBankBank(
    @SerializedName("code") val code: String, // Código do banco (ex: 001 para Banco do Brasil)
    @SerializedName("agency") val agency: String,
    @SerializedName("account") val account: String,
    @SerializedName("account_digit") val accountDigit: String? = null
)

// ========== SALDO ==========
data class PagBankBalance(
    @SerializedName("available") val available: PagBankAmount,
    @SerializedName("blocked") val blocked: PagBankAmount,
    @SerializedName("currency") val currency: String = "BRL"
)

// ========== WEBHOOK ==========
data class PagBankWebhook(
    @SerializedName("id") val id: String? = null,
    @SerializedName("url") val url: String,
    @SerializedName("events") val events: List<String>,
    @SerializedName("status") val status: String? = null
)

data class PagBankWebhookNotification(
    @SerializedName("id") val id: String,
    @SerializedName("reference_id") val referenceId: String,
    @SerializedName("event") val event: String,
    @SerializedName("data") val data: Map<String, Any>,
    @SerializedName("created_at") val createdAt: String
)

// ========== LINK ==========
data class PagBankLink(
    @SerializedName("rel") val rel: String,
    @SerializedName("href") val href: String,
    @SerializedName("media") val media: String? = null,
    @SerializedName("type") val type: String? = null
)

// ========== RESPOSTA DE ERRO ==========
data class PagBankError(
    @SerializedName("error_messages") val errorMessages: List<PagBankErrorMessage>? = null,
    @SerializedName("message") val message: String? = null
)

data class PagBankErrorMessage(
    @SerializedName("code") val code: String,
    @SerializedName("description") val description: String,
    @SerializedName("parameter_name") val parameterName: String? = null
)

// ========== RESPOSTA GENÉRICA ==========
data class PagBankResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: PagBankError? = null,
    val message: String? = null
)

