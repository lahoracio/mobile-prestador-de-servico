    "message": "Prestador criado com sucesso!",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "prestador": {
        "id": 1,
        "id_usuario": 113,
        "localizacao": [
            { "id": 176, "logradouro": "Av. Paulista", ... },
            { "id": 177, "logradouro": "Rua Augusta", ... }
        ]
    }
}
```

## ✅ Arquivos Modificados

1. ✅ `/app/src/main/java/com/exemple/facilita/model/LocalizacaoRequest.kt` (CRIADO)
2. ✅ `/app/src/main/java/com/exemple/facilita/model/TipoContaRequest.kt` (ATUALIZADO)
3. ✅ `/app/src/main/java/com/exemple/facilita/sevice/UserService.kt` (ATUALIZADO)
4. ✅ `/app/src/main/java/com/exemple/facilita/viewmodel/TipoContaViewModel.kt` (ATUALIZADO)
5. ⏳ `/app/src/main/java/com/exemple/facilita/screens/TelaCompletarPerfilPrestador.kt` (PRECISA SER ATUALIZADO)

## 🎯 Próximos Passos

A tela TelaCompletarPerfilPrestador.kt precisa ser reescrita para implementar o fluxo em 2 etapas.
Os componentes de backend (models, services, viewmodel) já estão prontos e funcionando.

## 📌 Referências

- Arquivo de backup: `TelaCompletarPerfilPrestador_OLD.kt`
- Documentação da API: Anexada pelo usuário
- Fluxo corrigido: `FLUXO_CORRIGIDO_CADASTRO.md`
# Implementação do Fluxo em 2 Etapas - Tela Completar Perfil

## 📋 Requisitos

A tela de Completar Perfil deve ser dividida em 2 etapas:

### Etapa 1: Cadastro de Endereços (2 localizações)
1. **Endereço onde mora** - Primeira localização
2. **Região onde atua** - Segunda localização

### Etapa 2: Cadastro de Documentos (após prestador ser criado)
1. CNH com EAR
2. Documentos (CPF, RG)
3. Informações do veículo

## 🔧 Componentes Criados

### 1. Model: LocalizacaoRequest.kt ✅
```kotlin
data class LocalizacaoRequest(
    val logradouro: String,
    val numero: String,
    val bairro: String,
    val cidade: String,
    val cep: String,
    val latitude: Double,
    val longitude: Double
)

data class LocalizacaoResponse(
    val id: Int,
    val logradouro: String,
    val numero: String,
    val bairro: String,
    val cidade: String,
    val cep: String,
    val latitude: String,
    val longitude: String
)
```

### 2. Model Atualizado: TipoContaRequest.kt ✅
```kotlin
// ANTES: recebia coordenadas
data class CriarPrestadorRequest(
    val localizacao: List<Double> // [latitude, longitude]
)

// DEPOIS: recebe IDs
data class CriarPrestadorRequest(
    val localizacao: List<Int> // [id_endereco_mora, id_regiao_atua]
)
```

### 3. Service Atualizado: UserService.kt ✅
```kotlin
@Headers("Content-Type: application/json")
@POST("v1/facilita/localizacao")
suspend fun criarLocalizacao(
    @Body request: LocalizacaoRequest
): Response<LocalizacaoResponse>
```

### 4. ViewModel Atualizado: TipoContaViewModel.kt (PrestadorViewModel) ✅
```kotlin
// Nova função para criar localização
fun criarLocalizacao(
    logradouro: String,
    numero: String,
    bairro: String,
    cidade: String,
    cep: String,
    latitude: Double,
    longitude: Double,
    onSuccess: (Int) -> Unit
)

// Função atualizada para criar prestador
fun criarPrestador(token: String, idsLocalizacao: List<Int>)

