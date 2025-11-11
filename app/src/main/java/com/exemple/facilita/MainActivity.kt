package com.exemple.facilita

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.exemple.facilita.screens.*
import com.exemple.facilita.viewmodel.PerfilViewModel

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
    // ViewModel compartilhado entre todas as telas
    val perfilViewModel: PerfilViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "tela_login"
    ) {


        composable("tela_login") {
            TelaLogin(navController)
        }
        composable("tela_cadastro") {
            TelaCadastro(navController)
        }

        composable("tela_recuperar_senha") {
            TelaRecuperarSenha(navController)
        }

        composable("tela_verificar_codigo/{emailOuTelefone}/{tipo}") { backStackEntry ->
            val emailOuTelefone = backStackEntry.arguments?.getString("emailOuTelefone") ?: ""
            val tipo = backStackEntry.arguments?.getString("tipo") ?: "email"
            TelaVerificarCodigo(navController, emailOuTelefone, tipo)
        }

        composable("tela_redefinir_senha/{usuarioId}") { backStackEntry ->
            val usuarioId = backStackEntry.arguments?.getString("usuarioId") ?: ""
            TelaRedefinirSenha(navController, usuarioId)
        }

        //  Nova rota para a tela inicial do prestador
        composable("tela_inicio_prestador") {
            TelaInicioPrestador()
        }

        composable("tela_tipo_veiculo") {
            TelaTipoVeiculo(navController)
        }

        composable("tela_tipo_conta_servico") {
            TelaTipoContaServico(navController)
        }

        // Rota alternativa para compatibilidade
        composable("tela_tipo_conta") {
            TelaTipoContaServico(navController)
        }

        composable("tela_permissao_localizacao_servico") {
            TelaPermissaoLocalizacaoServico(navController)
        }

        composable("tela_completar_perfil_prestador") {
            TelaCompletarPerfilPrestador(navController, perfilViewModel)
        }

        composable("tela_cnh") {
            TelaCNH(navController, perfilViewModel)
        }

        composable("tela_documentos") {
            TelaDocumentos(navController, perfilViewModel)
        }

        composable("tela_veiculo/{tiposVeiculo}") { backStackEntry ->
            val tiposVeiculo = backStackEntry.arguments?.getString("tiposVeiculo") ?: ""
            TelaInformacoesVeiculo(navController, tiposVeiculo, perfilViewModel)
        }

    }
}
