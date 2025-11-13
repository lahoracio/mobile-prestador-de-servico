package com.exemple.facilita.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import com.exemple.facilita.service.ApiResponse
import com.exemple.facilita.service.RetrofitFactory
import com.exemple.facilita.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resumeWithException



@Stable
data class Solicitacao(
    val id: Int,
    val numero: Int,
    val cliente: String,
    val servico: String,
    val distancia: String,
    val horario: String,
    val valor: String
)

@Composable
fun TelaInicioPrestador(navController: androidx.navigation.NavController) {
    val context = LocalContext.current
    var listaSolicitacoes by remember { mutableStateOf<List<Solicitacao>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val token = TokenManager.obterTokenComBearer(context) ?: ""

    // 🔄 Buscar solicitações
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
                } else {
                    Toast.makeText(context, "Erro: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
                isLoading = false
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(context, "Erro de conexão: ${t.message}", Toast.LENGTH_SHORT).show()
                isLoading = false
            }
        })
    }

    // Cores
    val backgroundColor = Color(0xFFF5F7FA)
    val primaryColor = Color(0xFF019D31)
    val primaryColorDark = Color(0xFF007A25)
    val textColorPrimary = Color(0xFF212121)
    val textColorSecondary = Color(0xFF757575)

    Scaffold(
        containerColor = backgroundColor,
        bottomBar = { com.exemple.facilita.components.BottomNavBar(navController = navController) }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
                .padding(vertical = 12.dp)
        ) {
        // 🔹 Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Olá, Vithor",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = textColorPrimary
                )
                Text(
                    text = "Seu trabalho facilita vidas.",
                    fontSize = 14.sp,
                    color = textColorSecondary
                )
            }
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                IconButton(onClick = { /* notificações */ }) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notificações",
                        modifier = Modifier.size(24.dp),
                        tint = textColorPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 🔹 Card de saldo
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.linearGradient(
                            colors = listOf(primaryColor, primaryColorDark)
                        )
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Text("Seu saldo", color = Color.White.copy(alpha = 0.9f), fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "R$ 5.100,00",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = "Ver extrato",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Solicitações disponíveis",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = textColorPrimary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Carregando solicitações...")
            }

            listaSolicitacoes.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nenhuma solicitação disponível no momento.")
            }

            else -> LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(listaSolicitacoes, key = { it.id }) { item ->
                    SolicitacaoCard(solicitacao = item, token = token)
                }
            }
        }
        } // Fecha Column
    } // Fecha Scaffold
}

@Composable
fun SolicitacaoCard(solicitacao: Solicitacao, token: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val acceptColor = Color(0xFF019D31)
    val rejectColor = Color(0xFFD32F2F)
    val labelColor = Color(0xFF757575)
    val valueColor = Color(0xFF212121)

    var showSuccessAnimation by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Usar um coroutine scope seguro para atualizações de estado
    val coroutineScope = rememberCoroutineScope()

    fun aceitarServico() {
        if (isLoading) return

        isLoading = true

        coroutineScope.launch {
            try {
                // Fazer a chamada em background
                val result = withContext(Dispatchers.IO) {
                    try {
                        val service = RetrofitFactory.getServicoService()
                        val response = service.aceitarServico(token, solicitacao.id).execute()
                        Result.success(response)
                    } catch (e: Exception) {
                        Result.failure<Response<ApiResponse>>(e)
                    }
                }

                // Processar o resultado na main thread
                when {
                    result.isSuccess -> {
                        val response = result.getOrNull()
                        if (response?.isSuccessful == true) {
                            showSuccessAnimation = true
                            Toast.makeText(context, "Serviço aceito com sucesso!", Toast.LENGTH_SHORT).show()
                        } else {
                            val errorCode = response?.code() ?: "Unknown"
                            val errorBody = response?.errorBody()?.string() ?: "No error body"
                            Toast.makeText(context, "Erro: $errorCode - $errorBody", Toast.LENGTH_LONG).show()
                        }
                    }
                    result.isFailure -> {
                        val exception = result.exceptionOrNull()
                        val errorMessage = when {
                            exception is java.net.SocketTimeoutException -> "Timeout na conexão"
                            exception is java.net.UnknownHostException -> "Sem conexão com a internet"
                            exception != null -> "Erro: ${exception.message ?: exception.javaClass.simpleName}"
                            else -> "Erro desconhecido"
                        }
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro inesperado: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    // Versão alternativa mais simples se a anterior não funcionar
    fun aceitarServicoAlternativo() {
        if (isLoading) return

        isLoading = true

        val service = RetrofitFactory.getServicoService()
        service.aceitarServico(token, solicitacao.id).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                isLoading = false
                if (response.isSuccessful) {
                    showSuccessAnimation = true
                    Toast.makeText(context, "Serviço aceito com sucesso!", Toast.LENGTH_SHORT).show()
                } else {
                    val errorMsg = when (response.code()) {
                        400 -> "Requisição inválida"
                        401 -> "Não autorizado"
                        404 -> "Serviço não encontrado"
                        409 -> "Serviço já foi aceito"
                        500 -> "Erro interno do servidor"
                        else -> "Erro: ${response.code()}"
                    }
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                isLoading = false
                val errorMsg = when (t) {
                    is java.net.SocketTimeoutException -> "Timeout na conexão"
                    is java.net.UnknownHostException -> "Sem conexão com a internet"
                    else -> "Erro de conexão: ${t.message ?: "Verifique sua internet"}"
                }
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            }
        })
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, Color(0xFFEAEAEA))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("#${solicitacao.numero}", fontWeight = FontWeight.Bold, color = valueColor)
                Text(solicitacao.cliente, color = valueColor, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text("${solicitacao.servico} • ${solicitacao.distancia}", fontSize = 13.sp, color = labelColor)

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🕒 ${solicitacao.horario}", fontSize = 13.sp, color = labelColor)
                Text(solicitacao.valor, fontWeight = FontWeight.Bold, color = valueColor, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        // Tente usar a versão alternativa primeiro
                        aceitarServicoAlternativo()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLoading) Color.Gray else acceptColor
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Aceitando...", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        Text("Aceitar", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Button(
                    onClick = {
                        if (!isLoading) {
                            Toast.makeText(context, "Serviço recusado!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLoading) Color.Gray else rejectColor
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    enabled = !isLoading
                ) {
                    Text("Recusar", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    // Animação de sucesso - fora do Card
    if (showSuccessAnimation) {
        ServicoAceitoAnimation(
            onDismiss = {
                showSuccessAnimation = false
            }
        )
    }
}

@Composable
fun ServicoAceitoAnimation(onDismiss: () -> Unit) {
    var visible by remember { mutableStateOf(true) }

    LaunchedEffect(visible) {
        if (visible) {
            delay(2500) // Mostra por 2.5 segundos
            visible = false
            delay(300) // Espera a animação de saída
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = { /* Não permitir fechar clicando fora */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(
                initialScale = 0.3f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn(animationSpec = tween(300)),
            exit = scaleOut(
                targetScale = 1.1f,
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(32.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Sucesso",
                        tint = Color(0xFF019D31),
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Serviço Aceito!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "O serviço foi aceito com sucesso.",
                        fontSize = 14.sp,
                        color = Color(0xFF757575),
                        textAlign = TextAlign.Center
                    )
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
