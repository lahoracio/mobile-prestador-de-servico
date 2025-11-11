package com.exemple.facilita.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.exemple.facilita.R

@Composable
fun TelaTipoContaServico(navController: NavController) {
    var selectedOption by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // NOTA: Esta tela não é mais usada no fluxo principal.
    // Mantida apenas para compatibilidade.


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header verde
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(
                bottomEnd = 24.dp,
                bottomStart = 24.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF019D31)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Voltar",
                    tint = Color.White,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { navController.popBackStack() }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Qual tipo de conta deseja criar?",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Escolha a opção que mais combina com seu perfil.",
                    fontSize = 18.sp,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(72.dp))

        // Opção 1 - Contratante
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { selectedOption = "contratante" },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (selectedOption == "contratante") Color(0xFFE8F5E9) else Color(0xFFF0F0F0)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.icontiposervico),
                    contentDescription = "Ícone contratante",
                    modifier = Modifier
                        .height(70.dp)
                        .width(70.dp)
                )
                Column(Modifier.padding(16.dp)) {
                    Text("Contratante", fontWeight = FontWeight.Bold)
                    Text("Quero contratar prestadores de serviço para minhas necessidades.")
                }
            }
        }

        // Opção 2 - Prestador de serviço
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { selectedOption = "prestador" },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (selectedOption == "prestador") Color(0xFFE8F5E9) else Color(0xFFF0F0F0)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.icontiposervico),
                    contentDescription = "Ícone prestador",
                    modifier = Modifier
                        .height(70.dp)
                        .width(70.dp)
                )
                Column(Modifier.padding(16.dp)) {
                    Text("Prestador de serviço", fontWeight = FontWeight.Bold)
                    Text("Quero oferecer meus serviços e encontrar clientes.")
                }
            }
        }

        Spacer(modifier = Modifier.height(150.dp))

        // Botão Entrar
        Button(
            onClick = {
                when (selectedOption) {
                    "contratante" -> navController.navigate("tela_completar_perfil_contratante")
                    "prestador" -> navController.navigate("tela_permissao_localizacao_servico")
                    else -> Toast.makeText(context, "Por favor, selecione um tipo de conta.", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(56.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            contentPadding = PaddingValues()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF019D31), Color(0xFF06C755))
                        ),
                        shape = RoundedCornerShape(50)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Entrar",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TelaTipoContaServicoPreview() {
    val navController = rememberNavController()
    TelaTipoContaServico(navController = navController)
}
