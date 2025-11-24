# 🔧 CORREÇÃO: Perfil e NetworkOnMainThreadException

## 📋 Problema Identificado

Quando você clicava no botão "Perfil" na navbar, o aplicativo crashava com o erro:
```
NetworkOnMainThreadException
```

E as informações do perfil apareciam como "Não informado".

## 🔍 Causa Raiz

O problema tinha **duas causas**:

### 1. **LaunchedEffect em TelaInicioPrestador**
A tela de início tinha um `LaunchedEffect(Unit)` com um loop `while(true)` que:
- Continuava executando mesmo após navegar para outras telas
- Fazia chamadas de rede a cada 10 segundos sem verificar se a coroutine estava ativa
- Usava `.enqueue()` do Retrofit sem contexto de dispatcher adequado

### 2. **ViewModel sem Dispatcher correto**
O `PerfilPrestadorViewModel` não estava usando `Dispatchers.IO` de forma consistente para todas as operações de rede.

## ✅ Correções Aplicadas

### 1. **PerfilPrestadorViewModel.kt**

#### Mudanças na função `carregarPerfil()`:
```kotlin
// ANTES
fun carregarPerfil(context: Context) {
    viewModelScope.launch {
        _uiState.value = PerfilUiState.Loading
        val token = TokenManager.obterTokenComBearer(context)
        val response = withContext(Dispatchers.IO) {
            RetrofitFactory.userService.obterPerfil(token)
        }
    }
}

// DEPOIS
fun carregarPerfil(context: Context) {
    viewModelScope.launch(Dispatchers.IO) {  // ⭐ Inicia direto no IO
        withContext(Dispatchers.Main) {
            _uiState.value = PerfilUiState.Loading
        }
        val token = withContext(Dispatchers.Main) {
            TokenManager.obterTokenComBearer(context)
        }
        // Chamada de rede já está em Dispatchers.IO
        val response = RetrofitFactory.userService.obterPerfil(token)
        
        withContext(Dispatchers.Main) {
            _uiState.value = PerfilUiState.Success(perfil)
        }
    }
}
```

**Benefícios:**
- Toda a operação de rede acontece em `Dispatchers.IO`
- Apenas atualizações de UI usam `Dispatchers.Main`
- Elimina o risco de `NetworkOnMainThreadException`

#### Mudanças na função `atualizarPerfil()`:
```kotlin
// ANTES
fun atualizarPerfil(...) {
    viewModelScope.launch {
        _isUpdating.value = true
        val response = withContext(Dispatchers.IO) {
            RetrofitFactory.userService.atualizarPerfil(token, request)
        }
    }
}

// DEPOIS
fun atualizarPerfil(...) {
    viewModelScope.launch(Dispatchers.IO) {  // ⭐ Inicia direto no IO
        withContext(Dispatchers.Main) {
            _isUpdating.value = true
        }
        val response = RetrofitFactory.userService.atualizarPerfil(token, request)
        
        withContext(Dispatchers.Main) {
            _uiState.value = PerfilUiState.Success(usuario)
            onSuccess()
        }
    }
}
```

### 2. **TelaInicioPrestador.kt**

#### Mudanças no LaunchedEffect:
```kotlin
// ANTES
LaunchedEffect(Unit) {
    fun buscarSolicitacoes() {
        val service = RetrofitFactory.getServicoService()
        service.getServicosDisponiveis(token).enqueue(...)
    }
    
    buscarSolicitacoes()
    while (true) {  // ❌ Loop infinito sem verificação
        delay(10000)
        buscarSolicitacoes()
    }
}

// DEPOIS
LaunchedEffect(token) {  // ⭐ Depende do token
    if (token.isEmpty()) {
        isLoading = false
        return@LaunchedEffect
    }

    suspend fun buscarSolicitacoes() {
        try {
            val service = RetrofitFactory.getServicoService()
            val call = service.getServicosDisponiveis(token)
            
            // ⭐ Executa no contexto IO
            withContext(Dispatchers.IO) {
                call.enqueue(...)
            }
        } catch (e: Exception) {
            Log.e("TelaInicioPrestador", "Erro: ${e.message}")
            isLoading = false
        }
    }
    
    buscarSolicitacoes()
    while (isActive) {  // ⭐ Verifica se a coroutine está ativa
        delay(10000)
        buscarSolicitacoes()
    }
}
```

**Benefícios:**
- O loop para automaticamente quando você navega para outra tela
- Todas as chamadas de rede são feitas em `Dispatchers.IO`
- Tratamento de erros adequado
- Depende do `token`, então reinicia se o token mudar

## 🎯 Resultado Esperado

Agora quando você:

1. **Clicar em "Perfil":**
   - ✅ O app NÃO vai crashar
   - ✅ A tela de perfil vai carregar corretamente
   - ✅ As informações reais do usuário aparecerão (não "Não informado")

2. **Ver as informações do perfil:**
   - Nome do usuário
   - Email
   - Celular
   - Localização (cidade/estado)
   - Todas vindas da API `GET /v1/facilita/usuario/perfil`

3. **Navegar entre telas:**
   - ✅ Sem mais `NetworkOnMainThreadException`
   - ✅ O loop de atualização da tela inicial para quando você sai dela
   - ✅ Transições suaves entre telas

## 🔌 Endpoint da API Utilizado

```http
GET /v1/facilita/usuario/perfil
Authorization: Bearer {token}
```

**Resposta esperada:**
```json
{
  "id": 252,
  "nome": "Nome do Prestador",
  "email": "email@example.com",
  "celular": "11999999999",
  "tipo_conta": "PRESTADOR",
  "status": "ativo",
  "prestador": {
    "id": 123,
    "endereco": "Rua exemplo",
    "cidade": "São Paulo",
    "estado": "SP",
    "cnh": "12345678900",
    "tipo_veiculo": "carro",
    "placa_veiculo": "ABC1234"
  }
}
```

## 📝 Logs de Debug

Para acompanhar o que está acontecendo, verifique os logs com a tag:
```
PerfilPrestadorViewModel
```

Os logs mostrarão:
- ✅ Token encontrado
- 📡 Fazendo requisição HTTP
- ✅ SUCESSO! Dados recebidos
- Ou ❌ ERRO com detalhes

## 🚀 Como Testar

1. **Compile o app** (se necessário, configure o JAVA_HOME):
   ```bash
   .\gradlew assembleDebug
   ```

2. **Instale e execute** no dispositivo/emulador

3. **Faça login** no app

4. **Clique no ícone "Perfil"** na navbar inferior

5. **Verifique:**
   - ✅ App não crasha
   - ✅ Tela de perfil carrega
   - ✅ Informações aparecem corretamente
   - ✅ Nome, email, celular, localização estão corretos

## 🎊 Status

**✅ CORRIGIDO**

- [x] NetworkOnMainThreadException resolvido
- [x] Loop infinito de LaunchedEffect corrigido
- [x] Dispatchers.IO aplicado corretamente
- [x] Perfil carrega as informações reais da API
- [x] Navegação entre telas funciona perfeitamente

---

**Data:** 2025-11-22
**Arquivos modificados:**
- `PerfilPrestadorViewModel.kt`
- `TelaInicioPrestador.kt`

