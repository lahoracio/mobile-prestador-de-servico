package com.exemple.facilita.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.exemple.facilita.model.BottomNavItem

@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("Início", Icons.Default.Home, "tela_inicio_prestador"),
        BottomNavItem("Serviços", Icons.Default.Work, "tela_servicos"),

        BottomNavItem("Carteira", Icons.Default.AccountBalanceWallet, "tela_carteira"),
        BottomNavItem("Histórico", Icons.Default.History, "tela_historico"),
        BottomNavItem("Perfil", Icons.Default.Person, "tela_perfil_prestador")
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 18.dp, end = 18.dp, bottom = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(
                    elevation = 9.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = Color.Black.copy(alpha = 0.2f),
                    spotColor = Color.Black.copy(alpha = 0.2f)
                )
                .border(
                    width = 1.dp,
                    color = Color(0x22000000),
                    shape = RoundedCornerShape(28.dp)
                ),
            color = Color.White,
            shape = RoundedCornerShape(28.dp)
        ) {
            val currentRoute =
                navController.currentBackStackEntryAsState().value?.destination?.route

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    // Pop até a tela home para manter apenas as telas principais na pilha
                                    popUpTo("tela_home") {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (currentRoute == item.route) Color(0xFF00A651) else Color.Gray
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                fontSize = 12.sp,
                                color = if (currentRoute == item.route) Color(0xFF00A651) else Color.Gray
                            )
                        },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent,
                            selectedIconColor = Color(0xFF00A651),
                            selectedTextColor = Color(0xFF00A651),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )
                }
            }
        }
    }
}
