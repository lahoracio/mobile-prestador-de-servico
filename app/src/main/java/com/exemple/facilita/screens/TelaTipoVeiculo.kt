package com.exemple.facilita.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.exemple.facilita.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaTipoVeiculo(
    navController: NavController,
    prestadorViewModel: com.exemple.facilita.viewmodel.PrestadorViewModel
) {
    var motoSelecionado by remember { mutableStateOf(false) }
    var carroSelecionado by remember { mutableStateOf(false) }
    var bicicletaSelecionado by remember { mutableStateOf(false) }

    var mensagemErro by remember { mutableStateOf("") }

    // Conta quantos estão selecionados
    val totalSelecionados = listOf(motoSelecionado, carroSelecionado, bicicletaSelecionado).count { it }

    Scaffold(
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Botão voltar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() }
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Voltar",
                        tint = Color(0xFF015B2B),
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Imagem do topo
            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.sejabemvindo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Escolha seu veículo",
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = Color(0xFF1A1A1A),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Selecione até 2 tipos de veículos que você utilizará para realizar as entregas",
                fontSize = 15.sp,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Cards de veículos com novo design
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Card Moto
                CardVeiculoModerno(
                    titulo = "Moto",
                    descricao = "Ideal para entregas rápidas",
                    iconRes = R.drawable.icontiposervico, // Você pode substituir por ícone específico
                    selecionado = motoSelecionado,
                    onClick = {
                        if (!motoSelecionado && totalSelecionados < 2) {
                            motoSelecionado = true
                            mensagemErro = ""
                        } else if (motoSelecionado) {
                            motoSelecionado = false
                            mensagemErro = ""
                        } else {
                            mensagemErro = "Você já selecionou 2 veículos. Desmarque um para selecionar outro."
                        }
                    }
                )

                // Card Carro
                CardVeiculoModerno(
                    titulo = "Carro",
                    descricao = "Para volumes maiores",
                    iconRes = R.drawable.icontiposervico,
                    selecionado = carroSelecionado,
                    onClick = {
                        if (!carroSelecionado && totalSelecionados < 2) {
                            carroSelecionado = true
                            mensagemErro = ""
                        } else if (carroSelecionado) {
                            carroSelecionado = false
                            mensagemErro = ""
                        } else {
                            mensagemErro = "Você já selecionou 2 veículos. Desmarque um para selecionar outro."
                        }
                    }
                )

                // Card Bicicleta
                CardVeiculoModerno(
                    titulo = "Bicicleta",
                    descricao = "Econômico e sustentável",
                    iconRes = R.drawable.icontiposervico,
                    selecionado = bicicletaSelecionado,
                    onClick = {
                        if (!bicicletaSelecionado && totalSelecionados < 2) {
                            bicicletaSelecionado = true
                            mensagemErro = ""
                        } else if (bicicletaSelecionado) {
                            bicicletaSelecionado = false
                            mensagemErro = ""
                        } else {
                            mensagemErro = "Você já selecionou 2 veículos. Desmarque um para selecionar outro."
                        }
                    }
                )
            }

            // Mensagem de erro
            if (mensagemErro.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = mensagemErro,
                        color = Color(0xFFD32F2F),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Indicador de seleção
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(2) { index ->
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(
                                if (index < totalSelecionados) Color(0xFF00B94A)
                                else Color(0xFFD9D9D9)
                            )
                    )
                    if (index < 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$totalSelecionados de 2 selecionados",
                    fontSize = 13.sp,
                    color = Color(0xFF666666)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botão Continuar
            Button(
                onClick = {
                    val tiposSelecionados = mutableListOf<String>()
                    if (motoSelecionado) tiposSelecionados.add("MOTO")
                    if (carroSelecionado) tiposSelecionados.add("CARRO")
                    if (bicicletaSelecionado) tiposSelecionados.add("BICICLETA")

                    // Navega para a tela de informações do veículo
                    val tipos = tiposSelecionados.joinToString(",")
                    navController.navigate("tela_informacoes_veiculo/$tipos")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = totalSelecionados > 0,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    disabledContainerColor = Color(0xFFE0E0E0)
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = if (totalSelecionados > 0) {
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF015B2B), Color(0xFF00B94A))
                                )
                            } else {
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFE0E0E0), Color(0xFFE0E0E0))
                                )
                            },
                            shape = RoundedCornerShape(28.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Continuar",
                        color = if (totalSelecionados > 0) Color.White else Color(0xFF999999),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun CardVeiculoModerno(
    titulo: String,
    descricao: String,
    iconRes: Int,
    selecionado: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() }
            .border(
                width = if (selecionado) 2.dp else 0.dp,
                color = if (selecionado) Color(0xFF00B94A) else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selecionado) Color(0xFFE8F5E9) else Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selecionado) 4.dp else 2.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ícone do veículo
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            if (selecionado) Color(0xFF00B94A).copy(alpha = 0.2f)
                            else Color(0xFFF5F5F5)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = titulo,
                        tint = if (selecionado) Color(0xFF00B94A) else Color(0xFF666666),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Textos
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = titulo,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selecionado) Color(0xFF00B94A) else Color(0xFF1A1A1A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = descricao,
                        fontSize = 13.sp,
                        color = Color(0xFF666666)
                    )
                }

                // Checkbox
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            if (selecionado) Color(0xFF00B94A)
                            else Color.Transparent
                        )
                        .border(
                            width = 2.dp,
                            color = if (selecionado) Color(0xFF00B94A) else Color(0xFFD9D9D9),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (selecionado) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selecionado",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// Preview comentado pois requer PrestadorViewModel
// @Preview(showBackground = true, showSystemUi = true)
// @Composable
// fun PreviewTelaTipoVeiculo() {
//     TelaTipoVeiculo(navController = rememberNavController())
// }

