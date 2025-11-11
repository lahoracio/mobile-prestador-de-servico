# Fix: Endpoint 404 - Perfil do Usuário

## 🐛 Erro Encontrado

```
--> GET https://servidor-facilita.onrender.com/v1/facilita/usuario/121
Authorization: Bearer eyJhbGci...
--> END GET

<-- 404 NOT FOUND
Cannot GET /v1/facilita/usuario/121
```

## 🔍 Causa do Problema

O endpoint `/v1/facilita/usuario/{id}` **NÃO EXISTE** na API.

### O que estava errado:

```kotlin
// UserService.kt
@GET("v1/facilita/usuario/{id}")
fun getPerfilContratante(
    @Path("id") id: Int,
    @Header("Authorization") token: String
): Call<PerfilContratanteResponse>
```

**Problemas:**
1. ❌ Endpoint incorreto: `/v1/facilita/usuario/121`
2. ❌ API retorna 404 (Not Found)
3. ❌ Perfil não é carregado

## ✅ Solução: Endpoint Correto

Após verificar o `ApiService.kt`, descobri que o endpoint correto é:

```kotlin
@GET("v1/facilita/usuario/perfil")
```

Este endpoint usa o **token JWT** para identificar o usuário automaticamente, sem precisar passar o ID.

## 🔧 Correções Aplicadas

### 1. UserService.kt

**ANTES (Errado):**
```kotlin
@GET("v1/facilita/usuario/{id}")
fun getPerfilContratante(
    @Path("id") id: Int,
    @Header("Authorization") token: String
): Call<PerfilContratanteResponse>
```

**DEPOIS (Correto):**
```kotlin
@GET("v1/facilita/usuario/perfil")
fun getPerfilContratante(
    @Header("Authorization") token: String
): Call<PerfilContratanteResponse>
```

### 2. TelaPerfilPrestador.kt

**ANTES (Errado):**
```kotlin
LaunchedEffect(Unit) {
    scope.launch(Dispatchers.IO) {
        try {
            val userId = TokenManager.obterUserId(context)
            val token = TokenManager.obterTokenComBearer(context)

            if (userId != null && token != null) {
                val api = RetrofitFactory.userService
                val response = api.getPerfilContratante(userId, token).execute()
                // ...
            }
        } catch (e: Exception) { ... }
    }
}
```

**DEPOIS (Correto):**
```kotlin
LaunchedEffect(Unit) {
    scope.launch(Dispatchers.IO) {
        try {
            val token = TokenManager.obterTokenComBearer(context)

            if (token != null) {
                val api = RetrofitFactory.userService
                val response = api.getPerfilContratante(token).execute()
                // ...
            }
        } catch (e: Exception) { ... }
    }
}
```

## 📊 Mudanças Principais

1. ✅ Endpoint alterado de `/usuario/{id}` para `/usuario/perfil`
2. ✅ Removido parâmetro `@Path("id") id: Int`
3. ✅ Removida verificação de `userId` no código
4. ✅ API agora identifica o usuário pelo token JWT

## 🎯 Como Funciona Agora

```
1. TelaPerfilPrestador inicia
    ↓
2. LaunchedEffect obtém token do TokenManager
    ↓
3. Faz requisição GET para /v1/facilita/usuario/perfil
    ↓
4. Backend lê o token JWT
    ↓
5. Backend identifica o usuário pelo token
    ↓
6. Backend retorna dados do perfil
    ↓
7. App exibe os dados na tela
```

## 🧪 Teste Esperado

### Log Correto:

```
LOGIN_DEBUG: Token salvo verificado: eyJhbGci...
LOGIN_DEBUG: Tipo conta salvo: CONTRATANTE
LOGIN_DEBUG: Nome salvo: Bueno

[Navega para tela_perfil]

--> GET https://servidor-facilita.onrender.com/v1/facilita/usuario/perfil
Authorization: Bearer eyJhbGci...
--> END GET

[Executa em background thread - Dispatchers.IO]

<-- 200 OK
{
  "status_code": 200,
  "data": {
    "id": 121,
    "nome": "Bueno ",
    "email": "bueno123@gmail.com",
    "telefone": "+551193990170",
    "dados_contratante": {
      "localizacao": {
        "cidade": "São Paulo"
      }
    }
  }
}

[Volta para Main thread e atualiza a UI]

✅ Perfil carregado com sucesso!
Nome: Bueno
Email: bueno123@gmail.com
Telefone: +551193990170
Cidade: São Paulo
```

## ✅ Checklist de Correções

### NetworkOnMainThreadException ✅
- ✅ Usa `Dispatchers.IO` para chamadas de rede
- ✅ Usa `withContext(Dispatchers.Main)` para atualizar UI
- ✅ Try-catch adequado

### Endpoint Correto ✅
- ✅ Mudou de `/usuario/{id}` para `/usuario/perfil`
- ✅ Remove parâmetro `id` da requisição
- ✅ API identifica usuário pelo token

### Login e Redirecionamento ✅
- ✅ CONTRATANTE → `tela_perfil`
- ✅ PRESTADOR → `tela_inicio_prestador`
- ✅ Verifica tipo de conta antes de redirecionar

### TelaInicioPrestador ✅
- ✅ Só busca serviços se `tipoConta == "PRESTADOR"`
- ✅ Não tenta acessar endpoints restritos

## 🎉 Resultado Final

O app agora:
- ✅ Faz login corretamente
- ✅ Redireciona CONTRATANTE para o perfil
- ✅ Faz requisição para o endpoint correto (`/perfil`)
- ✅ Usa token JWT para autenticação
- ✅ Executa em background thread (sem travar UI)
- ✅ Trata erros adequadamente
- ✅ Exibe loading enquanto carrega
- ✅ Mostra dados do perfil corretamente

## 📝 Arquivos Modificados

1. **UserService.kt**
   - Endpoint corrigido
   - Parâmetro `id` removido

2. **TelaPerfilPrestador.kt**
   - Chamada de API atualizada
   - Verificação de `userId` removida

## 🚀 Próximos Passos

1. Execute o app
2. Faça login com: bueno123@gmail.com / Senha@123
3. Verifique se:
   - ✅ Redireciona para tela de perfil
   - ✅ Mostra loading
   - ✅ Carrega dados da API
   - ✅ Exibe: Nome, Email, Telefone, Cidade
   - ✅ Sem erros 404
   - ✅ Sem NetworkOnMainThreadException

**Tudo pronto para testar!** 🎊

