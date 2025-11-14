package com.exemple.facilita.model

data class Carteira(
    val id: String = "",
    val usuarioId: String = "",
    val saldo: Double = 0.0,
    val saldoBloqueado: Double = 0.0,
    val dataCriacao: String = "",
    val dataAtualizacao: String = ""
)

data class ContaBancaria(
    val id: String = "",
    val usuarioId: String = "",
    val nomeTitular: String = "",
    val cpf: String = "",
    val banco: String = "",
    val codigoBanco: String = "",
    val agencia: String = "",
    val conta: String = "",
    val tipoConta: String = "CORRENTE", // CORRENTE ou POUPANCA
    val isPrincipal: Boolean = true,
    val dataCriacao: String = ""
)

data class Transacao(
    val id: String = "",
    val usuarioId: String = "",
    val tipo: TipoTransacao = TipoTransacao.DEPOSITO,
    val valor: Double = 0.0,
    val status: StatusTransacao = StatusTransacao.PENDENTE,
    val descricao: String = "",
    val contaBancariaId: String? = null,
    val dataTransacao: String = "",
    val dataProcessamento: String? = null,
    val comprovante: String? = null
)

enum class TipoTransacao {
    DEPOSITO,
    SAQUE,
    PAGAMENTO,
    PAGAMENTO_SERVICO,
    RECEBIMENTO,
    ESTORNO,
    TAXA,
    CASHBACK
}

enum class StatusTransacao {
    PENDENTE,
    PROCESSANDO,
    CONCLUIDA,
    CANCELADA,
    FALHOU
}

data class SolicitacaoSaque(
    val valor: Double,
    val contaBancariaId: String,
    val descricao: String = ""
)

data class SolicitacaoDeposito(
    val valor: Double,
    val metodoPagamento: String = "PIX",
    val comprovante: String? = null
)

// Modelo para exibição de saldo na UI
data class SaldoCarteira(
    val saldoDisponivel: Double = 0.0,
    val saldoBloqueado: Double = 0.0
)

// Modelo alternativo para transações (compatível com a UI)
data class TransacaoCarteira(
    val id: String = "",
    val tipo: TipoTransacao = TipoTransacao.DEPOSITO,
    val valor: Double = 0.0,
    val data: String = "",
    val descricao: String = "",
    val status: StatusTransacao = StatusTransacao.PENDENTE
)

