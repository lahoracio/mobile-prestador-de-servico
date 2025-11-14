package com.exemple.facilita.pagbank.repository

import android.util.Log
import com.exemple.facilita.pagbank.PagBankClient
import com.exemple.facilita.pagbank.PagBankConfig
import com.exemple.facilita.pagbank.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * Repositório para gerenciar operações com PagBank
 *
 * Suporta MODO SIMULADO para testes sem token real
 */
class PagBankRepository {

    private val service = PagBankClient.service
    private val tag = "PagBankRepository"

    // ⚠️ MODO SIMULADO - Mude para false quando tiver o token real
    private val MODO_SIMULADO = true

    // ========== PIX ==========

    /**
     * Gera QR Code PIX para depósito
     *
     * @param valor Valor em reais (será convertido para centavos)
     * @param referenceId ID de referência único
     * @param description Descrição do pagamento
     * @return QR Code gerado ou erro
     */
    suspend fun gerarQRCodePix(
        valor: Double,
        referenceId: String,
        description: String = "Depósito via PIX"
    ): PagBankResponse<PagBankCharge> = withContext(Dispatchers.IO) {
        try {
            // MODO SIMULADO - Para testar sem token real
            if (MODO_SIMULADO) {
                Log.d(tag, "⚠️ MODO SIMULADO - Gerando QR Code fake")
                delay(1500) // Simula delay da API

                val qrCodeSimulado = "00020126330014br.gov.bcb.pix0111123456789015204000053039865802BR5913Facilita App6009SAO PAULO62070503***63041D3D"

                val responseSimulado = PagBankCharge(
                    id = referenceId,
                    referenceId = referenceId,
                    status = "WAITING",
                    createdAt = System.currentTimeMillis().toString(),
                    description = description,
                    amount = PagBankAmount(
                        value = (valor * 100).toInt(),
                        currency = "BRL"
                    ),
                    paymentMethod = PagBankPaymentMethod(
                        type = "PIX",
                        pix = PagBankPix(
                            qrCode = qrCodeSimulado,
                            qrCodeBase64 = gerarQrCodeBase64Simulado(),
                            expirationDate = calcularDataExpiracao(10)
                        )
                    ),
                    links = listOf(
                        PagBankLink(
                            rel = "SELF",
                            href = qrCodeSimulado,
                            media = "application/json",
                            type = "GET"
                        ),
                        PagBankLink(
                            rel = "QRCODE",
                            href = "https://via.placeholder.com/300x300.png?text=QR+Code+Simulado",
                            media = "image/png",
                            type = "GET"
                        )
                    ),
                    notificationUrls = listOf(PagBankConfig.WEBHOOK_URL)
                )

                Log.d(tag, "✅ QR Code simulado gerado com sucesso")
                return@withContext PagBankResponse(
                    success = true,
                    data = responseSimulado,
                    message = "QR Code gerado com sucesso (MODO SIMULADO)"
                )
            }

            // MODO REAL - Chama API do PagBank
            // Validar configuração do token
            if (!PagBankConfig.isConfigured()) {
                Log.e(tag, "Token PagBank não configurado! Verifique PagBankConfig.kt")
                return@withContext PagBankResponse(
                    success = false,
                    message = "⚠️ Token PagBank não configurado. Veja o arquivo COMO_CONFIGURAR_PAGBANK_TOKEN.md"
                )
            }

            // Validações
            if (valor < PagBankConfig.MIN_TRANSACTION_VALUE) {
                return@withContext PagBankResponse(
                    success = false,
                    message = "Valor mínimo é R$ ${PagBankConfig.MIN_TRANSACTION_VALUE}"
                )
            }

            if (valor > PagBankConfig.MAX_TRANSACTION_VALUE) {
                return@withContext PagBankResponse(
                    success = false,
                    message = "Valor máximo é R$ ${PagBankConfig.MAX_TRANSACTION_VALUE}"
                )
            }

            // Converter para centavos
            val valorEmCentavos = (valor * 100).toInt()

            // Criar cobrança PIX
            val charge = PagBankCharge(
                referenceId = referenceId,
                description = description,
                amount = PagBankAmount(
                    value = valorEmCentavos,
                    currency = PagBankConfig.DEFAULT_CURRENCY
                ),
                paymentMethod = PagBankPaymentMethod(
                    type = PagBankConfig.PaymentMethod.PIX,
                    pix = PagBankPix(
                        expirationDate = calcularDataExpiracao(10)
                    )
                ),
                notificationUrls = listOf(PagBankConfig.WEBHOOK_URL)
            )

            Log.d(tag, "Criando cobrança PIX real: $charge")

            val response = service.createPixCharge(
                authorization = PagBankClient.getAuthorizationHeader(),
                charge = charge
            )

            if (response.isSuccessful && response.body() != null) {
                Log.d(tag, "QR Code PIX gerado com sucesso: ${response.body()?.id}")
                PagBankResponse(
                    success = true,
                    data = response.body(),
                    message = "QR Code gerado com sucesso"
                )
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(tag, "Erro ao gerar QR Code PIX: $errorBody")

                // Mensagem mais clara para erro de autenticação
                val message = when (response.code()) {
                    401 -> "⚠️ Token PagBank inválido ou expirado. Verifique PagBankConfig.kt"
                    403 -> "Acesso negado. Verifique as permissões do token"
                    else -> "Erro ao gerar QR Code PIX: $errorBody"
                }

                PagBankResponse(
                    success = false,
                    message = message
                )
            }

        } catch (e: Exception) {
            Log.e(tag, "Exceção ao gerar QR Code PIX", e)
            PagBankResponse(
                success = false,
                message = "Erro de conexão: ${e.message}"
            )
        }
    }

