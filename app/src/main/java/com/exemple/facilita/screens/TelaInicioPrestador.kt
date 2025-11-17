package com.exemple.facilita.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.exemple.facilita.model.Solicitacao
import com.exemple.facilita.model.Servico
import com.exemple.facilita.model.ServicoDetalhe
import com.exemple.facilita.model.ContratanteDetalhe
import com.exemple.facilita.model.UsuarioDetalhe
import com.exemple.facilita.model.CategoriaDetalhe
import com.exemple.facilita.model.LocalizacaoDetalhe
import com.exemple.facilita.service.ApiResponse
import com.exemple.facilita.service.AceitarServicoApiResponse
import com.exemple.facilita.service.RetrofitFactory
import com.exemple.facilita.utils.TokenManager
import com.exemple.facilita.viewmodel.ServicoViewModel
import kotlinx.coroutines.delay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

// Função de conversão
fun Servico.toServicoDetalhe(): ServicoDetalhe {
    return ServicoDetalhe(
        id = this.id,
        id_contratante = this.id_contratante,
        id_prestador = this.id_prestador,
        id_categoria = this.id_categoria,
        descricao = this.descricao,
        status = this.status,
        data_solicitacao = this.data_solicitacao,
        data_conclusao = null,
        data_confirmacao = null,
        id_localizacao = this.localizacao?.id,
        valor = this.valor,
        tempo_estimado = this.categoria.tempo_medio,
        data_inicio = null,
        contratante = ContratanteDetalhe(
            id = this.contratante.id,
            necessidade = this.contratante.necessidade,
            id_usuario = this.contratante.usuario.id,
            id_localizacao = this.localizacao?.id,
            cpf = "",
            usuario = UsuarioDetalhe(
                id = this.contratante.usuario.id,
                nome = this.contratante.usuario.nome,
                senha_hash = null,
                foto_perfil = this.contratante.usuario.foto_perfil,
                email = this.contratante.usuario.email,
                telefone = this.contratante.usuario.telefone,
                tipo_conta = "CONTRATANTE",
                criado_em = this.data_solicitacao
            )
        ),
        prestador = null,
        categoria = CategoriaDetalhe(
            id = this.categoria.id,
            nome = this.categoria.nome,
            descricao = this.categoria.descricao,
            icone = this.categoria.icone,
            preco_base = this.categoria.preco_base,
            tempo_medio = this.categoria.tempo_medio
        ),
        localizacao = this.localizacao?.let { loc ->
            LocalizacaoDetalhe(
                id = loc.id,
                endereco = loc.logradouro,
                bairro = loc.bairro,
                cidade = loc.cidade,
                estado = "SP", // Valor padrão
                cep = loc.cep,
                numero = loc.numero,
                complemento = null,
                latitude = loc.latitude.toDoubleOrNull() ?: 0.0,
                longitude = loc.longitude.toDoubleOrNull() ?: 0.0
            )
        }
    )
}

