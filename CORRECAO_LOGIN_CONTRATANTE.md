# Correção: Login de CONTRATANTE Redirecionando Corretamente

## 🐛 Problema Identificado

```
Token recebido: eyJhbGci...
Tipo de conta: CONTRATANTE
User ID: 121
Nome do usuário: Bueno

--> GET https://servidor-facilita.onrender.com/v1/facilita/servico/disponiveis
<-- 403 FORBIDDEN
{"status_code":403,"message":"Acesso permitido apenas para prestadores"}
```

### Causa
1. ❌ Login estava sempre redirecionando para `tela_inicio_prestador` independente do tipo de conta
2. ❌ TelaInicioPrestador tentava buscar serviços disponíveis sem verificar se era PRESTADOR
3. ❌ API retornava 403 porque CONTRATANTE não pode acessar endpoint de prestadores

## ✅ Solução Implementada

### 1. TelaLogin.kt - Redirecionamento Baseado no Tipo de Conta

**ANTES:**
```kotlin
TokenManager.salvarToken(context, token, tipoConta, userId, nomeUsuario)
navController.navigate("tela_inicio_prestador")
```

**DEPOIS:**
```kotlin
TokenManager.salvarToken(context, token, tipoConta, userId, nomeUsuario)

// Redirecionar baseado no tipo de conta
when (tipoConta) {
    "PRESTADOR" -> {
        navController.navigate("tela_inicio_prestador") {
            popUpTo("tela_login") { inclusive = true }
        }
    }
    "CONTRATANTE" -> {
        navController.navigate("tela_perfil") {
            popUpTo("tela_login") { inclusive = true }
        }
    }
    else -> {
        navController.navigate("tela_inicio_prestador")
    }
}
```

### 2. TelaInicioPrestador.kt - Verificação de Tipo de Conta

**ANTES:**
```kotlin
val token = TokenManager.obterTokenComBearer(context) ?: ""

LaunchedEffect(Unit) {
    val service = RetrofitFactory.getServicoService()
    service.getServicosDisponiveis(token).enqueue(...)
}
```

**DEPOIS:**
```kotlin
val token = TokenManager.obterTokenComBearer(context) ?: ""
val tipoConta = TokenManager.obterTipoConta(context)

LaunchedEffect(Unit) {
    if (tipoConta == "PRESTADOR") {
        val service = RetrofitFactory.getServicoService()
        service.getServicosDisponiveis(token).enqueue(...)
    } else {
        isLoading = false
        Toast.makeText(context, "Acesso apenas para prestadores", Toast.LENGTH_SHORT).show()
    }
}
```

## 🎯 Fluxo Corrigido

### Para PRESTADOR:
```
Login (PRESTADOR)
    ↓
TelaInicioPrestador (lista de solicitações)
    ↓ [API busca serviços disponíveis com sucesso]
    ↓
Lista de serviços é exibida
```

### Para CONTRATANTE:
```
Login (CONTRATANTE)
    ↓
TelaPerfilPrestador (perfil do contratante) ✅
    ↓ [API busca dados do usuário com sucesso]
    ↓
Perfil é exibido com:
    - Nome: Bueno
    - Email: bueno123@gmail.com
    - Telefone: +551193990170
    - Cidade: São Paulo
```

## 📝 Mudanças Detalhadas

### TelaLogin.kt
- ✅ Adicionado `when` para verificar `tipoConta`
- ✅ PRESTADOR → `tela_inicio_prestador`
- ✅ CONTRATANTE → `tela_perfil`
- ✅ Usa `popUpTo` para limpar a pilha de navegação (não volta pro login ao pressionar voltar)

### TelaInicioPrestador.kt
- ✅ Obtém `tipoConta` do TokenManager
- ✅ Só faz requisição à API se `tipoConta == "PRESTADOR"`
- ✅ Mostra Toast informativo se não for prestador
- ✅ Evita erro 403 da API

## 🧪 Teste Esperado

### Teste 1: Login como CONTRATANTE
1. ✅ Login com: bueno123@gmail.com
2. ✅ Tipo detectado: CONTRATANTE
3. ✅ Redireciona para `tela_perfil`
4. ✅ Perfil carrega dados da API
5. ✅ Exibe: Nome, Email, Telefone, Cidade
6. ✅ Sem erro 403

### Teste 2: Login como PRESTADOR
1. ✅ Login com conta de prestador
2. ✅ Tipo detectado: PRESTADOR
3. ✅ Redireciona para `tela_inicio_prestador`
4. ✅ Lista de serviços é carregada
5. ✅ Pode aceitar/recusar solicitações

### Teste 3: BottomNav
1. ✅ CONTRATANTE no perfil pode navegar pelo BottomNav
2. ✅ PRESTADOR na lista pode navegar pelo BottomNav
3. ✅ Todas as rotas funcionam

## 🔐 Segurança

- ✅ Verificação no frontend (TelaInicioPrestador)
- ✅ Verificação no backend (API retorna 403 se necessário)
- ✅ Token JWT com tipo de conta validado
- ✅ Usuário não consegue acessar telas incorretas

## 📊 Log Esperado (CONTRATANTE)

```
LOGIN_DEBUG: Token recebido: eyJhbGci...
LOGIN_DEBUG: Tipo de conta: CONTRATANTE
LOGIN_DEBUG: User ID: 121
LOGIN_DEBUG: Nome do usuário: Bueno
LOGIN_DEBUG: Token salvo verificado: eyJhbGci...
LOGIN_DEBUG: Tipo conta salvo: CONTRATANTE
LOGIN_DEBUG: Nome salvo: Bueno

[Navega para tela_perfil]

--> GET https://servidor-facilita.onrender.com/v1/facilita/usuario/121
<-- 200 OK
{
  "status_code": 200,
  "data": {
    "id": 121,
    "nome": "Bueno ",
    "email": "bueno123@gmail.com",
    "telefone": "+551193990170",
    "cidade": "São Paulo"
  }
}
```

## 🐛 Problema Adicional Encontrado: NetworkOnMainThreadException

```
--> GET https://servidor-facilita.onrender.com/v1/facilita/usuario/121
<-- HTTP FAILED: android.os.NetworkOnMainThreadException
```

### Causa
A chamada da API estava sendo feita na thread principal (UI thread) usando `.execute()` síncrono.

### Solução
Modificado `TelaPerfilPrestador.kt` para usar coroutines corretamente:

```kotlin
LaunchedEffect(Unit) {
    scope.launch(Dispatchers.IO) {  // ✅ Executa em background thread
        try {
            val api = RetrofitFactory.userService
            val response = api.getPerfilContratante(userId, token).execute()

            withContext(Dispatchers.Main) {  // ✅ Volta para UI thread
                if (response.isSuccessful && response.body() != null) {
                    perfilData = response.body()?.data
                } else {
                    errorMessage = "Erro ao carregar perfil: ${response.code()}"
                }
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

### Imports Adicionados
```kotlin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
```

## ✅ Status: Implementado e Testável

Todas as correções foram aplicadas. O app agora:
- ✅ Detecta o tipo de conta no login
- ✅ Redireciona CONTRATANTE para o perfil
- ✅ Redireciona PRESTADOR para lista de serviços
- ✅ Não faz requisições indevidas à API
- ✅ Evita erros 403
- ✅ Faz requisições de API em background thread (Dispatchers.IO)
- ✅ Atualiza UI na thread principal (Dispatchers.Main)
- ✅ Sem NetworkOnMainThreadException

**Próximo passo:** Execute o app e teste o login com a conta do Bueno!

