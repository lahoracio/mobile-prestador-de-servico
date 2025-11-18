package com.exemple.facilita.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exemple.facilita.api.CarteiraService
import com.exemple.facilita.model.*
import com.exemple.facilita.service.RetrofitFactory
import com.exemple.facilita.pagbank.repository.PagBankRepository
import com.exemple.facilita.pagbank.model.PagBankCharge
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.*

/**
 * ViewModel da Carteira integrado com PagBank Sandbox
 *
 * Funcionalidades:
 * - Sincronização automática com PagBank
 * - Depósitos via PIX (simulado)
 * - Saques para conta bancária (simulado)
 * - Histórico de transações
 * - Gerenciamento de contas bancárias
 */
class CarteiraViewModel(application: android.app.Application) : androidx.lifecycle.AndroidViewModel(application) {

    private val carteiraService: CarteiraService = RetrofitFactory.getCarteiraService()
    private val pagBankRepository = PagBankRepository()
    private val tag = "CarteiraViewModel"

    private val sharedPreferences = application.getSharedPreferences("carteira_prefs", android.content.Context.MODE_PRIVATE)

    private val _carteira = MutableStateFlow<Carteira?>(null)
    val carteira: StateFlow<Carteira?> = _carteira

    private val _transacoes = MutableStateFlow<List<Transacao>>(emptyList())
    val transacoes: StateFlow<List<Transacao>> = _transacoes

    private val _contasBancarias = MutableStateFlow<List<ContaBancaria>>(emptyList())
    val contasBancarias: StateFlow<List<ContaBancaria>> = _contasBancarias

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    // Estados específicos do PagBank
    private val _qrCodePix = MutableStateFlow<PagBankCharge?>(null)
    val qrCodePix: StateFlow<PagBankCharge?> = _qrCodePix

    private val _pixCopiaCola = MutableStateFlow<String?>(null)
    val pixCopiaCola: StateFlow<String?> = _pixCopiaCola

    private val _chargeId = MutableStateFlow<String?>(null)
    val chargeId: StateFlow<String?> = _chargeId

    // Estados para o dialog da TelaCarteira
    private val _pixQrCode = MutableStateFlow<String?>(null)
    val pixQrCode: StateFlow<String?> = _pixQrCode

    private val _pixQrCodeBase64 = MutableStateFlow<String?>(null)
    val pixQrCodeBase64: StateFlow<String?> = _pixQrCodeBase64

    // Estado de sincronização
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing

    private val _lastSyncTime = MutableStateFlow<Long>(0)
    val lastSyncTime: StateFlow<Long> = _lastSyncTime

    fun limparMensagens() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    /**
     * Sincroniza carteira local com PagBank Sandbox
     * Atualiza saldo e transações automaticamente
     *
     * NOTA: No modo simulado, mantém o saldo local do usuário
     */
    fun sincronizarComPagBank(usuarioId: String) {
        viewModelScope.launch {
            if (_isSyncing.value) return@launch

            _isSyncing.value = true
            Log.d(tag, "🔄 Iniciando sincronização com PagBank...")

            try {
                // No modo simulado, apenas atualizar timestamp sem alterar saldo
                // O saldo é gerenciado localmente através dos depósitos e saques

                // Atualizar timestamp
                _lastSyncTime.value = System.currentTimeMillis()

                Log.d(tag, "✅ Sincronização concluída (modo local)")

            } catch (e: Exception) {
                Log.e(tag, "❌ Erro na sincronização", e)
            } finally {
                _isSyncing.value = false
            }
        }
    }

    /**
     * Auto-sincronização periódica (a cada 30 segundos)
     */
    fun iniciarAutoSync(usuarioId: String) {
        viewModelScope.launch {
            while (true) {
                sincronizarComPagBank(usuarioId)
                delay(30000) // 30 segundos
            }
        }
    }

