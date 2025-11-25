# Fix: NetworkOnMainThreadException no Carregamento do Perfil

## 🐛 Erro Encontrado

```
--> GET https://servidor-facilita.onrender.com/v1/facilita/usuario/121
Authorization: Bearer eyJhbGci...
--> END GET
<-- HTTP FAILED: android.os.NetworkOnMainThreadException
```

## 🔍 Causa do Problema

O Android **NÃO PERMITE** operações de rede (chamadas de API) na thread principal (UI thread) por razões de performance. Se você tentar fazer isso, o app lança `NetworkOnMainThreadException`.

### O que estava errado:

```kotlin
LaunchedEffect(Unit) {
    scope.launch {  // ❌ Por padrão usa Dispatchers.Main
        val response = api.getPerfilContratante(userId, token).execute()  // ❌ Chamada síncrona
        // Código continua...
    }
}
```

**Problemas:**
1. ❌ `scope.launch { }` sem dispatcher específico usa `Dispatchers.Main`
2. ❌ `.execute()` é uma chamada **síncrona** (bloqueante)
3. ❌ Executa na UI thread → `NetworkOnMainThreadException`

## ✅ Solução Implementada

### Código Corrigido

```kotlin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

LaunchedEffect(Unit) {
    scope.launch(Dispatchers.IO) {  // ✅ Executa em thread de I/O (background)
        try {
            val userId = TokenManager.obterUserId(context)
            val token = TokenManager.obterTokenComBearer(context)

            if (userId != null && token != null) {
                val api = RetrofitFactory.userService
                val response = api.getPerfilContratante(userId, token).execute()

                withContext(Dispatchers.Main) {  // ✅ Volta para UI thread para atualizar a interface
                    if (response.isSuccessful && response.body() != null) {
                        perfilData = response.body()?.data
                    } else {
                        errorMessage = "Erro ao carregar perfil: ${response.code()}"
                    }
                    isLoading = false
                }
            } else {
                withContext(Dispatchers.Main) {
                    errorMessage = "Usuário não autenticado"
                    isLoading = false
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                errorMessage = "Erro: ${e.message}"
                isLoading = false
            }
        }
    }
}
```

## 📚 Entendendo os Dispatchers

### Dispatchers.IO
- ✅ Para operações de **entrada/saída** (I/O)
- ✅ Chamadas de API (Retrofit)
- ✅ Leitura/escrita de arquivos
- ✅ Acesso a banco de dados
- ✅ Não bloqueia a UI

### Dispatchers.Main
- ✅ Para atualizar a **interface do usuário**
- ✅ Modificar estados do Compose
- ✅ Mostrar Toasts
- ✅ Atualizar TextViews, etc.

### Fluxo Correto

```
LaunchedEffect inicia
    ↓
launch(Dispatchers.IO)  ← Thread de background
    ↓
Busca dados da API (pode demorar, não trava a UI)
    ↓
withContext(Dispatchers.Main)  ← Volta para UI thread
    ↓
Atualiza estados (perfilData, isLoading, errorMessage)
    ↓
Interface é recomposta automaticamente
```

## 🔄 Comparação: Antes vs Depois

### ❌ ANTES (Errado)
```kotlin
LaunchedEffect(Unit) {
    scope.launch {  // Main thread
        val response = api.call().execute()  // ❌ CRASH!
        data = response.body()
    }
}
```

### ✅ DEPOIS (Correto)
```kotlin
LaunchedEffect(Unit) {
    scope.launch(Dispatchers.IO) {  // Background thread
        val response = api.call().execute()  // ✅ OK!
        withContext(Dispatchers.Main) {  // UI thread
            data = response.body()  // ✅ OK!
        }
    }
}
```

## 📝 Arquivo Modificado

**TelaPerfilPrestador.kt**

### Imports Adicionados:
```kotlin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
```

### Mudanças no LaunchedEffect:
- ✅ `launch(Dispatchers.IO)` - executa em background
- ✅ `withContext(Dispatchers.Main)` - atualiza UI
- ✅ Try-catch para capturar erros
- ✅ Tratamento adequado de estados

## 🧪 Teste Esperado

### Log Correto:
```
--> GET https://servidor-facilita.onrender.com/v1/facilita/usuario/121
Authorization: Bearer eyJhbGci...
--> END GET

[Executa em background thread - não trava a UI]

<-- 200 OK
{
  "status_code": 200,
  "data": {
    "id": 121,
    "nome": "Bueno ",
    "email": "bueno123@gmail.com",
    "telefone": "+551193990170",
    ...
  }
}

[Volta para Main thread e atualiza a UI]

✅ Perfil carregado com sucesso!
```

## 🎯 Resultados

- ✅ Sem `NetworkOnMainThreadException`
- ✅ UI não trava durante o carregamento
- ✅ Loading spinner aparece enquanto busca dados
- ✅ Dados são exibidos corretamente após o carregamento
- ✅ Erros são tratados e exibidos ao usuário

## 💡 Boas Práticas

### ✅ FAZER:
- Use `Dispatchers.IO` para chamadas de rede
- Use `withContext(Dispatchers.Main)` para atualizar a UI
- Sempre trate exceções em coroutines
- Mostre loading/error states

### ❌ NÃO FAZER:
- Fazer chamadas de rede na Main thread
- Usar `.execute()` sem dispatcher apropriado
- Esquecer de atualizar o estado de loading
- Ignorar exceções

## 📚 Recursos Adicionais

### Alternativa: Suspend Functions (Melhor Abordagem)

Se o `UserService` usar suspend functions:

```kotlin
interface UserService {
    @GET("v1/facilita/usuario/{id}")
    suspend fun getPerfilContratante(  // ← suspend
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): PerfilContratanteResponse  // ← Direct return, not Call<>
}
```

Então o código ficaria ainda mais simples:

```kotlin
LaunchedEffect(Unit) {
    scope.launch(Dispatchers.IO) {
        try {
            val response = api.getPerfilContratante(userId, token)
            withContext(Dispatchers.Main) {
                perfilData = response.data
                isLoading = false
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                errorMessage = "Erro: ${e.message}"
                isLoading = false
            }
        }
    }
}
```

**Nota:** Por enquanto mantemos a abordagem com `.execute()` pois o serviço retorna `Call<>`, mas a abordagem com `suspend` é mais moderna e recomendada.

## ✅ Status

**Implementado e testável!** A correção foi aplicada e o app agora:
- ✅ Faz requisições de API em background
- ✅ Não trava a UI
- ✅ Trata erros corretamente
- ✅ Exibe loading/error states apropriadamente

