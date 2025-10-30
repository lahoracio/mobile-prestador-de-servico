package com.exemple.facilita

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.exemple.facilita.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            AppNavHost(navController)
        }
    }
}

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "tela_inicio_prestador"
    ) {

        // Telas já existentes

        // 🟢 Nova rota para a tela inicial do prestador
        composable("tela_inicio_prestador") {
            TelaInicioPrestador()
        }
        composable("tela_tipo_veiculo") {
            TelaTipoVeiculo(navController)
        }

        composable("tela_tipo_conta_servico") {
            TelaTipoContaServico(navController)
        }

        composable("tela_permissao_localizacao_servico") {
            TelaPermissaoLocalizacaoServico(navController)
        }

        composable("tela_completar_perfil_prestador") {
            TelaCompletarPerfilPrestador(navController)
        }

        composable("tela_veiculo") {
            TelaInformacoesVeiculo(navController)
        }

    }
}
