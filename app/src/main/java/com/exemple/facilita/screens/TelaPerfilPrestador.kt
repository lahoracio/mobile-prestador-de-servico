package com.exemple.facilita.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.exemple.facilita.R
import com.exemple.facilita.components.BottomNavBar
import com.exemple.facilita.viewmodel.PerfilPrestadorViewModel
import com.exemple.facilita.model.PerfilPrestadorData

@Composable
fun TelaPerfilPrestador(navController: NavController) {
    val viewModel: PerfilPrestadorViewModel = viewModel()
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val isUpdating by viewModel.isUpdating.collectAsState()
    var notificacoesAtivas by remember { mutableStateOf(false) }

    // Estados para diálogos de edição
    var showEditDialog by remember { mutableStateOf(false) }
    var editField by remember { mutableStateOf("") }
    var editValue by remember { mutableStateOf("") }
    var editTitle by remember { mutableStateOf("") }

    // Mensagens de feedback
    var showMessage by remember { mutableStateOf(false) }
    var messageText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    // Carregar perfil quando a tela é aberta
    LaunchedEffect(Unit) {
        viewModel.carregarPerfil(context)
    }

    // Mostrar mensagens temporárias
    LaunchedEffect(showMessage) {
        if (showMessage) {
            kotlinx.coroutines.delay(3000)
            showMessage = false
        }
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController) },
        snackbarHost = {
            if (showMessage) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = if (isError) Color.Red else Color(0xFF00A651),
                    contentColor = Color.White
                ) {
                    Text(messageText)
                }
            }
        }
    ) { innerPadding ->
        // Diálogo de edição
        if (showEditDialog) {
            EditProfileDialog(
                title = editTitle,
                value = editValue,
                field = editField,
                onDismiss = { showEditDialog = false },
                onConfirm = { newValue ->
                    // Atualizar perfil
                    when (editField) {
                        "email" -> {
                            viewModel.atualizarPerfil(
                                context = context,
                                email = newValue,
                                onSuccess = {
                                    showEditDialog = false
                                    messageText = "E-mail atualizado com sucesso!"
                                    isError = false
                                    showMessage = true
                                    viewModel.carregarPerfil(context) // Recarregar perfil
                                },
                                onError = { error ->
                                    messageText = error
                                    isError = true
                                    showMessage = true
                                }
                            )
                        }
                        "telefone" -> {
                            viewModel.atualizarPerfil(
                                context = context,
                                telefone = newValue,
                                onSuccess = {
                                    showEditDialog = false
                                    messageText = "Telefone atualizado com sucesso!"
                                    isError = false
                                    showMessage = true
                                    viewModel.carregarPerfil(context)
                                },
                                onError = { error ->
                                    messageText = error
                                    isError = true
                                    showMessage = true
                                }
                            )
                        }
                        "nome" -> {
                            viewModel.atualizarPerfil(
                                context = context,
                                nome = newValue,
                                onSuccess = {
                                    showEditDialog = false
                                    messageText = "Nome atualizado com sucesso!"
                                    isError = false
                                    showMessage = true
                                    viewModel.carregarPerfil(context)
                                },
                                onError = { error ->
                                    messageText = error
                                    isError = true
                                    showMessage = true
                                }
                            )
                        }
                    }
                }
            )
        }

        when (uiState) {
            is PerfilPrestadorViewModel.PerfilUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF00A651))
                }
            }

            is PerfilPrestadorViewModel.PerfilUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = (uiState as PerfilPrestadorViewModel.PerfilUiState.Error).message,
                            color = Color.Red,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { viewModel.carregarPerfil(context) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A651))
                        ) {
                            Text("Tentar Novamente")
                        }
                    }
                }
            }

            is PerfilPrestadorViewModel.PerfilUiState.Success -> {
                val perfil = (uiState as PerfilPrestadorViewModel.PerfilUiState.Success).perfil
                PerfilPrestadorContent(
                    perfil = perfil,
                    navController = navController,
                    notificacoesAtivas = notificacoesAtivas,
                    onNotificacoesChange = { notificacoesAtivas = it },
                    onEditField = { field, title, currentValue ->
                        editField = field
                        editTitle = title
                        editValue = currentValue
                        showEditDialog = true
                    }
                )
            }

            else -> {
                // Estado Idle - não faz nada, o LaunchedEffect vai carregar
            }
        }
    }
}

