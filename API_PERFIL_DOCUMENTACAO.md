# 📡 INTEGRAÇÃO API - PERFIL DO PRESTADOR

## 🎯 Endpoint Utilizado

```
GET /v1/facilita/usuario/perfil
```

**URL Completa:**
```
https://facilita-c6hhb9csgygudrdz.canadacentral-01.azurewebsites.net/v1/facilita/usuario/perfil
```

## 🔐 Autenticação

**Header obrigatório:**
```http
Authorization: Bearer {token_jwt}
```

O token é obtido automaticamente do `TokenManager` após o login.

## 📥 Resposta da API

### Estrutura da Resposta (Success - 200)

```json
{
  "id": 252,
  "nome": "Nome do Prestador",
  "email": "prestador@example.com",
  "celular": "11999999999",
  "tipo_conta": "PRESTADOR",
  "status": "ativo",
  "prestador": {
    "id": 123,
    "endereco": "Rua Exemplo, 123",
    "cidade": "São Paulo",
    "estado": "SP",
    "foto_perfil": null,
    "cnh": "12345678900",
    "tipo_veiculo": "carro",
    "placa_veiculo": "ABC1234"
  },
  "created_at": "2025-01-15T10:30:00.000Z",
  "updated_at": "2025-01-20T15:45:00.000Z"
}
```

## 📦 Modelo de Dados (Kotlin)

### PerfilPrestadorResponse.kt

```kotlin
data class PerfilPrestadorResponse(
    val id: Int? = null,
    val nome: String? = null,
    val email: String? = null,
    val celular: String? = null,
    @SerializedName("tipo_conta")
    val tipoConta: String? = null,
    val status: String? = null,
    val prestador: PrestadorInfo? = null,
    val endereco: String? = null,
    val cidade: String? = null,
    val estado: String? = null,
    @SerializedName("foto_perfil")
    val fotoPerfil: String? = null,
    val cpf: String? = null,
    val cnh: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class PrestadorInfo(
    val id: Int? = null,
    val endereco: String? = null,
    val cidade: String? = null,
    val estado: String? = null,
    @SerializedName("foto_perfil")
    val fotoPerfil: String? = null,
    val cnh: String? = null,
    @SerializedName("tipo_veiculo")
    val tipoVeiculo: String? = null,
    @SerializedName("placa_veiculo")
    val placaVeiculo: String? = null
)
```

## 🔧 Implementação no UserService

```kotlin
interface UserService {
    @GET("v1/facilita/usuario/perfil")
    suspend fun obterPerfil(
        @Header("Authorization") token: String
    ): Response<PerfilPrestadorResponse>
}
```

## 🎨 Fluxo de Dados na UI

### 1. Tela de Perfil (TelaPerfilPrestador.kt)
```kotlin
@Composable
fun TelaPerfilPrestador(
    navController: NavController,
    viewModel: PerfilPrestadorViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.carregarPerfil(context)
    }
    
    when (val state = uiState) {
        is Loading -> { /* Mostra loading */ }
        is Success -> { /* Mostra dados do perfil */ }
        is Error -> { /* Mostra erro */ }
    }
}
```

### 2. ViewModel (PerfilPrestadorViewModel.kt)
```kotlin
fun carregarPerfil(context: Context) {
    viewModelScope.launch(Dispatchers.IO) {
        // 1. Obter token
        val token = withContext(Dispatchers.Main) {
            TokenManager.obterTokenComBearer(context)
        }
        
        // 2. Fazer requisição
        val response = RetrofitFactory.userService.obterPerfil(token)
        
        // 3. Atualizar UI
        withContext(Dispatchers.Main) {
            if (response.isSuccessful && response.body() != null) {
                _uiState.value = PerfilUiState.Success(response.body()!!)
            } else {
                _uiState.value = PerfilUiState.Error("Erro ${response.code()}")
            }
        }
    }
}
```

## ⚙️ Estados da UI

