package com.exemple.facilita.screens

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.exemple.facilita.model.ContaBancaria
import com.exemple.facilita.viewmodel.CarteiraViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaAdicionarConta(navController: NavController, usuarioId: String = "user123") {
    val viewModel: CarteiraViewModel = viewModel()
    val isLoading by viewModel.isLoading.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    var nomeTitular by remember { mutableStateOf("") }
    var cpf by remember { mutableStateOf("") }
    var banco by remember { mutableStateOf("") }
    var agencia by remember { mutableStateOf("") }
    var conta by remember { mutableStateOf("") }
    var tipoConta by remember { mutableStateOf("CORRENTE") }

    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            kotlinx.coroutines.delay(1500)
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Adicionar Conta", fontWeight = FontWeight.Bold) },
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = nomeTitular,
                    onValueChange = { nomeTitular = it },
                    label = { Text("Nome do Titular") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF019D31),
                        focusedLabelColor = Color(0xFF019D31)
                    )
                )

                OutlinedTextField(
                    value = cpf,
                    onValueChange = { cpf = it },
                    label = { Text("CPF") },
                    leadingIcon = { Icon(Icons.Default.Badge, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF019D31),
                        focusedLabelColor = Color(0xFF019D31)
                    )
                )

                OutlinedTextField(
                    value = banco,
                    onValueChange = { banco = it },
                    label = { Text("Nome do Banco") },
                    leadingIcon = { Icon(Icons.Default.AccountBalance, null) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF019D31),
                        focusedLabelColor = Color(0xFF019D31)
                    )
                )

                OutlinedTextField(
                    value = agencia,
                    onValueChange = { agencia = it },
                    label = { Text("Agência") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF019D31),
                        focusedLabelColor = Color(0xFF019D31)
                    )
                )

                OutlinedTextField(
                    value = conta,
                    onValueChange = { conta = it },
                    label = { Text("Número da Conta") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF019D31),
                        focusedLabelColor = Color(0xFF019D31)
                    )
                )

                Text("Tipo de Conta", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF019D31))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf("CORRENTE", "POUPANCA").forEach { tipo ->
                        FilterChip(
                            selected = tipoConta == tipo,
                            onClick = { tipoConta = tipo },
                            label = { Text(if (tipo == "CORRENTE") "Corrente" else "Poupança") },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF019D31),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Button(
                    onClick = {
                        val novaConta = ContaBancaria(
                            usuarioId = usuarioId,
                            nomeTitular = nomeTitular,
                            cpf = cpf,
                            banco = banco,
                            agencia = agencia,
                            conta = conta,
                            tipoConta = tipoConta
                        )
                        viewModel.adicionarContaBancaria(novaConta, "dummy_token") {}
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = !isLoading && nomeTitular.isNotBlank() && cpf.isNotBlank() &&
                             banco.isNotBlank() && agencia.isNotBlank() && conta.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF019D31)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Default.Save, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Salvar Conta", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
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