@Composable
fun PerfilPrestadorContent(
    perfil: PerfilPrestadorData,
    navController: NavController,
    notificacoesAtivas: Boolean,
    onNotificacoesChange: (Boolean) -> Unit,
    onEditField: (field: String, title: String, currentValue: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .background(Color(0xFFF5F5F5)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Perfil",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Image(
                painter = painterResource(id = R.drawable.foto_perfil),
                contentDescription = "Foto de perfil",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Adicionar foto",
                tint = Color(0xFF00A651),
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(4.dp)
                    .align(Alignment.BottomEnd)
            )
        }

        Spacer(modifier = Modifier.height(25.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Informações do Perfil",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.Black,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                // Nome
                PerfilInfoItem(
                    icon = Icons.Default.Person,
                    label = perfil.nome,
                    onEdit = {
                        onEditField("nome", "Editar Nome", perfil.nome)
                    }
                )

                // Localização (primeira localização se existir)
                perfil.dadosPrestador?.localizacoes?.firstOrNull()?.let { loc ->
                    PerfilInfoItem(
                        icon = Icons.Default.LocationOn,
                        label = "${loc.cidade} - ${loc.bairro}",
                        onEdit = null // Localização não editável por enquanto
                    )
                }

                // Email
                PerfilInfoItem(
                    icon = Icons.Default.Email,
                    label = perfil.email,
                    onEdit = {
                        onEditField("email", "Editar E-mail", perfil.email)
                    }
                )

                // Telefone
                PerfilInfoItem(
                    icon = Icons.Default.Phone,
                    label = perfil.telefone,
                    onEdit = {
                        onEditField("telefone", "Editar Telefone", perfil.telefone)
                    }
                )

                // Documentos
                perfil.dadosPrestador?.let { dados ->
                    PerfilInfoItem(
                        icon = Icons.Default.ContactPage,
                        label = "Documentos Registrados (${dados.documentos.size})",
                        onEdit = {}
                    )
                }

                // Status Ativo/Inativo
                perfil.dadosPrestador?.let { dados ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (dados.ativo) Color(0xFF00A651) else Color.Gray
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (dados.ativo) "Conta Ativa" else "Conta Inativa",
                                fontSize = 15.sp,
                                color = if (dados.ativo) Color(0xFF00A651) else Color.Gray
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(25.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Text(
                    text = "Outras Configurações",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.Black,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Alterar senha",
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = "Alterar Senha", fontSize = 15.sp)
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Ativar notificações",
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = "Ativar Notificações", fontSize = 15.sp)
                    }
                    Switch(
                        checked = notificacoesAtivas,
                        onCheckedChange = onNotificacoesChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF00A651),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.Gray
                        )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate("tela_login") {
                                popUpTo("tela_home") { inclusive = true }
                            }
                        }
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Sair",
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Sair", fontSize = 15.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun EditProfileDialog(
    title: String,
    value: String,
    field: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var textValue by remember { mutableStateOf(value) }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = textValue,
                    onValueChange = {
                        textValue = it
                        errorMessage = "" // Limpar erro ao editar
                    },
                    label = {
                        Text(
                            when (field) {
                                "nome" -> "Nome completo"
                                "email" -> "E-mail"
                                "telefone" -> "Telefone"
                                else -> "Valor"
                            }
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = when (field) {
                            "email" -> KeyboardType.Email
                            "telefone" -> KeyboardType.Phone
                            else -> KeyboardType.Text
                        },
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00A651),
                        focusedLabelColor = Color(0xFF00A651)
                    )
                )

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Validação básica
                    when (field) {
                        "email" -> {
                            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(textValue).matches()) {
                                errorMessage = "E-mail inválido"
                                return@Button
                            }
                        }
                        "telefone" -> {
                            if (textValue.length < 10) {
                                errorMessage = "Telefone inválido"
                                return@Button
                            }
                        }
                        "nome" -> {
                            if (textValue.trim().length < 3) {
                                errorMessage = "Nome deve ter no mínimo 3 caracteres"
                                return@Button
                            }
                        }
                    }

                    onConfirm(textValue.trim())
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00A651)
                )
            ) {
                Text("Salvar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.Gray)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun PerfilInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onEdit: (() -> Unit)? // agora pode ser nulo
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = label, fontSize = 15.sp, color = Color.Black)
        }

        // Exibe o ícone de editar apenas se tiver função de edição
        if (onEdit != null) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Editar",
                tint = Color.Gray,
                modifier = Modifier.clickable { onEdit() }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTelaPerfilPrestador() {
    TelaPerfilPrestador(navController = rememberNavController())
}
