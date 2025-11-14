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
import com.exemple.facilita.screens.TelaInicioPrestador
import com.exemple.facilita.viewmodel.PerfilViewModel
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

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
    // ViewModels compartilhados entre todas as telas
    val perfilViewModel: PerfilViewModel = viewModel()
    val prestadorViewModel: com.exemple.facilita.viewmodel.PrestadorViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "splash_screen"
    ) {

        composable("splash_screen") {
            SplashScreen(navController)
        }

        composable("tela_inicio1") {
            TelaInicio1(navController)
        }

        composable("tela_inicio2") {
            TelaInicio2(navController)
        }

        composable("tela_inicio3") {
            TelaInicio3(navController)
        }

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

        //  Rotas do prestador
        composable("tela_inicio_prestador") {
            TelaInicioPrestador(navController)
        }

        composable("tela_perfil_prestador") {
            TelaPerfilPrestador(navController)
        }

        composable("tela_servicos") {
            TelaServicos(navController)
        }

        composable("tela_historico") {
            TelaHistorico(navController)
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
            TelaCompletarPerfilPrestador(navController, perfilViewModel, prestadorViewModel)
        }

        composable("tela_cnh") {
            TelaCNH(navController, perfilViewModel, prestadorViewModel)
        }

        composable("tela_documentos") {
            TelaDocumentos(navController, prestadorViewModel)
        }

        composable("tela_tipo_veiculo") {
            TelaTipoVeiculo(navController, prestadorViewModel)
        }

        composable("tela_detalhe_pedido/{servicoId}/{clienteNome}/{servicoDescricao}/{valor}/{local}/{horario}") { backStackEntry ->
            val servicoId = backStackEntry.arguments?.getString("servicoId")?.toIntOrNull() ?: 0
            val clienteNome = URLDecoder.decode(backStackEntry.arguments?.getString("clienteNome") ?: "", StandardCharsets.UTF_8.toString())
            val servicoDescricao = URLDecoder.decode(backStackEntry.arguments?.getString("servicoDescricao") ?: "", StandardCharsets.UTF_8.toString())
            val valor = URLDecoder.decode(backStackEntry.arguments?.getString("valor") ?: "", StandardCharsets.UTF_8.toString())
            val local = URLDecoder.decode(backStackEntry.arguments?.getString("local") ?: "", StandardCharsets.UTF_8.toString())
            val horario = URLDecoder.decode(backStackEntry.arguments?.getString("horario") ?: "", StandardCharsets.UTF_8.toString())

            TelaDetalhePedido(
                navController = navController,
                servicoId = servicoId,
                clienteNome = clienteNome,
                servicoDescricao = servicoDescricao,
                valor = valor,
                local = local,
                horario = horario
            )
        }

        // Rotas da Carteira
        composable("tela_carteira") {
            TelaCarteira(navController)
        }
        
        composable("tela_adicionar_dinheiro") {
            TelaAdicionarDinheiro(navController)
        }
        
        composable("tela_sacar_dinheiro") {
            TelaSacarDinheiro(navController)
        }
        
        composable("tela_contas_bancarias") {
            TelaContasBancarias(navController)
        }
        
        composable("tela_adicionar_conta") {
            TelaAdicionarConta(navController)
        }

        composable("tela_qrcode_pix/{valor}") { backStackEntry ->
            val valor = backStackEntry.arguments?.getString("valor")?.toDoubleOrNull() ?: 0.0
            TelaQRCodePix(navController, valor)
        }

        composable("tela_informacoes_veiculo/{tiposVeiculo}") { backStackEntry ->
            val tiposVeiculo = backStackEntry.arguments?.getString("tiposVeiculo") ?: ""
            TelaInformacoesVeiculo(navController, tiposVeiculo, prestadorViewModel)
        }

        // TODO: Descomentar após corrigir TelaInformacoesVeiculo.kt (remover código duplicado)
        // composable("tela_veiculo/{tiposVeiculo}") { backStackEntry ->
        //     val tiposVeiculo = backStackEntry.arguments?.getString("tiposVeiculo") ?: ""
        //     TelaInformacoesVeiculo(navController, tiposVeiculo, perfilViewModel)
        // }

        // composable("tela_tipo_veiculo/{tiposVeiculo}") { backStackEntry ->
        //     val tiposVeiculo = backStackEntry.arguments?.getString("tiposVeiculo") ?: ""
        //     TelaInformacoesVeiculo(navController, tiposVeiculo, perfilViewModel)
        // }

    }
}
