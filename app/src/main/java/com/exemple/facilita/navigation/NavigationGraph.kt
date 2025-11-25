package com.exemple.facilita.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.exemple.facilita.call.CallViewModel
import com.exemple.facilita.data.service.WebSocketService
import com.exemple.facilita.screens.IncomingCallScreen
import com.exemple.facilita.screens.TelaIniciarChamada
import com.exemple.facilita.screens.TelaTesteChamada
import com.exemple.facilita.screens.TelaVideoChamada

fun NavGraphBuilder.addCallNavigation(
    navController: NavHostController,
    webSocketService: WebSocketService,
    callViewModel: CallViewModel
) {
    // Tela de teste
    composable("tela_teste_chamada") {
        TelaTesteChamada(navController)
    }

    // Tela de iniciar chamada
    composable(
        "tela_iniciar_chamada/{servicoId}/{callerId}/{callerName}/{targetId}/{targetName}"
    ) { backStackEntry ->
        val args = backStackEntry.arguments
        val servicoId = args?.getString("servicoId")?.toIntOrNull() ?: 0
        val callerId = args?.getString("callerId")?.toIntOrNull() ?: 0
        val callerName = args?.getString("callerName") ?: ""
        val targetId = args?.getString("targetId")?.toIntOrNull() ?: 0
        val targetName = args?.getString("targetName") ?: ""

        TelaIniciarChamada(
            navController = navController,
            servicoId = servicoId,
            callerId = callerId,
            callerName = callerName,
            targetId = targetId,
            targetName = targetName,
            webSocketService = webSocketService,
            callViewModel = callViewModel
        )
    }

    // Tela de chamada recebida
    composable(
        "tela_chamada_recebida/{callerName}/{servicoId}/{callerId}"
    ) { backStackEntry ->
        val args = backStackEntry.arguments
        val callerName = args?.getString("callerName") ?: ""
        val servicoId = args?.getString("servicoId")?.toIntOrNull() ?: 0
        val callerId = args?.getString("callerId")?.toIntOrNull() ?: 0

        IncomingCallScreen(
            callerName = callerName,
            onAccept = {
                callViewModel.acceptCall(servicoId, callerId)
                navController.navigate("tela_video_chamada/$servicoId/$callerId/$callerName")
            },
            onReject = {
                // Implementar rejeição de chamada
                navController.popBackStack()
            }
        )
    }

    // Tela de vídeo chamada ativa
    composable(
        "tela_video_chamada/{servicoId}/{callerId}/{callerName}"
    ) { backStackEntry ->
        val args = backStackEntry.arguments
        val servicoId = args?.getString("servicoId")?.toIntOrNull() ?: 0
        val callerId = args?.getString("callerId")?.toIntOrNull() ?: 0
        val callerName = args?.getString("callerName") ?: ""

        TelaVideoChamada(
            navController = navController,
            servicoId = servicoId,
            callerId = callerId,
            callerName = callerName,
            callViewModel = callViewModel
        )
    }
}