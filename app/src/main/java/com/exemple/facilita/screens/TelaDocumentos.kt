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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaDocumentos(navController: NavController) {
    var rg by remember { mutableStateOf("") }
    var orgaoEmissor by remember { mutableStateOf("") }
    var dataEmissao by remember { mutableStateOf("") }
    var cpf by remember { mutableStateOf("") }
    var nomeCompleto by remember { mutableStateOf("") }
    var naturalidade by remember { mutableStateOf("") }

    Scaffold(containerColor = Color(0xFFE6E6E6)) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Botão de Voltar
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
                text = "Documentos",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Preencha seus dados de RG e CPF",
                fontSize = 16.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Campo Nome Completo
            OutlinedTextField(
                value = nomeCompleto,
                onValueChange = { nomeCompleto = it },
                label = { Text("Nome completo") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Campo RG
            OutlinedTextField(
                value = rg,
                onValueChange = { rg = it },
                label = { Text("Número do RG") },
                placeholder = { Text("Ex: 12.345.678-9") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Campo Órgão Emissor
            OutlinedTextField(
                value = orgaoEmissor,
                onValueChange = { orgaoEmissor = it },
                label = { Text("Órgão emissor do RG") },
                placeholder = { Text("Ex: SSP-SP") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Campo Data de Emissão
            OutlinedTextField(
                value = dataEmissao,
                onValueChange = { dataEmissao = it },
                label = { Text("Data de emissão do RG") },
                placeholder = { Text("Ex: 05/06/2018") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Campo CPF
            OutlinedTextField(
                value = cpf,
                onValueChange = { cpf = it },
                label = { Text("Número do CPF") },
                placeholder = { Text("Ex: 123.456.789-00") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Campo Naturalidade
            OutlinedTextField(
                value = naturalidade,
                onValueChange = { naturalidade = it },
                label = { Text("Naturalidade") },
                placeholder = { Text("Cidade / Estado") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Verifique se todos os dados estão corretos antes de prosseguir.",
                fontSize = 14.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botão Salvar
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
                        navController.popBackStack()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Salvar",
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
fun PreviewTelaDocumentos() {
    TelaDocumentos(navController = rememberNavController())
}
