package com.exemple.facilita.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import com.exemple.facilita.viewmodel.ServicoViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaPedidoEmAndamento(
    navController: NavController,
    servicoId: Int,
    servicoViewModel: ServicoViewModel
) {
    val context = LocalContext.current
    val servicoState by servicoViewModel.servicoState.collectAsState()

    // Modo Claro - Baseado no padrão do app
    val primaryGreen = Color(0xFF2E7D32)
    val accentCyan = Color(0xFF0097A7)
    val lightBg = Color(0xFFF5F5F5)
    val cardBg = Color.White
    val textPrimary = Color(0xFF212121)
    val textSecondary = Color(0xFF757575)
    val warningOrange = Color(0xFFFF6F00)

    // Carrega serviço ao iniciar
    LaunchedEffect(servicoId) {
        servicoViewModel.carregarServico(servicoId, context)
    }

    // Estados de animação
    var animateHeader by remember { mutableStateOf(false) }
    var animateCards by remember { mutableStateOf(false) }
    var animateButton by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animateHeader = true
        delay(200)
        animateCards = true
        delay(300)
        animateButton = true
    }

    // Animação de pulso
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

        when {
            servicoState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = primaryGreen)
                }
            }
            servicoState.servico != null -> {
                val servico = servicoState.servico!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    // Header
                    AnimatedVisibility(
                        visible = animateHeader,
                        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(cardBg)
                                .padding(horizontal = 20.dp, vertical = 16.dp)
                        ) {
                            IconButton(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier.align(Alignment.CenterStart)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Voltar",
                                    tint = textPrimary
                                )
                            }

                            Text(
                                text = "Pedido em Andamento",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary,
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
                        // Card de Status
                        AnimatedVisibility(
                            visible = animateCards,
                            enter = scaleIn(
                                spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                            ) + fadeIn()
                        ) {
                            Card(
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = cardBg
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Ícone de status com animação
                                    Box(
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // Glow effect
                                        Box(
                                            modifier = Modifier
                                                .size(100.dp)
                                                .scale(pulseScale)
                                                .background(
                                                    warningOrange.copy(alpha = 0.3f),
                                                    CircleShape
                                                )
                                                .blur(20.dp)
                                        )

                                        // Círculo principal
                                        Box(
                                            modifier = Modifier
                                                .size(70.dp)
                                                .background(
                                                    Brush.radialGradient(
                                                        colors = listOf(
                                                            warningOrange,
                                                            Color(0xFFE65100)
                                                        )
                                                    ),
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DirectionsCar,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(40.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = "Serviço em Andamento",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textPrimary,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "Status: ${servico.status}",
                                        fontSize = 14.sp,
                                        color = textSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        // Card de Informações do Serviço
                        AnimatedVisibility(
                            visible = animateCards,
                            enter = slideInHorizontally(
                                initialOffsetX = { -it },
                                animationSpec = tween(500)
                            ) + fadeIn()
                        ) {
                            InfoCard(
                                title = "Detalhes do Serviço",
                                icon = Icons.Default.Description,
                                iconColor = accentCyan,
                                cardBg = cardBg,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary
                            ) {
                                DetailRow("Descrição", servico.descricao, Icons.Default.Description, textPrimary, textSecondary)
                                Divider(color = textSecondary.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))
                                DetailRow("Valor", "R$ ${servico.valor}", Icons.Default.AttachMoney, textPrimary, textSecondary)
                                servico.tempo_estimado?.let {
                                    Divider(color = textSecondary.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))
                                    DetailRow("Tempo Estimado", "$it minutos", Icons.Default.Schedule, textPrimary, textSecondary)
                                }
                            }
                        }

                        // Card do Cliente
                        AnimatedVisibility(
                            visible = animateCards,
                            enter = slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec = tween(500, delayMillis = 100)
                            ) + fadeIn()
                        ) {
                            InfoCard(
                                title = "Cliente",
                                icon = Icons.Default.Person,
                                iconColor = primaryGreen,
                                cardBg = cardBg,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary
                            ) {
                                DetailRow("Nome", servico.contratante.usuario.nome, Icons.Default.Person, textPrimary, textSecondary)
                                Divider(color = textSecondary.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))
                                DetailRow("Telefone", servico.contratante.usuario.telefone, Icons.Default.Phone, textPrimary, textSecondary)
                                Divider(color = textSecondary.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))
                                DetailRow("Email", servico.contratante.usuario.email, Icons.Default.Email, textPrimary, textSecondary)

                                Spacer(modifier = Modifier.height(16.dp))

                                // Botões de contato
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                                data = Uri.parse("tel:${servico.contratante.usuario.telefone}")
                                            }
                                            context.startActivity(intent)
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = primaryGreen
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Phone,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Ligar")
                                    }

                                    Button(
                                        onClick = {
                                            navController.navigate(
                                                "chat_ao_vivo/${servico.id}/${servico.id_contratante}/" +
                                                        "${Uri.encode(servico.contratante.usuario.nome)}/" +
                                                        "${servico.id_prestador}/${Uri.encode(servico.prestador?.usuario?.nome ?: "Prestador")}"
                                            )
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = accentCyan
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Chat,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Chat")
                                    }
                                }
                            }
                        }

                        // Card de Localização
                        servico.localizacao?.let { localizacao ->
                            AnimatedVisibility(
                                visible = animateCards,
                                enter = slideInHorizontally(
                                    initialOffsetX = { -it },
                                    animationSpec = tween(500, delayMillis = 200)
                                ) + fadeIn()
                            ) {
                                InfoCard(
                                    title = "Localização",
                                    icon = Icons.Default.LocationOn,
                                    iconColor = Color(0xFFE53935),
                                    cardBg = cardBg,
                                    textPrimary = textPrimary,
                                    textSecondary = textSecondary
                                ) {
                                    DetailRow("Endereço", localizacao.endereco, Icons.Default.LocationOn, textPrimary, textSecondary)
                                    Divider(color = textSecondary.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))
                                    DetailRow("Bairro", localizacao.bairro, Icons.Default.Place, textPrimary, textSecondary)
                                    Divider(color = textSecondary.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))
                                    DetailRow("Cidade", "${localizacao.cidade} - ${localizacao.estado}", Icons.Default.LocationCity, textPrimary, textSecondary)

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Button(
                                        onClick = {
                                            navController.navigate("tela_mapa_rota/${servico.id}")
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFE53935)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Map,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Ver no Mapa")
                                    }
                                }
                            }
                        }

                        // Botão de Finalizar Serviço
                        AnimatedVisibility(
                            visible = animateButton,
                            enter = slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = tween(500)
                            ) + fadeIn()
                        ) {
                            Button(
                                onClick = {
                                    navController.navigate(
                                        "avaliacao_cliente/${servico.id}/" +
                                                "${Uri.encode(servico.contratante.usuario.nome)}/" +
                                                servico.valor
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = primaryGreen
                                ),
                                shape = RoundedCornerShape(16.dp),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 8.dp,
                                    pressedElevation = 12.dp
                                )
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Finalizar Serviço",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
            servicoState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = servicoState.error ?: "Erro desconhecido",
                            color = Color(0xFFE53935),
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { navController.popBackStack() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryGreen
                            )
                        ) {
                            Text("Voltar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    cardBg: Color,
    textPrimary: Color,
    textSecondary: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBg
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            iconColor.copy(alpha = 0.15f),
                            CircleShape
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
                    color = textPrimary
                )
            }

            content()
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    icon: ImageVector,
    textPrimary: Color,
    textSecondary: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textSecondary,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = textSecondary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 15.sp,
                color = textPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

