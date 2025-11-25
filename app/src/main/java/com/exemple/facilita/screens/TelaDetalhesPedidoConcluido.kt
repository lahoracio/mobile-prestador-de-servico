package com.exemple.facilita.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.exemple.facilita.service.PedidoHistorico
import com.exemple.facilita.service.Parada
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaDetalhesPedidoConcluido(
    navController: NavController,
    pedido: PedidoHistorico
) {
    var animateContent by remember { mutableStateOf(false) }
    var showSuccessAnimation by remember { mutableStateOf(false) }

    // Cores futuristas
    val primaryGreen = Color(0xFF00FF88)
    val darkBg = Color(0xFF0A0E27)
    val cardBg = Color(0xFF1A1F3A)
    val accentCyan = Color(0xFF00D9FF)
    val accentPurple = Color(0xFF9D4EDD)
    val successGreen = Color(0xFF00FF88)

    // Animação de entrada
    LaunchedEffect(Unit) {
        delay(100)
        animateContent = true
        delay(500)
        showSuccessAnimation = true
    }

    // Animação de pulse infinita
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Pedido #${pedido.id}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = darkBg
                )
            )
        },
        containerColor = darkBg
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Background com efeitos futuristas
            BackgroundEffects(rotation, pulseAlpha, primaryGreen, accentCyan, accentPurple)

            // Conteúdo principal
            LazyColumnContent(
                pedido = pedido,
                animateContent = animateContent,
                showSuccessAnimation = showSuccessAnimation,
                primaryGreen = primaryGreen,
                darkBg = darkBg,
                cardBg = cardBg,
                accentCyan = accentCyan,
                accentPurple = accentPurple,
                successGreen = successGreen
            )
        }
    }
}

@Composable
private fun BackgroundEffects(
    rotation: Float,
    pulseAlpha: Float,
    primaryGreen: Color,
    accentCyan: Color,
    accentPurple: Color
) {
    // Círculo rotativo de fundo
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(0.1f)
    ) {
        Box(
            modifier = Modifier
                .size(400.dp)
                .offset(x = (-100).dp, y = (-100).dp)
                .rotate(rotation)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryGreen.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 100.dp, y = 100.dp)
                .rotate(-rotation * 0.7f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentCyan.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
    }

    // Grid de fundo
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(pulseAlpha * 0.1f)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        accentPurple.copy(alpha = 0.05f),
                        Color.Transparent,
                        primaryGreen.copy(alpha = 0.05f)
                    )
                )
            )
    )
}

