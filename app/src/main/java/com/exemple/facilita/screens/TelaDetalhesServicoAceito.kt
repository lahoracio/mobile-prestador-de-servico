package com.exemple.facilita.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.exemple.facilita.model.ServicoDetalhe

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaDetalhesServicoAceito(
    navController: NavController,
    servicoDetalhe: ServicoDetalhe
) {
    val context = LocalContext.current

    // Cores inspiradas no iFood com identidade do projeto
    val primaryGreen = Color(0xFF00B14F)
    val bgLight = Color(0xFFFAFAFA)
    val cardBg = Color.White
    val textPrimary = Color(0xFF1A1A1A)
    val textSecondary = Color(0xFF757575)
    val dividerColor = Color(0xFFEEEEEE)
    val lightGreenBg = Color(0xFFE8F5E9)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Detalhes do Serviço",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
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
                    containerColor = Color.White,
                    titleContentColor = textPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        containerColor = bgLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Card Cliente - Estilo iFood
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar com inicial
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(lightGreenBg, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = servicoDetalhe.contratante.usuario.nome
                                    .split(" ")
                                    .firstOrNull()
                                    ?.firstOrNull()
                                    ?.uppercase() ?: "C",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryGreen
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Info Cliente
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = servicoDetalhe.contratante.usuario.nome,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = servicoDetalhe.contratante.usuario.telefone,
                                fontSize = 14.sp,
                                color = textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = dividerColor, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Botões de Ação - Estilo iFood
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Botão Ligar
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${servicoDetalhe.contratante.usuario.telefone}")
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = primaryGreen
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                width = 1.5.dp,
                                brush = androidx.compose.ui.graphics.SolidColor(primaryGreen)
                            )
                        ) {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Ligar",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Botão Chat
                        Button(
                            onClick = {
                                val servicoId = servicoDetalhe.id
                                val contratanteId = servicoDetalhe.contratante.id
                                val contratanteNome = java.net.URLEncoder.encode(servicoDetalhe.contratante.usuario.nome, "UTF-8")

                                // Buscar ID e nome do prestador LOGADO (não do serviço)
                                val prestadorId = com.exemple.facilita.utils.TokenManager.obterUsuarioId(context) ?: 0
                                val prestadorNome = java.net.URLEncoder.encode(
                                    com.exemple.facilita.utils.TokenManager.obterNomeUsuario(context) ?: "Prestador",
                                    "UTF-8"
                                )

                                android.util.Log.d("TelaDetalhes", "🔗 Navegando para chat: servicoId=$servicoId, contratanteId=$contratanteId, prestadorId=$prestadorId")

                                navController.navigate(
                                    "chat_ao_vivo/$servicoId/$contratanteId/$contratanteNome/$prestadorId/$prestadorNome"
                                )
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryGreen
                            )
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Message,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Chat ao vivo",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Card Detalhes do Serviço
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Informações do serviço",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Categoria
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(lightGreenBg, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Build,
                                contentDescription = null,
                                tint = primaryGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Categoria",
                                fontSize = 12.sp,
                                color = textSecondary
                            )
                            Text(
                                text = servicoDetalhe.categoria.nome,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = textPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = dividerColor)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Valor
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Valor do serviço",
                            fontSize = 15.sp,
                            color = textSecondary
                        )
                        Text(
                            text = "R$ ${servicoDetalhe.valor}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = dividerColor)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Descrição
                    Text(
                        text = "Descrição",
                        fontSize = 12.sp,
                        color = textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = servicoDetalhe.descricao,
                        fontSize = 15.sp,
                        color = textPrimary,
                        lineHeight = 22.sp
                    )
                }
            }

            // Card Rota / Localização - Estilo iFood
            servicoDetalhe.localizacao?.let { loc ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = primaryGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Local do serviço",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Endereço formatado
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = loc.endereco + if (!loc.numero.isNullOrBlank()) ", ${loc.numero}" else "",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = textPrimary
                            )
                            if (!loc.complemento.isNullOrBlank()) {
                                Text(
                                    text = loc.complemento,
                                    fontSize = 14.sp,
                                    color = textSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${loc.bairro} - ${loc.cidade}, ${loc.estado}",
                                fontSize = 14.sp,
                                color = textSecondary
                            )
                            Text(
                                text = "CEP: ${loc.cep}",
                                fontSize = 13.sp,
                                color = textSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Botão de navegação moderno
                        Button(
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryGreen
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Navigation,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Iniciar rota no Google Maps",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