    fun carregarCarteira(usuarioId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // Carregar saldo salvo localmente
                val saldoSalvo = sharedPreferences.getFloat("saldo_$usuarioId", 0f).toDouble()
                val saldoBloqueadoSalvo = sharedPreferences.getFloat("saldo_bloqueado_$usuarioId", 0f).toDouble()

                val response = carteiraService.getCarteira(usuarioId)
                if (response.isSuccessful) {
                    val carteiraApi = response.body()
                    // Usar saldo local se for maior (para manter depósitos simulados)
                    _carteira.value = carteiraApi?.copy(
                        saldo = maxOf(carteiraApi.saldo, saldoSalvo),
                        saldoBloqueado = maxOf(carteiraApi.saldoBloqueado, saldoBloqueadoSalvo)
                    )
                } else {
                    // Se não existir no backend, usar carteira local com saldo salvo
                    _carteira.value = Carteira(
                        id = usuarioId,
                        usuarioId = usuarioId,
                        saldo = saldoSalvo,
                        saldoBloqueado = saldoBloqueadoSalvo
                    )
                    Log.d(tag, "Carteira criada localmente com saldo: $saldoSalvo")
                }
            } catch (e: Exception) {
                // Em caso de erro, carregar saldo salvo localmente
                val saldoSalvo = sharedPreferences.getFloat("saldo_$usuarioId", 0f).toDouble()
                val saldoBloqueadoSalvo = sharedPreferences.getFloat("saldo_bloqueado_$usuarioId", 0f).toDouble()

                _carteira.value = Carteira(
                    id = usuarioId,
                    usuarioId = usuarioId,
                    saldo = saldoSalvo,
                    saldoBloqueado = saldoBloqueadoSalvo
                )
                Log.d(tag, "Carteira inicializada localmente: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun carregarTransacoes(usuarioId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val response = carteiraService.getTransacoes(usuarioId)
                if (response.isSuccessful) {
                    _transacoes.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao carregar transações: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun carregarContasBancarias(usuarioId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val response = carteiraService.getContasBancarias(usuarioId)
                if (response.isSuccessful) {
                    _contasBancarias.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao carregar contas: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun solicitarSaque(valor: Double, contaBancariaId: String, token: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // Validar saldo
                val saldoAtual = _carteira.value?.saldo ?: 0.0
                if (saldoAtual < valor) {
                    _errorMessage.value = "Saldo insuficiente para saque"
                    _isLoading.value = false
                    return@launch
                }

                // Buscar dados da conta bancária
                val conta = _contasBancarias.value.find { it.id == contaBancariaId }

                if (conta == null) {
                    _errorMessage.value = "Conta bancária não encontrada"
                    _isLoading.value = false
                    return@launch
                }

                // Realizar transferência via PagBank
                val referenceId = "SAQ_${System.currentTimeMillis()}"
                val resultado = pagBankRepository.realizarSaque(
                    valor = valor,
                    contaBancaria = conta,
                    referenceId = referenceId
                )

                if (resultado.success && resultado.data != null) {
                    // Adicionar transação
                    val novaTransacao = Transacao(
                        id = referenceId,
                        tipo = TipoTransacao.SAQUE,
                        valor = valor,
                        dataTransacao = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
                            .format(Date()),
                        status = StatusTransacao.PROCESSANDO,
                        descricao = "Saque para ${conta.banco} - Ag: ${conta.agencia} Cc: ${conta.conta}",
                        comprovante = resultado.data.id
                    )

                    _transacoes.value = listOf(novaTransacao) + _transacoes.value

                    // Debitar saldo imediatamente (bloquear)
                    val novoSaldo = saldoAtual - valor
                    val novoSaldoBloqueado = (_carteira.value?.saldoBloqueado ?: 0.0) + valor
                    _carteira.value = _carteira.value?.copy(
                        saldo = novoSaldo,
                        saldoBloqueado = novoSaldoBloqueado
                    )

                    // Salvar no SharedPreferences
                    val usuarioId = _carteira.value?.usuarioId ?: "user123"
                    sharedPreferences.edit()
                        .putFloat("saldo_$usuarioId", novoSaldo.toFloat())
                        .putFloat("saldo_bloqueado_$usuarioId", novoSaldoBloqueado.toFloat())
                        .apply()

                    val format = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
                    _successMessage.value = "Saque de ${format.format(valor)} solicitado! Processamento em até 1 dia útil."

                    Log.d(tag, "✅ Saque solicitado: $referenceId")

                    // MODO SIMULADO: Auto-confirmar após 3 segundos
                    if (resultado.message?.contains("SIMULADO") == true) {
                        Log.d(tag, "⚠️ MODO SIMULADO - Auto-confirmando saque em 3s...")
                        launch {
                            delay(3000)
                            confirmarSaqueSimulado(referenceId, valor)
                        }
                    }

                    onSuccess()
                } else {
                    _errorMessage.value = resultado.message ?: "Erro ao processar saque"
                }
            } catch (e: Exception) {
                Log.e(tag, "Erro ao solicitar saque", e)
                _errorMessage.value = "Erro de conexão: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Confirma saque automaticamente no modo simulado
     */
    private suspend fun confirmarSaqueSimulado(transacaoId: String, valor: Double) {
        Log.d(tag, "💸 Confirmando saque simulado: $transacaoId")

        // Atualizar status da transação
        _transacoes.value = _transacoes.value.map { transacao ->
            if (transacao.id == transacaoId) {
                transacao.copy(
                    status = StatusTransacao.CONCLUIDA,
                    descricao = transacao.descricao + " - Concluído (SIMULADO)"
                )
            } else {
                transacao
            }
        }

        // Desbloquear saldo (já foi debitado)
        _carteira.value = _carteira.value?.copy(
            saldoBloqueado = kotlin.math.max(0.0, (_carteira.value?.saldoBloqueado ?: 0.0) - valor)
        )

        val format = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        _successMessage.value = "✅ Saque confirmado! ${format.format(valor)} transferido."

        Log.d(tag, "✅ Saque confirmado: -${format.format(valor)}")
    }

    /**
     * Consulta saldo PagBank em tempo real
     */
    fun consultarSaldoPagBank() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val resultado = pagBankRepository.consultarSaldo()
                if (resultado.success && resultado.data != null) {
                    val saldoDisponivel = pagBankRepository.centavosParaReais(
                        resultado.data.available.value
                    )
                    val saldoBloqueado = pagBankRepository.centavosParaReais(
                        resultado.data.blocked.value
                    )

                    // Atualizar carteira local
                    _carteira.value = _carteira.value?.copy(
                        saldo = saldoDisponivel,
                        saldoBloqueado = saldoBloqueado
                    )

                    _successMessage.value = "Saldo atualizado"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao consultar saldo: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun solicitarDeposito(valor: Double, token: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null
            _qrCodePix.value = null
            _pixCopiaCola.value = null

            try {
                // Gerar QR Code PIX usando PagBank
                val referenceId = "DEP_${System.currentTimeMillis()}"
                val resultado = pagBankRepository.gerarQRCodePix(
                    valor = valor,
                    referenceId = referenceId,
                    description = "Depósito na Carteira Facilita"
                )

                if (resultado.success && resultado.data != null) {
                    _qrCodePix.value = resultado.data
                    _chargeId.value = resultado.data.id

                    // Extrair QR Code PIX
                    val qrCodePix = resultado.data.paymentMethod.pix?.qrCode
                    _pixCopiaCola.value = qrCodePix

                    // Adicionar transação pendente
                    val novaTransacao = Transacao(
                        id = referenceId,
                        tipo = TipoTransacao.DEPOSITO,
                        valor = valor,
                        dataTransacao = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
                            .format(Date()),
                        status = StatusTransacao.PENDENTE,
                        descricao = "Depósito via PIX - Aguardando pagamento",
                        comprovante = resultado.data.id
                    )

                    _transacoes.value = listOf(novaTransacao) + _transacoes.value

                    val format = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
                    _successMessage.value = "QR Code PIX gerado! ${format.format(valor)}"

                    Log.d(tag, "✅ Depósito criado: $referenceId")

                    // MODO SIMULADO: Usuário confirma manualmente via botão
                    Log.d(tag, "⚠️ MODO SIMULADO - Aguardando confirmação manual do usuário...")

                    onSuccess()
                } else {
                    _errorMessage.value = resultado.message ?: "Erro ao gerar QR Code PIX"
                }
            } catch (e: Exception) {
                Log.e(tag, "Erro ao solicitar depósito", e)
                _errorMessage.value = "Erro de conexão: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Confirma depósito manualmente no modo simulado (chamado via botão na UI)
     */
    /**
     * Método usado pelo dialog da TelaCarteira para gerar PIX
     */
    fun depositarViaPix(
        token: String,
        valor: Double,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Gerar QR Code PIX usando PagBank
                val referenceId = "DEP_${System.currentTimeMillis()}"
                val resultado = pagBankRepository.gerarQRCodePix(
                    valor = valor,
                    referenceId = referenceId,
                    description = "Depósito na Carteira Facilita"
                )

                if (resultado.success && resultado.data != null) {
                    _qrCodePix.value = resultado.data
                    _chargeId.value = resultado.data.id

                    // Extrair QR Code PIX
                    val qrCodeTexto = resultado.data.paymentMethod.pix?.qrCode ?:
                        "00020126330014br.gov.bcb.pix${referenceId}5204000053039865802BR5913Facilita App6009SAO PAULO"

                    _pixQrCode.value = qrCodeTexto
                    _pixCopiaCola.value = qrCodeTexto

                    // Adicionar transação pendente
                    val novaTransacao = Transacao(
                        id = referenceId,
                        tipo = TipoTransacao.DEPOSITO,
                        valor = valor,
                        dataTransacao = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
                            .format(Date()),
                        status = StatusTransacao.PENDENTE,
                        descricao = "Depósito via PIX - Aguardando pagamento",
                        comprovante = resultado.data.id
                    )

                    _transacoes.value = listOf(novaTransacao) + _transacoes.value

                    Log.d(tag, "✅ QR Code PIX gerado: $referenceId")
                    onSuccess()
                } else {
                    onError(resultado.message ?: "Erro ao gerar QR Code PIX")
                }
            } catch (e: Exception) {
                Log.e(tag, "Erro ao gerar PIX", e)
                onError("Erro: ${e.message}")
            }
        }
    }

    /**
     * Confirma pagamento PIX manualmente (botão "Já Paguei")
     */
    fun confirmarPagamentoPix(valor: Double) {
        viewModelScope.launch {
            val transacaoId = _qrCodePix.value?.id ?: _chargeId.value ?: "DEP_${System.currentTimeMillis()}"
            confirmarDepositoInterno(transacaoId, valor)
        }
    }

    fun confirmarDepositoSimulado(transacaoId: String, valor: Double) {
        viewModelScope.launch {
            confirmarDepositoInterno(transacaoId, valor)
        }
    }

    private suspend fun confirmarDepositoInterno(transacaoId: String, valor: Double) {
        Log.d(tag, "💰 Confirmando depósito simulado: $transacaoId")

        // Atualizar status da transação
        _transacoes.value = _transacoes.value.map { transacao ->
            if (transacao.id == transacaoId) {
                transacao.copy(
                    status = StatusTransacao.CONCLUIDA,
                    descricao = "Depósito via PIX - Confirmado (SIMULADO)"
                )
            } else {
                transacao
            }
        }

        // Adicionar saldo
        val novoSaldo = (_carteira.value?.saldo ?: 0.0) + valor
        _carteira.value = _carteira.value?.copy(saldo = novoSaldo)

        // Salvar no SharedPreferences
        val usuarioId = _carteira.value?.usuarioId ?: "user123"
        sharedPreferences.edit().putFloat("saldo_$usuarioId", novoSaldo.toFloat()).apply()

        val format = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        _successMessage.value = "✅ Depósito confirmado! ${format.format(valor)}"

        Log.d(tag, "✅ Depósito confirmado: +${format.format(valor)} | Saldo total: ${format.format(novoSaldo)}")
    }

    /**
     * Consulta status do pagamento PIX
     */
    fun consultarStatusPix(chargeId: String) {
        viewModelScope.launch {
            try {
                val resultado = pagBankRepository.consultarStatusPix(chargeId)
                if (resultado.success && resultado.data != null) {
                    val status = resultado.data.status
                    when (status) {
                        "PAID" -> {
                            _successMessage.value = "Pagamento confirmado!"
                            // Atualizar saldo local
                        }
                        "DECLINED" -> {
                            _errorMessage.value = "Pagamento recusado"
                        }
                        "CANCELED" -> {
                            _errorMessage.value = "Pagamento cancelado"
                        }
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao consultar status: ${e.message}"
            }
        }
    }

    fun adicionarContaBancaria(conta: ContaBancaria, token: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            try {
                val response = carteiraService.adicionarContaBancaria(conta, "Bearer $token")
                if (response.isSuccessful) {
                    _successMessage.value = "Conta bancária adicionada com sucesso!"
                    onSuccess()
                } else {
                    _errorMessage.value = "Erro ao adicionar conta: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro de conexão: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun definirContaPrincipal(contaId: String, token: String) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val response = carteiraService.definirContaPrincipal(contaId, "Bearer $token")
                if (response.isSuccessful) {
                    _successMessage.value = "Conta principal atualizada!"
                } else {
                    _errorMessage.value = "Erro ao atualizar conta principal"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun removerContaBancaria(contaId: String, token: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val response = carteiraService.removerContaBancaria(contaId, "Bearer $token")
                if (response.isSuccessful) {
                    _successMessage.value = "Conta removida com sucesso!"
                    onSuccess()
                } else {
                    _errorMessage.value = "Erro ao remover conta"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Método para sacar dinheiro (usado pela TelaCarteira)
     */
    fun sacar(
        token: String,
        valor: Double,
        contaBancariaId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        solicitarSaque(valor, contaBancariaId, token, onSuccess)
    }

    /**
     * Método para depósito via cartão (simulado)
     */
    fun depositarViaCartao(
        token: String,
        valor: Double,
        numeroCartao: String,
        mesExpiracao: String,
        anoExpiracao: String,
        cvv: String,
        nomeCompleto: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Simular processamento do cartão
                delay(2000)

                // Simular sucesso
                val novaTransacao = Transacao(
                    id = "CARD_${System.currentTimeMillis()}",
                    tipo = TipoTransacao.DEPOSITO,
                    valor = valor,
                    dataTransacao = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
                        .format(Date()),
                    status = StatusTransacao.CONCLUIDA,
                    descricao = "Depósito via Cartão - Final ${numeroCartao.takeLast(4)}"
                )

                _transacoes.value = listOf(novaTransacao) + _transacoes.value

                // Adicionar saldo
                val novoSaldo = (_carteira.value?.saldo ?: 0.0) + valor
                _carteira.value = _carteira.value?.copy(saldo = novoSaldo)

                // Salvar no SharedPreferences
                val usuarioId = _carteira.value?.usuarioId ?: "user123"
                sharedPreferences.edit().putFloat("saldo_$usuarioId", novoSaldo.toFloat()).apply()

                onSuccess()
            } catch (e: Exception) {
                onError("Erro ao processar cartão: ${e.message}")
            }
        }
    }

    /**
     * Adiciona conta bancária localmente
     */
    fun adicionarContaBancariaLocal(
        banco: String,
        agencia: String,
        conta: String,
        tipoConta: String,
        nomeCompleto: String,
        cpf: String,
        isPrincipal: Boolean
    ) {
        val novaConta = ContaBancaria(
            id = "CONTA_${System.currentTimeMillis()}",
            nomeTitular = nomeCompleto,
            cpf = cpf,
            banco = banco,
            agencia = agencia,
            conta = conta,
            tipoConta = tipoConta,
            isPrincipal = isPrincipal
        )

        _contasBancarias.value = _contasBancarias.value + novaConta
        Log.d(tag, "✅ Conta bancária adicionada: $banco")
    }
}

