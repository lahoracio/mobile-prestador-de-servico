package com.exemple.facilita.screens

import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.exemple.facilita.R
import com.exemple.facilita.model.RecuperarSenhaRequest
import com.exemple.facilita.model.RecuperarSenhaTelefoneRequest
import com.exemple.facilita.service.RetrofitFactory
import com.exemple.facilita.sevice.UserService
import com.exemple.facilita.utils.sdp
import com.exemple.facilita.utils.ssp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaRecuperarSenha(navController: NavController) {
    val facilitaApi: UserService = remember { RetrofitFactory.userService }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var recuperarPor by remember { mutableStateOf("email") } // "email" ou "telefone"
    var emailOuTelefone by remember { mutableStateOf("") }
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
                text = "Recuperar Senha",
                fontSize = 28.ssp(),
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Informe seu e-mail ou telefone para recuperar sua senha",
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
                    // Toggle entre Email e Telefone
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { recuperarPor = "email" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (recuperarPor == "email") Color(0xFF1E3A8A) else Color.LightGray
                            ),
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("E-mail")
                        }

                        Button(
                            onClick = { recuperarPor = "telefone" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (recuperarPor == "telefone") Color(0xFF1E3A8A) else Color.LightGray
                            ),
                            modifier = Modifier.weight(1f).padding(start = 8.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Telefone")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Campo de entrada
                    OutlinedTextField(
                        value = emailOuTelefone,
                        onValueChange = {
                            emailOuTelefone = it
                            errorMessage = null
                        },
                        label = { Text(if (recuperarPor == "email") "E-mail" else "Telefone") },
                        leadingIcon = {
                            Icon(
                                if (recuperarPor == "email") Icons.Default.Email else Icons.Default.Phone,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1E3A8A),
                            unfocusedBorderColor = Color.Gray
                        )
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

                    // Botão Enviar
                    Button(
                        onClick = {
                            if (emailOuTelefone.isBlank()) {
                                errorMessage = "Por favor, preencha o campo"
                                return@Button
                            }

                            if (recuperarPor == "email") {
                                if (!Patterns.EMAIL_ADDRESS.matcher(emailOuTelefone).matches()) {
                                    errorMessage = "E-mail inválido"
                                    return@Button
                                }
                            } else {
                                val telefoneLimpo = emailOuTelefone.replace(Regex("[^0-9]"), "")
                                if (telefoneLimpo.length != 11) {
                                    errorMessage = "Telefone deve ter 11 dígitos (DDD + número)"
                                    return@Button
                                }
                            }

                            isLoading = true
                            errorMessage = null
                            successMessage = null

                            coroutineScope.launch(Dispatchers.IO) {
                                try {
                                    if (recuperarPor == "email") {
                                        val request = RecuperarSenhaRequest(email = emailOuTelefone)
                                        val response = facilitaApi.recuperarSenha(request).await()

                                        withContext(Dispatchers.Main) {
                                            isLoading = false
                                            successMessage = response.message
                                            // Navegar para tela de verificação de código
                                            navController.navigate("tela_verificar_codigo/${emailOuTelefone}/email")
                                        }
                                    } else {
                                        val request = RecuperarSenhaTelefoneRequest(telefone = emailOuTelefone)
                                        val response = facilitaApi.recuperarSenhaTelefone(request).await()

                                        withContext(Dispatchers.Main) {
                                            isLoading = false
                                            successMessage = response.message
                                            // Navegar para tela de verificação de código
                                            navController.navigate("tela_verificar_codigo/${emailOuTelefone}/telefone")
                                        }
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        isLoading = false
                                        errorMessage = "Erro ao enviar código: ${e.message}"
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
                                "Enviar Código",
                                fontSize = 16.ssp(),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Link voltar ao login
                    Text(
                        text = "Voltar ao Login",
                        color = Color(0xFF1E3A8A),
                        fontSize = 14.ssp(),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}

