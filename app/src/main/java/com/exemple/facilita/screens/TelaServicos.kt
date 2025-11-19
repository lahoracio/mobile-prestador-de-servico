package com.exemple.facilita.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
                        // Filtrar APENAS serviços em andamento
                        val todosServicos = response.body()?.data ?: emptyList()
                        servicosEmAndamento = todosServicos.filter { it.status == "EM_ANDAMENTO" }

                        android.util.Log.d("TelaServicos", "✅ Total de serviços: ${todosServicos.size}")
                        android.util.Log.d("TelaServicos", "✅ Serviços EM ANDAMENTO: ${servicosEmAndamento.size}")
                    } else {
                        android.util.Log.e("TelaServicos", "❌ Erro ${response.code()}: ${response.errorBody()?.string()}")
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
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = primaryGreen.copy(alpha = 0.25f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Box {
            // Barra colorida lateral
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(120.dp)
                    .align(Alignment.CenterStart)
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(primaryGreen, Color(0xFF06C755))
                        ),
                        shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
            ) {
                // Header com código e status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Código",
                            tint = primaryGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "#${servico.id}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    }

                    // Status Badge com gradiente
                    Box(
                        modifier = Modifier
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(primaryGreen, Color(0xFF06C755))
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Text(
                            text = "Em andamento",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Informações do cliente e serviço
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Ícone do cliente
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .border(
                                width = 2.dp,
                                brush = Brush.linearGradient(
                                    listOf(primaryGreen, Color(0xFF06C755))
                                ),
                                shape = CircleShape
                            )
                            .padding(3.dp)
                            .background(
                                color = primaryGreen.copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Cliente",
                            tint = primaryGreen,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = servico.categoria.nome,
                            fontSize = 12.sp,
                            color = textSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = servico.contratante.usuario.nome,
                            fontSize = 16.sp,
                            color = textPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = servico.descricao,
                            fontSize = 13.sp,
                            color = textSecondary,
                            maxLines = 1
                        )

                        // Valor com gradiente
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(primaryGreen, Color(0xFF06C755))
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "R$ ${servico.valor}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Footer com ícone de toque
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Toque para ver detalhes",
                        fontSize = 11.sp,
                        color = primaryGreen,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Ver detalhes",
                        tint = primaryGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

