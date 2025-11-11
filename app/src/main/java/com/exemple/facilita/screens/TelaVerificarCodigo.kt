package com.exemple.facilita.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import com.exemple.facilita.model.VerificarCodigoRequest
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
fun TelaVerificarCodigo(
    navController: NavController,
    emailOuTelefone: String,
    tipo: String // "email" ou "telefone"
) {
    val facilitaApi: UserService = remember { RetrofitFactory.userService }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var codigo by remember { mutableStateOf("") }
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
                text = "Verificar Código",
                fontSize = 28.ssp(),
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Digite o código de 6 dígitos enviado para\n$emailOuTelefone",
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
                    // Campo de código
                    OutlinedTextField(
                        value = codigo,
                        onValueChange = {
                            if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                codigo = it
                                errorMessage = null
                            }
                        },
                        label = { Text("Código de Verificação") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

                    // Botão Verificar
                    Button(
                        onClick = {
                            if (codigo.length != 6) {
                                errorMessage = "O código deve ter 6 dígitos"
                                return@Button
                            }

                            isLoading = true
                            errorMessage = null
                            successMessage = null

                            coroutineScope.launch(Dispatchers.IO) {
                                try {
                                    val request = VerificarCodigoRequest(
                                        codigo = codigo
                                    )

                                    val response = facilitaApi.verificarCodigo(request).await()

                                    withContext(Dispatchers.Main) {
                                        isLoading = false
                                        successMessage = response.message
                                        // Navegar para tela de redefinir senha
                                        kotlinx.coroutines.delay(1500)
                                        navController.navigate("tela_redefinir_senha/${response.dados.usuario_id}") {
                                            popUpTo("tela_recuperar_senha") { inclusive = true }
                                        }
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        isLoading = false
                                        errorMessage = "Código inválido ou expirado"
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
                                "Verificar Código",
                                fontSize = 16.ssp(),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Link para reenviar código
                    Text(
                        text = "Não recebeu o código? Reenviar",
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

