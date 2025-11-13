package com.exemple.facilita.screens

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.exemple.facilita.model.Modalidade
import com.exemple.facilita.model.ModalidadeRequest
import com.exemple.facilita.service.RetrofitFactory
import com.exemple.facilita.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaInformacoesVeiculo(
    navController: NavController,
    tiposVeiculo: String, // Exemplo: "MOTO,CARRO"
    prestadorViewModel: com.exemple.facilita.viewmodel.PrestadorViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tipos = tiposVeiculo.split(",")

    // Estados para cada tipo de veículo
    val veiculosInfo = remember {
        mutableStateMapOf<String, VeiculoInfo>().apply {
            tipos.forEach { tipo ->
                put(tipo, VeiculoInfo())
            }
        }
    }

    var isLoading by remember { mutableStateOf(false) }
    var currentVehicleIndex by remember { mutableStateOf(0) }
    val currentTipo = tipos.getOrNull(currentVehicleIndex) ?: ""

    // Animações
    var visible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.9f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(500)
    )

    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    fun cadastrarVeiculos() {
        scope.launch(Dispatchers.IO) {
            try {
                isLoading = true
                val token = TokenManager.obterToken(context)

                if (token.isNullOrBlank()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Token não encontrado", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                // Validar todos os veículos
                val todosValidos = veiculosInfo.all { (_, info) ->
                    info.modelo.isNotBlank() && info.ano > 0
                }

                if (!todosValidos) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                // Criar lista de modalidades
                val modalidades = veiculosInfo.entries.map { entry ->
                    val tipo = entry.key
                    val info = entry.value
                    Modalidade(
                        tipo = tipo,
                        modelo_veiculo = info.modelo,
                        ano_veiculo = info.ano,
                        possui_seguro = info.possuiSeguro,
                        compartimento_adequado = info.compartimentoAdequado,
                        revisao_em_dia = info.revisaoEmDia,
                        antecedentes_criminais = info.antecedentesCriminais
                    )
                }

                val request = ModalidadeRequest(modalidades = modalidades)
                val response = RetrofitFactory.getModalidadeService()
                    .cadastrarModalidades("Bearer $token", request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        Toast.makeText(context, "✅ Veículo(s) cadastrado(s) com sucesso!", Toast.LENGTH_SHORT).show()
                        prestadorViewModel.marcarVeiculoCadastrado()
                        delay(500)
                        // Volta 2 telas: TelaInformacoesVeiculo -> TelaTipoVeiculo -> TelaCompletarPerfilPrestador
                        navController.popBackStack() // Volta para TelaTipoVeiculo
                        navController.popBackStack() // Volta para TelaCompletarPerfilPrestador
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: "Erro desconhecido"
                        Toast.makeText(context, "Erro: $errorMsg", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFF5F5F5),
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color(0xFF015B2B)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .scale(scale)
                    .alpha(alpha),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // Ícone do veículo
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .shadow(12.dp, CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF019D31), Color(0xFF015B2B))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (currentTipo) {
                            "MOTO" -> Icons.Default.TwoWheeler
                            "CARRO" -> Icons.Default.DirectionsCar
                            "BICICLETA" -> Icons.Default.DirectionsBike
                            else -> Icons.Default.DirectionsCar
                        },
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(50.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Título
                Text(
                    text = "Informações do Veículo",
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = Color(0xFF015B2B),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Indicador de progresso
                if (tipos.size > 1) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Veículo ${currentVehicleIndex + 1} de ${tipos.size}",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "• ${currentTipo}",
                            fontSize = 14.sp,
                            color = Color(0xFF019D31),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Campos do formulário
                veiculosInfo[currentTipo]?.let { info ->
                    // Tipo de Veículo (apenas exibição)
                    CampoTextoModerno(
                        label = "Tipo de Veículo",
                        value = currentTipo,
                        onValueChange = {},
                        enabled = false,
                        leadingIcon = Icons.Default.Category
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Modelo do Veículo
                    CampoTextoModerno(
                        label = "Modelo",
                        value = info.modelo,
                        onValueChange = {
                            veiculosInfo[currentTipo] = info.copy(modelo = it)
                        },
                        placeholder = "Ex: Honda CG 160",
                        leadingIcon = Icons.Default.DirectionsCar
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Ano do Veículo
                    CampoTextoModerno(
                        label = "Ano",
                        value = if (info.ano == 0) "" else info.ano.toString(),
                        onValueChange = {
                            veiculosInfo[currentTipo] = info.copy(
                                ano = it.toIntOrNull() ?: 0
                            )
                        },
                        placeholder = "Ex: 2020",
                        leadingIcon = Icons.Default.CalendarToday,
                        isNumeric = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Checkboxes com estilo moderno
                    Text(
                        text = "Verificações",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF015B2B),
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    CheckboxModerno(
                        label = "O veículo possui seguro?",
                        checked = info.possuiSeguro,
                        onCheckedChange = {
                            veiculosInfo[currentTipo] = info.copy(possuiSeguro = it)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    CheckboxModerno(
                        label = "O veículo tem compartimento adequado para transportar mercadorias?",
                        checked = info.compartimentoAdequado,
                        onCheckedChange = {
                            veiculosInfo[currentTipo] = info.copy(compartimentoAdequado = it)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    CheckboxModerno(
                        label = "O veículo possui a revisão em dia?",
                        checked = info.revisaoEmDia,
                        onCheckedChange = {
                            veiculosInfo[currentTipo] = info.copy(revisaoEmDia = it)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    CheckboxModerno(
                        label = "Não possuo antecedentes criminais",
                        checked = info.antecedentesCriminais,
                        onCheckedChange = {
                            veiculosInfo[currentTipo] = info.copy(antecedentesCriminais = it)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Botões de navegação
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Botão Voltar (se houver mais de um veículo)
                    if (currentVehicleIndex > 0) {
                        OutlinedButton(
                            onClick = { currentVehicleIndex-- },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF015B2B)
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                width = 2.dp,
                                brush = Brush.horizontalGradient(
                                    listOf(Color(0xFF015B2B), Color(0xFF00B94A))
                                )
                            )
                        ) {
                            Icon(Icons.Default.ArrowBack, null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Anterior", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Botão Próximo/Finalizar
                    Button(
                        onClick = {
                            if (currentVehicleIndex < tipos.size - 1) {
                                // Validar antes de avançar
                                val info = veiculosInfo[currentTipo]
                                if (info == null || info.modelo.isBlank() || info.ano == 0) {
                                    Toast.makeText(context, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                                } else {
                                    currentVehicleIndex++
                                }
                            } else {
                                cadastrarVeiculos()
                            }
                        },
                        modifier = Modifier.weight(1f).height(56.dp).shadow(8.dp, RoundedCornerShape(28.dp)),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(),
                        enabled = !isLoading
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
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (currentVehicleIndex < tipos.size - 1) "Próximo" else "Finalizar",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = if (currentVehicleIndex < tipos.size - 1)
                                            Icons.Default.ArrowForward else Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoTextoModerno(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean = true,
    isNumeric: Boolean = false
) {
    Column {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF015B2B)
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = {
                if (isNumeric) {
                    if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                        onValueChange(it)
                    }
                } else {
                    onValueChange(it)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            placeholder = { Text(placeholder, color = Color.Gray) },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = if (enabled) Color(0xFF019D31) else Color.Gray
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF019D31),
                unfocusedBorderColor = Color(0xFFE0E0E0),
                disabledBorderColor = Color(0xFFE0E0E0),
                focusedTextColor = Color(0xFF015B2B),
                unfocusedTextColor = Color.DarkGray,
                disabledTextColor = Color.Gray,
                cursorColor = Color(0xFF019D31),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color(0xFFF5F5F5)
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
    }
}

@Composable
fun CheckboxModerno(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (checked) Color(0xFFE8F5E9) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = if (checked) Color(0xFF019D31) else Color.Transparent,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .border(
                        width = 2.dp,
                        color = if (checked) Color(0xFF019D31) else Color(0xFFE0E0E0),
                        shape = RoundedCornerShape(6.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (checked) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                color = if (checked) Color(0xFF015B2B) else Color.DarkGray,
                fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

data class VeiculoInfo(
    val modelo: String = "",
    val ano: Int = 0,
    val possuiSeguro: Boolean = false,
    val compartimentoAdequado: Boolean = false,
    val revisaoEmDia: Boolean = false,
    val antecedentesCriminais: Boolean = false
)

