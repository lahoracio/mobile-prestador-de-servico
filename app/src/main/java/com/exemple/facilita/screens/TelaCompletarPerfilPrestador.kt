package com.exemple.facilita.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.exemple.facilita.R
import com.exemple.facilita.viewmodel.PerfilViewModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode

@Composable
fun TelaCompletarPerfilPrestador(
    navController: NavController,
    perfilViewModel: PerfilViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onFinalizar: () -> Unit = {},
    onVoltar: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val prestadorViewModel: com.exemple.facilita.viewmodel.PrestadorViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

    var endereco by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }

    val documentosValidados by perfilViewModel.documentosValidados.collectAsState()
    val mensagem by prestadorViewModel.mensagem.collectAsState()
    val sucesso by prestadorViewModel.sucesso.collectAsState()
    val novoToken by prestadorViewModel.novoToken.collectAsState()
    val isLoading by prestadorViewModel.isLoading.collectAsState()

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
                endereco = place.address ?: ""
                // Capturar latitude e longitude
                place.latLng?.let { latLng ->
                    latitude = latLng.latitude
                    longitude = latLng.longitude
                    android.util.Log.d("COMPLETAR_PERFIL", "Localização capturada: [${latLng.latitude}, ${latLng.longitude}]")

                    // CRIAR PRESTADOR IMEDIATAMENTE após selecionar endereço
                    val token = com.exemple.facilita.utils.TokenManager.obterToken(context)
                    if (!token.isNullOrBlank()) {
                        android.util.Log.d("COMPLETAR_PERFIL", "Chamando API para criar prestador")
                        android.util.Log.d("COMPLETAR_PERFIL", "Endereço: $endereco")
                        prestadorViewModel.criarPrestador(token, latLng.latitude, latLng.longitude)
                    } else {
                        android.widget.Toast.makeText(context, "Token não encontrado. Faça login novamente", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Lista de documentos e suas rotas correspondentes
    val opcoesDocs = listOf(
        "CNH com EAR" to "tela_cnh",
        "Documentos" to "tela_documentos",
        "Informações do veículo" to "tela_tipo_veiculo"
    )

    // Atualiza o estado quando volta de uma tela de validação
    LaunchedEffect(navController.currentBackStackEntry) {
        // Você pode adicionar lógica aqui se necessário
    }

    Scaffold(containerColor = Color(0xFFE6E6E6)) { padding ->
        LazyColumn(
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
                    text = "Complete seu perfil",
                    fontSize = 16.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            // CAMPO ENDEREÇO com Google Places Autocomplete
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // Configurar campos que queremos do Places
                            val fields = listOf(
                                Place.Field.ID,
                                Place.Field.NAME,
                                Place.Field.ADDRESS,
                                Place.Field.LAT_LNG
                            )

                            // Criar intent do Autocomplete
                            val intent = Autocomplete
                                .IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                                .setCountry("BR") // Limitar ao Brasil
                                .build(context)

                            // Abrir o autocomplete
                            autocompleteLauncher.launch(intent)
                        }
                ) {
                    OutlinedTextField(
                        value = endereco,
                        onValueChange = { },
                        label = { Text("Endereço completo") },
                        placeholder = { Text("Clique para buscar endereço") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Localização",
                                tint = Color(0xFF019D31)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = Color.Black,
                            disabledBorderColor = Color.Gray,
                            disabledLeadingIconColor = Color(0xFF019D31),
                            disabledLabelColor = Color.Gray,
                            disabledPlaceholderColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Indicador de loading quando criando prestador
            if (isLoading && endereco.isNotBlank()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF019D31),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Criando prestador no sistema...",
                            fontSize = 14.sp,
                            color = Color(0xFF019D31),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Cadastre alguns documentos para a validação do seu serviço",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }

            // LISTA DE DOCUMENTOS
            items(opcoesDocs.size) { index ->
                val (titulo, rota) = opcoesDocs[index]
                val isValidado = documentosValidados.contains(titulo)

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate(rota) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isValidado) Color(0xFF00B94A)
                                        else Color(0xFFD9D9D9)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isValidado) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Validado",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = titulo,
                                fontSize = 15.sp,
                                color = if (isValidado) Color(0xFF00B94A) else Color.Black
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForwardIos,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Divider(color = Color(0xFFD9D9D9), thickness = 1.dp)
                }
            }

            // Espaço antes do botão
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Botão Finalizar
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(220.dp)
                            .height(48.dp)
                            .clickable {
                                // Validar se endereço foi preenchido (prestador criado)
                                if (endereco.isBlank()) {
                                    android.widget.Toast.makeText(
                                        context,
                                        "Por favor, selecione um endereço primeiro",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                    return@clickable
                                }

                                // Verificar se prestador foi criado (token atualizado)
                                val tipoConta = com.exemple.facilita.utils.TokenManager.obterTipoConta(context)
                                if (tipoConta != "PRESTADOR") {
                                    android.widget.Toast.makeText(
                                        context,
                                        "Aguarde a criação do prestador...",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                    return@clickable
                                }

                                // Navegar para tela inicial
                                android.util.Log.d("COMPLETAR_PERFIL", "Finalizando cadastro, navegando para tela inicial")
                                navController.navigate("tela_inicio_prestador") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF015B2B), Color(0xFF00B94A))
                                ),
                                shape = RoundedCornerShape(50)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Finalizar",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Espaço no final
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TelaCompletarPerfilPrestadorPreview() {
    val navController = rememberNavController()
    TelaCompletarPerfilPrestador(navController = navController)
}
