package com.exemple.facilita.viewmodel

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
import java.util.UUID

class CarteiraViewModel : ViewModel() {

    private val carteiraService: CarteiraService = RetrofitFactory.getCarteiraService()
    private val pagBankRepository = PagBankRepository()

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

    fun carregarCarteira(usuarioId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val response = carteiraService.getCarteira(usuarioId)
                if (response.isSuccessful) {
                    _carteira.value = response.body()
                } else {
                    _errorMessage.value = "Erro ao carregar carteira: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro de conexão: ${e.message}"
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

    fun solicitarSaque(valor: Double, contaBancariaId: String, token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // Buscar dados da conta bancária
                val conta = _contasBancarias.value.find { it.id == contaBancariaId }

                if (conta == null) {
                    _errorMessage.value = "Conta bancária não encontrada"
                    _isLoading.value = false
                    return@launch
                }

                // Realizar transferência via PagBank
                val referenceId = "SAQ_${UUID.randomUUID().toString().take(8)}"
                val resultado = pagBankRepository.realizarSaque(
                    valor = valor,
                    contaBancaria = conta,
                    referenceId = referenceId
                )

                if (resultado.success && resultado.data != null) {
                    _successMessage.value = "Saque solicitado com sucesso! Será processado em até 1 dia útil."

                    // Salvar no backend local
                    val solicitacao = SolicitacaoSaque(
                        valor = valor,
                        contaBancariaId = contaBancariaId,
                        descricao = "Saque via PagBank - ID: ${resultado.data.id}"
                    )
                    carteiraService.solicitarSaque(solicitacao, "Bearer $token")
                } else {
                    _errorMessage.value = resultado.message ?: "Erro ao processar saque"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro de conexão: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
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
                val referenceId = "DEP_${UUID.randomUUID().toString().take(8)}"
                val resultado = pagBankRepository.gerarQRCodePix(
                    valor = valor,
                    referenceId = referenceId,
                    description = "Depósito na Carteira Facilita"
                )

                if (resultado.success && resultado.data != null) {
                    _qrCodePix.value = resultado.data
                    _chargeId.value = resultado.data.id

                    // Extrair link do QR Code das imagens
                    val qrCodeLink = resultado.data.links?.find {
                        it.media == "image/png" || it.rel == "QRCODE"
                    }?.href

                    // Pegar texto copia e cola (se disponível nos links)
                    val pixText = resultado.data.links?.find {
                        it.rel == "SELF"
                    }?.href

                    _pixCopiaCola.value = pixText
                    _successMessage.value = "QR Code PIX gerado! Escaneie para pagar."

                    // Também salvar no backend local
                    val solicitacao = SolicitacaoDeposito(
                        valor = valor,
                        metodoPagamento = "PIX",
                        comprovante = resultado.data.id
                    )
                    carteiraService.solicitarDeposito(solicitacao, "Bearer $token")

                    onSuccess()
                } else {
                    _errorMessage.value = resultado.message ?: "Erro ao gerar QR Code PIX"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro de conexão: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
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

    fun limparMensagens() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}

