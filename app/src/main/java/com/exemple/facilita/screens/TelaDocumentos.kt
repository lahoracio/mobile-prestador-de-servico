package com.exemple.facilita.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.exemple.facilita.R
import com.exemple.facilita.viewmodel.DocumentoViewModel
import com.exemple.facilita.viewmodel.PerfilViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaDocumentos(navController: NavController) {

    val viewModel: DocumentoViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val perfilViewModel: PerfilViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val mensagem by viewModel.mensagem.collectAsState()
    val documentosCadastrados by viewModel.documentosCadastrados.collectAsState()

    // Observa quando ambos documentos são cadastrados e volta para a tela anterior
    LaunchedEffect(documentosCadastrados) {
        if (documentosCadastrados.contains("RG") && documentosCadastrados.contains("CPF")) {
            perfilViewModel.marcarComoValidado("Documentos")
            kotlinx.coroutines.delay(1500) // Aguarda 1.5s para mostrar a mensagem
            navController.popBackStack()
        }
    }

    var rg by remember { mutableStateOf("") }
    var cpf by remember { mutableStateOf("") }
    var dataValidadeRG by remember { mutableStateOf("") }
    var dataValidadeCPF by remember { mutableStateOf("") }

    Scaffold(containerColor = Color(0xFFE6E6E6)) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Botão de Voltar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() },
                    modifier = Modifier.padding(0.dp)) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Voltar",
                        tint = Color(0xFF015B2B)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }

            // Imagem de topo
            Box(modifier = Modifier.size(130.dp), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE6E6E6)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.sejabemvindo),
                        contentDescription = null,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Documentos",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Cadastre seus documentos RG e CPF",
                fontSize = 16.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Mensagem de feedback
            mensagem?.let {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (it.startsWith("✅")) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    )
                ) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(12.dp),
                        color = if (it.startsWith("✅")) Color(0xFF2E7D32) else Color(0xFFC62828),
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ===== SEÇÃO RG =====
            Text(
                text = "RG",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Campo RG
            OutlinedTextField(
                value = rg,
                onValueChange = { rg = it },
                label = { Text("Número do RG") },
                placeholder = { Text("Ex: 123456789") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Data de Validade do RG
            OutlinedTextField(
                value = dataValidadeRG,
                onValueChange = { dataValidadeRG = it },
                label = { Text("Data de validade do RG") },
                placeholder = { Text("Ex: 2030-12-31") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ===== SEÇÃO CPF =====
            Text(
                text = "CPF",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Campo CPF
            OutlinedTextField(
                value = cpf,
                onValueChange = { cpf = it },
                label = { Text("Número do CPF") },
                placeholder = { Text("Ex: 12345678901 (11 dígitos)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Data de Validade do CPF
            OutlinedTextField(
                value = dataValidadeCPF,
                onValueChange = { dataValidadeCPF = it },
                label = { Text("Data de validade do CPF") },
                placeholder = { Text("Ex: 2030-12-31") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Serão cadastrados 2 documentos: RG e CPF. Verifique se os dados estão corretos.",
                fontSize = 14.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botão Salvar Documentos
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(48.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF015B2B), Color(0xFF00B94A))
                        ),
                        shape = RoundedCornerShape(50)
                    )
                    .clickable {
                        // Validação básica
                        if (rg.isBlank() || cpf.isBlank() || dataValidadeRG.isBlank() || dataValidadeCPF.isBlank()) {
                            viewModel.setMensagem("❌ Por favor, preencha todos os campos")
                            return@clickable
                        }

                        // Validação do formato da data (YYYY-MM-DD)
                        val dateRegex = Regex("""^\d{4}-\d{2}-\d{2}${'$'}""")
                        if (!dataValidadeRG.matches(dateRegex) || !dataValidadeCPF.matches(dateRegex)) {
                            viewModel.setMensagem("❌ Formato de data inválido. Use: AAAA-MM-DD (Ex: 2030-12-31)")
                            return@clickable
                        }

                        // Validação do CPF (11 dígitos)
                        val cpfLimpo = cpf.replace(Regex("[^0-9]"), "")
                        if (cpfLimpo.length != 11) {
                            viewModel.setMensagem("❌ CPF deve conter 11 dígitos")
                            return@clickable
                        }

                        val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6ODYsInRpcG9fY29udGEiOiJQUkVTVEFET1IiLCJlbWFpbCI6Imdpb3Zhbm5hQGdtYWlsLmNvbSIsImlhdCI6MTc2MjQzMzU5NiwiZXhwIjoxNzYyNDYyMzk2fQ.5_XHwGBFhYTSFGbsQBILho56o2mm1FnzDUMZhN7RkoY"

                        // Cadastrar RG
                        viewModel.cadastrarDocumento(
                            token = token,
                            tipoDocumento = "RG",
                            valor = rg,
                            dataValidade = dataValidadeRG
                        )

                        // Cadastrar CPF
                        viewModel.cadastrarDocumento(
                            token = token,
                            tipoDocumento = "CPF",
                            valor = cpfLimpo,
                            dataValidade = dataValidadeCPF
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Salvar Documentos",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTelaDocumentos() {
    TelaDocumentos(navController = rememberNavController())
}
