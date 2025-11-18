package com.exemple.facilita.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.exemple.facilita.components.BottomNavBar
import com.exemple.facilita.model.ServicoDetalhe
import com.exemple.facilita.service.RetrofitFactory
import com.exemple.facilita.utils.TokenManager
import com.exemple.facilita.viewmodel.ServicoViewModel
import kotlinx.coroutines.delay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaServicos(
    navController: NavController,
    servicoViewModel: ServicoViewModel = viewModel()
) {
    val context = LocalContext.current
    var servicosEmAndamento by remember { mutableStateOf<List<ServicoDetalhe>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var animateContent by remember { mutableStateOf(false) }

    val token = TokenManager.obterTokenComBearer(context) ?: ""
    val primaryGreen = Color(0xFF019D31)
    val lightBg = Color(0xFFF8F9FA)
    val cardBg = Color.White
    val textPrimary = Color(0xFF212121)
    val textSecondary = Color(0xFF666666)

    // Animação de entrada
    LaunchedEffect(Unit) {
        delay(100)
        animateContent = true
    }

    // Buscar serviços em andamento
    LaunchedEffect(Unit) {
        fun buscarServicosEmAndamento() {
            val service = RetrofitFactory.getServicoService()
            service.getServicosEmAndamento(token).enqueue(object : Callback<com.exemple.facilita.service.ServicosResponse> {
                override fun onResponse(
                    call: Call<com.exemple.facilita.service.ServicosResponse>,
                    response: Response<com.exemple.facilita.service.ServicosResponse>
                ) {
                    if (response.isSuccessful) {
                        servicosEmAndamento = response.body()?.servicos ?: emptyList()
                    }
                    isLoading = false
                }

                override fun onFailure(call: Call<com.exemple.facilita.service.ServicosResponse>, t: Throwable) {
                    Toast.makeText(context, "Erro ao carregar serviços: ${t.message}", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
            })
        }

        buscarServicosEmAndamento()

        // Atualizar a cada 30 segundos
        while (true) {
            delay(30000)
            buscarServicosEmAndamento()
        }
    }

    Scaffold(
        containerColor = lightBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Meus Serviços",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryGreen,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = { BottomNavBar(navController) }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = primaryGreen,
                            strokeWidth = 3.dp
                        )
                        Text(
                            "Carregando serviços...",
                            color = textSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(vertical = 16.dp, horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header
                    item {
                        AnimatedVisibility(
                            visible = animateContent,
                            enter = slideInVertically(
                                initialOffsetY = { -50 },
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                            ) + fadeIn()
                        ) {
                            Column {
                                Text(
                                    "Serviços em Andamento",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                                Text(
                                    "${servicosEmAndamento.size} serviço(s) ativo(s)",
                                    fontSize = 14.sp,
                                    color = textSecondary
                                )
                            }
                        }
                    }

                    // Lista de serviços ou estado vazio
                    if (servicosEmAndamento.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 60.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Work,
                                        contentDescription = null,
                                        tint = textSecondary.copy(alpha = 0.5f),
                                        modifier = Modifier.size(80.dp)
                                    )
                                    Text(
                                        "Nenhum serviço em andamento",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = textPrimary,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        "Aceite novos serviços na tela inicial",
                                        fontSize = 14.sp,
                                        color = textSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        items(servicosEmAndamento, key = { it.id }) { servico ->
                            ServicoEmAndamentoCard(
                                servico = servico,
                                onClick = {
                                    navController.navigate("tela_detalhes_servico_aceito/${servico.id}")
                                },
                                primaryGreen = primaryGreen,
                                cardBg = cardBg,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ServicoEmAndamentoCard(
    servico: ServicoDetalhe,
    onClick: () -> Unit,
    primaryGreen: Color,
    cardBg: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header com status e valor
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(primaryGreen, CircleShape)
                    )
                    Text(
                        "#${servico.id}",
                        fontWeight = FontWeight.Bold,
                        color = textPrimary,
                        fontSize = 16.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = primaryGreen.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, primaryGreen.copy(alpha = 0.3f))
                ) {
                    Text(
                        "R$ ${servico.valor}",
                        color = primaryGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cliente
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(primaryGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = primaryGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text("Cliente", color = textSecondary, fontSize = 11.sp)
                    Text(
                        servico.contratante.usuario.nome,
                        color = textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Serviço
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(primaryGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        tint = primaryGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Serviço", color = textSecondary, fontSize = 11.sp)
                    Text(
                        servico.descricao,
                        color = textPrimary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        maxLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer com localização
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                servico.localizacao?.let { loc ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = primaryGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            loc.cidade,
                            color = textSecondary,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Ver detalhes",
                    tint = primaryGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

