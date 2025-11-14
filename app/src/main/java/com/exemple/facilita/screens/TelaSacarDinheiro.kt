package com.exemple.facilita.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.exemple.facilita.viewmodel.CarteiraViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaSacarDinheiro(navController: NavController, usuarioId: String = "user123") {
    val viewModel: CarteiraViewModel = viewModel()
    val carteira by viewModel.carteira.collectAsState()
    val contasBancarias by viewModel.contasBancarias.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    var valor by remember { mutableStateOf("") }
    var contaSelecionada by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.carregarCarteira(usuarioId)
        viewModel.carregarContasBancarias(usuarioId)
    }

    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            kotlinx.coroutines.delay(2000)
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sacar Dinheiro", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF019D31),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFFF1F9F4), Color.White)))
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Card Saldo Disponível
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF019D31))
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                        Text("Saldo Disponível", fontSize = 14.sp, color = Color.White.copy(0.8f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "R$ ${String.format("%.2f", carteira?.saldo ?: 0.0)}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Campo Valor
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                        Text("Valor do Saque", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF019D31))
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = valor,
                            onValueChange = { valor = it },
                            label = { Text("Valor (R$)") },
                            leadingIcon = { Icon(Icons.Default.AttachMoney, null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF019D31),
                                focusedLabelColor = Color(0xFF019D31)
                            )
                        )
                    }
                }

                // Selecionar Conta
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Conta Bancária", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF019D31))
                            TextButton(onClick = { navController.navigate("tela_contas_bancarias") }) {
                                Text("Gerenciar", color = Color(0xFF019D31))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (contasBancarias.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                                onClick = { navController.navigate("tela_adicionar_conta") }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Add, null, tint = Color(0xFFFF9800))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Adicionar conta bancária", color = Color(0xFFFF9800))
                                }
                            }
                        } else {
                            contasBancarias.forEach { conta ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (contaSelecionada == conta.id)
                                            Color(0xFF019D31).copy(0.1f)
                                        else Color(0xFFF5F5F5)
                                    ),
                                    onClick = { contaSelecionada = conta.id }
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(conta.banco, fontWeight = FontWeight.Bold)
                                            Text("Ag: ${conta.agencia} - CC: ${conta.conta}", fontSize = 13.sp, color = Color(0xFF757575))
                                        }
                                        if (contaSelecionada == conta.id) {
                                            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF019D31))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Botão Confirmar
                Button(
                    onClick = {
                        val v = valor.replace(",", ".").toDoubleOrNull()
                        if (v != null && v > 0 && contaSelecionada != null) {
                            viewModel.solicitarSaque(v, contaSelecionada!!, "dummy_token")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = !isLoading && valor.toDoubleOrNull() != null && contaSelecionada != null,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF019D31)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Default.Check, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Confirmar Saque", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            errorMessage?.let {
                Snackbar(modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp), containerColor = Color(0xFFF44336)) {
                    Text(it, color = Color.White)
                }
            }
            successMessage?.let {
                Snackbar(modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp), containerColor = Color(0xFF4CAF50)) {
                    Text(it, color = Color.White)
                }
            }
        }
    }
}