    /**
     * Consulta status de uma cobrança PIX
     */
    suspend fun consultarStatusPix(chargeId: String): PagBankResponse<PagBankCharge> =
        withContext(Dispatchers.IO) {
            try {
                // MODO SIMULADO
                if (MODO_SIMULADO) {
                    Log.d(tag, "⚠️ MODO SIMULADO - Consultando status fake")
                    delay(500)

                    // Simula pagamento aprovado após 30 segundos
                    val tempoDecorrido = System.currentTimeMillis() - chargeId.substringAfter("_").toLongOrNull()!!
                    val status = if (tempoDecorrido > 30000) "PAID" else "WAITING"

                    val chargeSimulado = PagBankCharge(
                        id = chargeId,
                        referenceId = chargeId,
                        status = status,
                        createdAt = System.currentTimeMillis().toString(),
                        description = "Depósito via PIX",
                        amount = PagBankAmount(value = 0, currency = "BRL"),
                        paymentMethod = PagBankPaymentMethod(type = "PIX")
                    )

                    return@withContext PagBankResponse(
                        success = true,
                        data = chargeSimulado,
                        message = "Status: $status (SIMULADO)"
                    )
                }

                // MODO REAL
                val response = service.getCharge(
                    authorization = PagBankClient.getAuthorizationHeader(),
                    chargeId = chargeId
                )

                if (response.isSuccessful && response.body() != null) {
                    PagBankResponse(
                        success = true,
                        data = response.body(),
                        message = "Status: ${response.body()?.status}"
                    )
                } else {
                    PagBankResponse(
                        success = false,
                        message = "Erro ao consultar status: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                Log.e(tag, "Erro ao consultar status PIX", e)
                PagBankResponse(
                    success = false,
                    message = "Erro: ${e.message}"
                )
            }
        }

    // ========== CARTÃO DE CRÉDITO ==========

    /**
     * Cria cobrança via cartão de crédito
     */
    suspend fun criarCobrancaCartao(
        referenceId: String,
        valor: Double,
        descricao: String,
        numeroCartao: String,
        mesExpiracao: String,
        anoExpiracao: String,
        cvv: String,
        nomeCompleto: String,
        parcelamento: Int = 1
    ): PagBankResponse<PagBankCharge> = withContext(Dispatchers.IO) {
        try {
            // MODO SIMULADO
            if (MODO_SIMULADO) {
                Log.d(tag, "⚠️ MODO SIMULADO - Processando cartão fake")
                delay(2000) // Simula delay da API

                // Simula aprovação/recusa baseado no número do cartão
                val aprovado = numeroCartao.endsWith("1111") // Cartão 4111111111111111 aprova

                val responseSimulado = PagBankCharge(
                    id = referenceId,
                    referenceId = referenceId,
                    status = if (aprovado) "PAID" else "DECLINED",
                    createdAt = System.currentTimeMillis().toString(),
                    description = descricao,
                    amount = PagBankAmount(
                        value = (valor * 100).toInt(),
                        currency = "BRL"
                    ),
                    paymentMethod = PagBankPaymentMethod(
                        type = "CREDIT_CARD",
                        installments = parcelamento
                    )
                )

                if (aprovado) {
                    Log.d(tag, "✅ Cartão simulado aprovado")
                } else {
                    Log.d(tag, "❌ Cartão simulado recusado")
                }

                return@withContext PagBankResponse(
                    success = true,
                    data = responseSimulado,
                    message = if (aprovado) "Pagamento aprovado (SIMULADO)" else "Pagamento recusado (SIMULADO)"
                )
            }

            // MODO REAL
            val charge = PagBankCharge(
                referenceId = referenceId,
                description = descricao,
                amount = PagBankAmount(
                    value = (valor * 100).toInt(),
                    currency = "BRL"
                ),
                paymentMethod = PagBankPaymentMethod(
                    type = "CREDIT_CARD",
                    installments = parcelamento,
                    capture = true,
                    card = PagBankCard(
                        number = numeroCartao.replace(" ", ""),
                        expMonth = mesExpiracao,
                        expYear = anoExpiracao,
                        securityCode = cvv,
                        holder = PagBankCardHolder(name = nomeCompleto)
                    )
                )
            )

            Log.d(tag, "Criando cobrança cartão")

            val response = service.createPixCharge(
                authorization = PagBankClient.getAuthorizationHeader(),
                charge = charge
            )

            if (response.isSuccessful && response.body() != null) {
                Log.d(tag, "Cobrança cartão criada: ${response.body()}")
                PagBankResponse(
                    success = true,
                    data = response.body(),
                    message = "Pagamento processado"
                )
            } else {
                val error = "Erro ao criar cobrança cartão: ${response.code()}"
                Log.e(tag, error)
                PagBankResponse(
                    success = false,
                    message = error
                )
            }
        } catch (e: Exception) {
            Log.e(tag, "Erro ao criar cobrança cartão", e)
            PagBankResponse(
                success = false,
                message = "Erro: ${e.message}"
            )
        }
    }

    // ========== TRANSFERÊNCIAS/SAQUES ==========

    /**
     * Realiza saque/transferência para conta bancária
     */
    suspend fun realizarSaque(
        valor: Double,
        contaBancaria: com.exemple.facilita.model.ContaBancaria,
        referenceId: String
    ): PagBankResponse<PagBankTransfer> = withContext(Dispatchers.IO) {
        try {
            // MODO SIMULADO
            if (MODO_SIMULADO) {
                Log.d(tag, "⚠️ MODO SIMULADO - Realizando saque fake")
                delay(1000)

                val transferSimulada = PagBankTransfer(
                    id = referenceId,
                    referenceId = referenceId,
                    status = "PROCESSING",
                    amount = PagBankAmount(value = (valor * 100).toInt()),
                    source = PagBankAccount(
                        holder = PagBankAccountHolder(
                            name = "Facilita App",
                            taxId = "00000000000"
                        ),
                        bank = PagBankBank(
                            code = "290",
                            agency = "0001",
                            account = "00000000"
                        ),
                        type = "CHECKING"
                    ),
                    destination = PagBankAccount(
                        holder = PagBankAccountHolder(
                            name = contaBancaria.nomeTitular,
                            taxId = contaBancaria.cpf
                        ),
                        bank = PagBankBank(
                            code = contaBancaria.codigoBanco,
                            agency = contaBancaria.agencia,
                            account = contaBancaria.conta
                        ),
                        type = contaBancaria.tipoConta
                    ),
                    createdAt = System.currentTimeMillis().toString()
                )

                Log.d(tag, "✅ Saque simulado criado com sucesso")
                return@withContext PagBankResponse(
                    success = true,
                    data = transferSimulada,
                    message = "Saque solicitado com sucesso (SIMULADO)"
                )
            }

            // MODO REAL
            // Validações
            if (valor < PagBankConfig.MIN_TRANSACTION_VALUE) {
                return@withContext PagBankResponse(
                    success = false,
                    message = "Valor mínimo é R$ ${PagBankConfig.MIN_TRANSACTION_VALUE}"
                )
            }

            val valorEmCentavos = (valor * 100).toInt()

            // Mapear conta bancária
            val destination = PagBankAccount(
                holder = PagBankAccountHolder(
                    name = contaBancaria.nomeTitular,
                    taxId = contaBancaria.cpf.replace("[^0-9]".toRegex(), "")
                ),
                bank = PagBankBank(
                    code = contaBancaria.codigoBanco.padStart(3, '0'),
                    agency = contaBancaria.agencia,
                    account = contaBancaria.conta.replace("-", ""),
                    accountDigit = contaBancaria.conta.substringAfterLast("-", "")
                ),
                type = if (contaBancaria.tipoConta == "CORRENTE")
                    PagBankConfig.AccountType.CHECKING
                else
                    PagBankConfig.AccountType.SAVINGS
            )

            // Criar transferência
            val transfer = PagBankTransfer(
                referenceId = referenceId,
                amount = PagBankAmount(value = valorEmCentavos),
                source = PagBankAccount(
                    holder = PagBankAccountHolder(
                        name = "Facilita Prestador",
                        taxId = "00000000000"
                    ),
                    bank = PagBankBank(
                        code = "290",
                        agency = "0001",
                        account = "00000000"
                    ),
                    type = PagBankConfig.AccountType.CHECKING
                ),
                destination = destination
            )

            val response = service.createTransfer(
                authorization = PagBankClient.getAuthorizationHeader(),
                transfer = transfer
            )

            if (response.isSuccessful && response.body() != null) {
                Log.d(tag, "Saque realizado com sucesso: ${response.body()?.id}")
                PagBankResponse(
                    success = true,
                    data = response.body(),
                    message = "Saque solicitado com sucesso"
                )
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(tag, "Erro ao realizar saque: $errorBody")
                PagBankResponse(
                    success = false,
                    message = "Erro ao processar saque: ${response.code()}"
                )
            }

        } catch (e: Exception) {
            Log.e(tag, "Exceção ao realizar saque", e)
            PagBankResponse(
                success = false,
                message = "Erro de conexão: ${e.message}"
            )
        }
    }

    // ========== SALDO ==========

    /**
     * Consulta saldo da conta PagBank
     */
    suspend fun consultarSaldo(): PagBankResponse<PagBankBalance> =
        withContext(Dispatchers.IO) {
            try {
                // MODO SIMULADO
                if (MODO_SIMULADO) {
                    Log.d(tag, "⚠️ MODO SIMULADO - Consultando saldo fake")
                    delay(500)

                    val saldoSimulado = PagBankBalance(
                        available = PagBankAmount(value = 0), // R$ 0,00 - Usuário inicia com saldo zerado
                        blocked = PagBankAmount(value = 0),   // R$ 0,00
                        currency = "BRL"
                    )

                    return@withContext PagBankResponse(
                        success = true,
                        data = saldoSimulado,
                        message = "Saldo atualizado (SIMULADO)"
                    )
                }

                // MODO REAL
                val response = service.getBalance(
                    authorization = PagBankClient.getAuthorizationHeader()
                )

                if (response.isSuccessful && response.body() != null) {
                    Log.d(tag, "Saldo consultado: ${response.body()}")
                    PagBankResponse(
                        success = true,
                        data = response.body(),
                        message = "Saldo atualizado"
                    )
                } else {
                    PagBankResponse(
                        success = false,
                        message = "Erro ao consultar saldo: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                Log.e(tag, "Erro ao consultar saldo", e)
                PagBankResponse(
                    success = false,
                    message = "Erro: ${e.message}"
                )
            }
        }

    // ========== UTILITÁRIOS ==========

    /**
     * Converte centavos para reais
     */
    fun centavosParaReais(centavos: Int): Double {
        return centavos / 100.0
    }

    /**
     * Converte reais para centavos
     */
    fun reaisParaCentavos(reais: Double): Int {
        return (reais * 100).toInt()
    }

    /**
     * Gera QR Code Base64 simulado
     */
    private fun gerarQrCodeBase64Simulado(): String {
        // QR Code simulado em Base64 (imagem pequena de exemplo)
        return "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAFUlEQVR42mNk+M9Qz0AEYBxVSF+FABJADveWkH6oAAAAAElFTkSuQmCC"
    }

    /**
     * Calcula data de expiração
     */
    private fun calcularDataExpiracao(minutos: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, minutos)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        dateFormat.timeZone = TimeZone.getTimeZone("America/Sao_Paulo")

        return dateFormat.format(calendar.time)
    }

    /**
     * Cancela uma cobrança
     */
    suspend fun cancelarCobranca(chargeId: String): PagBankResponse<PagBankCharge> =
        withContext(Dispatchers.IO) {
            try {
                // MODO SIMULADO
                if (MODO_SIMULADO) {
                    Log.d(tag, "⚠️ MODO SIMULADO - Cancelando cobrança fake")
                    delay(500)

                    val chargeCancelado = PagBankCharge(
                        id = chargeId,
                        referenceId = chargeId,
                        status = "CANCELED",
                        createdAt = System.currentTimeMillis().toString(),
                        description = "Cobrança cancelada",
                        amount = PagBankAmount(value = 0, currency = "BRL"),
                        paymentMethod = PagBankPaymentMethod(type = "PIX")
                    )

                    return@withContext PagBankResponse(
                        success = true,
                        data = chargeCancelado,
                        message = "Cobrança cancelada (SIMULADO)"
                    )
                }

                // MODO REAL
                val response = service.cancelCharge(
                    authorization = PagBankClient.getAuthorizationHeader(),
                    chargeId = chargeId
                )

                if (response.isSuccessful && response.body() != null) {
                    PagBankResponse(
                        success = true,
                        data = response.body(),
                        message = "Cobrança cancelada"
                    )
                } else {
                    PagBankResponse(
                        success = false,
                        message = "Erro ao cancelar cobrança"
                    )
                }
            } catch (e: Exception) {
                Log.e(tag, "Erro ao cancelar cobrança", e)
                PagBankResponse(
                    success = false,
                    message = "Erro: ${e.message}"
                )
            }
        }
}

