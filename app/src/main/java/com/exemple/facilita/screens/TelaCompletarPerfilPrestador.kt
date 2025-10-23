package com.exemple.facilita.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.exemple.facilita.R

@Composable
fun TelaCompletarPerfilPrestador(
    navController: androidx.navigation.NavController,
    onFinalizar: () -> Unit = {},
    onVoltar: (() -> Unit)? = null
) {
    var regiao by remember { mutableStateOf(TextFieldValue("Jandira, SP")) }
    var endereco by remember { mutableStateOf(TextFieldValue("")) }

    // Lista de opções com destino de navegação
    val opcoesDocs = listOf(
        "Foto da CNH com EAR" to "telaCNH",
        "Foto do RG e CPF" to "telaDocumentos",
        "Contato de emergência" to "telaContatoEmergencia",
        "Informações do veículo" to null
    )
    Scaffold(
        containerColor = Color(0xFFE6E6E6)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Foto de perfil com ícone sobreposto
            Box(
                modifier = Modifier.size(130.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE6E6E6)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.foto_perfil),
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                    )
                }

                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Adicionar foto",
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .absoluteOffset(x = -8.dp, y = -3.dp)
                        .size(28.dp)
                        .background(Color(0xFF019D31), CircleShape)
                        .padding(5.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Bem vindo entregador",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Complete seu perfil",
                fontSize = 16.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))
            


            OutlinedTextField(
                value = endereco,
                onValueChange = { endereco = it },
                label = { Text("Endereço completo") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Cadastre alguns documentos para a validação do seu serviço",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Lista de itens
            Column(modifier = Modifier.fillMaxWidth()) {
                opcoesDocs.forEach { (texto, rota) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                rota?.let { navController.navigate(it) }
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFD9D9D9))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(texto, fontSize = 15.sp)
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForwardIos,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Divider(color = Color(0xFFD9D9D9), thickness = 1.dp)
                }
            }

            Spacer(modifier = Modifier.height(140.dp))

            Box(
                modifier = Modifier
                    .width(220.dp)
                    .height(48.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF015B2B), Color(0xFF00B94A))
                        ),
                        shape = RoundedCornerShape(50)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Finalizar",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewTelaCompletarPerfilPrestador() {
    val navController = rememberNavController()
    TelaCompletarPerfilPrestador(navController = navController)
}