@Composable
fun TelaInicioPrestador(
    navController: androidx.navigation.NavController,
    servicoViewModel: ServicoViewModel = viewModel()
) {
    val context = LocalContext.current
    var listaSolicitacoes by remember { mutableStateOf<List<Solicitacao>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var animateHeader by remember { mutableStateOf(false) }
    var animateBalance by remember { mutableStateOf(false) }

    val token = TokenManager.obterTokenComBearer(context) ?: ""

    // Animações de entrada
    LaunchedEffect(Unit) {
        delay(100)
        animateHeader = true
        delay(200)
        animateBalance = true
    }

    // Buscar solicitações da API
    LaunchedEffect(Unit) {
        val service = RetrofitFactory.getServicoService()
        service.getServicosDisponiveis(token).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    val data = response.body()?.data ?: emptyList()
                    listaSolicitacoes = data.map { servico ->
                        Solicitacao(
                            id = servico.id,
                            numero = servico.id,
                            cliente = servico.contratante.usuario.nome,
                            servico = servico.descricao,
                            distancia = servico.localizacao?.bairro ?: "Não informado",
                            horario = servico.data_solicitacao.substring(11, 16),
                            valor = "R$ ${servico.valor}"
                        )
                    }
                }
                isLoading = false
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(context, "Erro: ${t.message}", Toast.LENGTH_SHORT).show()
                isLoading = false
            }
        })
    }

    // Cores do tema - Verde profissional
    val primaryGreen = Color(0xFF2E7D32) // Verde escuro
    val secondaryGreen = Color(0xFF388E3C) // Verde médio
    val accentGreen = Color(0xFF4CAF50) // Verde suave
    val lightBg = Color(0xFFF5F5F5)
    val cardBg = Color.White
    val textPrimary = Color(0xFF212121)
    val textSecondary = Color(0xFF757575)

    // Animação de pulso para o saldo
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Scaffold(
        containerColor = lightBg,
        bottomBar = { com.exemple.facilita.components.BottomNavBar(navController = navController) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFAFAFA), Color(0xFFF5F5F5), Color(0xFFEEEEEE))
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Header com animação
                AnimatedVisibility(
                    visible = animateHeader,
                    enter = slideInVertically(
                        initialOffsetY = { -100 },
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                    ) + fadeIn()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Olá, Prestador 👋",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 28.sp,
                                color = textPrimary,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Seu trabalho facilita vidas.",
                                fontSize = 14.sp,
                                color = textSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Indicador de notificações
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notificações",
                                tint = primaryGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color(0xFFFF5252), CircleShape)
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-4).dp, y = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Card de Saldo Compacto
                AnimatedVisibility(
                    visible = animateBalance,
                    enter = slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
                    ) + fadeIn()
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .graphicsLayer {
                                scaleX = pulseScale
                                scaleY = pulseScale
                            },
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            primaryGreen,
                                            secondaryGreen,
                                            accentGreen
                                        ),
                                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                        end = androidx.compose.ui.geometry.Offset(1000f, 1000f)
                                    )
                                )
                                .padding(20.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.12f),
                                                Color.Transparent
                                            )
                                        ),
                                        CircleShape
                                    )
                                    .align(Alignment.TopEnd)
                                    .offset(x = 30.dp, y = (-30).dp)
                            )

                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.9f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        "Saldo Disponível",
                                        color = Color.White.copy(alpha = 0.95f),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "R$ 5.100,00",
                                    color = Color.White,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = 0.8f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Column {
                                            Text(
                                                "Hoje",
                                                color = Color.White.copy(alpha = 0.7f),
                                                fontSize = 10.sp
                                            )
                                            Text(
                                                "+R$ 320",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.weight(1f))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = 0.8f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Column {
                                            Text(
                                                "Serviços",
                                                color = Color.White.copy(alpha = 0.7f),
                                                fontSize = 10.sp
                                            )
                                            Text(
                                                "12",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Seção de Solicitações
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Solicitações Disponíveis",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            color = textPrimary
                        )
                        Text(
                            "${listaSolicitacoes.size} serviços aguardando",
                            fontSize = 13.sp,
                            color = textSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    IconButton(
                        onClick = { },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filtrar",
                            tint = primaryGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Lista de solicitações
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
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
                                    "Buscando solicitações...",
                                    color = textSecondary,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    listaSolicitacoes.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Inbox,
                                    contentDescription = null,
                                    tint = textSecondary.copy(alpha = 0.5f),
                                    modifier = Modifier.size(80.dp)
                                )
                                Text(
                                    "Nenhuma solicitação disponível",
                                    color = textPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    "Aguarde novas oportunidades de trabalho",
                                    color = textSecondary,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(listaSolicitacoes, key = { it.id }) { solicitacao ->
                                SolicitacaoCardPremium(
                                    solicitacao = solicitacao,
                                    token = token,
                                    primaryColor = primaryGreen,
                                    cardBg = cardBg,
                                    textSecondary = textSecondary,
                                    navController = navController,
                                    servicoViewModel = servicoViewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SolicitacaoCardPremium(
    solicitacao: Solicitacao,
    token: String,
    primaryColor: Color,
    cardBg: Color,
    textSecondary: Color,
    navController: androidx.navigation.NavController,
    servicoViewModel: ServicoViewModel
) {
    val context = LocalContext.current
    var showSuccessDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    fun aceitarServico() {
        if (isLoading) return
        isLoading = true

        val service = RetrofitFactory.getServicoService()
        service.aceitarServico(token, solicitacao.id).enqueue(object : Callback<AceitarServicoApiResponse> {
            override fun onResponse(call: Call<AceitarServicoApiResponse>, response: Response<AceitarServicoApiResponse>) {
                isLoading = false
                if (response.isSuccessful) {
                    val servico = response.body()?.data
                    if (servico != null) {
                        // Converter Servico para ServicoDetalhe
                        val servicoDetalhe = servico.toServicoDetalhe()

                        // Salvar no ViewModel
                        servicoViewModel.salvarServicoAceito(servicoDetalhe)

                        // Mostrar dialog de sucesso brevemente
                        showSuccessDialog = true

                        // Navegar para tela de detalhes após 1 segundo
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            navController.navigate("tela_detalhes_servico_aceito/${servicoDetalhe.id}")
                        }, 1000)
                    } else {
                        Toast.makeText(context, "Erro ao processar resposta", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Erro ao aceitar: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AceitarServicoApiResponse>, t: Throwable) {
                isLoading = false
                Toast.makeText(context, "Erro de conexão: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg.copy(alpha = 0.9f)),
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
                    .align(Alignment.TopEnd)
                    .offset(x = 40.dp, y = (-40).dp)
            )

            Column(modifier = Modifier.padding(20.dp)) {
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
                                .background(primaryColor, CircleShape)
                        )
                        Text(
                            "#${solicitacao.numero}",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121),
                            fontSize = 17.sp
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = primaryColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f))
                    ) {
                        Text(
                            solicitacao.valor,
                            color = primaryColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text("Cliente", color = textSecondary, fontSize = 12.sp)
                        Text(
                            solicitacao.cliente,
                            color = Color(0xFF212121),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Serviço", color = textSecondary, fontSize = 12.sp)
                        Text(
                            solicitacao.servico,
                            color = Color(0xFF212121),
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text("Local", color = textSecondary, fontSize = 11.sp)
                            Text(
                                solicitacao.distancia,
                                color = Color(0xFF212121),
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text("Horário", color = textSecondary, fontSize = 11.sp)
                            Text(solicitacao.horario, color = Color(0xFF212121), fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    textSecondary.copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { aceitarServico() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            disabledContainerColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f).height(56.dp),
                        enabled = !isLoading,
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 10.dp
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text("Aceitar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            if (!isLoading) {
                                Toast.makeText(context, "Serviço recusado", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f).height(56.dp),
                        enabled = !isLoading,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFFF5252)
                        ),
                        border = BorderStroke(2.dp, Color(0xFFFF5252))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Text("Recusar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showSuccessDialog) {
        SuccessDialog(
            onDismiss = { showSuccessDialog = false },
            onNavigate = {
                val clienteNomeEncoded = URLEncoder.encode(solicitacao.cliente, StandardCharsets.UTF_8.toString())
                val servicoDescricaoEncoded = URLEncoder.encode(solicitacao.servico, StandardCharsets.UTF_8.toString())
                val valorEncoded = URLEncoder.encode(solicitacao.valor, StandardCharsets.UTF_8.toString())
                val distanciaEncoded = URLEncoder.encode(solicitacao.distancia, StandardCharsets.UTF_8.toString())
                val horarioEncoded = URLEncoder.encode(solicitacao.horario, StandardCharsets.UTF_8.toString())

                navController.navigate(
                    "tela_detalhe_pedido/${solicitacao.id}/$clienteNomeEncoded/$servicoDescricaoEncoded/$valorEncoded/$distanciaEncoded/$horarioEncoded"
                )
            }
        )
    }
}

@Composable
fun SuccessDialog(
    onDismiss: () -> Unit,
    onNavigate: () -> Unit
) {
    var visible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(2000) // Mostra o dialog por 2 segundos
        visible = false // Inicia animação de saída
        delay(300) // Aguarda animação de saída
        onDismiss() // Fecha o dialog
        delay(100) // Pequeno delay
        onNavigate() // Navega para tela de detalhes
    }

    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(
                initialScale = 0.3f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            ) + fadeIn(),
            exit = scaleOut(targetScale = 1.1f) + fadeOut()
        ) {
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1F3A)),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Box(modifier = Modifier.padding(48.dp), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF4CAF50).copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                ),
                                CircleShape
                            )
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .background(
                                    Color(0xFF4CAF50).copy(alpha = 0.2f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Sucesso",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(52.dp)
                            )
                        }

                        Text(
                            text = "Serviço Aceito!",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Prepare-se para realizar o serviço",
                            fontSize = 15.sp,
                            color = Color(0xFFB0B8C8),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTelaInicioPrestador() {
    TelaInicioPrestador(navController = androidx.navigation.compose.rememberNavController())
}

