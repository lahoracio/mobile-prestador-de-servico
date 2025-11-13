package com.exemple.facilita.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.exemple.facilita.viewmodel.CarteiraViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaContasBancarias(navController: NavController, usuarioId: String = "user123") {
    val viewModel: CarteiraViewModel = viewModel()
    val contasBancarias by viewModel.contasBancarias.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.carregarContasBancarias(usuarioId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contas Bancárias", fontWeight = FontWeight.Bold) },
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("tela_adicionar_conta") },
                containerColor = Color(0xFF019D31)
            ) {
                Icon(Icons.Default.Add, "Adicionar", tint = Color.White)
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFFF1F9F4), Color.White)))
                .padding(padding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (contasBancarias.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.AccountBalance, null, modifier = Modifier.size(80.dp), tint = Color(0xFFBDBDBD))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Nenhuma conta cadastrada", fontSize = 18.sp, color = Color(0xFF757575))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Adicione uma conta para sacar", fontSize = 14.sp, color = Color(0xFF9E9E9E))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(contasBancarias) { conta ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(conta.banco, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(conta.nomeTitular, fontSize = 14.sp, color = Color(0xFF757575))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Agência: ${conta.agencia}", fontSize = 13.sp)
                                        Text("Conta: ${conta.conta}", fontSize = 13.sp)
                                        Text("Tipo: ${conta.tipoConta}", fontSize = 13.sp)
                                    }
                                    if (conta.isPrincipal) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFF4CAF50).copy(0.1f)
                                        ) {
                                            Text(
                                                "Principal",
                                                fontSize = 11.sp,
                                                color = Color(0xFF4CAF50),
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

