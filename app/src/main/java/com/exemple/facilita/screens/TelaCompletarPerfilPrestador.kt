package com.exemple.facilita.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.exemple.facilita.R
import com.exemple.facilita.viewmodel.PerfilViewModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class EnderecoInfo(
    val endereco: String,
    val logradouro: String,
    val numero: String,
    val bairro: String,
    val cidade: String,
    val cep: String,
    val latitude: Double,
    val longitude: Double,
    val idLocalizacao: Int? = null
)

@Composable
fun TelaCompletarPerfilPrestador(
    navController: NavController,
    perfilViewModel: PerfilViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    prestadorViewModel: com.exemple.facilita.viewmodel.PrestadorViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Estados dos endereços
    var enderecoMora by remember { mutableStateOf<EnderecoInfo?>(null) }
    var enderecoAtua by remember { mutableStateOf<EnderecoInfo?>(null) }
    var tipoEnderecoCadastro by remember { mutableStateOf<String?>(null) } // "mora" ou "atua"

    // Estados do fluxo
    val prestadorCriado by prestadorViewModel.prestadorCriado.collectAsState()
    val documentoCadastrado by prestadorViewModel.documentoCadastrado.collectAsState()
    val cnhCadastrada by prestadorViewModel.cnhCadastrada.collectAsState()
    val veiculoCadastrado by prestadorViewModel.veiculoCadastrado.collectAsState()
    val mensagem by prestadorViewModel.mensagem.collectAsState()
    val sucesso by prestadorViewModel.sucesso.collectAsState()
    val novoToken by prestadorViewModel.novoToken.collectAsState()
    val isLoading by prestadorViewModel.isLoading.collectAsState()

    // Estado de loading para finalizar cadastro
    var isFinalizando by remember { mutableStateOf(false) }

    // Estado do scroll para manter a posição ao voltar do Google Places
    val listState = rememberLazyListState()

    // Função para finalizar o cadastro
    fun finalizarCadastro() {
        scope.launch(Dispatchers.IO) {
            try {
                isFinalizando = true
                val token = com.exemple.facilita.utils.TokenManager.obterToken(context)

                if (token.isNullOrBlank()) {
                    withContext(Dispatchers.Main) {
                        android.widget.Toast.makeText(context, "Token não encontrado. Faça login novamente", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val response = com.exemple.facilita.service.RetrofitFactory.getPrestadorService()
                    .finalizarCadastro("Bearer $token")

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        android.widget.Toast.makeText(context, "✅ Cadastro finalizado com sucesso!", android.widget.Toast.LENGTH_SHORT).show()
                        kotlinx.coroutines.delay(800)
                        navController.navigate("tela_inicio_prestador") {
                            popUpTo(0) { inclusive = true }
                        }
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: "Erro desconhecido"
                        android.widget.Toast.makeText(context, "Erro ao finalizar: $errorMsg", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(context, "Erro: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isFinalizando = false
                }
            }
        }
    }

    // Observar sucesso para salvar token
    LaunchedEffect(sucesso, novoToken) {
        if (sucesso && !novoToken.isNullOrBlank()) {
            com.exemple.facilita.utils.TokenManager.salvarToken(context, novoToken!!, "PRESTADOR")
            android.util.Log.d("COMPLETAR_PERFIL", "Novo token salvo! Prestador criado no backend.")
            android.widget.Toast.makeText(context, "Prestador criado com sucesso!", android.widget.Toast.LENGTH_SHORT).show()
            prestadorViewModel.resetState()
        }
    }

    // Mostrar mensagens
    LaunchedEffect(mensagem) {
        mensagem?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    // Inicializar Google Places API
    LaunchedEffect(Unit) {
        if (!Places.isInitialized()) {
            Places.initialize(context, context.getString(R.string.google_maps_key))
        }
    }

    // Launcher para abrir o autocomplete do Google Places
    val autocompleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                val place = Autocomplete.getPlaceFromIntent(data)
                val enderecoCompleto = place.address ?: ""

                place.latLng?.let { latLng ->
                    // Parsear endereço (simplificado)
                    val partes = enderecoCompleto.split(",")
                    val logradouroNumero = partes.getOrNull(0)?.trim() ?: ""
                    val bairro = partes.getOrNull(1)?.trim() ?: ""
                    val cidadeEstado = partes.getOrNull(2)?.trim() ?: ""
                    val cep = partes.getOrNull(3)?.trim()?.replace("-", "") ?: "00000000"

                    val cidade = cidadeEstado.split("-").getOrNull(0)?.trim() ?: "São Paulo"
                    val logradouro = logradouroNumero.substringBeforeLast(" ", logradouroNumero)
                    val numero = logradouroNumero.substringAfterLast(" ", "S/N")

                    // Criar localização via API
                    prestadorViewModel.criarLocalizacao(
                        logradouro = logradouro,
                        numero = numero,
                        bairro = bairro,
                        cidade = cidade,
                        cep = cep,
                        latitude = latLng.latitude,
                        longitude = latLng.longitude
                    ) { idLocalizacao ->
                        val enderecoInfo = EnderecoInfo(
                            endereco = enderecoCompleto,
                            logradouro = logradouro,
                            numero = numero,
                            bairro = bairro,
                            cidade = cidade,
                            cep = cep,
                            latitude = latLng.latitude,
                            longitude = latLng.longitude,
                            idLocalizacao = idLocalizacao
                        )

                        when (tipoEnderecoCadastro) {
                            "mora" -> enderecoMora = enderecoInfo
                            "atua" -> enderecoAtua = enderecoInfo
                        }
                        tipoEnderecoCadastro = null
                    }
                }
            }
        }
    }


    Scaffold(containerColor = Color(0xFFE6E6E6)) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Espaço no topo
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // FOTO DE PERFIL
            item {
                Box(modifier = Modifier.size(130.dp), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE6E6E6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.icontiposervico),
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
            }

            // Textos de boas-vindas
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Bem vindo entregador",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (prestadorCriado) "Complete seus documentos" else "Complete seu perfil",
                    fontSize = 16.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ETAPA 1: CADASTRO DE ENDEREÇOS
            if (!prestadorCriado) {
                item {
                    Text(
                        text = "📍 Cadastre seus endereços",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF019D31)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Precisamos de 2 endereços: onde você mora e a região onde você atua",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Card Endereço onde mora
                item {
                    CardEndereco(
                        titulo = "Onde você mora",
                        endereco = enderecoMora,
                        onClick = {
                            tipoEnderecoCadastro = "mora"
                            val fields = listOf(
                                Place.Field.ID,
                                Place.Field.NAME,
                                Place.Field.ADDRESS,
                                Place.Field.LAT_LNG
                            )
                            val intent = Autocomplete
                                .IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                                .build(context)
                            autocompleteLauncher.launch(intent)
                        },
                        isLoading = isLoading && tipoEnderecoCadastro == "mora"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Card Endereço onde atua
                item {
                    CardEndereco(
                        titulo = "Região onde você atua",
                        endereco = enderecoAtua,
                        onClick = {
                            tipoEnderecoCadastro = "atua"
                            val fields = listOf(
                                Place.Field.ID,
                                Place.Field.NAME,
                                Place.Field.ADDRESS,
                                Place.Field.LAT_LNG
                            )
                            val intent = Autocomplete
                                .IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                                .build(context)
                            autocompleteLauncher.launch(intent)
                        },
                        isLoading = isLoading && tipoEnderecoCadastro == "atua"
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Botão confirmar endereços
                item {
                    Button(
                        onClick = {
                            if (enderecoMora == null || enderecoAtua == null) {
                                android.widget.Toast.makeText(context, "Por favor, cadastre ambos os endereços", android.widget.Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val id1 = enderecoMora?.idLocalizacao
                            val id2 = enderecoAtua?.idLocalizacao

                            if (id1 == null || id2 == null) {
                                android.widget.Toast.makeText(context, "Aguarde o cadastro dos endereços...", android.widget.Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val token = com.exemple.facilita.utils.TokenManager.obterToken(context)
                            if (!token.isNullOrBlank()) {
                                prestadorViewModel.criarPrestador(token, listOf(id1, id2))
                            } else {
                                android.widget.Toast.makeText(context, "Token não encontrado. Faça login novamente", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = enderecoMora != null && enderecoAtua != null && !isLoading,
                        modifier = Modifier.width(220.dp).height(48.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues()
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(
                                brush = Brush.horizontalGradient(listOf(Color(0xFF015B2B), Color(0xFF00B94A))),
                                shape = RoundedCornerShape(50)
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading && tipoEnderecoCadastro == null) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text(text = "Confirmar Endereços", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            // ETAPA 2: CADASTRO DE DOCUMENTOS (ORDEM SEQUENCIAL)
            if (prestadorCriado) {
                item {
                    Text(
                        text = "📄 Cadastre seus documentos",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF019D31)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Complete todos os documentos necessários",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 1. DOCUMENTOS (CPF ou RG) - Primeiro
                item {
                    CardDocumento(
                        titulo = "Documento (CPF ou RG)",
                        descricao = if (documentoCadastrado) "✓ Documento cadastrado" else "Cadastre seu CPF ou RG",
                        isValidado = documentoCadastrado,
                        onClick = {
                            if (!documentoCadastrado) {
                                navController.navigate("tela_documentos")
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // 2. CNH - Segundo
                item {
                    CardDocumento(
                        titulo = "CNH com EAR",
                        descricao = if (cnhCadastrada) "✓ CNH cadastrada" else "Cadastre sua CNH",
                        isValidado = cnhCadastrada,
                        onClick = {
                            if (!cnhCadastrada && documentoCadastrado) {
                                navController.navigate("tela_cnh")
                            } else if (!documentoCadastrado) {
                                android.widget.Toast.makeText(context, "Cadastre o documento (CPF/RG) primeiro", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // 3. VEÍCULO - Terceiro
                item {
                    CardDocumento(
                        titulo = "Informações do Veículo",
                        descricao = if (veiculoCadastrado) "✓ Veículo cadastrado" else "Cadastre seu veículo",
                        isValidado = veiculoCadastrado,
                        onClick = {
                            if (!veiculoCadastrado && documentoCadastrado && cnhCadastrada) {
                                navController.navigate("tela_tipo_veiculo")
                            } else if (!documentoCadastrado) {
                                android.widget.Toast.makeText(context, "Cadastre o documento (CPF/RG) primeiro", android.widget.Toast.LENGTH_SHORT).show()
                            } else if (!cnhCadastrada) {
                                android.widget.Toast.makeText(context, "Cadastre a CNH primeiro", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                // Botão Finalizar (só aparece quando todos documentos estão cadastrados)
                if (documentoCadastrado && cnhCadastrada && veiculoCadastrado) {
                    item {
                        Button(
                            onClick = { finalizarCadastro() },
                            modifier = Modifier
                                .width(220.dp)
                                .height(56.dp)
                                .shadow(8.dp, RoundedCornerShape(28.dp)),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(),
                            enabled = !isFinalizando
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(Color(0xFF015B2B), Color(0xFF00B94A))
                                        ),
                                        shape = RoundedCornerShape(28.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isFinalizando) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Finalizar Cadastro",
                                            color = Color.White,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun CardEndereco(
    titulo: String,
    endereco: EnderecoInfo?,
    onClick: () -> Unit,
    isLoading: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = !isLoading) { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (endereco != null) Color(0xFFE8F5E9) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (endereco != null) Icons.Default.CheckCircle else Icons.Default.LocationOn,
                contentDescription = null,
                tint = if (endereco != null) Color(0xFF019D31) else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                if (isLoading) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color(0xFF019D31)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Cadastrando...", fontSize = 12.sp, color = Color.Gray)
                    }
                } else if (endereco != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = endereco.endereco, fontSize = 12.sp, color = Color.Gray)
                } else {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Clique para adicionar", fontSize = 12.sp, color = Color.Gray)
                }
            }
            if (!isLoading) {
                Icon(
                    imageVector = if (endereco != null) Icons.Default.Edit else Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun CardDocumento(
    titulo: String,
    descricao: String,
    isValidado: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isValidado) Color(0xFFE8F5E9) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isValidado) Color(0xFF019D31) else Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isValidado) Icons.Default.CheckCircle else Icons.Default.Add,
                        contentDescription = null,
                        tint = if (isValidado) Color.White else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = titulo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isValidado) Color(0xFF019D31) else Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = descricao,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

