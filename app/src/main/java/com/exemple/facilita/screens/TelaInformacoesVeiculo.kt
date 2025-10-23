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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
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
fun TelaInformacoesVeiculo(navController: NavController) {
    var tipoVeiculoExpanded by remember { mutableStateOf(false) }
    val tiposVeiculo = listOf("Moto", "Bicicleta", "Carro", "A pé")
    var tipoVeiculo by remember { mutableStateOf("") }

    var modelo by remember { mutableStateOf("") }

    var anoExpanded by remember { mutableStateOf(false) }
    val anos = (2025 downTo 1990).map { it.toString() }
    var ano by remember { mutableStateOf("") }

    var possuiSeguro by remember { mutableStateOf("") }
    var compartimento by remember { mutableStateOf("") }
    var revisao by remember { mutableStateOf("") }
    var antecedentes by remember { mutableStateOf("") }

    Scaffold(containerColor = Color(0xFFE6E6E6)) { padding ->
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
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .absoluteOffset(x = -8.dp, y = -3.dp)
                        .size(28.dp)
                        .background(Color(0xFF019D31), CircleShape)
                        .padding(5.dp)
                )
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
            Text(
                text = "Informações do veículo",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111111)
            )

            Spacer(modifier = Modifier.height(10.dp))

            ExposedDropdownMenuBox(
                expanded = tipoVeiculoExpanded,
                onExpandedChange = { tipoVeiculoExpanded = !tipoVeiculoExpanded }
            ) {
                OutlinedTextField(
                    value = tipoVeiculo,
                    onValueChange = { tipoVeiculo = it },
                    label = { Text("Tipo de veículo") },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tipoVeiculoExpanded) },
                    readOnly = true
                )
                ExposedDropdownMenu(
                    expanded = tipoVeiculoExpanded,
                    onDismissRequest = { tipoVeiculoExpanded = false }
                ) {
                    tiposVeiculo.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                tipoVeiculo = option
                                tipoVeiculoExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = modelo,
                    onValueChange = { modelo = it },
                    label = { Text("Modelo") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = anoExpanded,
                    onExpandedChange = { anoExpanded = !anoExpanded },
                    modifier = Modifier.weight(1f) // ocupa a outra metade do espaço
                ) {
                    OutlinedTextField(
                        value = ano,
                        onValueChange = { ano = it },
                        label = { Text("Ano") },
                        modifier = Modifier
                            .menuAnchor()
                            .weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = anoExpanded) },
                        readOnly = true
                    )
                    ExposedDropdownMenu(
                        expanded = anoExpanded,
                        onDismissRequest = { anoExpanded = false }
                    ) {
                        anos.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    ano = option
                                    anoExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = possuiSeguro,
                onValueChange = { possuiSeguro = it },
                label = { Text("O veículo possui seguro?") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = compartimento,
                onValueChange = { compartimento = it },
                label = { Text("O veículo tem compartimento adequado para transportar mercadorias?") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = revisao,
                onValueChange = { revisao = it },
                label = { Text("O veículo possui a revisão em dia?") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = antecedentes,
                onValueChange = { antecedentes = it },
                label = { Text("Antecedentes Criminais") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))
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
                    text = "Finalizar",
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
fun PreviewTelaInformacoesVeiculo() {
    TelaInformacoesVeiculo(navController = rememberNavController())
}
