package com.exemple.facilita.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.exemple.facilita.model.Transacao
import com.exemple.facilita.model.TipoTransacao
import com.exemple.facilita.model.StatusTransacao
import com.exemple.facilita.viewmodel.CarteiraViewModel
import com.exemple.facilita.components.BottomNavBar
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaCarteira(navController: NavController, usuarioId: String = "user123") {
    val viewModel: CarteiraViewModel = viewModel()
    val carteira by viewModel.carteira.collectAsState()
    val transacoes by viewModel.transacoes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "carteira")
    val shimmer by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer"
    )

    LaunchedEffect(Unit) {
        viewModel.carregarCarteira(usuarioId)
        viewModel.carregarTransacoes(usuarioId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Minha Carteira", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF019D31),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = { BottomNavBar(navController) }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF1F9F4),
                            Color.White
                        )
                    )
                )
                .padding(padding)
        ) {

            // Efeitos de fundo animados
            Canvas(modifier = Modifier.fillMaxSize()) {
                for (i in 0..20) {
                    val offsetX = (size.width / 20f) * i
                    val offsetY = size.height * ((shimmer + i * 0.05f) % 1f)
                    val alpha = kotlin.math.abs(kotlin.math.sin((shimmer + i * 0.1f) * 3.14f)) * 0.2f

                    drawCircle(
                        color = Color(0xFF019D31).copy(alpha = alpha),
                        radius = 8f,
                        center = Offset(offsetX, offsetY)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Card de Saldo
                item {
                    CardSaldo(
                        saldo = carteira?.saldo ?: 0.0,
                        saldoBloqueado = carteira?.saldoBloqueado ?: 0.0,
                        isLoading = isLoading
                    )
                }

                // Botões de Ação
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        BotaoAcao(
                            icon = Icons.Default.Add,
                            texto = "Adicionar",
                            cor = Color(0xFF019D31),
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate("tela_adicionar_dinheiro") }
                        )
                        BotaoAcao(
                            icon = Icons.Default.ArrowDownward,
                            texto = "Sacar",
                            cor = Color(0xFF019D31),
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate("tela_sacar_dinheiro") }
                        )
                        BotaoAcao(
                            icon = Icons.Default.AccountBalance,
                            texto = "Contas",
                            cor = Color(0xFF019D31),
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate("tela_contas_bancarias") }
                        )
                    }
                }

                // Título Transações
                item {
                    Text(
                        text = "Transações Recentes",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF019D31),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // Lista de Transações
                if (transacoes.isEmpty() && !isLoading) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Receipt,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = Color(0xFFBDBDBD)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Nenhuma transação ainda",
                                    fontSize = 16.sp,
                                    color = Color(0xFF757575),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(transacoes) { transacao ->
                        ItemTransacao(transacao)
                    }
                }
            }

            // Mensagem de erro
            errorMessage?.let { message ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.limparMensagens() }) {
                            Text("OK", color = Color.White)
                        }
                    }
                ) {
                    Text(message)
                }
            }
        }
    }
}

@Composable
fun CardSaldo(saldo: Double, saldoBloqueado: Double, isLoading: Boolean) {
    val numberFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF019D31)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            // Efeitos decorativos
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.1f),
                    radius = size.minDimension * 0.5f,
                    center = Offset(size.width * 0.8f, size.height * 0.3f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    radius = size.minDimension * 0.7f,
                    center = Offset(size.width * 0.2f, size.height * 0.7f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Saldo Disponível",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(28.dp)
                    )
                }

                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    Column {
                        Text(
                            text = numberFormat.format(saldo),
                            fontSize = 36.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        if (saldoBloqueado > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Bloqueado: ${numberFormat.format(saldoBloqueado)}",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BotaoAcao(
    icon: ImageVector,
    texto: String,
    cor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(cor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = cor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = texto,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF424242),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ItemTransacao(transacao: Transacao) {
    val numberFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    val (icon, cor) = when (transacao.tipo) {
        TipoTransacao.DEPOSITO -> Icons.Default.ArrowUpward to Color(0xFF4CAF50)
        TipoTransacao.SAQUE -> Icons.Default.ArrowDownward to Color(0xFFF44336)
        TipoTransacao.RECEBIMENTO -> Icons.Default.AttachMoney to Color(0xFF4CAF50)
        TipoTransacao.PAGAMENTO -> Icons.Default.ShoppingCart to Color(0xFFF44336)
        TipoTransacao.ESTORNO -> Icons.Default.Refresh to Color(0xFFFF9800)
        TipoTransacao.TAXA -> Icons.Default.Receipt to Color(0xFF9C27B0)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(cor.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = cor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = transacao.tipo.name.replace("_", " ").lowercase()
                            .replaceFirstChar { it.uppercase() },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = transacao.descricao,
                        fontSize = 13.sp,
                        color = Color(0xFF757575)
                    )
                    Text(
                        text = transacao.dataTransacao,
                        fontSize = 12.sp,
                        color = Color(0xFF9E9E9E)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = numberFormat.format(transacao.valor),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (transacao.tipo == TipoTransacao.DEPOSITO ||
                                transacao.tipo == TipoTransacao.RECEBIMENTO)
                            Color(0xFF4CAF50) else Color(0xFFF44336)
                )

                StatusChip(status = transacao.status)
            }
        }
    }
}

@Composable
fun StatusChip(status: StatusTransacao) {
    val (texto, cor) = when (status) {
        StatusTransacao.CONCLUIDA -> "Concluída" to Color(0xFF4CAF50)
        StatusTransacao.PENDENTE -> "Pendente" to Color(0xFFFF9800)
        StatusTransacao.PROCESSANDO -> "Processando" to Color(0xFF2196F3)
        StatusTransacao.CANCELADA -> "Cancelada" to Color(0xFF9E9E9E)
        StatusTransacao.FALHOU -> "Falhou" to Color(0xFFF44336)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = cor.copy(alpha = 0.1f),
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Text(
            text = texto,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = cor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

