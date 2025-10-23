package com.exemple.facilita

import TelaInicio1
import TelaInicio2
import TelaInicio3
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
import com.exemple.facilita.service.RetrofitFactory
import com.exemple.facilita.viewmodel.EnderecoViewModel

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
        startDestination = "tela_tipo_conta_servico"
    ) {
        composable("splash") {
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
        composable("tela_termos") {
            TelaTermos(navController)
        }
        composable("tela_recuperar_senha") {
            TelaRecuperacaoSenha(navController)
        }
        composable("tela_tipo_conta") {
            TelaTipoConta(navController)
        }
        composable("tela_completar_perfil_contratante") {
            TelaCompletarPerfilContratante(navController)
        }
        composable("tela_endereco") {
            val enderecoViewModel: EnderecoViewModel = viewModel()
            val retrofitFactory = RetrofitFactory()
            TelaEnderecoContent(
                navController = navController,
                viewModel = enderecoViewModel,
            )
        }
        composable("tela_home") {
            TelaHome(navController)
        }
        composable("tela_nova_senha/{codigo}") { backStackEntry ->
            val codigo = backStackEntry.arguments?.getString("codigo") ?: ""
            TelaNovaSenha(navController, codigo)
        }
        composable("tela_montar_servico") {
            TelaMontarServico(navController)
        }
        composable("tela_perfil") {
            TelaPerfilContratante(navController)
        }
        composable("tela_status_pagamento") {
            TelaStatusPagamento(navController)
        }
        composable("tela_historico_pedido") {
            TelaPedidosHistorico(navController)
        }
        composable("tela_pedido_confirmado") {
            TelaPedidoConfirmado(navController)
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


    }
}
