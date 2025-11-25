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

    // Cores tema LIGHT moderno
    val primaryGreen = Color(0xFF00B14F)
    val lightBg = Color(0xFFF5F7FA)
    val cardBg = Color.White
    val accentBlue = Color(0xFF2196F3)
    val accentOrange = Color(0xFFFF9800)
    val successGreen = Color(0xFF4CAF50)
    val textPrimary = Color(0xFF212121)
    val textSecondary = Color(0xFF757575)

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
                        color = textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Voltar",
                            tint = textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = lightBg
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(lightBg)
        ) {
            // Conteúdo principal
            LazyColumnContent(
                pedido = pedido,
                animateContent = animateContent,
                showSuccessAnimation = showSuccessAnimation,
                primaryGreen = primaryGreen,
                lightBg = lightBg,
                cardBg = cardBg,
                accentBlue = accentBlue,
                accentOrange = accentOrange,
                successGreen = successGreen,
                textPrimary = textPrimary,
                textSecondary = textSecondary
            )
        }
    }
}

// Função BackgroundEffects removida - usando tema light agora

@Composable
private fun LazyColumnContent(
    pedido: PedidoHistorico,
    animateContent: Boolean,
    showSuccessAnimation: Boolean,
    primaryGreen: Color,
    lightBg: Color,
    cardBg: Color,
    accentBlue: Color,
    accentOrange: Color,
    successGreen: Color,
    textPrimary: Color,
    textSecondary: Color
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
                pedido = pedido,
                textPrimary = textPrimary,
                textSecondary = textSecondary
            )
        }

        // Informações principais
        AnimatedVisibility(
            visible = animateContent,
            enter = fadeIn(animationSpec = tween(600, delayMillis = 200)) +
                    slideInVertically(initialOffsetY = { 100 }, animationSpec = tween(600, delayMillis = 200))
        ) {
            InfoPrincipaisCard(pedido, cardBg, primaryGreen, accentBlue, textPrimary, textSecondary)
        }

        // Timeline de paradas
        if (pedido.paradas.isNotEmpty()) {
            AnimatedVisibility(
                visible = animateContent,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 400)) +
                        slideInVertically(initialOffsetY = { 100 }, animationSpec = tween(600, delayMillis = 400))
            ) {
                TimelineParadasCard(pedido.paradas, cardBg, primaryGreen, accentBlue, textPrimary, textSecondary)
            }
        }

        // Informações do contratante
        AnimatedVisibility(
            visible = animateContent,
            enter = fadeIn(animationSpec = tween(600, delayMillis = 600)) +
                    slideInVertically(initialOffsetY = { 100 }, animationSpec = tween(600, delayMillis = 600))
        ) {
            ContratanteCard(pedido, cardBg, accentOrange, textPrimary, textSecondary)
        }

        // Localização
        pedido.localizacao?.let { loc ->
            AnimatedVisibility(
                visible = animateContent,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 800)) +
                        slideInVertically(initialOffsetY = { 100 }, animationSpec = tween(600, delayMillis = 800))
            ) {
                LocalizacaoCard(loc, cardBg, accentBlue, textPrimary, textSecondary)
            }
        }

        // Resumo financeiro
        AnimatedVisibility(
            visible = animateContent,
            enter = fadeIn(animationSpec = tween(600, delayMillis = 1000)) +
                    slideInVertically(initialOffsetY = { 100 }, animationSpec = tween(600, delayMillis = 1000))
        ) {
            ResumoFinanceiroCard(pedido, cardBg, successGreen, textPrimary)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun StatusSuccessCard(
    showSuccessAnimation: Boolean,
    successGreen: Color,
    cardBg: Color,
    pedido: PedidoHistorico,
    textPrimary: Color,
    textSecondary: Color
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
                    color = textSecondary
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
    accentBlue: Color,
    textPrimary: Color,
    textSecondary: Color
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
            SectionHeader("Informações Principais", primaryGreen, textPrimary)

            Spacer(modifier = Modifier.height(20.dp))

            // Grid de informações
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoRow(
                    icon = Icons.Default.Build,
                    label = "Categoria",
                    value = pedido.categoria.nome,
                    iconColor = primaryGreen,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary
                )

                InfoRow(
                    icon = Icons.Default.Description,
                    label = "Descrição",
                    value = pedido.descricao,
                    iconColor = accentBlue,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary
                )

                InfoRow(
                    icon = Icons.Default.DateRange,
                    label = "Data Solicitação",
                    value = formatarDataDetalhes(pedido.data_solicitacao),
                    iconColor = primaryGreen,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary
                )

                InfoRow(
                    icon = Icons.Default.CheckCircle,
                    label = "Data Conclusão",
                    value = formatarDataDetalhes(pedido.data_conclusao ?: "N/A"),
                    iconColor = accentBlue,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary
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
    accentBlue: Color,
    textPrimary: Color,
    textSecondary: Color
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
            SectionHeader("Timeline de Paradas", accentBlue, textPrimary)

            Spacer(modifier = Modifier.height(20.dp))

            paradas.forEachIndexed { index, parada ->
                TimelineItem(
                    parada = parada,
                    isLast = index == paradas.size - 1,
                    primaryGreen = primaryGreen,
                    accentBlue = accentBlue,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary
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
    accentBlue: Color,
    textPrimary: Color,
    textSecondary: Color
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
                    .background(primaryGreen, CircleShape)
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
                        .background(primaryGreen.copy(alpha = 0.3f))
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
                color = textPrimary
            )

            if (parada.descricao.isNotEmpty()) {
                Text(
                    text = parada.descricao,
                    fontSize = 14.sp,
                    color = textSecondary
                )
            }

            if (parada.tempo_estimado_chegada != null) {
                Text(
                    text = "ETA: ${parada.tempo_estimado_chegada}",
                    fontSize = 13.sp,
                    color = textSecondary
                )
            }
        }
    }
}

@Composable
private fun ContratanteCard(
    pedido: PedidoHistorico,
    cardBg: Color,
    accentOrange: Color,
    textPrimary: Color,
    textSecondary: Color
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
            SectionHeader("Contratante", accentOrange, textPrimary)

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(accentOrange, CircleShape),
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
                        color = textPrimary
                    )

                    Text(
                        text = pedido.contratante.usuario.email,
                        fontSize = 14.sp,
                        color = textSecondary
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
    accentBlue: Color,
    textPrimary: Color,
    textSecondary: Color
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
            SectionHeader("Localização", accentBlue, textPrimary)

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = accentBlue.copy(alpha = 0.1f),
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
                        tint = accentBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = localizacao.cidade,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "ID da Localização: #${localizacao.id}",
                    fontSize = 13.sp,
                    color = textSecondary
                )
            }
        }
    }
}

@Composable
private fun ResumoFinanceiroCard(
    pedido: PedidoHistorico,
    cardBg: Color,
    successGreen: Color,
    textPrimary: Color
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
                    color = textPrimary.copy(alpha = 0.6f),
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Medium
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
                    color = successGreen.copy(alpha = 0.15f),
                    modifier = Modifier.border(
                        1.dp,
                        successGreen,
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
private fun SectionHeader(title: String, color: Color, textPrimary: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(24.dp)
                .background(color, shape = RoundedCornerShape(2.dp))
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = textPrimary
        )

        Spacer(modifier = Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(color.copy(alpha = 0.2f))
        )
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    iconColor: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    color = iconColor.copy(alpha = 0.1f),
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
                color = textSecondary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = textPrimary
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