@Composable
private fun LazyColumnContent(
    pedido: PedidoHistorico,
    animateContent: Boolean,
    showSuccessAnimation: Boolean,
    primaryGreen: Color,
    darkBg: Color,
    cardBg: Color,
    accentCyan: Color,
    accentPurple: Color,
    successGreen: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Card de status com animação de sucesso
        AnimatedVisibility(
            visible = animateContent,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -100 })
        ) {
            StatusSuccessCard(
                showSuccessAnimation = showSuccessAnimation,
                successGreen = successGreen,
                cardBg = cardBg,
                pedido = pedido
            )
        }

        // Informações principais
        AnimatedVisibility(
            visible = animateContent,
            enter = fadeIn(animationSpec = tween(600, delayMillis = 200)) +
                    slideInVertically(initialOffsetY = { 100 }, animationSpec = tween(600, delayMillis = 200))
        ) {
            InfoPrincipaisCard(pedido, cardBg, primaryGreen, accentCyan)
        }

        // Timeline de paradas
        if (pedido.paradas.isNotEmpty()) {
            AnimatedVisibility(
                visible = animateContent,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 400)) +
                        slideInVertically(initialOffsetY = { 100 }, animationSpec = tween(600, delayMillis = 400))
            ) {
                TimelineParadasCard(pedido.paradas, cardBg, primaryGreen, accentCyan)
            }
        }

        // Informações do contratante
        AnimatedVisibility(
            visible = animateContent,
            enter = fadeIn(animationSpec = tween(600, delayMillis = 600)) +
                    slideInVertically(initialOffsetY = { 100 }, animationSpec = tween(600, delayMillis = 600))
        ) {
            ContratanteCard(pedido, cardBg, accentPurple)
        }

        // Localização
        pedido.localizacao?.let { loc ->
            AnimatedVisibility(
                visible = animateContent,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 800)) +
                        slideInVertically(initialOffsetY = { 100 }, animationSpec = tween(600, delayMillis = 800))
            ) {
                LocalizacaoCard(loc, cardBg, accentCyan)
            }
        }

        // Resumo financeiro
        AnimatedVisibility(
            visible = animateContent,
            enter = fadeIn(animationSpec = tween(600, delayMillis = 1000)) +
                    slideInVertically(initialOffsetY = { 100 }, animationSpec = tween(600, delayMillis = 1000))
        ) {
            ResumoFinanceiroCard(pedido, cardBg, successGreen)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun StatusSuccessCard(
    showSuccessAnimation: Boolean,
    successGreen: Color,
    cardBg: Color,
    pedido: PedidoHistorico
) {
    var scale by remember { mutableStateOf(0f) }

    LaunchedEffect(showSuccessAnimation) {
        if (showSuccessAnimation) {
            animate(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) { value, _ ->
                scale = value
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            successGreen.copy(alpha = 0.2f),
                            successGreen.copy(alpha = 0.1f)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Ícone de sucesso com pulse
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    // Anel externo pulsante
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .scale(scale)
                            .alpha(0.3f)
                            .background(successGreen, CircleShape)
                    )

                    // Ícone central
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(successGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "PEDIDO CONCLUÍDO",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = successGreen,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = formatarDataDetalhes(pedido.data_conclusao ?: ""),
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun InfoPrincipaisCard(
    pedido: PedidoHistorico,
    cardBg: Color,
    primaryGreen: Color,
    accentCyan: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Título da seção com linha gradiente
            SectionHeader("Informações Principais", primaryGreen)

            Spacer(modifier = Modifier.height(20.dp))

            // Grid de informações
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoRow(
                    icon = Icons.Default.Build,
                    label = "Categoria",
                    value = pedido.categoria.nome,
                    iconColor = primaryGreen
                )

                InfoRow(
                    icon = Icons.Default.Description,
                    label = "Descrição",
                    value = pedido.descricao,
                    iconColor = accentCyan
                )

                InfoRow(
                    icon = Icons.Default.DateRange,
                    label = "Data Solicitação",
                    value = formatarDataDetalhes(pedido.data_solicitacao),
                    iconColor = primaryGreen
                )

                InfoRow(
                    icon = Icons.Default.CheckCircle,
                    label = "Data Conclusão",
                    value = formatarDataDetalhes(pedido.data_conclusao ?: "N/A"),
                    iconColor = accentCyan
                )
            }
        }
    }
}

@Composable
private fun TimelineParadasCard(
    paradas: List<Parada>,
    cardBg: Color,
    primaryGreen: Color,
    accentCyan: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            SectionHeader("Timeline de Paradas", accentCyan)

            Spacer(modifier = Modifier.height(20.dp))

            paradas.forEachIndexed { index, parada ->
                TimelineItem(
                    parada = parada,
                    isLast = index == paradas.size - 1,
                    primaryGreen = primaryGreen,
                    accentCyan = accentCyan
                )

                if (index < paradas.size - 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun TimelineItem(
    parada: Parada,
    isLast: Boolean,
    primaryGreen: Color,
    accentCyan: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Timeline indicator
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(primaryGreen, accentCyan)
                        ),
                        shape = CircleShape
                    )
                    .border(3.dp, primaryGreen.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${parada.ordem}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(40.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    primaryGreen.copy(alpha = 0.5f),
                                    accentCyan.copy(alpha = 0.3f)
                                )
                            )
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

                // Conteúdo da parada
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = parada.endereco_completo,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )

                    if (parada.descricao.isNotEmpty()) {
                        Text(
                            text = parada.descricao,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }

                    if (parada.tempo_estimado_chegada != null) {
                        Text(
                            text = "ETA: ${parada.tempo_estimado_chegada}",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
    }
}

@Composable
private fun ContratanteCard(
    pedido: PedidoHistorico,
    cardBg: Color,
    accentPurple: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            SectionHeader("Contratante", accentPurple)

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(accentPurple, accentPurple.copy(alpha = 0.7f))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = pedido.contratante.usuario.nome.firstOrNull()?.uppercase() ?: "C",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = pedido.contratante.usuario.nome,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = pedido.contratante.usuario.email,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun LocalizacaoCard(
    localizacao: com.exemple.facilita.service.LocalizacaoSimples,
    cardBg: Color,
    accentCyan: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            SectionHeader("Localização", accentCyan)

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = accentCyan.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = accentCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = localizacao.cidade,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "ID da Localização: #${localizacao.id}",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun ResumoFinanceiroCard(
    pedido: PedidoHistorico,
    cardBg: Color,
    successGreen: Color
) {
    val format = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            successGreen.copy(alpha = 0.2f),
                            successGreen.copy(alpha = 0.1f)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "VALOR TOTAL",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = format.format(pedido.valor),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = successGreen
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Badge de status
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = successGreen.copy(alpha = 0.2f),
                    modifier = Modifier.border(
                        1.dp,
                        successGreen.copy(alpha = 0.5f),
                        RoundedCornerShape(20.dp)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = successGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PAGAMENTO CONCLUÍDO",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = successGreen,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(24.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(color, color.copy(alpha = 0.5f))
                    ),
                    shape = RoundedCornerShape(2.dp)
                )
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            color.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    iconColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    color = iconColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}

private fun formatarDataDetalhes(dataISO: String): String {
    return try {
        if (dataISO.isEmpty() || dataISO == "N/A") return "N/A"

        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR"))
        val date = inputFormat.parse(dataISO)
        outputFormat.format(date ?: Date())
    } catch (_: Exception) {
        dataISO
    }
}

