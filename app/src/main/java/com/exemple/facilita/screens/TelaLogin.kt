package com.exemple.facilita.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import android.util.Log
import com.exemple.facilita.R
import com.exemple.facilita.model.Login
import com.exemple.facilita.model.LoginResponse
import com.exemple.facilita.model.Usuario
import com.exemple.facilita.service.RetrofitFactory
import com.exemple.facilita.sevice.UserService
import com.exemple.facilita.utils.TokenManager
import com.exemple.facilita.utils.sdp
import com.exemple.facilita.utils.ssp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.await
import kotlin.text.isNotBlank
import kotlin.text.take

@Composable
fun TelaLogin(navController: NavController) {
    val facilitaApi: UserService = remember { RetrofitFactory.userService }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var tentativaSenhaErrada by remember { mutableStateOf(0) }
    var loginType by remember { mutableStateOf("email") } // "email" ou "celular"
    var loginInput by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var senhaVisivel by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Animação do toggle
    val emailScale by animateFloatAsState(
        targetValue = if (loginType == "email") 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "emailScale"
    )

    val celularScale by animateFloatAsState(
        targetValue = if (loginType == "celular") 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "celularScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF444444))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Parte superior original (Logo e imagens) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(3f)
                    .background(Color(0xFF444444))
            ) {
                Image(
                    painter = painterResource(R.drawable.logotcc),
                    contentDescription = "Logo Facilita",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .height(90.sdp())
                        .width(120.sdp())
                        .padding(start = 24.sdp())
                )
                Image(
                    painter = painterResource(R.drawable.texturalateral),
                    contentDescription = "Textura lateral",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .height(180.sdp())
                        .width(180.sdp())
                )
                Image(
                    painter = painterResource(R.drawable.icongeladeiralogin),
                    contentDescription = "Ícone da geladeira",
                    modifier = Modifier
                        .height(320.sdp())
                        .width(280.sdp())
                        .padding(top = 110.sdp(), start = 24.sdp())
                )
            }

            // --- Card branco moderno ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(4f)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(topStart = 28.sdp(), topEnd = 28.sdp())
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.sdp()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Fazer login",
                        fontSize = 22.ssp(),
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 20.sdp())
                    )

                    // Toggle Email/Celular - Moderno com animação
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.sdp())
                            .clip(RoundedCornerShape(25.sdp()))
                            .background(Color(0xFFF5F5F5))
                            .padding(3.sdp()),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Opção Email
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .scale(emailScale)
                                .clip(RoundedCornerShape(22.sdp()))
                                .then(
                                    if (loginType == "email")
                                        Modifier.background(
                                            brush = Brush.horizontalGradient(
                                                listOf(Color(0xFF019D31), Color(0xFF06C755))
                                            )
                                        )
                                    else Modifier.background(Color.Transparent)
                                )
                                .clickable {
                                    loginType = "email"
                                    loginInput = ""
                                    errorMessage = null
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email",
                                    tint = if (loginType == "email") Color.White else Color.Gray,
                                    modifier = Modifier.size(18.sdp())
                                )
                                Spacer(modifier = Modifier.width(7.sdp()))
                                Text(
                                    text = "Email",
                                    fontSize = 15.ssp(),
                                    fontWeight = if (loginType == "email") FontWeight.Bold else FontWeight.Normal,
                                    color = if (loginType == "email") Color.White else Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(7.sdp()))

                        // Opção Celular
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .scale(celularScale)
                                .clip(RoundedCornerShape(22.sdp()))
                                .then(
                                    if (loginType == "celular")
                                        Modifier.background(
                                            brush = Brush.horizontalGradient(
                                                listOf(Color(0xFF019D31), Color(0xFF06C755))
                                            )
                                        )
                                    else Modifier.background(Color.Transparent)
                                )
                                .clickable {
                                    loginType = "celular"
                                    loginInput = ""
                                    errorMessage = null
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Celular",
                                    tint = if (loginType == "celular") Color.White else Color.Gray,
                                    modifier = Modifier.size(18.sdp())
                                )
                                Spacer(modifier = Modifier.width(7.sdp()))
                                Text(
                                    text = "Celular",
                                    fontSize = 15.ssp(),
                                    fontWeight = if (loginType == "celular") FontWeight.Bold else FontWeight.Normal,
                                    color = if (loginType == "celular") Color.White else Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.sdp()))

                    // Campo de Login (Email ou Celular)
                    OutlinedTextField(
                        value = loginInput,
                        onValueChange = { loginInput = it; errorMessage = null },
                        label = { Text(if (loginType == "email") "Email" else "Celular") },
                        placeholder = {
                            Text(if (loginType == "email") "seuemail@gmail.com" else "(00) 00000-0000")
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (loginType == "email") Icons.Default.Email else Icons.Default.Phone,
                                contentDescription = null,
                                tint = Color(0xFF019D31)
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (loginType == "email") KeyboardType.Email else KeyboardType.Phone
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(11.sdp()),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF019D31),
                            focusedLabelColor = Color(0xFF019D31),
                            cursorColor = Color(0xFF019D31)
                        )
                    )

                    Spacer(modifier = Modifier.height(14.sdp()))

                    // Campo Senha
                    OutlinedTextField(
                        value = senha,
                        onValueChange = { senha = it; errorMessage = null },
                        label = { Text("Senha") },
                        placeholder = { Text("Digite sua senha") },
                        visualTransformation = if (senhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFF019D31)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { senhaVisivel = !senhaVisivel }) {
                                Icon(
                                    imageVector = if (senhaVisivel) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = "Mostrar/Ocultar senha",
                                    tint = Color.Gray
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(11.sdp()),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF019D31),
                            focusedLabelColor = Color(0xFF019D31),
                            cursorColor = Color(0xFF019D31)
                        )
                    )

                    // Mensagem de erro
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(7.sdp()))
                        Text(
                            text = errorMessage!!,
                            color = Color.Red,
                            fontSize = 13.ssp(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(18.sdp()))

                    // Botão Entrar
                    Button(
                        onClick = {
                            if (loginInput.isNotBlank() && senha.isNotBlank()) {
                                isLoading = true
                                coroutineScope.launch(Dispatchers.IO) {
                                    try {
                                        val login = Login(
                                            login = loginInput,
                                            senha = senha
                                        )

                                        val response: LoginResponse = facilitaApi.loginUser(login).await()


                                        withContext(Dispatchers.Main) {
                                            isLoading = false
                                            tentativaSenhaErrada = 0

                                            val token = response.token
                                            val tipoConta = response.usuario.tipo_conta
                                            val userId = response.usuario.id
                                            val nomeUsuario = response.usuario.nome

                                            Log.d(
                                                "LOGIN_DEBUG",
                                                "Token recebido: ${token.take(50)}..."
                                            )
                                            Log.d("LOGIN_DEBUG", "Tipo de conta: $tipoConta")
                                            Log.d("LOGIN_DEBUG", "User ID: $userId")
                                            Log.d("LOGIN_DEBUG", "Nome do usuário: $nomeUsuario")

                                            TokenManager.salvarToken(
                                                context,
                                                token,
                                                tipoConta,
                                                userId,
                                                nomeUsuario
                                            )

                                            val tokenSalvo = TokenManager.obterToken(context)
                                            val tipoContaSalvo =
                                                TokenManager.obterTipoConta(context)
                                            val nomeSalvo = TokenManager.obterNomeUsuario(context)
                                            Log.d(
                                                "LOGIN_DEBUG",
                                                "Token salvo verificado: ${tokenSalvo?.take(50)}..."
                                            )
                                            Log.d(
                                                "LOGIN_DEBUG",
                                                "Tipo conta salvo: $tipoContaSalvo"
                                            )
                                            Log.d("LOGIN_DEBUG", "Nome salvo: $nomeSalvo")

                                            navController.navigate("tela_inicio_prestador")
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            isLoading = false
                                            tentativaSenhaErrada++
                                            errorMessage = "Login ou senha incorretos"
                                            Log.e("LOGIN_ERROR", "Erro no login", e)
                                        }
                                    }
                                }
                            } else {
                                errorMessage = "Preencha todos os campos"
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.sdp()),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(),
                        enabled = !isLoading
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
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(22.sdp())
                                )
                            } else {
                                Text(
                                    text = "Entrar",
                                    fontSize = 17.ssp(),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Link Esqueceu a senha
                    if (tentativaSenhaErrada >= 2) {
                        Spacer(modifier = Modifier.height(14.sdp()))
                        Text(
                            text = "Esqueceu a senha?",
                            color = Color(0xFF019D31),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.ssp(),
                            modifier = Modifier.clickable {
                                navController.navigate("tela_recuperar_senha")
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.sdp()))

                    // Link Cadastro
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Não possui uma conta? ",
                            fontSize = 13.ssp(),
                            color = Color.Gray
                        )
                        Text(
                            text = "Cadastre-se",
                            fontSize = 13.ssp(),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF019D31),
                            modifier = Modifier.clickable {
                                navController.navigate("tela_cadastro")
                            }
                        )
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TelaLoginPreview() {
    val navController = rememberNavController()
    TelaLogin(navController = navController)
}