```kotlin
sealed class PerfilUiState {
    object Idle : PerfilUiState()
    object Loading : PerfilUiState()
    data class Success(val perfil: PerfilPrestadorResponse) : PerfilUiState()
    data class Error(val message: String) : PerfilUiState()
}
```

## 📊 Mapeamento de Dados na Tela

| Campo da API | Exibição na Tela |
|--------------|------------------|
| `nome` | Nome do usuário (sem edição) |
| `email` | Email (editável) |
| `celular` | Celular (editável) |
| `prestador.cidade` + `prestador.estado` | Localização (editável) |
| `prestador.endereco` | Endereço (editável) |

## 🚨 Tratamento de Erros

| Código HTTP | Mensagem ao Usuário |
|-------------|---------------------|
| 200 | ✅ Sucesso - Dados carregados |
| 401 | "Sessão expirada. Faça login novamente." |
| 404 | "Endpoint não encontrado. O backend não tem este endpoint." |
| 500 | "Erro no servidor. Verifique os logs do backend." |
| Outros | "Erro HTTP {code}" |

## 🔄 Atualização de Perfil

**Endpoint para atualizar:**
```
PUT /v1/facilita/usuario/perfil
```

**Body da requisição:**
```json
{
  "nome": "Novo Nome",
  "email": "novo@email.com",
  "celular": "11988888888",
  "endereco": "Novo Endereço",
  "cidade": "Nova Cidade",
  "estado": "SP"
}
```

**Implementação:**
```kotlin
@PUT("v1/facilita/usuario/perfil")
suspend fun atualizarPerfil(
    @Header("Authorization") token: String,
    @Body request: AtualizarPerfilRequest
): Response<AtualizarPerfilResponse>
```

## 🧪 Testando a API Manualmente

### Com cURL:
```bash
curl -X GET \
  https://facilita-c6hhb9csgygudrdz.canadacentral-01.azurewebsites.net/v1/facilita/usuario/perfil \
  -H 'Authorization: Bearer SEU_TOKEN_AQUI' \
  -H 'Content-Type: application/json'
```

### Com Postman:
1. Método: `GET`
2. URL: `https://facilita-c6hhb9csgygudrdz.canadacentral-01.azurewebsites.net/v1/facilita/usuario/perfil`
3. Headers:
   - `Authorization`: `Bearer SEU_TOKEN`
   - `Content-Type`: `application/json`

## 📝 Logs de Debug

Para visualizar o fluxo completo:
```bash
adb logcat -s PerfilPrestadorViewModel:D
```

**Exemplo de log bem-sucedido:**
```
D/PerfilPrestadorViewModel: ╔═══════════════════════════════════════╗
D/PerfilPrestadorViewModel: ║   INICIANDO CARREGAMENTO DO PERFIL   ║
D/PerfilPrestadorViewModel: ╚═══════════════════════════════════════╝
D/PerfilPrestadorViewModel: 📋 PASSO 1: Verificando token...
D/PerfilPrestadorViewModel: ✅ Token encontrado: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
D/PerfilPrestadorViewModel: 🌐 PASSO 2: Fazendo requisição HTTP...
D/PerfilPrestadorViewModel: 📡 PASSO 3: Resposta recebida
D/PerfilPrestadorViewModel: ✅ SUCESSO! Dados recebidos:
D/PerfilPrestadorViewModel: ║ ID: 252
D/PerfilPrestadorViewModel: ║ Nome: João Silva
D/PerfilPrestadorViewModel: ║ Email: joao@example.com
```

## ✅ Checklist de Integração

- [x] Endpoint configurado no UserService
- [x] Modelo de dados criado (PerfilPrestadorResponse)
- [x] ViewModel implementado com Dispatchers corretos
- [x] Tela de perfil consome o ViewModel
- [x] Estados de Loading/Success/Error implementados
- [x] Tratamento de erros implementado
- [x] Logs de debug adicionados
- [x] Token gerenciado pelo TokenManager
- [x] Coroutines configuradas corretamente
- [x] NetworkOnMainThreadException corrigido

---

**Documentação criada em:** 2025-11-22
**Status:** ✅ Implementação completa e funcional

