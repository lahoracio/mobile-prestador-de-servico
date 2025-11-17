package com.exemple.facilita.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.exemple.facilita.model.ServicoDetalhe
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaDetalhesServicoAceito(
    navController: NavController,
    servicoDetalhe: ServicoDetalhe
) {
    val context = LocalContext.current

    // Cores do aplicativo (mesmas usadas em todo o app)
    val primaryGreen = Color(0xFF019D31)      // Verde principal do app
    val darkGreen = Color(0xFF015B2B)         // Verde escuro
    val darkBg = Color(0xFF0F1419)            // Fundo escuro
    val cardBg = Color(0xFF1A1F26)            // Fundo dos cards
    val accentColor = Color(0xFF019D31)       // Cor de destaque (mesmo verde)
    val textPrimary = Color.White
    val textSecondary = Color(0xFFB0B8C8)

    // Animação de entrada
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    // Animação pulsante para o status
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBg)
    ) {
        // Fundo animado com círculos
        AnimatedBackground()

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header futurista
                FuturisticHeader(
                    navController = navController,
                    pulseAlpha = pulseAlpha
                )

                // Conteúdo scrollável
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Card de Valor com animação
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(
                            initialOffsetY = { -100 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ) + fadeIn()
                    ) {
                        ValorCard(valor = servicoDetalhe.valor, primaryGreen = primaryGreen, cardBg = cardBg)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Informações do Cliente
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInHorizontally(
                            initialOffsetX = { -100 },
                            animationSpec = tween(600, delayMillis = 100)
                        ) + fadeIn()
                    ) {
                        ClienteInfoCard(
                            servicoDetalhe = servicoDetalhe,
                            cardBg = cardBg,
                            primaryGreen = primaryGreen,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Detalhes do Serviço
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInHorizontally(
                            initialOffsetX = { 100 },
                            animationSpec = tween(600, delayMillis = 200)
                        ) + fadeIn()
                    ) {
                        DetalhesServicoCard(
                            servicoDetalhe = servicoDetalhe,
                            cardBg = cardBg,
                            primaryGreen = primaryGreen,
                            accentColor = accentColor,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Localização
                    servicoDetalhe.localizacao?.let { loc ->
                        AnimatedVisibility(
                            visible = isVisible,
                            enter = slideInVertically(
                                initialOffsetY = { 100 },
                                animationSpec = tween(600, delayMillis = 300)
                            ) + fadeIn()
                        ) {
                            LocalizacaoCard(
                                localizacao = loc,
                                cardBg = cardBg,
                                primaryGreen = primaryGreen,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(280.dp))
                }
            }

            // Três botões de ação FIXOS na parte inferior
            servicoDetalhe.localizacao?.let { loc ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    darkBg,
                                    darkBg
                                )
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Botão 1: Ver Rota no Mapa (Estilo Uber) - PRINCIPAL
                    SwipeToStartButton(
                        text = "Arraste para Ver Rota no Mapa",
                        onSwipeComplete = {
                            // Navegar para tela de mapa com rota
                            navController.navigate("tela_mapa_rota/${servicoDetalhe.id}")
                        },
                        primaryGreen = primaryGreen,
                        darkGreen = darkGreen,
                        icon = Icons.Default.Map
                    )

                    // Botão 2: Rastreamento em Tempo Real
                    Button(
                        onClick = {
                            navController.navigate("tela_rastreamento_servico/${servicoDetalhe.id}")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryGreen.copy(alpha = 0.8f)
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Rastreamento em Tempo Real",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Botão 3: Google Maps Externo
                    OutlinedButton(
                        onClick = {
                            // Abrir Google Maps diretamente
                            val uri = Uri.parse(
                                "google.navigation:q=${loc.latitude},${loc.longitude}&mode=d"
                            )
                            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                setPackage("com.google.android.apps.maps")
                            }

                            if (intent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(intent)
                            } else {
                                val browserIntent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${loc.latitude},${loc.longitude}")
                                )
                                context.startActivity(browserIntent)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        border = BorderStroke(2.dp, Color(0xFF1E88E5)),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = null,
                                tint = Color(0xFF1E88E5)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Abrir no Google Maps",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E88E5)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "background")

    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset1"
    )

    val offset2 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset2"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2

        // Círculo 1 - Verde do app
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF019D31).copy(alpha = 0.1f),
                    Color.Transparent
                ),
                center = Offset(
                    centerX + centerX * 0.5f * kotlin.math.cos(Math.toRadians(offset1.toDouble())).toFloat(),
                    centerY + centerY * 0.5f * kotlin.math.sin(Math.toRadians(offset1.toDouble())).toFloat()
                )
            ),
            radius = 300f
        )

        // Círculo 2 - Verde claro do app
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF00B94A).copy(alpha = 0.08f),
                    Color.Transparent
                ),
                center = Offset(
                    centerX + centerX * 0.3f * kotlin.math.cos(Math.toRadians(offset2.toDouble())).toFloat(),
                    centerY + centerY * 0.3f * kotlin.math.sin(Math.toRadians(offset2.toDouble())).toFloat()
                )
            ),
            radius = 250f
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuturisticHeader(
    navController: NavController,
    pulseAlpha: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF141B2D),
                        Color(0xFF0A0E1A).copy(alpha = 0.9f)
                    )
                )
            )
            .padding(top = 40.dp, bottom = 20.dp, start = 20.dp, end = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botão voltar
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = Color(0xFF1E2740),
                        shape = RoundedCornerShape(14.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Voltar",
                    tint = Color.White
                )
            }

            // Título com status
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SERVIÇO ACEITO",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF019D31),
                    letterSpacing = 2.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF019D31))
                            .alpha(pulseAlpha)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Em andamento",
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Botão de mais opções
            IconButton(
                onClick = { /* Menu de opções */ },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = Color(0xFF1E2740),
                        shape = RoundedCornerShape(14.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Mais opções",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun ValorCard(valor: String, primaryGreen: Color, cardBg: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF015B2B),
                        Color(0xFF019D31),
                        Color(0xFF00B94A)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = cardBg,
                    shape = RoundedCornerShape(23.dp)
                )
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "VALOR DO SERVIÇO",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB0B8C8),
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "R$ $valor",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = primaryGreen,
                    letterSpacing = (-2).sp
                )
            }
        }
    }
}

