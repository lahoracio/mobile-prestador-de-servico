package com.exemple.facilita.pagbank.repository

import android.util.Log
import com.exemple.facilita.pagbank.PagBankClient
import com.exemple.facilita.pagbank.PagBankConfig
import com.exemple.facilita.pagbank.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repositório para gerenciar operações com PagBank
 *
 * Centraliza toda a lógica de comunicação com a API
 */
class PagBankRepository {

    private val service = PagBankClient.service
    private val tag = "PagBankRepository"

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
                    type = PagBankConfig.PaymentMethod.PIX
                ),
                notificationUrls = listOf(PagBankConfig.WEBHOOK_URL)
            )

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
                PagBankResponse(
                    success = false,
                    message = "Erro ao gerar QR Code: ${response.code()}"
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

    // ========== TRANSFERÊNCIAS/SAQUES ==========

    /**
     * Realiza saque/transferência para conta bancária
     *
     * @param valor Valor em reais
     * @param contaBancaria Dados da conta destino
     * @param referenceId ID de referência único
     * @return Resultado da transferência
     */
    suspend fun realizarSaque(
        valor: Double,
        contaBancaria: com.exemple.facilita.model.ContaBancaria,
        referenceId: String
    ): PagBankResponse<PagBankTransfer> = withContext(Dispatchers.IO) {
        try {
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

    /**
     * Consulta status de uma transferência
     */
    suspend fun consultarStatusTransferencia(
        transferId: String
    ): PagBankResponse<PagBankTransfer> = withContext(Dispatchers.IO) {
        try {
            val response = service.getTransfer(
                authorization = PagBankClient.getAuthorizationHeader(),
                transferId = transferId
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
                    message = "Erro ao consultar transferência"
                )
            }
        } catch (e: Exception) {
            PagBankResponse(
                success = false,
                message = "Erro: ${e.message}"
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

    // ========== WEBHOOKS ==========

    /**
     * Configura webhook para receber notificações
     */
    suspend fun configurarWebhook(url: String): PagBankResponse<PagBankWebhook> =
        withContext(Dispatchers.IO) {
        try {
            val webhook = PagBankWebhook(
                url = url,
                events = listOf(
                    "charge.paid",
                    "charge.declined",
                    "transfer.completed",
                    "transfer.failed"
                )
            )

            val response = service.createWebhook(
                authorization = PagBankClient.getAuthorizationHeader(),
                webhook = webhook
            )

            if (response.isSuccessful && response.body() != null) {
                Log.d(tag, "Webhook configurado: ${response.body()?.id}")
                PagBankResponse(
                    success = true,
                    data = response.body(),
                    message = "Webhook configurado"
                )
            } else {
                PagBankResponse(
                    success = false,
                    message = "Erro ao configurar webhook"
                )
            }
        } catch (e: Exception) {
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
     * Valida CPF/CNPJ
     */
    fun validarCpfCnpj(documento: String): Boolean {
        val apenasNumeros = documento.replace("[^0-9]".toRegex(), "")
        return apenasNumeros.length == 11 || apenasNumeros.length == 14
    }
}

