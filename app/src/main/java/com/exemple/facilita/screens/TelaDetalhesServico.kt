package com.exemple.facilita.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.exemple.facilita.model.ServicoDetalhe

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaDetalhesServico(
    navController: NavController,
    servicoDetalhe: ServicoDetalhe
) {
    val context = LocalContext.current
    val primaryColor = Color(0xFF019D31)
    val backgroundColor = Color(0xFFF5F7FA)

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Detalhes do Serviço") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF212121)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card Status
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (servicoDetalhe.status) {
                        "EM_ANDAMENTO" -> Color(0xFFE8F5E9)
                        "PENDENTE" -> Color(0xFFFFF3E0)
                        "CONCLUIDO" -> Color(0xFFE3F2FD)
                        else -> Color.White
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(primaryColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Status do Serviço",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = when (servicoDetalhe.status) {
                                    "EM_ANDAMENTO" -> "Em Andamento"
                                    "PENDENTE" -> "Pendente"
                                    "CONCLUIDO" -> "Concluído"
                                    else -> servicoDetalhe.status
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                        }
                    }
                    Text(
                        text = "R$ ${servicoDetalhe.valor}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }
            }

            // Card Cliente
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cliente",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))

                    InfoRow("Nome", servicoDetalhe.contratante.usuario.nome)
                    InfoRow("Telefone", servicoDetalhe.contratante.usuario.telefone)
                    InfoRow("Email", servicoDetalhe.contratante.usuario.email)
                }
            }

            // Card Descrição do Serviço
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Descrição",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = servicoDetalhe.descricao,
                        fontSize = 14.sp,
                        color = Color(0xFF212121)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    InfoRow("Categoria", servicoDetalhe.categoria.nome)
                    servicoDetalhe.tempo_estimado?.let {
                        InfoRow("Tempo Estimado", "$it minutos")
                    }
                }
            }

            // Card Localização (se houver)
            servicoDetalhe.localizacao?.let { loc ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Localização",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF212121)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(12.dp))

                        InfoRow("Endereço", loc.endereco)
                        loc.numero?.let { InfoRow("Número", it) }
                        loc.complemento?.let { InfoRow("Complemento", it) }
                        InfoRow("Bairro", loc.bairro)
                        InfoRow("Cidade", "${loc.cidade} - ${loc.estado}")
                        InfoRow("CEP", loc.cep)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botão Iniciar Rota
            servicoDetalhe.localizacao?.let { loc ->
                Button(
                    onClick = {
                        // Abrir Google Maps com a rota
                        val uri = Uri.parse(
                            "google.navigation:q=${loc.latitude},${loc.longitude}&mode=d"
                        )
                        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                            setPackage("com.google.android.apps.maps")
                        }

                        // Verificar se o Google Maps está instalado
                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                        } else {
                            // Fallback para o browser
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
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF015B2B), Color(0xFF00B94A))
                                ),
                                shape = RoundedCornerShape(28.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Iniciar Rota",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } ?: run {
                // Se não houver localização, mostrar mensagem
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFF57C00)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Localização não disponível para este serviço",
                            fontSize = 14.sp,
                            color = Color(0xFF212121)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color(0xFF212121),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(2f),
            textAlign = TextAlign.End
        )
    }
}

