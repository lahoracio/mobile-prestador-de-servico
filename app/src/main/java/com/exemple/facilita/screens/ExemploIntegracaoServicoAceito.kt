package com.exemple.facilita.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.exemple.facilita.model.*
import com.exemple.facilita.viewmodel.ServicoViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * EXEMPLO DE INTEGRAÇÃO COMPLETA
 *
 * Esta tela demonstra como integrar todo o fluxo:
 * 1. Aceitar serviço
 * 2. Salvar no ViewModel
 * 3. Navegar para detalhes
 */
@Composable
fun ExemploIntegracaoServicoAceito(
    navController: NavController,
    servicoViewModel: ServicoViewModel = viewModel()
) {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E1A)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Exemplo de Integração",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (isLoading) {
                CircularProgressIndicator(color = Color(0xFF00FF88))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Aceitando serviço...",
                    color = Color.White
                )
            } else {
                Button(
                    onClick = {
                        isLoading = true

                        // Simular chamada à API
                        scope.launch {
                            delay(1500)

                            // Criar um serviço de exemplo
                            val servicoExemplo = criarServicoExemplo()

                            // Salvar no ViewModel
                            servicoViewModel.salvarServicoAceito(servicoExemplo)

                            // Navegar para a tela de detalhes
                            isLoading = false
                            navController.navigate("tela_detalhes_servico_aceito/${servicoExemplo.id}")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00FF88)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "Simular Aceitação de Serviço",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0A0E1A)
                    )
                }
            }

            errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = error,
                    color = Color.Red,
                    fontSize = 14.sp
                )
            }
        }
    }
}

/**
 * Cria um serviço de exemplo para demonstração
 */
private fun criarServicoExemplo(): ServicoDetalhe {
    return ServicoDetalhe(
        id = 123,
        id_contratante = 456,
        id_prestador = 789,
        id_categoria = 1,
        descricao = "Preciso de entrega de documentos importantes em três locais diferentes: cartório, banco e escritório de contabilidade. Os documentos já estão organizados e etiquetados.",
        status = "EM_ANDAMENTO",
        data_solicitacao = "2024-11-17T14:30:00",
        data_conclusao = null,
        data_confirmacao = "2024-11-17T14:35:00",
        id_localizacao = 1,
        valor = "85.50",
        tempo_estimado = 45,
        data_inicio = "2024-11-17T14:35:00",
        contratante = ContratanteDetalhe(
            id = 456,
            necessidade = "Entrega urgente de documentos",
            id_usuario = 789,
            id_localizacao = 1,
            cpf = "123.456.789-00",
            usuario = UsuarioDetalhe(
                id = 789,
                nome = "João Silva Santos",
                senha_hash = null,
                foto_perfil = null,
                email = "joao.silva@email.com",
                telefone = "(11) 98765-4321",
                tipo_conta = "CONTRATANTE",
                criado_em = "2024-01-15T10:00:00"
            )
        ),
        prestador = null,
        categoria = CategoriaDetalhe(
            id = 1,
            nome = "Entrega de Documentos",
            descricao = "Serviço de entrega rápida e segura de documentos",
            icone = "document",
            preco_base = "50.00",
            tempo_medio = 30
        ),
        localizacao = LocalizacaoDetalhe(
            id = 1,
            endereco = "Avenida Paulista",
            bairro = "Bela Vista",
            cidade = "São Paulo",
            estado = "SP",
            cep = "01310-100",
            numero = "1578",
            complemento = "Conjunto 405 - Torre A",
            latitude = -23.5613,
            longitude = -46.6565
        )
    )
}

/**
 * EXEMPLO DE USO EM UMA TELA REAL
 *
 * Como integrar com uma chamada de API real:
 */
/*
@Composable
fun TelaAceitarServicoReal(
    navController: NavController,
    servicoId: Int,
    servicoViewModel: ServicoViewModel = viewModel()
) {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    suspend fun aceitarServico() {
        isLoading = true
        errorMessage = null

        try {
            // Fazer chamada à API
            val api = RetrofitClient.instance.create(ServicoService::class.java)
            val response = api.aceitarServico(servicoId)

            if (response.isSuccessful) {
                val servicoDetalhe = response.body()?.data

                if (servicoDetalhe != null) {
                    // Salvar no ViewModel
                    servicoViewModel.salvarServicoAceito(servicoDetalhe)

                    // Navegar para detalhes
                    navController.navigate("tela_detalhes_servico_aceito/${servicoDetalhe.id}")
                } else {
                    errorMessage = "Erro ao processar resposta do servidor"
                }
            } else {
                errorMessage = "Erro ao aceitar serviço: ${response.code()}"
            }
        } catch (e: Exception) {
            errorMessage = "Erro: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // UI da tela...
}
*/

