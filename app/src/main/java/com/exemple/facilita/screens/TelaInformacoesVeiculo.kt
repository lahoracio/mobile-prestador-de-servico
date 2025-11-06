package com.exemple.facilita.screens


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.exemple.facilita.R
import com.exemple.facilita.model.ModalidadeItem
import com.exemple.facilita.viewmodel.ModalidadeViewModel
import com.exemple.facilita.viewmodel.PerfilViewModel
import com.exemple.facilita.utils.TokenManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaInformacoesVeiculo(
    navController: NavController,
    tiposVeiculo: String = "", // Recebe os tipos separados por vírgula
    perfilViewModel: PerfilViewModel = viewModel()
) {
    val tiposList = remember { tiposVeiculo.split(",").filter { it.isNotEmpty() } }
    val modalidadeViewModel: ModalidadeViewModel = viewModel()

    val mensagem by modalidadeViewModel.mensagem.collectAsState()
    val modalidadesCadastradas by modalidadeViewModel.modalidadesCadastradas.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Token do usuário autenticado (tipo PRESTADOR)
    val token = TokenManager.getToken()

    // Estados para cada veículo (vamos criar campos dinâmicos)
    val veiculosInfo = remember {
        tiposList.map { tipo ->
            mutableStateOf(
                VeiculoInfo(
                    tipo = tipo,
                    modelo = "",
                    ano = "",
                    possuiSeguro = "",
                    compartimento = "",
                    revisao = "",
                    antecedentes = ""
                )
            )
        }
    }

    var veiculoAtualIndex by remember { mutableStateOf(0) }
    val veiculoAtual = if (veiculosInfo.isNotEmpty()) veiculosInfo[veiculoAtualIndex] else null

    // Observa quando as modalidades forem cadastradas com sucesso
    LaunchedEffect(modalidadesCadastradas) {
        if (modalidadesCadastradas) {
            // Marca como validado
            perfilViewModel.marcarComoValidado("Informações do veículo")
            // Reseta o estado para evitar problemas futuros
            modalidadeViewModel.resetModalidadesCadastradas()
            modalidadeViewModel.setMensagem(null)
            // Volta para completar perfil
            navController.navigate("tela_completar_perfil_prestador") {
                popUpTo("tela_tipo_veiculo") { inclusive = true }
            }
        }
    }

    // Mostra mensagens no Snackbar
    LaunchedEffect(mensagem) {
        mensagem?.let { msg ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = msg,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFE6E6E6),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { snackbarData ->
                    Snackbar(
                        snackbarData = snackbarData,
                        containerColor = if (snackbarData.visuals.message.contains("sucesso"))
                            Color(0xFF00B94A) else Color(0xFFD32F2F),
                        contentColor = Color.White,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() },
                    modifier = Modifier.padding(0.dp) ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Voltar",
                        tint = Color(0xFF015B2B)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }

            Box(modifier = Modifier.size(130.dp), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE6E6E6)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.sejabemvindo),
                        contentDescription = null,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Bem vindo entregador",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Complete seu perfil",
                fontSize = 16.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            if (veiculoAtual != null) {
                val isBicicleta = veiculoAtual.value.tipo.uppercase() == "BICICLETA"

                Text(
                    text = "Informações do veículo (${veiculoAtualIndex + 1}/${veiculosInfo.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF111111)
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Tipo: ${veiculoAtual.value.tipo}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF00B94A)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Para bicicleta, apenas modelo (opcional)
                if (isBicicleta) {
                    OutlinedTextField(
                        value = veiculoAtual.value.modelo,
                        onValueChange = {
                            veiculoAtual.value = veiculoAtual.value.copy(modelo = it)
                        },
                        label = { Text("Modelo (opcional)") },
                        placeholder = { Text("Ex: Caloi, Shimano...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "✅ Para entregas com bicicleta, não é necessário fornecer mais informações.",
                        fontSize = 14.sp,
                        color = Color(0xFF00B94A),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    // Formulário completo para outros veículos (Moto, Carro, Caminhão, Van)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = veiculoAtual.value.modelo,
                            onValueChange = {
                                veiculoAtual.value = veiculoAtual.value.copy(modelo = it)
                            },
                            label = { Text("Modelo") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = veiculoAtual.value.ano,
                            onValueChange = {
                                veiculoAtual.value = veiculoAtual.value.copy(ano = it)
                            },
                            label = { Text("Ano") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = veiculoAtual.value.possuiSeguro,
                        onValueChange = {
                            veiculoAtual.value = veiculoAtual.value.copy(possuiSeguro = it)
                        },
                        label = { Text("O veículo possui seguro? (sim/não)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = veiculoAtual.value.compartimento,
                        onValueChange = {
                            veiculoAtual.value = veiculoAtual.value.copy(compartimento = it)
                        },
                        label = { Text("Compartimento adequado? (sim/não)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = veiculoAtual.value.revisao,
                        onValueChange = {
                            veiculoAtual.value = veiculoAtual.value.copy(revisao = it)
                        },
                        label = { Text("Revisão em dia? (sim/não)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = veiculoAtual.value.antecedentes,
                        onValueChange = {
                            veiculoAtual.value = veiculoAtual.value.copy(antecedentes = it)
                        },
                        label = { Text("Sem antecedentes criminais? (sim/não)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))


                // Botões de navegação entre veículos ou finalizar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Botão Anterior (se não for o primeiro)
                    if (veiculoAtualIndex > 0) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .padding(end = 8.dp)
                                .background(
                                    color = Color.Gray,
                                    shape = RoundedCornerShape(50)
                                )
                                .clickable {
                                    veiculoAtualIndex--
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Anterior",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Botão Próximo ou Finalizar
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .padding(start = if (veiculoAtualIndex > 0) 8.dp else 0.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF015B2B), Color(0xFF00B94A))
                                ),
                                shape = RoundedCornerShape(50)
                            )
                            .clickable {
                                if (veiculoAtualIndex < veiculosInfo.size - 1) {
                                    // Próximo veículo
                                    veiculoAtualIndex++
                                } else {
                                    // Finalizar e enviar para API
                                    scope.launch {
                                        val modalidades = veiculosInfo.map { veiculoState ->
                                            val v = veiculoState.value
                                            val isBike = v.tipo.uppercase() == "BICICLETA"

                                            ModalidadeItem(
                                                tipo = v.tipo,
                                                modelo_veiculo = v.modelo.ifEmpty { null },
                                                ano_veiculo = if (isBike) null else v.ano.toIntOrNull(),
                                                possui_seguro = if (isBike) false else v.possuiSeguro.lowercase() == "sim",
                                                compartimento_adequado = if (isBike) true else v.compartimento.lowercase() == "sim",
                                                revisao_em_dia = if (isBike) true else v.revisao.lowercase() == "sim",
                                                antecedentes_criminais = if (isBike) true else v.antecedentes.lowercase() == "sim"
                                            )
                                        }

                                        modalidadeViewModel.cadastrarModalidades(token, modalidades)
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (veiculoAtualIndex < veiculosInfo.size - 1) "Próximo" else "Finalizar",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Data class auxiliar
data class VeiculoInfo(
    val tipo: String,
    val modelo: String,
    val ano: String,
    val possuiSeguro: String,
    val compartimento: String,
    val revisao: String,
    val antecedentes: String
)

@Preview(showBackground = true)
@Composable
fun PreviewTelaInformacoesVeiculo() {
    TelaInformacoesVeiculo(navController = rememberNavController(), tiposVeiculo = "MOTO,CARRO")
}
