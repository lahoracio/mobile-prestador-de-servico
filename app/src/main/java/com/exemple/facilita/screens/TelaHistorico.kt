package com.exemple.facilita.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.util.Locale
import com.exemple.facilita.components.BottomNavBar
import com.exemple.facilita.service.*
import com.exemple.facilita.utils.TokenManager
import kotlinx.coroutines.delay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaHistorico(navController: NavController) {
    val context = LocalContext.current
    var pedidos by remember { mutableStateOf<List<PedidoHistorico>>(emptyList()) }
    var paginacao by remember { mutableStateOf<Paginacao?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var animateContent by remember { mutableStateOf(false) }
    var paginaAtual by remember { mutableStateOf(1) }

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

    // Buscar histórico com atualização automática a cada 10 segundos
    LaunchedEffect(paginaAtual) {
        fun buscarHistorico(mostrarLoading: Boolean = false) {
            if (mostrarLoading) isLoading = true

            val service = RetrofitFactory.getServicoService()
            service.getHistoricoPedidos(token, paginaAtual, 10).enqueue(object : Callback<HistoricoPedidosResponse> {
                override fun onResponse(
                    call: Call<HistoricoPedidosResponse>,
                    response: Response<HistoricoPedidosResponse>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.data?.let { data ->
                            val pedidosAnteriores = pedidos.size
                            pedidos = data.pedidos
                            paginacao = data.paginacao

                            android.util.Log.d("TelaHistorico", "✅ Pedidos carregados: ${pedidos.size}")
                            android.util.Log.d("TelaHistorico", "📊 Status dos pedidos:")
                            pedidos.forEach { pedido ->
                                android.util.Log.d("TelaHistorico", "  - #${pedido.id}: ${pedido.status}")
                            }

                            if (pedidos.size > pedidosAnteriores) {
                                android.util.Log.d("TelaHistorico", "🆕 Novos pedidos adicionados!")
                            }
                        }
                    } else {
                        android.util.Log.e("TelaHistorico", "❌ Erro ${response.code()}: ${response.errorBody()?.string()}")
                    }
                    isLoading = false
                }

                override fun onFailure(call: Call<HistoricoPedidosResponse>, t: Throwable) {
                    android.util.Log.e("TelaHistorico", "❌ Falha: ${t.message}")
                    isLoading = false
                }
            })
        }

        // Primeira busca (com loading)
        buscarHistorico(mostrarLoading = true)

        // Atualizar a cada 10 segundos (sem loading)
        while (true) {
            delay(10000) // 10 segundos
            android.util.Log.d("TelaHistorico", "🔄 Atualizando histórico automaticamente...")
            buscarHistorico(mostrarLoading = false)
        }
    }

    Scaffold(
        containerColor = lightBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Histórico de Pedidos",
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
                            "Carregando histórico...",
                            color = textSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
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
                                        "Todos os Pedidos",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textPrimary
                                    )
                                    paginacao?.let { pag ->
                                        Text(
                                            "${pag.total_pedidos} pedido(s) no total",
                                            fontSize = 14.sp,
                                            color = textSecondary
                                        )
                                    }
                                }
                            }
                        }

                        // Lista de pedidos ou estado vazio
                        if (pedidos.isEmpty()) {
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
                                            imageVector = Icons.Default.History,
                                            contentDescription = null,
                                            tint = textSecondary.copy(alpha = 0.5f),
                                            modifier = Modifier.size(80.dp)
                                        )
                                        Text(
                                            "Nenhum pedido no histórico",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = textPrimary,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            "Os pedidos aceitos aparecerão aqui",
                                            fontSize = 14.sp,
                                            color = textSecondary,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                        items(pedidos, key = { it.id }) { pedido ->
                            PedidoHistoricoCard(
                                pedido = pedido,
                                onClick = {
                                    navController.navigate("tela_detalhes_servico_aceito/${pedido.id}")
                                },
                                primaryGreen = primaryGreen,
                                cardBg = cardBg,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary
                            )
                        }
                        }
                    }

                    // Paginação
                    paginacao?.let { pag ->
                        if (pag.total_paginas > 1) {
                            Surface(
                                color = Color.White,
                                shadowElevation = 4.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Botão Anterior
                                    OutlinedButton(
                                        onClick = {
                                            if (pag.pagina_atual > 1) {
                                                paginaAtual = pag.pagina_atual - 1
                                            }
                                        },
                                        enabled = pag.pagina_atual > 1,
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = primaryGreen
                                        )
                                    ) {
                                        Icon(Icons.Default.ChevronLeft, contentDescription = null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Anterior")
                                    }

                                    // Indicador de página
                                    Text(
                                        "${pag.pagina_atual} / ${pag.total_paginas}",
                                        fontWeight = FontWeight.SemiBold,
                                        color = textPrimary
                                    )

                                    // Botão Próximo
                                    Button(
                                        onClick = {
                                            if (pag.pagina_atual < pag.total_paginas) {
                                                paginaAtual = pag.pagina_atual + 1
                                            }
                                        },
                                        enabled = pag.pagina_atual < pag.total_paginas,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = primaryGreen
                                        )
                                    ) {
                                        Text("Próximo")
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PedidoHistoricoCard(
    pedido: PedidoHistorico,
    onClick: () -> Unit,
    primaryGreen: Color,
    cardBg: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    val statusColor = when (pedido.status) {
        "EM_ANDAMENTO" -> primaryGreen
        "CONCLUIDO", "FINALIZADO" -> Color(0xFF4CAF50)
        "CANCELADO" -> Color(0xFFF44336)
        else -> textSecondary
    }

    val statusText = when (pedido.status) {
        "EM_ANDAMENTO" -> "Em andamento"
        "CONCLUIDO", "FINALIZADO" -> "Finalizado"
        "CANCELADO" -> "Cancelado"
        "PENDENTE" -> "Pendente"
        else -> pedido.status
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = statusColor.copy(alpha = 0.25f)
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
                        brush = when (pedido.status) {
                            "FINALIZADO", "CONCLUIDO" -> Brush.verticalGradient(
                                listOf(Color(0xFF019D31), Color(0xFF06C755))
                            )
                            "CANCELADO" -> Brush.verticalGradient(
                                listOf(Color(0xFFD32F2F), Color(0xFFEF5350))
                            )
                            "EM_ANDAMENTO" -> Brush.verticalGradient(
                                listOf(Color(0xFFFFA726), Color(0xFFFFB74D))
                            )
                            else -> Brush.verticalGradient(
                                listOf(Color(0xFF42A5F5), Color(0xFF64B5F6))
                            )
                        },
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
                            tint = statusColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "#${pedido.id}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    }

                    // Status Badge com gradiente
                    Box(
                        modifier = Modifier
                            .background(
                                brush = when (pedido.status) {
                                    "FINALIZADO", "CONCLUIDO" -> Brush.horizontalGradient(
                                        listOf(Color(0xFF019D31), Color(0xFF06C755))
                                    )
                                    "CANCELADO" -> Brush.horizontalGradient(
                                        listOf(Color(0xFFD32F2F), Color(0xFFEF5350))
                                    )
                                    "EM_ANDAMENTO" -> Brush.horizontalGradient(
                                        listOf(Color(0xFFFFA726), Color(0xFFFFB74D))
                                    )
                                    else -> Brush.horizontalGradient(
                                        listOf(Color(0xFF42A5F5), Color(0xFF64B5F6))
                                    )
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Text(
                            text = statusText,
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
                                    listOf(statusColor, statusColor.copy(alpha = 0.7f))
                                ),
                                shape = CircleShape
                            )
                            .padding(3.dp)
                            .background(
                                color = statusColor.copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Cliente",
                            tint = statusColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = pedido.categoria.nome,
                            fontSize = 12.sp,
                            color = textSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = pedido.contratante.usuario.nome,
                            fontSize = 16.sp,
                            color = textPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = pedido.descricao,
                            fontSize = 13.sp,
                            color = textSecondary,
                            maxLines = 1
                        )

                        // Valor com gradiente
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    brush = when (pedido.status) {
                                        "FINALIZADO", "CONCLUIDO" -> Brush.horizontalGradient(
                                            listOf(Color(0xFF019D31), Color(0xFF06C755))
                                        )
                                        "CANCELADO" -> Brush.horizontalGradient(
                                            listOf(Color(0xFFD32F2F), Color(0xFFEF5350))
                                        )
                                        "EM_ANDAMENTO" -> Brush.horizontalGradient(
                                            listOf(Color(0xFFFFA726), Color(0xFFFFB74D))
                                        )
                                        else -> Brush.horizontalGradient(
                                            listOf(Color(0xFF42A5F5), Color(0xFF64B5F6))
                                        )
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "R$ ${String.format(Locale.getDefault(), "%.2f", pedido.valor)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Footer com data
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatarData(pedido.data_solicitacao),
                        fontSize = 11.sp,
                        color = textSecondary,
                        fontWeight = FontWeight.Medium
                    )

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Ver detalhes",
                        tint = statusColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

fun formatarData(dataISO: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dataISO)
        date?.let { outputFormat.format(it) } ?: dataISO
    } catch (e: Exception) {
        dataISO
    }
}