// Novos estados
val prestadorCriado: StateFlow<Boolean>
val etapaAtual: StateFlow<Int> // 1 = endereços, 2 = documentos
```

## 📝 Fluxo de Implementação

### Passo 1: Cadastro do Primeiro Endereço
```kotlin
// Usuário clica no card "Onde você mora"
// Abre Google Places Autocomplete
// Seleciona o endereço
// Chama API POST /v1/facilita/localizacao
// Recebe o ID da localização (ex: 176)
// Salva: enderecoMora = EnderecoInfo(id = 176, ...)
```

### Passo 2: Cadastro do Segundo Endereço
```kotlin
// Usuário clica no card "Região onde você atua"
// Abre Google Places Autocomplete
// Seleciona o endereço
// Chama API POST /v1/facilita/localizacao  
// Recebe o ID da localização (ex: 177)
// Salva: enderecoAtua = EnderecoInfo(id = 177, ...)
```

### Passo 3: Confirmação dos Endereços
```kotlin
// Botão "Confirmar Endereços" habilitado após ambos preenchidos
// Ao clicar:
val id1 = enderecoMora.idLocalizacao // 176
val id2 = enderecoAtua.idLocalizacao // 177

// Chama API POST /v1/facilita/prestador
prestadorViewModel.criarPrestador(token, listOf(id1, id2))

// Request enviado:
{
  "localizacao": [176, 177]
}

// Response recebido:
{
  "message": "Prestador criado com sucesso!",
  "token": "novo_token_atualizado",
  "prestador": { ... }
}

// Salva novo token
// Atualiza prestadorCriado = true
// UI automaticamente mostra Etapa 2 (documentos)
```

### Passo 4: Cadastro de Documentos
```kotlin
// Após prestadorCriado == true
// A tela mostra:
// - CNH com EAR → navegar para tela_cnh
// - Documentos → navegar para tela_documentos  
// - Informações do veículo → navegar para tela_tipo_veiculo
```

## 🎨 Estrutura da Tela

```kotlin
@Composable
fun TelaCompletarPerfilPrestador(
    navController: NavController,
    perfilViewModel: PerfilViewModel
) {
    val prestadorCriado by prestadorViewModel.prestadorCriado.collectAsState()
    
    // Estados dos endereços
    var enderecoMora by remember { mutableStateOf<EnderecoInfo?>(null) }
    var enderecoAtua by remember { mutableStateOf<EnderecoInfo?>(null) }
    
    // UI
    if (!prestadorCriado) {
        // ETAPA 1: Mostrar cards de endereços
        CardEndereco(titulo = "Onde você mora", endereco = enderecoMora)
        CardEndereco(titulo = "Região onde você atua", endereco = enderecoAtua)
        Button("Confirmar Endereços") {
            // Criar prestador com os IDs
        }
    } else {
        // ETAPA 2: Mostrar lista de documentos
        ListaDocumentos()
        Button("Finalizar")
    }
}
```

## 📊 Data Class Auxiliar

```kotlin
data class EnderecoInfo(
    val endereco: String,           // Endereço completo formatado
    val logradouro: String,         // Rua
    val numero: String,             // Número
    val bairro: String,             // Bairro
    val cidade: String,             // Cidade
    val cep: String,                // CEP
    val latitude: Double,           // Coordenada
    val longitude: Double,          // Coordenada
    val idLocalizacao: Int? = null  // ID retornado pela API
)
```

## 🔄 Fluxo Completo da API

### 1. POST /v1/facilita/localizacao (2x - para cada endereço)
```
Request:
{
    "logradouro": "Av. Paulista",
    "numero": "1000",
    "bairro": "Bela Vista",
    "cidade": "São Paulo",
    "cep": "01310100",
    "latitude": -23.564,
    "longitude": -46.652
}

Response:
{
    "id": 176,
    "logradouro": "Av. Paulista",
    ...
}
```

### 2. POST /v1/facilita/prestador
```
Request:
{
    "localizacao": [176, 177]
}

Response:
{

