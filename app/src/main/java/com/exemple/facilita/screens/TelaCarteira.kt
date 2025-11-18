package com.exemple.facilita.screens


import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.exemple.facilita.components.BottomNavBar
import com.exemple.facilita.model.*
import com.exemple.facilita.viewmodel.CarteiraViewModel
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaCarteira(navController: NavController) {
    val context = LocalContext.current
    val viewModel: CarteiraViewModel = viewModel()

    val carteira by viewModel.carteira.collectAsState()
    val transacoes by viewModel.transacoes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var mostrarSaldo by remember { mutableStateOf(true) }
    var mostrarDialogDepositar by remember { mutableStateOf(false) }
    var mostrarDialogSacar by remember { mutableStateOf(false) }
    var mostrarDialogContaBancaria by remember { mutableStateOf(false) }

    // Obter nome real do usuário do TokenManager
    val nomeUsuario = remember {
        com.exemple.facilita.utils.TokenManager.obterNomeUsuario(context) ?: "Usuário"
    }
    val token = remember {
        com.exemple.facilita.utils.TokenManager.obterToken(context) ?: ""
    }

    val contasBancarias by viewModel.contasBancarias.collectAsState()

    // Criar um objeto de saldo a partir da carteira
    val saldo = remember(carteira) {
        SaldoCarteira(
            saldoDisponivel = carteira?.saldo ?: 0.0,
            saldoBloqueado = carteira?.saldoBloqueado ?: 0.0
        )
    }

    // Obter ID real do usuário logado
    val usuarioId = remember {
        com.exemple.facilita.utils.TokenManager.obterUsuarioId(context)?.toString() ?: "0"
    }

    LaunchedEffect(usuarioId) {
        viewModel.carregarCarteira(usuarioId)
        viewModel.carregarTransacoes(usuarioId)
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            LazyColumn(Modifier.fillMaxSize()) {
                item {
                    HeaderCarteira(
                        nomeUsuario = nomeUsuario,
                        saldo = saldo,
                        mostrarSaldo = mostrarSaldo,
                        onToggleSaldo = { mostrarSaldo = !mostrarSaldo },
                        visible = visible,
                        onAdicionarContaClick = { mostrarDialogContaBancaria = true }
                    )
                }

                item {
                    BotoesAcao(
                        visible = visible,
                        onDepositarClick = { mostrarDialogDepositar = true },
                        onSacarClick = { mostrarDialogSacar = true }
                    )
                }

                item {
                    Box(
                        Modifier.fillMaxWidth()
                            .background(Color(0xFFF4F4F4))
                            .padding(vertical = 16.dp, horizontal = 20.dp)
                    ) {
                        Text(
                            "Histórico de Movimentações",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D2D2D)
                        )
                    }
                }

                items(transacoes) { transacao ->
                    val transacaoCarteira = TransacaoCarteira(
                        id = transacao.id,
                        tipo = transacao.tipo,
                        valor = transacao.valor,
                        data = transacao.dataTransacao,
                        descricao = transacao.descricao,
                        status = transacao.status
                    )
                    ItemTransacao(transacaoCarteira, visible)
                }

                if (transacoes.isEmpty()) {
                    item {
                        Box(
                            Modifier.fillMaxWidth().padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.AccountBalanceWallet,
                                    null,
                                    modifier = Modifier.size(64.dp),
                                    tint = Color(0xFF6D6D6D).copy(alpha = 0.5f)
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "Nenhuma movimentação ainda",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color(0xFF6D6D6D)
                                )
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }
            }

            if (isLoading) {
                Box(
                    Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF00B14F))
                }
            }
        }
    }

    if (mostrarDialogDepositar) {
        DialogDepositoSimplificado(
            viewModel = viewModel,
            token = token,
            onDismiss = { mostrarDialogDepositar = false }
        )
    }

    if (mostrarDialogSacar) {
        DialogSaqueSimplificado(
            viewModel = viewModel,
            token = token,
            saldoDisponivel = saldo.saldoDisponivel,
            contasBancarias = contasBancarias,
            onDismiss = { mostrarDialogSacar = false },
            onAddBankAccount = { mostrarDialogContaBancaria = true }
        )
    }

    if (mostrarDialogContaBancaria) {
        DialogAdicionarContaBancaria(
            viewModel = viewModel,
            onDismiss = { mostrarDialogContaBancaria = false }
        )
    }
}