@Composable
fun ClienteInfoCard(
    servicoDetalhe: ServicoDetalhe,
    cardBg: Color,
    primaryGreen: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF015B2B), Color(0xFF019D31))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CLIENTE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryGreen,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = servicoDetalhe.contratante.usuario.nome,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                }

                IconButton(
                    onClick = {
                        /* Ligar para o cliente */
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = primaryGreen.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Ligar",
                        tint = primaryGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Linha divisória futurística
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                primaryGreen.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Informações de contato
            InfoRowFuturistic(
                icon = Icons.Default.Phone,
                label = "Telefone",
                value = servicoDetalhe.contratante.usuario.telefone,
                primaryGreen = primaryGreen,
                textSecondary = textSecondary,
                textPrimary = textPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            InfoRowFuturistic(
                icon = Icons.Default.Email,
                label = "Email",
                value = servicoDetalhe.contratante.usuario.email,
                primaryGreen = primaryGreen,
                textSecondary = textSecondary,
                textPrimary = textPrimary
            )
        }
    }
}

@Composable
fun DetalhesServicoCard(
    servicoDetalhe: ServicoDetalhe,
    cardBg: Color,
    primaryGreen: Color,
    accentColor: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "DETALHES DO SERVIÇO",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    letterSpacing = 1.5.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Categoria
            ServiceDetailItem(
                icon = Icons.Default.Category,
                label = "Categoria",
                value = servicoDetalhe.categoria.nome,
                primaryGreen = primaryGreen,
                textSecondary = textSecondary,
                textPrimary = textPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tempo estimado
            servicoDetalhe.tempo_estimado?.let { tempo ->
                ServiceDetailItem(
                    icon = Icons.Default.Schedule,
                    label = "Tempo Estimado",
                    value = "$tempo minutos",
                    primaryGreen = primaryGreen,
                    textSecondary = textSecondary,
                    textPrimary = textPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Descrição
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFF0A0E1A),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "DESCRIÇÃO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = textSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = servicoDetalhe.descricao,
                        fontSize = 14.sp,
                        color = textPrimary,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
fun LocalizacaoCard(
    localizacao: com.exemple.facilita.model.LocalizacaoDetalhe,
    cardBg: Color,
    primaryGreen: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = primaryGreen.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = primaryGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "LOCALIZAÇÃO",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryGreen,
                    letterSpacing = 1.5.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LocationInfoRow("Endereço", localizacao.endereco, textSecondary, textPrimary)
                localizacao.numero?.let {
                    LocationInfoRow("Número", it, textSecondary, textPrimary)
                }
                localizacao.complemento?.let {
                    LocationInfoRow("Complemento", it, textSecondary, textPrimary)
                }
                LocationInfoRow("Bairro", localizacao.bairro, textSecondary, textPrimary)
                LocationInfoRow("Cidade", "${localizacao.cidade} - ${localizacao.estado}", textSecondary, textPrimary)
                LocationInfoRow("CEP", localizacao.cep, textSecondary, textPrimary)
            }
        }
    }
}

