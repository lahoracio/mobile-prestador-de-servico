package com.exemple.facilita.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.exemple.facilita.R

@Composable
fun TelaAlterarTelefone(navController: NavController) {
    var telefoneAtual by remember { mutableStateOf("") }
    var novoTelefone by remember { mutableStateOf("") }
    var confirmarTelefone by remember { mutableStateOf("") }
    var mensagem by remember { mutableStateOf("") }
    var erro by remember { mutableStateOf(false) }

    // ✅ Requisitos da senha
    val hasUppercase = novoTelefone.any { it.isUpperCase() }
    val hasLowercase = novoTelefone.any { it.isLowerCase() }
    val hasDigit = novoTelefone.any { it.isDigit() }
    val hasMinLength = novoTelefone.length >= 6

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Voltar",
                tint = Color.Black,
                modifier = Modifier
                    .size(32.dp)
                    .clickable { navController.popBackStack() }
            )

            Text(
                text = "Informações do Perfil",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.fillMaxWidth()
                    .padding(start = 60.dp)
            )
        }


        Spacer(modifier = Modifier.height(20.dp))


        Box(contentAlignment = Alignment.BottomEnd) {
            Image(
                painter = painterResource(id = R.drawable.foto_perfil),
                contentDescription = "Foto do usuário",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.LightGray, CircleShape),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color.LightGray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = Color(0xFF019D31), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Alterar Telefone",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(60.dp))

        // 🔹 Campos de email
        OutlinedTextField(
            value = telefoneAtual,
            onValueChange = { telefoneAtual = it },
            placeholder = { Text("Digite o telefone atual") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF019D31),
                unfocusedBorderColor = Color.LightGray
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = novoTelefone,
            onValueChange = { novoTelefone = it },
            placeholder = { Text("Novo Telefone") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF019D31),
                unfocusedBorderColor = Color.LightGray
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = confirmarTelefone,
            onValueChange = { confirmarTelefone = it },
            placeholder = { Text("Confirmar novo telefone") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF019D31),
                unfocusedBorderColor = Color.LightGray
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 🔹 Mensagem de erro ou sucesso
        if (mensagem.isNotEmpty()) {
            Text(
                text = mensagem,
                color = if (erro) Color.Red else Color(0xFF019D31),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )


        }

        Spacer(modifier = Modifier.height(200.dp))

        // 🔹 Botão salvar
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(50.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF019D31), Color(0xFF007A26))
                    )
                )
                .clickable {
                    if (novoTelefone != confirmarTelefone) {
                        erro = true
                        mensagem = "Os emails não coincidem."
                    } else if (!hasUppercase || !hasLowercase || !hasDigit || !hasMinLength) {
                        erro = true
                        mensagem = "O email não atende aos requisitos."
                    } else {
                        erro = false
                        mensagem = "Email alterado com sucesso!"
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Salvar",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewTelaAlterarTelefone() {
    val navController = rememberNavController()
    TelaAlterarTelefone(navController)
}

