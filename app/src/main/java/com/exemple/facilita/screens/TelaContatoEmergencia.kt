package com.exemple.facilita.screens


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaContatoEmergencia(navController: NavController) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Contato de Emergência") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color(0xFF015B2B))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFE6E6E6))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Informe um contato de emergência:", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(value = "", onValueChange = {}, label = { Text("Nome completo") })
            OutlinedTextField(value = "", onValueChange = {}, label = { Text("Telefone") })
            OutlinedTextField(value = "", onValueChange = {}, label = { Text("Parentesco") })
            Button(
                onClick = { /* Enviar */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF015B2B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salvar", color = Color.White)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTelaContatoEmergencia() {
    TelaContatoEmergencia(navController = rememberNavController())
}
