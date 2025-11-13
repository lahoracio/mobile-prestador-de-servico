package com.exemple.facilita.screens

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.exemple.facilita.model.DocumentoRequest
import com.exemple.facilita.service.RetrofitFactory
import com.exemple.facilita.utils.TokenManager
import com.exemple.facilita.viewmodel.PerfilViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaDocumentos(
    navController: NavController,
    prestadorViewModel: com.exemple.facilita.viewmodel.PrestadorViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Estados para rastrear o que foi cadastrado
    var rgCadastrado by remember { mutableStateOf(false) }
    var cpfCadastrado by remember { mutableStateOf(false) }

    var tipoDocumento by remember { mutableStateOf("") }
    var numeroDocumento by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var mostrarDialogSelecao by remember { mutableStateOf(false) }

    var visible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 600)
    )

    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    fun cadastrarDocumento() {
        if (numeroDocumento.isBlank()) {
            Toast.makeText(context, "Digite o número do documento", Toast.LENGTH_SHORT).show()
            return
        }

        if (tipoDocumento == "CPF" && numeroDocumento.replace("[^0-9]".toRegex(), "").length != 11) {
            Toast.makeText(context, "CPF deve ter 11 dígitos", Toast.LENGTH_SHORT).show()
            return
        }

        if (tipoDocumento == "RG" && numeroDocumento.replace("[^0-9]".toRegex(), "").length < 7) {
            Toast.makeText(context, "RG inválido", Toast.LENGTH_SHORT).show()
            return
        }

        val token = TokenManager.obterToken(context)
        if (token.isNullOrBlank()) {
            Toast.makeText(context, "Token não encontrado. Faça login novamente", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch(Dispatchers.IO) {
            try {
                isLoading = true
                val documentoLimpo = numeroDocumento.replace("[^0-9]".toRegex(), "")

                val request = DocumentoRequest(
                    tipo_documento = tipoDocumento,
                    valor = documentoLimpo
                )

                val response = RetrofitFactory.getDocumentoService()
                    .cadastrarDocumento("Bearer $token", request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "✅ Documento cadastrado com sucesso!", Toast.LENGTH_SHORT).show()

                        // Marcar como cadastrado
                        if (tipoDocumento == "CPF") {
                            cpfCadastrado = true
                        } else if (tipoDocumento == "RG") {
                            rgCadastrado = true
                        }

                        // Limpar campos
                        numeroDocumento = ""
                        tipoDocumento = ""
                        mostrarDialogSelecao = false
                    } else {
                        Toast.makeText(context, "Erro ao cadastrar: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }

    fun finalizarCadastro() {
        prestadorViewModel.marcarDocumentoCadastrado()
        // Apenas volta para a tela anterior (TelaCompletarPerfilPrestador)
        navController.popBackStack()
    }

    // Verificar se CPF ou RG foi cadastrado
    val documentoCadastradoLocal = rgCadastrado || cpfCadastrado

    Scaffold(
        containerColor = Color(0xFFE6E6E6),
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color(0xFF015B2B))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFFE6E6E6)).padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp).scale(scale).alpha(alpha),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier.size(100.dp).shadow(8.dp, CircleShape).background(
                        brush = Brush.radialGradient(colors = listOf(Color(0xFF019D31), Color(0xFF015B2B))),
                        shape = CircleShape
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Badge, contentDescription = null, tint = Color.White, modifier = Modifier.size(50.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "Cadastre seu Documento", fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color(0xFF015B2B), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Escolha entre CPF ou RG", fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(32.dp))

                // Apenas 2 cards (CPF e RG) lado a lado
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    CardDocumento(
                        titulo = "CPF",
                        descricao = "Cadastro Pessoa Física",
                        icon = Icons.Default.Person,
                        isCadastrado = cpfCadastrado,
                        onClick = {
                            if (!cpfCadastrado && !rgCadastrado) {
                                tipoDocumento = "CPF"
                                mostrarDialogSelecao = true
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    CardDocumento(
                        titulo = "RG",
                        descricao = "Registro Geral",
                        icon = Icons.Default.CreditCard,
                        isCadastrado = rgCadastrado,
                        onClick = {
                            if (!rgCadastrado && !cpfCadastrado) {
                                tipoDocumento = "RG"
                                mostrarDialogSelecao = true
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Botão Finalizar (só aparece quando CPF ou RG está cadastrado)
                if (documentoCadastradoLocal) {
                    Button(
                        onClick = { finalizarCadastro() },
                        modifier = Modifier.fillMaxWidth().height(56.dp).shadow(8.dp, RoundedCornerShape(28.dp)),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues()
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(
                                brush = Brush.horizontalGradient(colors = listOf(Color(0xFF015B2B), Color(0xFF00B94A))),
                                shape = RoundedCornerShape(28.dp)
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Continuar", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Dialog para inserir número do documento
        if (mostrarDialogSelecao) {
            AlertDialog(
                onDismissRequest = { mostrarDialogSelecao = false },
                containerColor = Color.White,
                title = {
                    Text("Cadastrar $tipoDocumento", color = Color(0xFF015B2B), fontWeight = FontWeight.Bold, fontSize = 22.sp)
                },
                text = {
                    Column {
                        Text("Digite o número do $tipoDocumento", color = Color.Gray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = numeroDocumento,
                            onValueChange = { numeroDocumento = it },
                            label = { Text("Número", color = Color.Gray) },
                            placeholder = { Text(if (tipoDocumento == "CPF") "000.000.000-00" else "00.000.000-0", color = Color.LightGray) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF019D31),
                                unfocusedBorderColor = Color.LightGray,
                                focusedLabelColor = Color(0xFF019D31),
                                cursorColor = Color(0xFF019D31),
                                focusedTextColor = Color(0xFF015B2B),
                                unfocusedTextColor = Color.DarkGray,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (tipoDocumento == "CPF") "11 dígitos" else "Mínimo 7 dígitos",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { cadastrarDocumento() },
                        enabled = !isLoading && numeroDocumento.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(),
                        modifier = Modifier.height(48.dp).shadow(8.dp, RoundedCornerShape(24.dp))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(
                                brush = if (isLoading || numeroDocumento.isEmpty()) {
                                    Brush.horizontalGradient(colors = listOf(Color.LightGray, Color.LightGray))
                                } else {
                                    Brush.horizontalGradient(colors = listOf(Color(0xFF015B2B), Color(0xFF00B94A)))
                                },
                                shape = RoundedCornerShape(24.dp)
                            ).padding(horizontal = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Cadastrar", color = if (numeroDocumento.isEmpty()) Color.Gray else Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        mostrarDialogSelecao = false
                        numeroDocumento = ""
                        tipoDocumento = ""
                    }) {
                        Text("Cancelar", color = Color.Gray)
                    }
                }
            )
        }
    }
}

@Composable
fun CardDocumento(
    titulo: String,
    descricao: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isCadastrado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isCadastrado) 1.0f else 0.98f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
    )

    Card(
        modifier = modifier
            .height(140.dp)
            .scale(scale)
            .clickable(enabled = !isCadastrado) { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCadastrado) Color(0xFFE8F5E9) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCadastrado) 6.dp else 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().then(
                if (isCadastrado) {
                    Modifier.border(
                        width = 2.dp,
                        brush = Brush.linearGradient(colors = listOf(Color(0xFF019D31), Color(0xFF00B94A))),
                        shape = RoundedCornerShape(20.dp)
                    )
                } else Modifier
            )
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.size(50.dp).background(
                        brush = if (isCadastrado) {
                            Brush.radialGradient(colors = listOf(Color(0xFF019D31).copy(alpha = 0.3f), Color(0xFF00B94A).copy(alpha = 0.1f)))
                        } else {
                            Brush.radialGradient(colors = listOf(Color(0xFFE0E0E0).copy(alpha = 0.5f), Color.Transparent))
                        },
                        shape = CircleShape
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isCadastrado) Color(0xFF019D31) else Color.Gray,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = titulo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (isCadastrado) Color(0xFF015B2B) else Color.DarkGray
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = descricao,
                    fontSize = 10.sp,
                    color = if (isCadastrado) Color(0xFF019D31) else Color.Gray,
                    textAlign = TextAlign.Center
                )

                if (isCadastrado) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Cadastrado",
                            tint = Color(0xFF00B94A),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Cadastrado",
                            fontSize = 11.sp,
                            color = Color(0xFF00B94A),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