@Composable
private fun HeaderCarteira(
    nomeUsuario: String,
    saldo: SaldoCarteira,
    mostrarSaldo: Boolean,
    onToggleSaldo: () -> Unit,
    visible: Boolean,
    onAdicionarContaClick: () -> Unit
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("pt", "BR")) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 800), label = ""
    )

    var mostrarMenu by remember { mutableStateOf(false) }

    Box(
        Modifier.fillMaxWidth().height(230.dp)
            .background(Brush.horizontalGradient(listOf(Color(0xFF3C604B), Color(0xFF00B14F))))
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .alpha(alpha)
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    Modifier.size(48.dp).clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        nomeUsuario.take(2).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text("Olá,", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                    Text(nomeUsuario, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(onClick = {}) {
                    Icon(Icons.Default.Notifications, "Notificações", tint = Color.White)
                }

                Box {
                    IconButton(onClick = { mostrarMenu = true }) {
                        Icon(Icons.Default.MoreVert, "Menu", tint = Color.White)
                    }

                    DropdownMenu(
                        expanded = mostrarMenu,
                        onDismissRequest = { mostrarMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Adicionar Conta Bancária") },
                            onClick = {
                                mostrarMenu = false
                                onAdicionarContaClick()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = Color(0xFF00B14F)
                                )
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth().height(80.dp)
            ) {
                Box(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column {
                            Text("Saldo Disponível", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (mostrarSaldo) currencyFormat.format(saldo.saldoDisponivel) else "R$ ••••••",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                        }

                        IconButton(onClick = onToggleSaldo, modifier = Modifier.size(40.dp)) {
                            Icon(
                                if (mostrarSaldo) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                "Mostrar/ocultar",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text("💳 Acompanhe seu saldo após os serviços realizados", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun BotoesAcao(
    visible: Boolean,
    onDepositarClick: () -> Unit,
    onSacarClick: () -> Unit
) {
    var buttonsVisible by remember { mutableStateOf(false) }
    LaunchedEffect(visible) {
        if (visible) {
            delay(400)
            buttonsVisible = true
        }
    }

    Column(
        Modifier.fillMaxWidth()
            .background(Color(0xFFF4F4F4))
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            AnimatedVisibility(
                visible = buttonsVisible,
                enter = fadeIn() + slideInVertically(),
                modifier = Modifier.weight(1f)
            ) {
                BotaoAcao("Depositar", Icons.Default.Add, Color(0xFF00B14F), onDepositarClick)
            }

            AnimatedVisibility(
                visible = buttonsVisible,
                enter = fadeIn() + slideInVertically(),
                modifier = Modifier.weight(1f)
            ) {
                BotaoAcao("Sacar", Icons.AutoMirrored.Filled.TrendingDown, Color(0xFF3C604B), onSacarClick)
            }
        }
    }
}

@Composable
private fun BotaoAcao(
    text: String,
    icon: ImageVector,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = Modifier.fillMaxWidth().height(100.dp).clickable(onClick = onClick)
    ) {
        Column(
            Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier.size(48.dp).clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(text, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
        }
    }
}

@Composable
private fun ItemTransacao(transacao: TransacaoCarteira, visible: Boolean) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("pt", "BR")) }
    var itemVisible by remember { mutableStateOf(false) }
    LaunchedEffect(visible) {
        if (visible) {
            delay(100)
            itemVisible = true
        }
    }

    AnimatedVisibility(
        visible = itemVisible,
        enter = fadeIn() + slideInVertically()
    ) {
        Box(
            Modifier.fillMaxWidth()
                .background(Color(0xFFF4F4F4))
                .padding(horizontal = 20.dp, vertical = 6.dp)
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val iconColor = when (transacao.tipo) {
                        TipoTransacao.DEPOSITO -> Color(0xFF00B14F)
                        TipoTransacao.SAQUE -> Color(0xFFFF6B6B)
                        TipoTransacao.PAGAMENTO_SERVICO -> Color(0xFF3C604B)
                        TipoTransacao.RECEBIMENTO -> Color(0xFF4CAF50)
                        TipoTransacao.CASHBACK -> Color(0xFFFFB300)
                        TipoTransacao.ESTORNO -> Color(0xFF2196F3)
                        TipoTransacao.PAGAMENTO -> Color(0xFF9C27B0)
                        TipoTransacao.TAXA -> Color(0xFF607D8B)
                    }

                    Box(
                        Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))
                            .background(iconColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            when (transacao.tipo) {
                                TipoTransacao.DEPOSITO -> Icons.Default.Add
                                TipoTransacao.SAQUE -> Icons.AutoMirrored.Filled.TrendingDown
                                TipoTransacao.PAGAMENTO_SERVICO -> Icons.Default.ShoppingCart
                                TipoTransacao.RECEBIMENTO -> Icons.AutoMirrored.Filled.TrendingUp
                                TipoTransacao.CASHBACK -> Icons.Default.CardGiftcard
                                TipoTransacao.ESTORNO -> Icons.Default.Refresh
                                TipoTransacao.PAGAMENTO -> Icons.Default.Payment
                                TipoTransacao.TAXA -> Icons.Default.Receipt
                            },
                            null,
                            tint = iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(Modifier.weight(1f)) {
                        Text(
                            transacao.descricao,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF2D2D2D),
                            maxLines = 1
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(transacao.data, fontSize = 10.sp, color = Color(0xFF6D6D6D))
                    }

                    Text(
                        if (transacao.valor >= 0) "+ ${currencyFormat.format(transacao.valor)}"
                        else "- ${currencyFormat.format(kotlin.math.abs(transacao.valor))}",
                        color = if (transacao.valor >= 0) Color(0xFF00B14F) else Color(0xFFFF6B6B),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogDepositoSimplificado(
    viewModel: CarteiraViewModel,
    token: String,
    onDismiss: () -> Unit
) {
    var valor by remember { mutableStateOf("") }
    var showAnimation by remember { mutableStateOf(false) }
    var metodoPagamento by remember { mutableStateOf<String?>(null) }
    var mostrarFormularioCartao by remember { mutableStateOf(false) }

    // Dados do cartão
    var numeroCartao by remember { mutableStateOf("") }
    var nomeCompleto by remember { mutableStateOf("") }
    var mesExpiracao by remember { mutableStateOf("") }
    var anoExpiracao by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }

    var mensagemErro by remember { mutableStateOf<String?>(null) }
    var mensagemSucesso by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { showAnimation = true }

    val scale by animateFloatAsState(
        targetValue = if (showAnimation) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = ""
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.scale(scale)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when {
                    mensagemSucesso -> {
                        // Tela de sucesso
                        Icon(
                            Icons.Default.CheckCircle,
                            null,
                            tint = Color(0xFF00B14F),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("Depósito Realizado!", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text("Seu saldo foi atualizado", fontSize = 14.sp, color = Color(0xFF6D6D6D))
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B14F))
                        ) {
                            Text("Concluir")
                        }
                    }

                    mostrarFormularioCartao -> {
                        // Formulário do cartão
                        Text("Dados do Cartão", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))

                        OutlinedTextField(
                            value = numeroCartao,
                            onValueChange = { if (it.length <= 19) numeroCartao = formatarNumeroCartao(it) },
                            label = { Text("Número do Cartão") },
                            placeholder = { Text("0000 0000 0000 0000") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = nomeCompleto,
                            onValueChange = { nomeCompleto = it.uppercase() },
                            label = { Text("Nome no Cartão") },
                            placeholder = { Text("NOME COMPLETO") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(12.dp))

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = mesExpiracao,
                                onValueChange = { if (it.length <= 2) mesExpiracao = it },
                                label = { Text("Mês") },
                                placeholder = { Text("MM") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = anoExpiracao,
                                onValueChange = { if (it.length <= 2) anoExpiracao = it },
                                label = { Text("Ano") },
                                placeholder = { Text("AA") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = cvv,
                                onValueChange = { if (it.length <= 4) cvv = it },
                                label = { Text("CVV") },
                                placeholder = { Text("123") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        mensagemErro?.let {
                            Spacer(Modifier.height(8.dp))
                            Text(it, color = Color.Red, fontSize = 12.sp, textAlign = TextAlign.Center)
                        }

                        Spacer(Modifier.height(16.dp))

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { mostrarFormularioCartao = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Voltar")
                            }

                            Button(
                                onClick = {
                                    if (validarDadosCartao(numeroCartao, nomeCompleto, mesExpiracao, anoExpiracao, cvv)) {
                                        viewModel.depositarViaCartao(
                                            token = token,
                                            valor = valor.replace(",", ".").toDoubleOrNull() ?: 0.0,
                                            numeroCartao = numeroCartao.replace(" ", ""),
                                            mesExpiracao = mesExpiracao,
                                            anoExpiracao = anoExpiracao,
                                            cvv = cvv,
                                            nomeCompleto = nomeCompleto,
                                            onSuccess = {
                                                mensagemSucesso = true
                                            },
                                            onError = { erro ->
                                                mensagemErro = erro
                                            }
                                        )
                                    } else {
                                        mensagemErro = "Preencha todos os campos corretamente"
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B14F))
                            ) {
                                Text("Pagar")
                            }
                        }
                    }

                    metodoPagamento != null -> {
                        // PIX selecionado - Mostra QR Code
                        if (metodoPagamento == "PIX") {
                            val pixQrCode by viewModel.pixQrCode.collectAsState()
                            val pixQrCodeBase64 by viewModel.pixQrCodeBase64.collectAsState()

                            if (pixQrCode != null) {
                                // QR Code gerado com sucesso
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Pagar com PIX", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "R$ ${valor.replace(".", ",")}",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00B14F)
                                    )

                                    Spacer(Modifier.height(16.dp))

                                    // QR Code
                                    Card(
                                        modifier = Modifier.size(200.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(4.dp)
                                    ) {
                                        Box(
                                            Modifier.fillMaxSize().padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.QrCode2,
                                                contentDescription = "QR Code",
                                                modifier = Modifier.size(160.dp),
                                                tint = Color(0xFF2D2D2D)
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(16.dp))

                                    Text(
                                        "Escaneie o QR Code com o app do seu banco",
                                        fontSize = 12.sp,
                                        color = Color(0xFF6D6D6D),
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(Modifier.height(12.dp))

                                    // Código PIX
                                    OutlinedCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                                    ) {
                                        Column(Modifier.padding(12.dp)) {
                                            Text(
                                                "Código PIX:",
                                                fontSize = 10.sp,
                                                color = Color(0xFF6D6D6D)
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                pixQrCode!!.take(40) + "...",
                                                fontSize = 11.sp,
                                                color = Color(0xFF2D2D2D),
                                                maxLines = 2
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(12.dp))

                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                metodoPagamento = null
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Voltar")
                                        }

                                        Button(
                                            onClick = {
                                                // Confirmar pagamento PIX - Atualizar saldo e adicionar transação
                                                val valorDouble = valor.replace(",", ".").toDoubleOrNull() ?: 0.0
                                                viewModel.confirmarPagamentoPix(valorDouble)
                                                mensagemSucesso = true
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF00B14F)
                                            )
                                        ) {
                                            Text("Já Paguei")
                                        }
                                    }
                                }
                            } else {
                                // Aguardando QR Code
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Gerando QR Code PIX...", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(16.dp))
                                    CircularProgressIndicator(color = Color(0xFF00B14F))
                                    Spacer(Modifier.height(16.dp))
                                    Text(
                                        "Aguarde alguns instantes",
                                        fontSize = 14.sp,
                                        color = Color(0xFF6D6D6D)
                                    )

                                    LaunchedEffect(Unit) {
                                        viewModel.depositarViaPix(
                                            token = token,
                                            valor = valor.replace(",", ".").toDoubleOrNull() ?: 0.0,
                                            onSuccess = {
                                                // QR Code gerado
                                            },
                                            onError = { erro ->
                                                mensagemErro = erro
                                                metodoPagamento = null
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        // Tela inicial
                        Box(
                            Modifier.size(70.dp).clip(CircleShape)
                                .background(Color(0xFF00B14F).copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, null, tint = Color(0xFF00B14F), modifier = Modifier.size(36.dp))
                        }

                        Spacer(Modifier.height(16.dp))

                        Text("Depositar Saldo", fontSize = 22.sp, fontWeight = FontWeight.Bold)

                        Spacer(Modifier.height(8.dp))

                        Text(
                            "Digite o valor e escolha o método",
                            fontSize = 13.sp,
                            color = Color(0xFF6D6D6D),
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(20.dp))

                        OutlinedTextField(
                            value = valor,
                            onValueChange = {
                                if (it.isEmpty() || it.matches(Regex("^\\d*[.,]?\\d{0,2}\$"))) {
                                    valor = it
                                }
                            },
                            label = { Text("Valor") },
                            placeholder = { Text("0,00") },
                            leadingIcon = {
                                Text("R$", fontWeight = FontWeight.Bold, color = Color(0xFF00B14F), fontSize = 16.sp)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00B14F),
                                focusedLabelColor = Color(0xFF00B14F)
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))

                        // Botões de método de pagamento
                        Text("Escolha o método:", fontSize = 14.sp, fontWeight = FontWeight.Bold)

                        Spacer(Modifier.height(12.dp))

                        // PIX
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                if (valor.isNotEmpty() && (valor.toDoubleOrNull() ?: 0.0) > 0) {
                                    metodoPagamento = "PIX"
                                }
                            },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF00B14F).copy(alpha = 0.1f))
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.QrCode2, null, tint = Color(0xFF00B14F))
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("PIX", fontWeight = FontWeight.Bold)
                                    Text("Instantâneo", fontSize = 12.sp, color = Color(0xFF6D6D6D))
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // Cartão de Crédito
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                if (valor.isNotEmpty() && (valor.toDoubleOrNull() ?: 0.0) > 0) {
                                    mostrarFormularioCartao = true
                                }
                            },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2196F3).copy(alpha = 0.1f))
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CreditCard, null, tint = Color(0xFF2196F3))
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("Cartão de Crédito", fontWeight = FontWeight.Bold)
                                    Text("Aprovação imediata", fontSize = 12.sp, color = Color(0xFF6D6D6D))
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancelar")
                        }
                    }
                }
            }
        }
    }
}

private fun formatarNumeroCartao(input: String): String {
    val digitos = input.replace(" ", "")
    return digitos.chunked(4).joinToString(" ")
}

private fun validarDadosCartao(
    numero: String,
    nome: String,
    mes: String,
    ano: String,
    cvv: String
): Boolean {
    return numero.replace(" ", "").length >= 13 &&
            nome.length >= 3 &&
            mes.toIntOrNull() in 1..12 &&
            ano.length == 2 &&
            cvv.length >= 3
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogAdicionarContaBancaria(
    viewModel: CarteiraViewModel,
    onDismiss: () -> Unit
) {
    var banco by remember { mutableStateOf("") }
    var agencia by remember { mutableStateOf("") }
    var conta by remember { mutableStateOf("") }
    var tipoConta by remember { mutableStateOf("CORRENTE") }
    var nomeCompleto by remember { mutableStateOf("") }
    var cpf by remember { mutableStateOf("") }
    var isPrincipal by remember { mutableStateOf(false) }
    var showAnimation by remember { mutableStateOf(false) }
    var mensagemErro by remember { mutableStateOf<String?>(null) }
    var mensagemSucesso by remember { mutableStateOf(false) }
    var mostrarSeletorBanco by remember { mutableStateOf(false) }
    var mostrarSeletorTipoConta by remember { mutableStateOf(false) }

    val bancosPredefinidos = listOf(
        "Banco do Brasil",
        "Caixa Econômica Federal",
        "Bradesco",
        "Itaú",
        "Santander",
        "Nubank",
        "Inter",
        "C6 Bank",
        "BTG Pactual",
        "Banco Original",
        "Outro"
    )

    LaunchedEffect(Unit) { showAnimation = true }

    val scale by animateFloatAsState(
        targetValue = if (showAnimation) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = ""
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.scale(scale)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when {
                    mensagemSucesso -> {
                        // Tela de sucesso
                        Icon(
                            Icons.Default.CheckCircle,
                            null,
                            tint = Color(0xFF00B14F),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("Conta Adicionada!", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Sua conta bancária foi cadastrada com sucesso",
                            fontSize = 14.sp,
                            color = Color(0xFF6D6D6D),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B14F))
                        ) {
                            Text("Concluir")
                        }
                    }

                    else -> {
                        Box(
                            Modifier.size(70.dp).clip(CircleShape)
                                .background(Color(0xFF00B14F).copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AccountBalance,
                                null,
                                tint = Color(0xFF00B14F),
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        Text("Adicionar Conta Bancária", fontSize = 20.sp, fontWeight = FontWeight.Bold)

                        Spacer(Modifier.height(8.dp))

                        Text(
                            "Cadastre uma conta para realizar saques",
                            fontSize = 13.sp,
                            color = Color(0xFF6D6D6D),
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(20.dp))

                        // Seletor de Banco
                        Text(
                            "Banco",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D2D2D),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(4.dp))

                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth().clickable { mostrarSeletorBanco = true },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.AccountBalance,
                                    null,
                                    tint = Color(0xFF00B14F)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    banco.ifEmpty { "Selecione o banco" },
                                    modifier = Modifier.weight(1f),
                                    color = if (banco.isEmpty()) Color(0xFF6D6D6D) else Color(0xFF2D2D2D)
                                )
                                Icon(Icons.Default.KeyboardArrowDown, null, tint = Color(0xFF6D6D6D))
                            }
                        }

                        if (mostrarSeletorBanco) {
                            AlertDialog(
                                onDismissRequest = { mostrarSeletorBanco = false }
                            ) {
                                Card(shape = RoundedCornerShape(16.dp)) {
                                    Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                                        Text("Selecione o banco", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.height(16.dp))

                                        bancosPredefinidos.forEach { bancoItem ->
                                            OutlinedCard(
                                                modifier = Modifier.fillMaxWidth().clickable {
                                                    banco = bancoItem
                                                    mostrarSeletorBanco = false
                                                },
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (banco == bancoItem)
                                                        Color(0xFF00B14F).copy(alpha = 0.1f)
                                                    else Color.White
                                                )
                                            ) {
                                                Row(
                                                    Modifier.padding(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.Default.AccountBalance, null, tint = Color(0xFF00B14F))
                                                    Spacer(Modifier.width(12.dp))
                                                    Text(bancoItem, fontWeight = FontWeight.Medium)
                                                }
                                            }
                                            Spacer(Modifier.height(8.dp))
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Agência
                        OutlinedTextField(
                            value = agencia,
                            onValueChange = { if (it.length <= 6) agencia = it },
                            label = { Text("Agência") },
                            placeholder = { Text("0001") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(12.dp))

                        // Tipo de Conta e Número
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Tipo de Conta
                            Column(Modifier.weight(1f)) {
                                Text("Tipo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                OutlinedCard(
                                    modifier = Modifier.fillMaxWidth().clickable { mostrarSeletorTipoConta = true },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            if (tipoConta == "CORRENTE") "Corrente" else "Poupança",
                                            fontSize = 13.sp
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Icon(Icons.Default.KeyboardArrowDown, null, modifier = Modifier.size(16.dp))
                                    }
                                }

                                if (mostrarSeletorTipoConta) {
                                    AlertDialog(onDismissRequest = { mostrarSeletorTipoConta = false }) {
                                        Card {
                                            Column(Modifier.padding(16.dp)) {
                                                Text("Tipo de conta", fontWeight = FontWeight.Bold)
                                                Spacer(Modifier.height(12.dp))
                                                listOf("CORRENTE" to "Corrente", "POUPANCA" to "Poupança").forEach { (valor, nome) ->
                                                    OutlinedCard(
                                                        modifier = Modifier.fillMaxWidth().clickable {
                                                            tipoConta = valor
                                                            mostrarSeletorTipoConta = false
                                                        }
                                                    ) {
                                                        Text(nome, Modifier.padding(12.dp))
                                                    }
                                                    Spacer(Modifier.height(8.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Número da Conta
                            OutlinedTextField(
                                value = conta,
                                onValueChange = { if (it.length <= 15) conta = it },
                                label = { Text("Conta") },
                                placeholder = { Text("12345-6") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(2f)
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // Nome Completo
                        OutlinedTextField(
                            value = nomeCompleto,
                            onValueChange = { nomeCompleto = it },
                            label = { Text("Nome Completo") },
                            placeholder = { Text("Seu nome completo") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(12.dp))

                        // CPF
                        OutlinedTextField(
                            value = cpf,
                            onValueChange = { if (it.length <= 14) cpf = formatarCPF(it) },
                            label = { Text("CPF") },
                            placeholder = { Text("000.000.000-00") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))

                        // Checkbox Conta Principal
                        Row(
                            Modifier.fillMaxWidth().clickable { isPrincipal = !isPrincipal },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isPrincipal,
                                onCheckedChange = { isPrincipal = it },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00B14F))
                            )
                            Text("Definir como conta principal")
                        }

                        mensagemErro?.let {
                            Spacer(Modifier.height(8.dp))
                            Text(it, color = Color.Red, fontSize = 12.sp, textAlign = TextAlign.Center)
                        }

                        Spacer(Modifier.height(24.dp))

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Cancelar")
                            }

                            Button(
                                onClick = {
                                    when {
                                        banco.isEmpty() -> mensagemErro = "Selecione o banco"
                                        agencia.isEmpty() -> mensagemErro = "Digite a agência"
                                        conta.isEmpty() -> mensagemErro = "Digite o número da conta"
                                        nomeCompleto.length < 3 -> mensagemErro = "Digite o nome completo"
                                        cpf.replace(".", "").replace("-", "").length != 11 ->
                                            mensagemErro = "CPF inválido"
                                        else -> {
                                            viewModel.adicionarContaBancariaLocal(
                                                banco = banco,
                                                agencia = agencia,
                                                conta = conta,
                                                tipoConta = tipoConta,
                                                nomeCompleto = nomeCompleto,
                                                cpf = cpf,
                                                isPrincipal = isPrincipal
                                            )
                                            mensagemSucesso = true
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = banco.isNotEmpty() && agencia.isNotEmpty() && conta.isNotEmpty(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B14F))
                            ) {
                                Text("Adicionar")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatarCPF(input: String): String {
    val digitos = input.replace(".", "").replace("-", "")
    return buildString {
        digitos.forEachIndexed { index, char ->
            append(char)
            when (index) {
                2, 5 -> append(".")
                8 -> append("-")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogSaqueSimplificado(
    viewModel: CarteiraViewModel,
    token: String,
    saldoDisponivel: Double,
    contasBancarias: List<ContaBancaria>,
    onDismiss: () -> Unit,
    onAddBankAccount: () -> Unit
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("pt", "BR")) }
    var valor by remember { mutableStateOf("") }
    var showAnimation by remember { mutableStateOf(false) }
    var mensagemErro by remember { mutableStateOf<String?>(null) }
    var mensagemSucesso by remember { mutableStateOf(false) }
    var contaSelecionada by remember { mutableStateOf<ContaBancaria?>(contasBancarias.firstOrNull { it.isPrincipal } ?: contasBancarias.firstOrNull()) }
    var mostrarSeletorConta by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { showAnimation = true }

    val scale by animateFloatAsState(
        targetValue = if (showAnimation) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = ""
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.scale(scale)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when {
                    mensagemSucesso -> {
                        // Tela de sucesso
                        Icon(
                            Icons.Default.CheckCircle,
                            null,
                            tint = Color(0xFF00B14F),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("Saque Solicitado!", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "O valor será transferido em até 2 dias úteis",
                            fontSize = 14.sp,
                            color = Color(0xFF6D6D6D),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B14F))
                        ) {
                            Text("Concluir")
                        }
                    }

                    else -> {
                        Box(
                            Modifier.size(70.dp).clip(CircleShape)
                                .background(Color(0xFF3C604B).copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.TrendingDown,
                                null,
                                tint = Color(0xFF3C604B),
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        Text("Sacar Saldo", fontSize = 22.sp, fontWeight = FontWeight.Bold)

                        Spacer(Modifier.height(8.dp))

                        Text(
                            "Saldo disponível: ${currencyFormat.format(saldoDisponivel)}",
                            fontSize = 14.sp,
                            color = Color(0xFF00B14F),
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(20.dp))

                        OutlinedTextField(
                            value = valor,
                            onValueChange = {
                                if (it.isEmpty() || it.matches(Regex("^\\d*[.,]?\\d{0,2}\$"))) {
                                    valor = it
                                    mensagemErro = null
                                }
                            },
                            label = { Text("Valor") },
                            placeholder = { Text("0,00") },
                            leadingIcon = {
                                Text("R$", fontWeight = FontWeight.Bold, color = Color(0xFF3C604B), fontSize = 16.sp)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF3C604B),
                                focusedLabelColor = Color(0xFF3C604B)
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            isError = mensagemErro != null
                        )

                        Spacer(Modifier.height(16.dp))

                        // Seletor de Conta Bancária
                        Text(
                            "Conta para receber:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D2D2D),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(8.dp))

                        if (contasBancarias.isEmpty()) {
                            // Card chamativo para adicionar conta bancária
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            Icons.Default.Warning,
                                            null,
                                            tint = Color(0xFFD32F2F),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "Nenhuma conta cadastrada",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD32F2F)
                                        )
                                    }
                                    Spacer(Modifier.height(8.dp))

                                    // Botão para adicionar conta
                                    Button(
                                        onClick = {
                                            onDismiss() // Fecha o dialog de saque
                                            onAddBankAccount() // Abre o dialog de adicionar conta
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFD32F2F)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            "Adicionar Conta Bancária",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        } else {
                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth().clickable { mostrarSeletorConta = true },
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFF00B14F))
                            ) {
                                Row(
                                    Modifier.fillMaxWidth().padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        Modifier.size(40.dp).clip(CircleShape)
                                            .background(Color(0xFF00B14F).copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.AccountBalance,
                                            null,
                                            tint = Color(0xFF00B14F),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(Modifier.width(12.dp))

                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            contaSelecionada?.banco ?: "Selecione uma conta",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        if (contaSelecionada != null) {
                                            Text(
                                                "Ag: ${contaSelecionada!!.agencia} • Conta: ${contaSelecionada!!.conta}",
                                                fontSize = 12.sp,
                                                color = Color(0xFF6D6D6D)
                                            )
                                        }
                                    }

                                    Icon(
                                        Icons.Default.KeyboardArrowDown,
                                        null,
                                        tint = Color(0xFF6D6D6D)
                                    )
                                }
                            }

                            // Dropdown de seleção de conta
                            if (mostrarSeletorConta) {
                                AlertDialog(
                                    onDismissRequest = { mostrarSeletorConta = false }
                                ) {
                                    Card(shape = RoundedCornerShape(16.dp)) {
                                        Column(Modifier.padding(16.dp)) {
                                            Text(
                                                "Selecione a conta",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(Modifier.height(16.dp))

                                            contasBancarias.forEach { conta ->
                                                OutlinedCard(
                                                    modifier = Modifier.fillMaxWidth().clickable {
                                                        contaSelecionada = conta
                                                        mostrarSeletorConta = false
                                                    },
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = if (contaSelecionada?.id == conta.id)
                                                            Color(0xFF00B14F).copy(alpha = 0.1f)
                                                        else Color.White
                                                    )
                                                ) {
                                                    Row(
                                                        Modifier.padding(12.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(
                                                            Icons.Default.AccountBalance,
                                                            null,
                                                            tint = Color(0xFF00B14F)
                                                        )
                                                        Spacer(Modifier.width(12.dp))
                                                        Column {
                                                            Text(
                                                                conta.banco,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                            Text(
                                                                "Ag: ${conta.agencia} • Conta: ${conta.conta}",
                                                                fontSize = 12.sp,
                                                                color = Color(0xFF6D6D6D)
                                                            )
                                                        }
                                                    }
                                                }
                                                Spacer(Modifier.height(8.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        mensagemErro?.let {
                            Spacer(Modifier.height(8.dp))
                            Text(it, color = Color.Red, fontSize = 12.sp, textAlign = TextAlign.Center)
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            "O valor será transferido para a conta selecionada",
                            fontSize = 12.sp,
                            color = Color(0xFF6D6D6D),
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(24.dp))

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Cancelar")
                            }

                            Button(
                                onClick = {
                                    val valorDouble = valor.replace(",", ".").toDoubleOrNull() ?: 0.0

                                    when {
                                        valorDouble <= 0 -> {
                                            mensagemErro = "Digite um valor válido"
                                        }
                                        valorDouble > saldoDisponivel -> {
                                            mensagemErro = "Saldo insuficiente"
                                        }
                                        contasBancarias.isEmpty() -> {
                                            mensagemErro = "Adicione uma conta bancária primeiro"
                                        }
                                        contaSelecionada == null -> {
                                            mensagemErro = "Selecione uma conta bancária"
                                        }
                                        else -> {
                                            viewModel.sacar(
                                                token = token,
                                                valor = valorDouble,
                                                contaBancariaId = contaSelecionada!!.id,
                                                onSuccess = {
                                                    mensagemSucesso = true
                                                },
                                                onError = { erro ->
                                                    mensagemErro = erro
                                                }
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = valor.isNotEmpty() && contasBancarias.isNotEmpty(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3C604B))
                            ) {
                                Text("Confirmar")
                            }
                        }
                    }
                }
            }
        }
    }
}