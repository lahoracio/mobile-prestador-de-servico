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
import androidx.lifecycle.ViewModelProvider
import com.exemple.facilita.model.Solicitacao
import com.exemple.facilita.viewmodel.CarteiraViewModel
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
import kotlinx.coroutines.isActive
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
    val carteiraViewModel: CarteiraViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(context.applicationContext as android.app.Application)
    )

    var listaSolicitacoes by remember { mutableStateOf<List<Solicitacao>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var animateHeader by remember { mutableStateOf(false) }
    var animateBalance by remember { mutableStateOf(false) }
    var mostrarSaldo by remember { mutableStateOf(false) }

    // Set para armazenar IDs de serviços recusados (persiste durante a sessão)
    val servicosRecusados = remember { mutableStateSetOf<Int>() }

    val token = TokenManager.obterTokenComBearer(context) ?: ""

    // Obter dados reais do usuário logado
    val usuarioId = remember {
        TokenManager.obterUsuarioId(context)?.toString() ?: "0"
    }

    val nomeUsuario = remember {
        TokenManager.obterNomeUsuario(context) ?: "Prestador"
    }

    // Obter saldo da carteira
    val carteira by carteiraViewModel.carteira.collectAsState()
    val saldoReal = carteira?.saldo ?: 0.0

    // Animações de entrada
    LaunchedEffect(Unit) {
        delay(100)
        animateHeader = true
        delay(200)
        animateBalance = true
    }

    // Carregar carteira com ID real do usuário
    LaunchedEffect(usuarioId) {
        carteiraViewModel.carregarCarteira(usuarioId)
    }

    // Buscar solicitações da API com atualização automática a cada 10 segundos
    LaunchedEffect(token) {
        if (token.isEmpty()) {
            isLoading = false
            return@LaunchedEffect
        }

        fun buscarSolicitacoes() {
            try {
                val service = RetrofitFactory.getServicoService()
                val call = service.getServicosDisponiveis(token)

                // .enqueue() já executa em background thread automaticamente
                call.enqueue(object : Callback<ApiResponse> {
                    override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                        if (response.isSuccessful) {
                            val data = response.body()?.data ?: emptyList()
                            // Filtrar serviços recusados antes de mapear
                            listaSolicitacoes = data
                                .filter { servico -> servico.id !in servicosRecusados }
                                .map { servico ->
                                // Montar localização de forma mais completa
                                val localizacao = servico.localizacao?.let { loc ->
                                    buildString {
                                        if (!loc.logradouro.isNullOrBlank()) {
                                            append(loc.logradouro)
                                        }
                                        if (!loc.numero.isNullOrBlank()) {
                                            if (isNotEmpty()) append(", ")
                                            append(loc.numero)
                                        }
                                        if (!loc.bairro.isNullOrBlank()) {
                                            if (isNotEmpty()) append(" - ")
                                            append(loc.bairro)
                                        }
                                        if (!loc.cidade.isNullOrBlank()) {
                                            if (isNotEmpty()) append(", ")
                                            append(loc.cidade)
                                        }
                                        // Se nada foi adicionado, tenta o bairro ou cidade
                                        if (isEmpty()) {
                                            append(loc.bairro ?: loc.cidade ?: "Localização disponível")
                                        }
                                    }
                                } ?: "Não informado"

                                Solicitacao(
                                    id = servico.id,
                                    numero = servico.id,
                                    cliente = servico.contratante.usuario.nome,
                                    servico = servico.descricao,
                                    distancia = localizacao,
                                    horario = servico.data_solicitacao.substring(11, 16),
                                    valor = "R$ ${servico.valor}"
                                )
                            }
                        }
                        isLoading = false
                    }

                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                        // Não mostrar toast em atualizações automáticas, apenas na primeira
                        if (isLoading) {
                            Toast.makeText(context, "Erro: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                        isLoading = false
                    }
                })
            } catch (e: Exception) {
                android.util.Log.e("TelaInicioPrestador", "Erro ao buscar solicitações: ${e.message}")
                isLoading = false
            }
        }

        // Busca inicial
        buscarSolicitacoes()

        // Atualização automática a cada 10 segundos
        while (isActive) {
            delay(10000) // 10 segundos
            buscarSolicitacoes()
        }
    }

    // Cores do tema - Verde profissional
    val primaryGreen = Color(0xFF2E7D32) // Verde escuro
    val lightBg = Color(0xFFF5F5F5)
    val cardBg = Color.White
    val textPrimary = Color(0xFF212121)
    val textSecondary = Color(0xFF757575)


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
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(color = primaryGreen, strokeWidth = 3.dp)
                            Text("Buscando solicitações...", color = textSecondary, fontSize = 14.sp)
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header
                        item {
                            AnimatedVisibility(
                                visible = animateHeader,
                                enter = slideInVertically(initialOffsetY = { -100 }, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Olá, $nomeUsuario", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = textPrimary)
                                        Text("Seu trabalho facilita vidas.", fontSize = 13.sp, color = textSecondary, fontWeight = FontWeight.Normal)
                                    }

                                    Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Notifications, "Notificações", tint = primaryGreen, modifier = Modifier.size(24.dp))
                                        Box(modifier = Modifier.size(10.dp).background(Color(0xFFFF5252), CircleShape).align(Alignment.TopEnd).offset(x = (-4).dp, y = 4.dp))
                                    }
                                }
                            }
                        }

                        // Card de Saldo
                        item {
                            AnimatedVisibility(
                                visible = animateBalance,
                                enter = slideInHorizontally(initialOffsetX = { -it }, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)) + fadeIn()
                            ) {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                                ) {
                                    Box(modifier = Modifier.background(Brush.horizontalGradient(listOf(Color(0xFF3C604B), Color(0xFF00B14F)))).padding(16.dp)) {
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Icon(Icons.Default.AccountBalanceWallet, null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(16.dp))
                                                    Text("Saldo Disponível", color = Color.White.copy(alpha = 0.95f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                                }

                                                IconButton(onClick = { mostrarSaldo = !mostrarSaldo }, modifier = Modifier.size(32.dp)) {
                                                    Icon(
                                                        if (mostrarSaldo) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                        if (mostrarSaldo) "Ocultar saldo" else "Mostrar saldo",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = if (mostrarSaldo) "R$ %.2f".format(saldoReal) else "R$ ••••••",
                                                color = Color.White,
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Seção de Solicitações
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Solicitações Disponíveis", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textPrimary)
                                    Text("${listaSolicitacoes.size} serviços aguardando", fontSize = 12.sp, color = textSecondary, fontWeight = FontWeight.Normal)
                                }

                                IconButton(onClick = { }, modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Color.White)) {
                                    Icon(Icons.Default.FilterList, "Filtrar", tint = primaryGreen, modifier = Modifier.size(20.dp))
                                }
                            }
                        }

                        // Lista ou estado vazio
                        if (listaSolicitacoes.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Icon(Icons.Default.Inbox, null, tint = textSecondary.copy(alpha = 0.5f), modifier = Modifier.size(80.dp))
                                        Text("Nenhuma solicitação disponível", color = textPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                                        Text("Aguarde novas oportunidades de trabalho", color = textSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)
                                    }
                                }
                            }
                        } else {
                            items(listaSolicitacoes, key = { it.id }) { solicitacao ->
                                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                                    SolicitacaoCardPremium(
                                        solicitacao = solicitacao,
                                        token = token,
                                        primaryColor = primaryGreen,
                                        cardBg = cardBg,
                                        textSecondary = textSecondary,
                                        navController = navController,
                                        servicoViewModel = servicoViewModel,
                                        onRecusar = { id ->
                                            // Adicionar ao Set de recusados (persiste durante a sessão)
                                            servicosRecusados.add(id)
                                            // Remover da lista
                                            listaSolicitacoes = listaSolicitacoes.filter { it.id != id }
                                            android.util.Log.d("TelaInicioPrestador", "✅ Serviço $id recusado. Total recusados: ${servicosRecusados.size}")
                                        }
                                    )
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
fun SolicitacaoCardPremium(
    solicitacao: Solicitacao,
    token: String,
    primaryColor: Color,
    cardBg: Color,
    textSecondary: Color,
    navController: androidx.navigation.NavController,
    servicoViewModel: ServicoViewModel,
    onRecusar: (Int) -> Unit = {}
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
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(48.dp),
                        enabled = !isLoading,
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text("Aceitar", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            if (!isLoading) {
                                onRecusar(solicitacao.id)
                                Toast.makeText(context, "Serviço recusado", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(48.dp),
                        enabled = !isLoading,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFFF5252)
                        ),
                        border = BorderStroke(1.5.dp, Color(0xFFFF5252))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Text("Recusar", fontSize = 15.sp, fontWeight = FontWeight.Bold)
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
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Box(modifier = Modifier.padding(48.dp), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF019D31).copy(alpha = 0.15f),
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
                                    Color(0xFF019D31).copy(alpha = 0.1f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Sucesso",
                                tint = Color(0xFF019D31),
                                modifier = Modifier.size(52.dp)
                            )
                        }

                        Text(
                            text = "Serviço Aceito!",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF212121),
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Prepare-se para realizar o serviço",
                            fontSize = 15.sp,
                            color = Color(0xFF666666),
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
