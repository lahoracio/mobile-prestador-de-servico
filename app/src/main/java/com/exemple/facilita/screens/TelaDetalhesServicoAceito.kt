package com.exemple.facilita.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.exemple.facilita.model.ServicoDetalhe
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaDetalhesServicoAceito(
    navController: NavController,
    servicoDetalhe: ServicoDetalhe
) {
    val context = LocalContext.current

    // Modo Claro - Baseado no TelaInicioPrestador
    val primaryGreen = Color(0xFF2E7D32)
    val darkGreen = Color(0xFF1B5E20)
    val accentCyan = Color(0xFF0097A7)
    val lightBg = Color(0xFFF5F5F5)
    val cardBg = Color.White
    val textPrimary = Color(0xFF212121)
    val textSecondary = Color(0xFF757575)

    // Estados de animação
    var animateHeader by remember { mutableStateOf(false) }
    var animateCards by remember { mutableStateOf(false) }
    var animateButton by remember { mutableStateOf(false) }

    // Animação de pulso para o ícone de sucesso
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    LaunchedEffect(Unit) {
        animateHeader = true
        delay(200)
        animateCards = true
        delay(300)
        animateButton = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFAFAFA),
                        lightBg,
                        Color(0xFFEEEEEE)
                    )
                )
            )
    ) {
        // Efeitos de fundo decorativos
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-100).dp, y = (-100).dp)
                .background(
                    primaryGreen.copy(alpha = 0.1f),
                    CircleShape
                )
                .blur(80.dp)
        )

        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 100.dp, y = 100.dp)
                .background(
                    accentCyan.copy(alpha = 0.08f),
                    CircleShape
                )
                .blur(60.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header personalizado com animação
            AnimatedVisibility(
                visible = animateHeader,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    cardBg.copy(alpha = 0.98f),
                                    cardBg.copy(alpha = 0.98f)
                                )
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = "Serviço Aceito",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            // Conteúdo com scroll
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Card de sucesso com animação
                AnimatedVisibility(
                    visible = animateCards,
                    enter = scaleIn(
                        spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                    ) + fadeIn()
                ) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = cardBg.copy(alpha = 0.9f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Ícone de sucesso com brilho
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                // Glow effect
                                Box(
                                    modifier = Modifier
                                        .size(120.dp)
                                        .scale(pulseScale)
                                        .alpha(glowAlpha)
                                        .background(
                                            primaryGreen.copy(alpha = 0.3f),
                                            CircleShape
                                        )
                                        .blur(20.dp)
                                )

                                // Círculo principal
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    primaryGreen,
                                                    darkGreen
                                                )
                                            ),
                                            CircleShape
                                        )
                                        .border(
                                            3.dp,
                                            primaryGreen.copy(alpha = 0.5f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "Serviço Aceito!",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryGreen,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Confira todos os detalhes abaixo",
                                fontSize = 15.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Card do Cliente
                AnimatedVisibility(
                    visible = animateCards,
                    enter = slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(500)
                    ) + fadeIn()
                ) {
                    FuturisticInfoCard(
                        title = "Cliente",
                        icon = Icons.Default.Person,
                        iconColor = accentCyan
                    ) {
                        DetailRow("Nome", servicoDetalhe.contratante.usuario.nome, Icons.Default.Person)
                        Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))
                        DetailRow("Telefone", servicoDetalhe.contratante.usuario.telefone, Icons.Default.Phone)
                        Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))
                        DetailRow("Email", servicoDetalhe.contratante.usuario.email, Icons.Default.Email)

                        Spacer(modifier = Modifier.height(16.dp))

                        // Botões de contato
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${servicoDetalhe.contratante.usuario.telefone}")
                                    }
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accentCyan
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.Phone,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ligar", color = textPrimary, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    // Navegar para o chat
                                    navController.navigate(
                                        "chat_ao_vivo/${servicoDetalhe.id}/${servicoDetalhe.id_contratante}/${servicoDetalhe.contratante.usuario.nome}/${servicoDetalhe.id_prestador}/${servicoDetalhe.prestador?.usuario?.nome ?: "Prestador"}"
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = primaryGreen
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.Chat,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Chat", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Card do Serviço
                AnimatedVisibility(
                    visible = animateCards,
                    enter = slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(500, delayMillis = 100)
                    ) + fadeIn()
                ) {
                    FuturisticInfoCard(
                        title = "Detalhes do Serviço",
                        icon = Icons.Default.Description,
                        iconColor = primaryGreen
                    ) {
                        DetailRow("Categoria", servicoDetalhe.categoria.nome, Icons.Default.Category)
                        Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    Icons.Default.Notes,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Descrição",
                                        fontSize = 13.sp,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        servicoDetalhe.descricao,
                                        fontSize = 15.sp,
                                        color = Color.White,
                                        lineHeight = 22.sp
                                    )
                                }
                            }
                        }

                        Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))

                        servicoDetalhe.tempo_estimado?.let {
                            DetailRow("Tempo Estimado", "$it minutos", Icons.Default.Schedule)
                            Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))
                        }

                        // Valor em destaque
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = primaryGreen.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.AttachMoney,
                                        contentDescription = null,
                                        tint = primaryGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Valor do Serviço",
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                                Text(
                                    "R$ ${servicoDetalhe.valor}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryGreen
                                )
                            }
                        }
                    }
                }

                // Card da Localização
                servicoDetalhe.localizacao?.let { loc ->
                    AnimatedVisibility(
                        visible = animateCards,
                        enter = slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(500, delayMillis = 200)
                        ) + fadeIn()
                    ) {
                        FuturisticInfoCard(
                            title = "Localização",
                            icon = Icons.Default.LocationOn,
                            iconColor = Color(0xFFFF5252)
                        ) {
                            DetailRow("Endereço", loc.endereco, Icons.Default.Home)
                            loc.numero?.let {
                                Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))
                                DetailRow("Número", it, Icons.Default.Pin)
                            }
                            loc.complemento?.let {
                                Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))
                                DetailRow("Complemento", it, Icons.Default.Info)
                            }
                            Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))
                            DetailRow("Bairro", loc.bairro, Icons.Default.LocationCity)
                            Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))
                            DetailRow("Cidade", "${loc.cidade} - ${loc.estado}", Icons.Default.LocationCity)
                            Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))
                            DetailRow("CEP", loc.cep, Icons.Default.Place)

                            Spacer(modifier = Modifier.height(16.dp))

                            // Botão de Navegação Interna (Tempo Real) - PRINCIPAL
                            Button(
                                onClick = {
                                    // Obter localização atual (origem)
                                    val localizacaoManager = context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
                                    try {
                                        val ultimaLocalizacao = localizacaoManager.getLastKnownLocation(
                                            android.location.LocationManager.GPS_PROVIDER
                                        ) ?: localizacaoManager.getLastKnownLocation(
                                            android.location.LocationManager.NETWORK_PROVIDER
                                        )

                                        val origemLat = ultimaLocalizacao?.latitude ?: loc.latitude
                                        val origemLng = ultimaLocalizacao?.longitude ?: loc.longitude

                                        // Navegar para tela de navegação em tempo real
                                        navController.navigate(
                                            "navegacao_tempo_real/$origemLat/$origemLng/${loc.latitude}/${loc.longitude}"
                                        )
                                    } catch (e: SecurityException) {
                                        // Usar localização do serviço como origem
                                        navController.navigate(
                                            "navegacao_tempo_real/${loc.latitude}/${loc.longitude}/${loc.latitude}/${loc.longitude}"
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF0066FF)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                elevation = ButtonDefaults.buttonElevation(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Navigation,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            "Iniciar Navegação",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                        Text(
                                            "Tempo real com rota",
                                            fontSize = 12.sp,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Botão alternativo para Google Maps externo
                            OutlinedButton(
                                onClick = {
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
                                modifier = Modifier.fillMaxWidth(),
                                border = BorderStroke(2.dp, Color(0xFF00E676)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(
                                    Icons.Default.Map,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = Color(0xFF00E676)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Abrir no Google Maps",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = Color(0xFF00E676)
                                )
                            }
                        }
                    }
                }

                // Card de Paradas (se houver)
                servicoDetalhe.paradas?.let { paradas ->
                    if (paradas.isNotEmpty()) {
                        AnimatedVisibility(
                            visible = animateCards,
                            enter = slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = tween(500, delayMillis = 250)
                            ) + fadeIn()
                        ) {
                            FuturisticInfoCard(
                                title = "Paradas (${paradas.size})",
                                icon = Icons.Default.Route,
                                iconColor = Color(0xFFFFAB00)
                            ) {
                                paradas.sortedBy { it.ordem }.forEach { parada ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        // Indicador de parada
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(
                                                    when (parada.tipo) {
                                                        "ORIGEM" -> Color(0xFF4CAF50)
                                                        "DESTINO" -> Color(0xFFFF5252)
                                                        else -> Color(0xFFFFAB00)
                                                    },
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = parada.ordem.toString(),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = parada.tipo,
                                                fontSize = 12.sp,
                                                color = when (parada.tipo) {
                                                    "ORIGEM" -> Color(0xFF4CAF50)
                                                    "DESTINO" -> Color(0xFFFF5252)
                                                    else -> Color(0xFFFFAB00)
                                                },
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = parada.descricao,
                                                fontSize = 14.sp,
                                                color = Color.White,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = parada.endereco_completo,
                                                fontSize = 12.sp,
                                                color = Color.White.copy(alpha = 0.6f),
                                                lineHeight = 18.sp
                                            )
                                        }
                                    }

                                    if (parada != paradas.last()) {
                                        Divider(
                                            color = Color.White.copy(alpha = 0.1f),
                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(90.dp))
            }
        }

        // Botão flutuante para prosseguir
        AnimatedVisibility(
            visible = animateButton,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            ) + fadeIn(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = cardBg.copy(alpha = 0.98f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Button(
                    onClick = {
                        navController.navigate("tela_pedido_em_andamento/${servicoDetalhe.id}")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Brush.horizontalGradient(
                            colors = listOf(primaryGreen, darkGreen)
                        ).let { Color(0xFF00E676) }
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Prosseguir para o Pedido",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun FuturisticInfoCard(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF252D47).copy(alpha = 0.7f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                Color.White.copy(alpha = 0.1f),
                RoundedCornerShape(20.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header do card
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            iconColor.copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            content()
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
