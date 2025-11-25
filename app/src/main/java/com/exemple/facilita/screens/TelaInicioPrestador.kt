package com.exemple.facilita.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exemple.facilita.components.BottomNavBar
import com.exemple.facilita.service.ApiResponse
import com.exemple.facilita.service.RetrofitFactory
import com.exemple.facilita.utils.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
fun TelaInicioPrestador() {
    val context = LocalContext.current
    var listaSolicitacoes by remember { mutableStateOf<List<Solicitacao>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // 🔑 Token JWT recuperado do TokenManager após o login
    val token = TokenManager.obterTokenComBearer(context) ?: ""

    // 🔄 Buscar dados da API quando a tela for aberta
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

    // 🎨 Paleta de cores
    val backgroundColor = Color(0xFFF5F7FA)
    val primaryColor = Color(0xFF019D31)
    val primaryColorDark = Color(0xFF007A25)
    val cardBackgroundColor = Color.White
    val textColorPrimary = Color(0xFF212121)
    val textColorSecondary = Color(0xFF757575)

    Scaffold(
        bottomBar = {
            if (navController != null) {
                BottomNavBar(navController)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(backgroundColor)
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
                colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                IconButton(onClick = { /* Notificação */ }) {
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
                    Text(
                        text = "Seu saldo",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 15.sp
                    )
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

        // 🔹 Título da lista
        Text(
            "Solicitações disponíveis",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = textColorPrimary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Conteúdo principal
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Carregando solicitações...")
                }
            }
            listaSolicitacoes.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhuma solicitação disponível no momento.")
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(listaSolicitacoes, key = { it.id }) { item ->
                        SolicitacaoCard(solicitacao = item)
                    }
                }
            }
        }
        }
    }
}

@Composable
fun SolicitacaoCard(solicitacao: Solicitacao, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val acceptColor = Color(0xFF019D31)
    val rejectColor = Color(0xFFD32F2F)
    val labelColor = Color(0xFF757575)
    val valueColor = Color(0xFF212121)

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, Color(0xFFEAEAEA))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Cabeçalho
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("#${solicitacao.numero}", fontWeight = FontWeight.Bold, color = valueColor)
                Text(solicitacao.cliente, color = valueColor, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${solicitacao.servico} • ${solicitacao.distancia}",
                fontSize = 13.sp,
                color = labelColor
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🕒 ${solicitacao.horario}", fontSize = 13.sp, color = labelColor)
                Text(
                    solicitacao.valor,
                    fontWeight = FontWeight.Bold,
                    color = valueColor,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 🔹 Botões Aceitar e Recusar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        Toast.makeText(context, "Serviço aceito!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = acceptColor),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Text(
                        text = "Aceitar",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = {
                        Toast.makeText(context, "Serviço recusado!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = rejectColor),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Text(
                        text = "Recusar",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTelaInicioPrestador() {
    TelaInicioPrestador()
}
