package com.exemple.facilita.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.exemple.facilita.R
import com.exemple.facilita.components.BottomNavBar

@Composable
fun TelaPerfilPrestador(navController: NavController) {
    var notificacoesAtivas by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = { BottomNavBar(navController) },
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .background(Color(0xFFF5F5F5)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Perfil",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Image(
                    painter = painterResource(id = R.drawable.foto_perfil),
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Adicionar foto",
                    tint = Color(0xFF00A651),
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(4.dp)
                        .align(Alignment.BottomEnd)
                )
            }

            Spacer(modifier = Modifier.height(25.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Informações do Perfil",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.Black,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    // 🔹 Sem ícone de editar
                    PerfilInfoItem(
                        icon = Icons.Default.Person,
                        label = "Jair Bolsonaro",
                        onEdit = null
                    )
                    // 🔹 Com ícone de editar
                    PerfilInfoItem(
                        icon = Icons.Default.LocationOn,
                        label = "São Paulo/Osasco",
                        onEdit = {}
                    )
                    PerfilInfoItem(
                        icon = Icons.Default.Email,
                        label = "jairbolsonaro@gmail.com",
                        onEdit = {}
                    )
                    PerfilInfoItem(
                        icon = Icons.Default.Phone,
                        label = "(11) 94660-1277",
                        onEdit = {}
                    )
                    PerfilInfoItem(
                        icon = Icons.Default.ContactPage,
                        label = "Documentos Registrados",
                        onEdit = {}
                    )
                }
            }

            Spacer(modifier = Modifier.height(25.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Text(
                        text = "Outras Configurações",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.Black,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Alterar senha",
                                tint = Color.Black
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "Alterar Senha", fontSize = 15.sp)
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Ativar notificações",
                                tint = Color.Black
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "Ativar Notificações", fontSize = 15.sp)
                        }
                        Switch(
                            checked = notificacoesAtivas,
                            onCheckedChange = { notificacoesAtivas = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF00A651),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.Gray
                            )
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate("tela_login") {
                                    popUpTo("tela_home") { inclusive = true }
                                }
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Sair",
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = "Sair", fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun PerfilInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onEdit: (() -> Unit)? // agora pode ser nulo
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = label, fontSize = 15.sp, color = Color.Black)
        }

        // Exibe o ícone de editar apenas se tiver função de edição
        if (onEdit != null) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Editar",
                tint = Color.Gray,
                modifier = Modifier.clickable { onEdit() }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTelaPerfilPrestador() {
    TelaPerfilPrestador(navController = rememberNavController())
}
