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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.exemple.facilita.R
import com.exemple.facilita.viewmodel.CNHViewModel
import com.exemple.facilita.viewmodel.PerfilViewModel
import com.exemple.facilita.utils.TokenManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaCNH(
    navController: NavController,
    perfilViewModel: PerfilViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {

    val viewModel: CNHViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val mensagem by viewModel.mensagem.collectAsState()
    val cnhValidada by viewModel.cnhValidada.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Observa quando CNH é validada com sucesso e volta para a tela anterior
    LaunchedEffect(cnhValidada) {
        if (cnhValidada) {
            perfilViewModel.marcarComoValidado("CNH com EAR")
            kotlinx.coroutines.delay(1500) // Aguarda 1.5s para mostrar a mensagem
            navController.navigate("tela_completar_perfil_prestador") {
                popUpTo("tela_cnh") { inclusive = true }
            }
            // Reseta o estado
            viewModel.resetCnhValidada()
            viewModel.setMensagem("")
        }
    }

    // Mostra mensagens no Snackbar
    LaunchedEffect(mensagem) {
        mensagem?.let { msg ->
            if (msg.isNotEmpty()) {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = msg,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    var categoriaExpanded by remember { mutableStateOf(false) }
    val categorias = listOf("A", "B", "AB", "C", "D", "E")
    var categoria by remember { mutableStateOf("") }

    var numeroCNH by remember { mutableStateOf("") }
    var validade by remember { mutableStateOf("") }
    var earExpanded by remember { mutableStateOf(false) }
    val opcoesEAR = listOf("Sim", "Não")
    var possuiEAR by remember { mutableStateOf("") }
    var pontuacao by remember { mutableStateOf("") }

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
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ícone Voltar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() },
                    modifier = Modifier.padding(0.dp)) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Voltar",
                        tint = Color(0xFF015B2B)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }

            // Imagem de topo
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
                text = "Informações da CNH",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Preencha seus dados de habilitação",
                fontSize = 16.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Campo número CNH
            OutlinedTextField(
                value = numeroCNH,
                onValueChange = { numeroCNH = it },
                label = { Text("Número da CNH") },
                placeholder = { Text("Ex: 12345678901 (11 dígitos)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Categoria CNH (dropdown)
            ExposedDropdownMenuBox(
                expanded = categoriaExpanded,
                onExpandedChange = { categoriaExpanded = !categoriaExpanded }
            ) {
                OutlinedTextField(
                    value = categoria,
                    onValueChange = { categoria = it },
                    label = { Text("Categoria da CNH") },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoriaExpanded) },
                    readOnly = true
                )
                ExposedDropdownMenu(
                    expanded = categoriaExpanded,
                    onDismissRequest = { categoriaExpanded = false }
                ) {
                    categorias.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                categoria = option
                                categoriaExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Validade CNH
            OutlinedTextField(
                value = validade,
                onValueChange = { validade = it },
                label = { Text("Validade da CNH") },
                placeholder = { Text("Ex: 2028-12-31") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // EAR (Exerce Atividade Remunerada) - Dropdown
            ExposedDropdownMenuBox(
                expanded = earExpanded,
                onExpandedChange = { earExpanded = !earExpanded }
            ) {
                OutlinedTextField(
                    value = possuiEAR,
                    onValueChange = { possuiEAR = it },
                    label = { Text("Possui EAR (Exerce Atividade Remunerada)?") },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = earExpanded) },
                    readOnly = true
                )
                ExposedDropdownMenu(
                    expanded = earExpanded,
                    onDismissRequest = { earExpanded = false }
                ) {
                    opcoesEAR.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                possuiEAR = option
                                earExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pontuação atual na CNH
            OutlinedTextField(
                value = pontuacao,
                onValueChange = { pontuacao = it },
                label = { Text("Pontuação atual na CNH") },
                placeholder = { Text("Ex: 10 pontos") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Lembre-se: para exercer atividade remunerada, é obrigatório ter EAR incluso na CNH.",
                fontSize = 14.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ✅ Botão Finalizar (corrigido)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(48.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF015B2B), Color(0xFF00B94A))
                        ),
                        shape = RoundedCornerShape(50)
                    )
                    .clickable {
                        // Validação básica
                        if (numeroCNH.isBlank() || categoria.isBlank() || validade.isBlank() || possuiEAR.isBlank()) {
                            viewModel.setMensagem("Por favor, preencha todos os campos obrigatórios")
                            return@clickable
                        }

                        // Validação do formato da data (YYYY-MM-DD)
                        val dateRegex = Regex("""^\d{4}-\d{2}-\d{2}$""")
                        if (!validade.matches(dateRegex)) {
                            viewModel.setMensagem("Formato de data inválido. Use: AAAA-MM-DD (Ex: 2030-05-12)")
                            return@clickable
                        }

                        // Validação do número da CNH (deve ter 11 dígitos)
                        if (numeroCNH.length != 11 || !numeroCNH.all { it.isDigit() }) {
                            viewModel.setMensagem("Número da CNH deve conter exatamente 11 dígitos")
                            return@clickable
                        }

                        val token = TokenManager.getToken()

                        // Converte "Sim"/"Não" para Boolean
                        val possuiEARBoolean = possuiEAR.equals("Sim", ignoreCase = true)

                        viewModel.validarCNH(
                            token = token,
                            numeroCNH = numeroCNH,
                            categoria = categoria,
                            validade = validade,
                            possuiEAR = possuiEARBoolean
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Validar CNH",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium
                )
            }


            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTelaCNH() {
    TelaCNH(navController = rememberNavController())
}