@Composable
fun SwipeToStartButton(
    text: String = "Arraste para Iniciar",
    icon: ImageVector = Icons.Default.Navigation,
    onSwipeComplete: () -> Unit,
    primaryGreen: Color,
    darkGreen: Color
) {
    var offsetX by remember { mutableStateOf(0f) }
    val maxOffset = with(LocalDensity.current) { 280.dp.toPx() }
    val threshold = maxOffset * 0.8f

    val animatedOffset by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "swipeOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFF0A0E1A)
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // Container do botão
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .background(
                    color = Color(0xFF141B2D),
                    shape = RoundedCornerShape(35.dp)
                )
                .border(
                    width = 2.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(darkGreen, primaryGreen, darkGreen)
                    ),
                    shape = RoundedCornerShape(35.dp)
                )
        ) {
            // Texto de instrução
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(1f - (animatedOffset / maxOffset)),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = primaryGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = text,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = primaryGreen.copy(alpha = 0.5f)
                    )
                }
            }

            // Botão deslizante
            Box(
                modifier = Modifier
                    .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                    .padding(4.dp)
                    .size(62.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(darkGreen, primaryGreen)
                        ),
                        shape = RoundedCornerShape(31.dp)
                    )
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (offsetX >= threshold) {
                                    onSwipeComplete()
                                } else {
                                    offsetX = 0f
                                }
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                val newOffset = (offsetX + dragAmount).coerceIn(0f, maxOffset)
                                offsetX = newOffset
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Arrastar",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun InfoRowFuturistic(
    icon: ImageVector,
    label: String,
    value: String,
    primaryGreen: Color,
    textSecondary: Color,
    textPrimary: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    color = primaryGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = primaryGreen,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = textSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = textPrimary
            )
        }
    }
}

@Composable
fun ServiceDetailItem(
    icon: ImageVector,
    label: String,
    value: String,
    primaryGreen: Color,
    textSecondary: Color,
    textPrimary: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = primaryGreen,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = textSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = textPrimary
            )
        }
    }
}

@Composable
fun LocationInfoRow(
    label: String,
    value: String,
    textSecondary: Color,
    textPrimary: Color
) {
    Column {
        Text(
            text = label.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textSecondary,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = textPrimary,
            lineHeight = 18.sp
        )
    }
}

