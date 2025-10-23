package com.exemple.facilita.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.exemple.facilita.R

@Composable
fun TelaTipoVeiculo(navController: NavController) {
    val opcoes = listOf("Moto", "Bicicleta", "Carro", "A pé")
    var selecionadas by remember { mutableStateOf(setOf<String>()) }

    fun alternarSelecao(opcao: String) {
        selecionadas = if (selecionadas.contains(opcao)) {
            selecionadas - opcao
        } else {
            if (selecionadas.size < 2) selecionadas + opcao else selecionadas
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F2))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Cabeçalho
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp) // aumentei a altura para dar espaço vertical
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF019D31), Color(0xFF019D31))
                        ),
                        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                    )
                    
            ) {
                // Ícone de voltar à esquerda
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Voltar",
                    tint = Color.Black,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 12.dp, top = 23.dp) // ajusta verticalmente
                        .clickable { navController.popBackStack() }
                )

                // Texto centralizado
                Text(
                    text = "De que forma você pretende\nrealizar os serviços",
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center) // centraliza na Box
                        .padding(top = 8.dp) // desce um pouco se necessário
                )
            }

            // Conteúdo principal com rolagem
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp)) // dá espaçamento abaixo do header

                Text(
                    text = "Escolha o melhor tipo de locomoção para você\n realizar as entregas. Você pode escolher\n até duas opções.",
                    fontSize = 14.sp,
                    color = Color(0xFF444444),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(70.dp)) // afasta os cards do texto

                // Grade de opções
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OpcaoTransporte(
                            nome = "Moto",
                            selecionado = selecionadas.contains("Moto"),
                            onClick = { alternarSelecao("Moto") },
                            imagem = R.drawable.moto
                        )
                        OpcaoTransporte(
                            nome = "Bicicleta",
                            selecionado = selecionadas.contains("Bicicleta"),
                            onClick = { alternarSelecao("Bicicleta") },
                            imagem = R.drawable.bike
                        )
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OpcaoTransporte(
                            nome = "Carro",
                            selecionado = selecionadas.contains("Carro"),
                            onClick = { alternarSelecao("Carro") },
                            imagem = R.drawable.carro
                        )
                        OpcaoTransporte(
                            nome = "A pé",
                            selecionado = selecionadas.contains("A pé"),
                            onClick = { alternarSelecao("A pé") },
                            imagem = R.drawable.ape
                        )
                    }
                }

                Spacer(modifier = Modifier.height(120.dp)) // espaço pro botão fixo
            }
        }

        // Botão fixo no rodapé
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 70.dp)
                .fillMaxWidth(0.6f)
                .height(44.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF015B2B), Color(0xFF00B94A))
                    ),
                    shape = RoundedCornerShape(50)
                )
                .clickable(enabled = selecionadas.isNotEmpty()) {
                    navController.navigate("tela_completar_perfil_prestador")
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Avançar",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

@Composable
fun OpcaoTransporte(
    nome: String,
    selecionado: Boolean,
    onClick: () -> Unit,
    imagem: Int
) {
    val bordaCor = if (selecionado) Color(0xFF00B94A) else Color(0xFFE0E0E0)
    val fundo = if (selecionado) Color(0xFFE8F8EF) else Color.White

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(130.dp)
            .height(130.dp)
            .background(fundo, shape = RoundedCornerShape(16.dp))
            .border(2.dp, bordaCor, shape = RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(20.dp)
    ) {
        Image(
            painter = painterResource(id = imagem),
            contentDescription = nome,
            modifier = Modifier
                .height(60.dp)
                .fillMaxWidth(),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = nome,
            fontSize = 14.sp,
            color = Color(0xFF111111)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TelaTipoVeiculoPreview() {
    val navController = rememberNavController()
    TelaTipoVeiculo(navController)
}
