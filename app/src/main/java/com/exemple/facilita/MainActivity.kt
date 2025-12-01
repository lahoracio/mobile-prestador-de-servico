package com.exemple.facilita

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.exemple.facilita.screens.*
import com.exemple.facilita.screens.TelaChatAoVivo
import com.exemple.facilita.screens.TelaPedidoEmAndamento
import com.exemple.facilita.viewmodel.PerfilViewModel
import com.exemple.facilita.viewmodel.ServicoViewModel
import com.exemple.facilita.viewmodel.ChatViewModel
import com.exemple.facilita.call.CallViewModel
import com.exemple.facilita.data.service.WebSocketService
import com.exemple.facilita.utils.TokenManager
import com.exemple.facilita.service.RetrofitFactory
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import com.exemple.facilita.webrtc.WebRtcModule
import com.exemple.facilita.navigation.addCallNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            AppNavHost(navController)
        }
    }
}

@Composable
fun AppNavHost(navController: NavHostController) {
    // ViewModels compartilhados entre todas as telas
    val perfilViewModel: PerfilViewModel = viewModel()
    val prestadorViewModel: com.exemple.facilita.viewmodel.PrestadorViewModel = viewModel()
    val servicoViewModel: ServicoViewModel = viewModel()
    val notificacaoViewModel: com.exemple.facilita.viewmodel.NotificacaoServicoViewModel = viewModel()

    // 🔴 ADICIONE ESTES VIEWMODELS PARA CHAMADAS
    val callViewModel: CallViewModel = viewModel()
    val webSocketService = remember { WebSocketService() }

    val context = LocalContext.current
    val token = TokenManager.obterTokenComBearer(context) ?: ""

    // Estados de notificação
    val novoServico by notificacaoViewModel.novoServico.collectAsState()
    val mostrarNotificacao by notificacaoViewModel.mostrarNotificacao.collectAsState()
    val tempoRestante by notificacaoViewModel.tempoRestante.collectAsState()

    // 🔴 INICIALIZA O WEBRTC MODULE AQUI (com os parâmetros necessários)
    LaunchedEffect(Unit) {
        WebRtcModule.initialize(context, webSocketService, callViewModel)
    }

    // Inicia monitoramento quando o prestador estiver logado
    LaunchedEffect(token) {
        if (token.isNotEmpty()) {
            notificacaoViewModel.iniciarMonitoramento(token)
        }
    }

    // 🔴 INICIALIZA O WEBSOCKET PARA CHAMADAS
    LaunchedEffect(Unit) {
        webSocketService.connect()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = "splash_screen"
        ) {

            // 🔴 ADICIONE ESTA LINHA - INCLUI TODAS AS ROTAS DE CHAMADA
            addCallNavigation(navController, webSocketService, callViewModel)

            composable("splash_screen") {
                SplashScreen(navController)
            }

            composable("tela_inicio1") {
                TelaInicio1(navController)
            }

            composable("tela_inicio2") {
                TelaInicio2(navController)
            }

            composable("tela_inicio3") {
                TelaInicio3(navController)
            }

            composable("tela_login") {
                TelaLogin(navController)
            }
            composable("tela_cadastro") {
                TelaCadastro(navController)
            }

            composable("tela_recuperar_senha") {
                TelaRecuperarSenha(navController)
            }

            composable("tela_verificar_codigo/{emailOuTelefone}/{tipo}") { backStackEntry ->
                val emailOuTelefone = backStackEntry.arguments?.getString("emailOuTelefone") ?: ""
                val tipo = backStackEntry.arguments?.getString("tipo") ?: "email"
                TelaVerificarCodigo(navController, emailOuTelefone, tipo)
            }

            composable("tela_redefinir_senha/{usuarioId}") { backStackEntry ->
                val usuarioId = backStackEntry.arguments?.getString("usuarioId") ?: ""
                TelaRedefinirSenha(navController, usuarioId)
            }

            //  Rotas do prestador
            composable("tela_inicio_prestador") {
                TelaInicioPrestador(navController)
            }

            composable("tela_perfil_prestador") {
                TelaPerfilPrestador(navController)
            }

            composable("tela_servicos") {
                TelaServicos(navController)
            }

            composable("tela_historico") {
                TelaHistorico(navController)
            }

            // Rota para detalhes do pedido concluído
            composable("detalhes_pedido_concluido/{pedidoId}") { backStackEntry ->
                val pedidoId = backStackEntry.arguments?.getString("pedidoId")?.toIntOrNull() ?: 0
                val context = LocalContext.current
                val token = TokenManager.obterTokenComBearer(context) ?: ""

                var pedido by remember { mutableStateOf<com.exemple.facilita.service.PedidoHistorico?>(null) }
                var isLoading by remember { mutableStateOf(true) }

                LaunchedEffect(pedidoId) {
                    val service = RetrofitFactory.getServicoService()
                    service.getHistoricoPedidos(token, 1, 100).enqueue(object : retrofit2.Callback<com.exemple.facilita.service.HistoricoPedidosResponse> {
                        override fun onResponse(
                            call: retrofit2.Call<com.exemple.facilita.service.HistoricoPedidosResponse>,
                            response: retrofit2.Response<com.exemple.facilita.service.HistoricoPedidosResponse>
                        ) {
                            if (response.isSuccessful) {
                                pedido = response.body()?.data?.pedidos?.find { it.id == pedidoId }
                            }
                            isLoading = false
                        }

                        override fun onFailure(call: retrofit2.Call<com.exemple.facilita.service.HistoricoPedidosResponse>, t: Throwable) {
                            isLoading = false
                        }
                    })
                }

                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF019D31))
                        }
                    }
                    pedido != null -> {
                        TelaDetalhesPedidoConcluido(
                            navController = navController,
                            pedido = pedido!!
                        )
                    }
                    else -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color.Red,
                                    modifier = Modifier.size(64.dp)
                                )
                                Text("Pedido não encontrado", color = Color.Red)
                                Button(onClick = { navController.popBackStack() }) {
                                    Text("Voltar")
                                }
                            }
                        }
                    }
                }
            }

            composable("tela_tipo_conta_servico") {
                TelaTipoContaServico(navController)
            }

            // Rota alternativa para compatibilidade
            composable("tela_tipo_conta") {
                TelaTipoContaServico(navController)
            }

            composable("tela_permissao_localizacao_servico") {
                TelaPermissaoLocalizacaoServico(navController)
            }

            composable("tela_completar_perfil_prestador") {
                TelaCompletarPerfilPrestador(navController, perfilViewModel, prestadorViewModel)
            }

            composable("tela_cnh") {
                TelaCNH(navController, perfilViewModel, prestadorViewModel)
            }

            composable("tela_documentos") {
                TelaDocumentos(navController, prestadorViewModel)
            }

            composable("tela_tipo_veiculo") {
                TelaTipoVeiculo(navController, prestadorViewModel)
            }

            composable("tela_detalhe_pedido/{servicoId}/{clienteNome}/{servicoDescricao}/{valor}/{local}/{horario}") { backStackEntry ->
                val servicoId = backStackEntry.arguments?.getString("servicoId")?.toIntOrNull() ?: 0
                val clienteNome = URLDecoder.decode(backStackEntry.arguments?.getString("clienteNome") ?: "", StandardCharsets.UTF_8.toString())
                val servicoDescricao = URLDecoder.decode(backStackEntry.arguments?.getString("servicoDescricao") ?: "", StandardCharsets.UTF_8.toString())
                val valor = URLDecoder.decode(backStackEntry.arguments?.getString("valor") ?: "", StandardCharsets.UTF_8.toString())
                val local = URLDecoder.decode(backStackEntry.arguments?.getString("local") ?: "", StandardCharsets.UTF_8.toString())
                val horario = URLDecoder.decode(backStackEntry.arguments?.getString("horario") ?: "", StandardCharsets.UTF_8.toString())

                TelaDetalhePedido(
                    navController = navController,
                    servicoId = servicoId,
                    clienteNome = clienteNome,
                    servicoDescricao = servicoDescricao,
                    valor = valor,
                    local = local,
                    horario = horario
                )
            }

            // Rotas da Carteira
            composable("tela_carteira") {
                TelaCarteira(navController)
            }

            composable("tela_adicionar_dinheiro") {
                TelaAdicionarDinheiro(navController)
            }

            composable("tela_sacar_dinheiro") {
                TelaSacarDinheiro(navController)
            }

            composable("tela_contas_bancarias") {
                TelaContasBancarias(navController)
            }

            composable("tela_adicionar_conta") {
                TelaAdicionarConta(navController)
            }

            composable("tela_qrcode_pix/{valor}") { backStackEntry ->
                val valor = backStackEntry.arguments?.getString("valor")?.toDoubleOrNull() ?: 0.0
                TelaQRCodePix(navController, valor)
            }

            composable("tela_informacoes_veiculo/{tiposVeiculo}") { backStackEntry ->
                val tiposVeiculo = backStackEntry.arguments?.getString("tiposVeiculo") ?: ""
                TelaInformacoesVeiculo(navController, tiposVeiculo, prestadorViewModel)
            }

            // TODO: Descomentar após corrigir TelaInformacoesVeiculo.kt (remover código duplicado)
            // composable("tela_veiculo/{tiposVeiculo}") { backStackEntry ->
            //     val tiposVeiculo = backStackEntry.arguments?.getString("tiposVeiculo") ?: ""
            //     TelaInformacoesVeiculo(navController, tiposVeiculo, perfilViewModel)
            // }

            // composable("tela_tipo_veiculo/{tiposVeiculo}") { backStackEntry ->
            //     val tiposVeiculo = backStackEntry.arguments?.getString("tiposVeiculo") ?: ""
            //     TelaInformacoesVeiculo(navController, tiposVeiculo, perfilViewModel)
            // }

            // Rota para tela de detalhes do serviço aceito
            composable("tela_detalhes_servico_aceito/{servicoId}") { backStackEntry ->
                val servicoId = backStackEntry.arguments?.getString("servicoId")?.toIntOrNull() ?: 0
                val context = LocalContext.current

                // Observar o estado do serviço
                val servicoState by servicoViewModel.servicoState.collectAsState()

                LaunchedEffect(servicoId) {
                    servicoViewModel.carregarServico(servicoId, context)
                }

                when {
                    servicoState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF00FF88))
                        }
                    }
                    servicoState.servico != null -> {
                        TelaDetalhesServicoAceito(
                            navController = navController,
                            servicoDetalhe = servicoState.servico!!
                        )
                    }
                    servicoState.error != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = servicoState.error ?: "Erro desconhecido",
                                    color = Color.Red
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { navController.popBackStack() }) {
                                    Text("Voltar")
                                }
                            }
                        }
                    }
                }
            }

            // Rota para tela de mapa com rota (estilo Uber)
            composable("tela_mapa_rota/{servicoId}") { backStackEntry ->
                val servicoId = backStackEntry.arguments?.getString("servicoId")?.toIntOrNull() ?: 0
                val context = LocalContext.current

                // Observar o estado do serviço
                val servicoState by servicoViewModel.servicoState.collectAsState()

                LaunchedEffect(servicoId) {
                    servicoViewModel.carregarServico(servicoId, context)
                }

                when {
                    servicoState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF019D31))
                        }
                    }
                    servicoState.servico != null -> {
                        TelaMapaRota(
                            navController = navController,
                            servicoDetalhe = servicoState.servico!!
                        )
                    }
                    servicoState.error != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = servicoState.error ?: "Erro desconhecido",
                                    color = Color.Red
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { navController.popBackStack() }) {
                                    Text("Voltar")
                                }
                            }
                        }
                    }
                }
            }

            // Rota para tela de detalhes do serviço aceito
            composable("tela_detalhes_servico_aceito/{servicoId}") { backStackEntry ->
                val servicoId = backStackEntry.arguments?.getString("servicoId")?.toIntOrNull() ?: 0
                val context = LocalContext.current

                // Observar o estado do serviço
                val servicoState by servicoViewModel.servicoState.collectAsState()

                LaunchedEffect(servicoId) {
                    servicoViewModel.carregarServico(servicoId, context)
                }

                when {
                    servicoState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF019D31))
                        }
                    }
                    servicoState.servico != null -> {
                        TelaDetalhesServicoAceito(
                            navController = navController,
                            servicoDetalhe = servicoState.servico!!
                        )
                    }
                    servicoState.error != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = servicoState.error ?: "Erro ao carregar serviço",
                                    color = Color.Red
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { navController.popBackStack() }) {
                                    Text("Voltar")
                                }
                            }
                        }
                    }
                }
            }

            // Rota para tela de pedido em andamento
            composable("tela_pedido_em_andamento/{servicoId}") { backStackEntry ->
                val servicoId = backStackEntry.arguments?.getString("servicoId")?.toIntOrNull() ?: 0
                TelaPedidoEmAndamento(
                    navController = navController,
                    servicoId = servicoId,
                    servicoViewModel = servicoViewModel
                )
            }

            // Rota para navegação 3D com mapa
            composable("rota_navegacao/{servicoId}") { backStackEntry ->
                val servicoId = backStackEntry.arguments?.getString("servicoId")?.toIntOrNull() ?: 0
                TelaRotaNavegacao(
                    navController = navController,
                    servicoId = servicoId,
                    servicoViewModel = servicoViewModel
                )
            }

            // Rota para tela de avaliação do cliente
            composable("avaliacao_cliente/{servicoId}/{clienteNome}/{valorServico}") { backStackEntry ->
                val servicoId = backStackEntry.arguments?.getString("servicoId")?.toIntOrNull() ?: 0
                val clienteNome = backStackEntry.arguments?.getString("clienteNome") ?: ""
                val valorServico = backStackEntry.arguments?.getString("valorServico") ?: "0"
                TelaAvaliacaoCliente(
                    navController = navController,
                    servicoId = servicoId,
                    clienteNome = clienteNome,
                    valorServico = valorServico
                )
            }

            // Rota para navegação em tempo real
            composable(
                "navegacao_tempo_real/{origemLat}/{origemLng}/{destinoLat}/{destinoLng}?paradas={paradas}"
            ) { backStackEntry ->
                val origemLat = backStackEntry.arguments?.getString("origemLat")?.toDoubleOrNull() ?: 0.0
                val origemLng = backStackEntry.arguments?.getString("origemLng")?.toDoubleOrNull() ?: 0.0
                val destinoLat = backStackEntry.arguments?.getString("destinoLat")?.toDoubleOrNull() ?: 0.0
                val destinoLng = backStackEntry.arguments?.getString("destinoLng")?.toDoubleOrNull() ?: 0.0
                val paradasJson = backStackEntry.arguments?.getString("paradas")

                TelaNavegacaoTempoReal(
                    navController = navController,
                    origemLat = origemLat,
                    origemLng = origemLng,
                    destinoLat = destinoLat,
                    destinoLng = destinoLng,
                    paradasJson = paradasJson
                )
            }

            // Rota para tela de rastreamento em tempo real
            composable("tela_rastreamento_servico/{servicoId}") { backStackEntry ->
                val servicoId = backStackEntry.arguments?.getString("servicoId")?.toIntOrNull() ?: 0
                val context = LocalContext.current

                // Observar o estado do serviço
                val servicoState by servicoViewModel.servicoState.collectAsState()

                LaunchedEffect(servicoId) {
                    servicoViewModel.carregarServico(servicoId, context)
                }

                when {
                    servicoState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF019D31))
                        }
                    }
                    servicoState.servico != null -> {
                        TelaRastreamentoServico(
                            navController = navController,
                            servicoDetalhe = servicoState.servico!!
                        )
                    }
                }
            }

            // Rota para chat ao vivo
            composable("chat_ao_vivo/{servicoId}/{contratanteId}/{contratanteNome}/{prestadorId}/{prestadorNome}") { backStackEntry ->
                val servicoId = backStackEntry.arguments?.getString("servicoId")?.toIntOrNull() ?: 0
                val contratanteId = backStackEntry.arguments?.getString("contratanteId")?.toIntOrNull() ?: 0
                val contratanteNome = URLDecoder.decode(backStackEntry.arguments?.getString("contratanteNome") ?: "", StandardCharsets.UTF_8.toString())
                val prestadorId = backStackEntry.arguments?.getString("prestadorId")?.toIntOrNull() ?: 0
                val prestadorNome = URLDecoder.decode(backStackEntry.arguments?.getString("prestadorNome") ?: "", StandardCharsets.UTF_8.toString())

                TelaChatAoVivo(
                    navController = navController,
                    servicoId = servicoId,
                    contratanteId = contratanteId,
                    contratanteNome = contratanteNome,
                    prestadorId = prestadorId,
                    prestadorNome = prestadorNome
                )
            }

        }

        // Notificação de novo serviço (aparece sobre qualquer tela)
        if (mostrarNotificacao && novoServico != null) {
            NotificacaoNovoServico(
                servico = novoServico!!,
                tempoRestante = tempoRestante,
                onAceitar = {
                    notificacaoViewModel.fecharNotificacao()
                },
                onVoltar = {
                    notificacaoViewModel.fecharNotificacao()
                },
                navController = navController,
                servicoViewModel = servicoViewModel
            )
        }
    }
}