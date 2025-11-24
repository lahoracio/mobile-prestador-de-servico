package com.exemple.facilita.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.exemple.facilita.R
import com.exemple.facilita.components.BottomNavBar
import com.exemple.facilita.components.EditarCampoDialog
import com.exemple.facilita.utils.TokenManager
import com.exemple.facilita.viewmodel.PerfilPrestadorViewModel

@Composable
fun TelaPerfilPrestador(
    navController: NavController,
    viewModel: PerfilPrestadorViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val isUpdating by viewModel.isUpdating.collectAsState()

    var notificacoesAtivas by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingField by remember { mutableStateOf<EditingField?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var hasLoadedOnce by remember { mutableStateOf(false) }

    // Carrega o perfil quando a tela é aberta
    LaunchedEffect(Unit) {
        if (!hasLoadedOnce) {
            hasLoadedOnce = true
            try {
                android.util.Log.d("TelaPerfilPrestador", "Iniciando carregamento do perfil...")
                kotlinx.coroutines.delay(100) // Pequeno delay para UI estabilizar
                viewModel.carregarPerfil(context)
            } catch (e: Exception) {
                android.util.Log.e("TelaPerfilPrestador", "Erro crítico ao carregar perfil", e)
                e.printStackTrace()
                errorMessage = "Erro ao inicializar: ${e.message}"
            }
        }
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController) },
        snackbarHost = {
            // Mostra mensagens de erro ou sucesso
            errorMessage?.let { msg ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { errorMessage = null }) {
                            Text("OK", color = Color.White)
                        }
                    }
                ) {
                    Text(msg)
                }
            }
            successMessage?.let { msg ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = Color(0xFF00A651),
                    action = {
                        TextButton(onClick = { successMessage = null }) {
                            Text("OK", color = Color.White)
                        }
                    }
                ) {
                    Text(msg, color = Color.White)
                }
            }
        }
    ) { innerPadding ->

        when (val state = uiState) {
            is PerfilPrestadorViewModel.PerfilUiState.Idle,
            is PerfilPrestadorViewModel.PerfilUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF00A651))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Carregando perfil...", color = Color.Gray)
                    }
                }
            }

            is PerfilPrestadorViewModel.PerfilUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = state.message,
                        color = Color.Red,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.carregarPerfil(context) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00A651)
                        )
                    ) {
                        Text("Tentar Novamente")
                    }
                }
            }

            is PerfilPrestadorViewModel.PerfilUiState.Success -> {
                val perfil = state.perfil

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
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

                            // Nome (sem editar)
                            PerfilInfoItem(
                                icon = Icons.Default.Person,
                                label = perfil.nome ?: "Não informado",
                                onEdit = null
                            )

                            // Localização (editável)
                            val localizacao = buildString {
                                perfil.prestador?.cidade?.let { append(it) }
                                if (perfil.prestador?.cidade != null && perfil.prestador.estado != null) {
                                    append("/")
                                }
                                perfil.prestador?.estado?.let { append(it) }
                            }.ifEmpty { "Não informado" }

                            PerfilInfoItem(
                                icon = Icons.Default.LocationOn,
                                label = localizacao,
                                onEdit = {
                                    editingField = EditingField.Localizacao(
                                        cidade = perfil.prestador?.cidade ?: "",
                                        estado = perfil.prestador?.estado ?: ""
                                    )
                                    showEditDialog = true
                                }
                            )

                            // Email
                            PerfilInfoItem(
                                icon = Icons.Default.Email,
                                label = perfil.email ?: "Não informado",
                                onEdit = {
                                    editingField = EditingField.Email(perfil.email ?: "")
                                    showEditDialog = true
                                }
                            )

                            // Telefone
                            PerfilInfoItem(
                                icon = Icons.Default.Phone,
                                label = perfil.celular ?: "Não informado",
                                onEdit = {
                                    editingField = EditingField.Telefone(perfil.celular ?: "")
                                    showEditDialog = true
                                }
                            )

                            // Endereço
                            PerfilInfoItem(
                                icon = Icons.Default.Home,
                                label = perfil.prestador?.endereco ?: "Não informado",
                                onEdit = {
                                    editingField = EditingField.Endereco(perfil.prestador?.endereco ?: "")
                                    showEditDialog = true
                                }
                            )
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
                                    onCheckedChange = { notificacoesAtivas = it },
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
                                        TokenManager.limparToken(context)
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
                }
            }
        }
    }

    // Dialog de edição
    if (showEditDialog && editingField != null) {
        when (val field = editingField) {
            is EditingField.Email -> {
                EditarCampoDialog(
                    titulo = "Editar Email",
                    valorAtual = field.valor,
                    onDismiss = { showEditDialog = false },
                    onConfirmar = { novoValor ->
                        viewModel.atualizarPerfil(
                            context = context,
                            email = novoValor,
                            onSuccess = {
                                successMessage = "Email atualizado com sucesso!"
                                showEditDialog = false
                            },
                            onError = { msg ->
                                errorMessage = msg
                                showEditDialog = false
                            }
                        )
                    },
                    placeholder = "Digite o novo email",
                    isEmail = true
                )
            }
            is EditingField.Telefone -> {
                EditarCampoDialog(
                    titulo = "Editar Telefone",
                    valorAtual = field.valor,
                    onDismiss = { showEditDialog = false },
                    onConfirmar = { novoValor ->
                        viewModel.atualizarPerfil(
                            context = context,
                            celular = novoValor,
                            onSuccess = {
                                successMessage = "Telefone atualizado com sucesso!"
                                showEditDialog = false
                            },
                            onError = { msg ->
                                errorMessage = msg
                                showEditDialog = false
                            }
                        )
                    },
                    placeholder = "Digite o novo telefone",
                    isTelefone = true
                )
            }
            is EditingField.Endereco -> {
                EditarCampoDialog(
                    titulo = "Editar Endereço",
                    valorAtual = field.valor,
                    onDismiss = { showEditDialog = false },
                    onConfirmar = { novoValor ->
                        viewModel.atualizarPerfil(
                            context = context,
                            endereco = novoValor,
                            onSuccess = {
                                successMessage = "Endereço atualizado com sucesso!"
                                showEditDialog = false
                            },
                            onError = { msg ->
                                errorMessage = msg
                                showEditDialog = false
                            }
                        )
                    },
                    placeholder = "Digite o novo endereço"
                )
            }
            is EditingField.Localizacao -> {
                // Para localização, você pode criar um dialog mais complexo
                // Por enquanto, vamos usar um simples para cidade
                EditarCampoDialog(
                    titulo = "Editar Cidade/Estado",
                    valorAtual = "${field.cidade}/${field.estado}",
                    onDismiss = { showEditDialog = false },
                    onConfirmar = { novoValor ->
                        val partes = novoValor.split("/")
                        val cidade = partes.getOrNull(0)?.trim() ?: ""
                        val estado = partes.getOrNull(1)?.trim() ?: ""

                        viewModel.atualizarPerfil(
                            context = context,
                            cidade = cidade,
                            estado = estado,
                            onSuccess = {
                                successMessage = "Localização atualizada com sucesso!"
                                showEditDialog = false
                            },
                            onError = { msg ->
                                errorMessage = msg
                                showEditDialog = false
                            }
                        )
                    },
                    placeholder = "Ex: São Paulo/SP"
                )
            }
            null -> {}
        }
    }
}

// Sealed class para representar os campos editáveis
sealed class EditingField {
    data class Email(val valor: String) : EditingField()
    data class Telefone(val valor: String) : EditingField()
    data class Endereco(val valor: String) : EditingField()
    data class Localizacao(val cidade: String, val estado: String) : EditingField()
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
