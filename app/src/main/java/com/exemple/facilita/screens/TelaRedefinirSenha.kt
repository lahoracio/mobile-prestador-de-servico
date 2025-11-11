package com.exemple.facilita.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.exemple.facilita.R
import com.exemple.facilita.utils.sdp
import com.exemple.facilita.utils.ssp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaRedefinirSenha(
    navController: NavController,
    usuarioId: String
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var novaSenha by remember { mutableStateOf("") }
    var confirmarSenha by remember { mutableStateOf("") }
    var novaSenhaVisivel by remember { mutableStateOf(false) }
    var confirmarSenhaVisivel by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1E3A8A),
            Color(0xFF3B82F6)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Logo
            Image(
                painter = painterResource(id = R.drawable.logotcc),
                contentDescription = "Logo Facilita",
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Nova Senha",
                fontSize = 28.ssp(),
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Digite sua nova senha",
                fontSize = 14.ssp(),
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Card principal
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Campo Nova Senha
                    OutlinedTextField(
                        value = novaSenha,
                        onValueChange = {
                            novaSenha = it
                            errorMessage = null
                        },
                        label = { Text("Nova Senha") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null)
                        },
                        trailingIcon = {
                            IconButton(onClick = { novaSenhaVisivel = !novaSenhaVisivel }) {
                                Icon(
                                    if (novaSenhaVisivel) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (novaSenhaVisivel) "Ocultar senha" else "Mostrar senha"
                                )
                            }
                        },
                        visualTransformation = if (novaSenhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1E3A8A),
                            unfocusedBorderColor = Color.Gray
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo Confirmar Senha
                    OutlinedTextField(
                        value = confirmarSenha,
                        onValueChange = {
                            confirmarSenha = it
                            errorMessage = null
                        },
                        label = { Text("Confirmar Nova Senha") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null)
                        },
                        trailingIcon = {
                            IconButton(onClick = { confirmarSenhaVisivel = !confirmarSenhaVisivel }) {
                                Icon(
                                    if (confirmarSenhaVisivel) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (confirmarSenhaVisivel) "Ocultar senha" else "Mostrar senha"
                                )
                            }
                        },
                        visualTransformation = if (confirmarSenhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1E3A8A),
                            unfocusedBorderColor = Color.Gray
                        )
                    )

                    // Requisitos de senha
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Mínimo de 6 caracteres\n• As senhas devem ser iguais",
                        fontSize = 12.ssp(),
                        color = Color.Gray,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Mensagens de erro/sucesso
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage!!,
                            color = Color.Red,
                            fontSize = 12.ssp(),
                            textAlign = TextAlign.Center
                        )
                    }

                    if (successMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = successMessage!!,
                            color = Color(0xFF10B981),
                            fontSize = 12.ssp(),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botão Redefinir Senha
                    Button(
                        onClick = {
                            when {
                                novaSenha.isBlank() || confirmarSenha.isBlank() -> {
                                    errorMessage = "Preencha todos os campos"
                                    return@Button
                                }
                                novaSenha.length < 6 -> {
                                    errorMessage = "A senha deve ter no mínimo 6 caracteres"
                                    return@Button
                                }
                                novaSenha != confirmarSenha -> {
                                    errorMessage = "As senhas não coincidem"
                                    return@Button
                                }
                                else -> {
                                    isLoading = true
                                    errorMessage = null
                                    successMessage = null

                                    coroutineScope.launch(Dispatchers.IO) {
                                        try {
                                            // Aqui você deve implementar a chamada à API para redefinir a senha
                                            // Por enquanto, vou simular sucesso
                                            kotlinx.coroutines.delay(1500)

                                            withContext(Dispatchers.Main) {
                                                isLoading = false
                                                successMessage = "Senha redefinida com sucesso!"
                                                kotlinx.coroutines.delay(1500)
                                                navController.navigate("tela_login") {
                                                    popUpTo(0) { inclusive = true }
                                                }
                                            }
                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) {
                                                isLoading = false
                                                errorMessage = "Erro ao redefinir senha: ${e.message}"
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E3A8A)
                        ),
                        shape = RoundedCornerShape(25.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Text(
                                "Redefinir Senha",
                                fontSize = 16.ssp(),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

