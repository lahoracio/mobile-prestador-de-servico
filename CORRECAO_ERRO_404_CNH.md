# 🔧 CORREÇÃO: Erro 404 "Prestador não encontrado" ao Cadastrar CNH

## 🐛 Problema Identificado

### Erro:
```
2025-11-11 16:04:43.980 22866-22866 CNH_ERROR
Erro HTTP 404: {"message":"Prestador não encontrado."}
```

### Causa Raiz:
O erro acontecia porque **o usuário não estava registrado como PRESTADOR no banco de dados da API**.

O fluxo estava assim:
```
1. Cadastro → Token salvo
2. Login → Token recuperado
3. Escolher tipo de conta → ❌ APENAS navegação, SEM chamada à API
4. Tentar cadastrar CNH → ❌ ERRO 404: Prestador não encontrado
```

**Por quê?** A tela `TelaTipoContaServico` apenas navegava para a próxima tela, mas **NÃO informava à API** que o usuário escolheu ser PRESTADOR. O backend não sabia que aquele token pertencia a um prestador!

---

## ✅ Solução Implementada

### Novo Fluxo:
```
1. Cadastro → Token salvo
2. Login → Token recuperado
3. Escolher tipo de conta → ✅ CHAMA API: POST /v1/facilita/usuario/tipo-conta
   └─> Backend cria registro de PRESTADOR ou CONTRATANTE
4. Cadastrar CNH → ✅ SUCESSO: Prestador existe no banco de dados
```

---

## 📁 Arquivos Criados/Modificados

### 1️⃣ **TipoContaRequest.kt** (NOVO)
Modelo de dados para requisição:
```kotlin
data class TipoContaRequest(
    val tipo_conta: String // "PRESTADOR" ou "CONTRATANTE"
)

data class TipoContaResponse(
    val message: String,
    val usuario: Usuario? = null
)
```

### 2️⃣ **TipoContaViewModel.kt** (NOVO)
ViewModel para gerenciar a chamada à API:
```kotlin
class TipoContaViewModel : ViewModel() {
    fun definirTipoConta(token: String, tipoConta: String) {
        // Chama API para registrar tipo de conta
    }
}
```

### 3️⃣ **UserService.kt** (MODIFICADO)
Adicionado endpoint:
```kotlin
@POST("v1/facilita/usuario/tipo-conta")
suspend fun definirTipoConta(
    @Header("Authorization") token: String,
    @Body request: TipoContaRequest
): Response<TipoContaResponse>
```

### 4️⃣ **TelaTipoContaServico.kt** (MODIFICADO)
Integrado com ViewModel e API:
```kotlin
Button(
    onClick = {
        val token = TokenManager.obterToken(context)
        val tipoContaUpperCase = selectedOption!!.uppercase()
        viewModel.definirTipoConta(token, tipoContaUpperCase)
    }
)
```

### 5️⃣ **CNHViewModel.kt** (MODIFICADO)
Mensagem de erro 404 mais clara:
```kotlin
404 -> _mensagem.value = "Prestador não encontrado. Certifique-se de ter escolhido 'Prestador de Serviço' no tipo de conta."
```

---

## 🎯 Endpoint da API

### POST `/v1/facilita/usuario/tipo-conta`

**Headers:**
```http
Authorization: Bearer {token}
Content-Type: application/json
```

**Body:**
```json
{
  "tipo_conta": "PRESTADOR"
}
```

**Response (200 OK):**
```json
{
  "message": "Tipo de conta definido com sucesso",
  "usuario": {
    "id": 116,
    "nome": "Kaike",
    "email": "kaikedodedao@gmail.com",
    "tipo_conta": "PRESTADOR"
  }
}
```

---

## 🔄 Fluxo Completo Corrigido

### Para Prestador de Serviço:

```
1. TelaCadastro
   └─> Usuário cadastra
       └─> API retorna token + proximo_passo = "escolher_tipo_conta"
           └─> 2. TelaTipoContaServico
               └─> Usuário escolhe "Prestador de serviço"
               └─> Clica em "Entrar"
                   └─> ✅ API é chamada: POST /usuario/tipo-conta
                       └─> Backend cria registro de PRESTADOR
                           └─> Token é atualizado no SharedPreferences
                               └─> 3. TelaPermissaoLocalizacaoServico
                                   └─> ... (fluxo continua)
                                       └─> N. TelaCompletarPerfilPrestador
                                           └─> Clica em "CNH com EAR"
                                               └─> ✅ SUCESSO: Prestador existe!
```

---

## 🧪 Como Testar

### Cenário 1: Novo Cadastro (RECOMENDADO)

1. **Fazer novo cadastro** no app
2. **Escolher "Prestador de serviço"** na tela de tipo de conta
3. **Verificar logs:**
   ```
   D/TELA_TIPO_CONTA: Enviando tipo de conta: PRESTADOR
   D/TIPO_CONTA_DEBUG: Resposta: ...
   ```
4. **Continuar o fluxo** até CNH
5. **Cadastrar CNH** → Deve funcionar! ✅

### Cenário 2: Usuário Existente (PROBLEMA)

Se você já tinha feito cadastro ANTES da correção:

**Problema:** O usuário não tem tipo de conta definido no backend

**Solução 1 - Fazer novo cadastro:**
- Logout
- Cadastrar com novo email
- Escolher tipo de conta
- Testar CNH

**Solução 2 - Forçar chamada da API:**
- Logout
- Login novamente
- Ir para tela de tipo de conta manualmente
- Escolher tipo de conta novamente
- Isso vai chamar a API e registrar

---

## 🔍 Logs de Debug

### Filtrar por estas tags no Logcat:

1. **TELA_TIPO_CONTA** - Logs da tela
2. **TIPO_CONTA_DEBUG** - Logs do ViewModel
3. **TIPO_CONTA_ERROR** - Erros
4. **CNH_DEBUG** - Logs da CNH
5. **CNH_ERROR** - Erros da CNH

### Logs Esperados ao Escolher Tipo de Conta:

```logcat
D/TELA_TIPO_CONTA: Enviando tipo de conta: PRESTADOR
D/TIPO_CONTA_DEBUG: Iniciando definição de tipo de conta
D/TIPO_CONTA_DEBUG: Token: eyJhbGciOiJIUzI1NiI...
D/TIPO_CONTA_DEBUG: Tipo conta: PRESTADOR
D/TIPO_CONTA_DEBUG: Resposta: TipoContaResponse(message=Tipo de conta definido com sucesso, ...)
```

### Logs Esperados ao Cadastrar CNH (após correção):

```logcat
D/CNH_DEBUG: Iniciando cadastro de CNH
D/CNH_DEBUG: Token recebido (primeiros 20 chars): eyJhbGciOiJIUzI1NiI...
D/CNH_DEBUG: Enviando request: CNHRequest(...)
D/CNH_DEBUG: Resposta recebida: CNHResponse(message=CNH cadastrada com sucesso, ...)
```

---

## ⚠️ IMPORTANTE

### Para Usuários Antigos:

Se você fez cadastro ANTES desta correção:

1. **O erro 404 vai continuar** porque seu usuário não tem registro de prestador no backend
2. **Solução:** Faça novo cadastro OU entre em contato com o suporte para adicionar manualmente

### Para Novos Usuários:

1. ✅ Tudo funcionará normalmente
2. ✅ O tipo de conta será salvo automaticamente
3. ✅ A CNH poderá ser cadastrada sem problemas

---

## 🎯 Próximos Passos

1. **Build do projeto** (já feito automaticamente)
2. **Instalar APK** no dispositivo
3. **Fazer NOVO cadastro** (importante!)
4. **Escolher "Prestador de serviço"**
5. **Verificar logs** da chamada à API
6. **Continuar fluxo** até CNH
7. **Cadastrar CNH** → Deve funcionar! ✅

---

## 📋 Checklist

- [x] ✅ Modelo TipoContaRequest criado
- [x] ✅ Endpoint adicionado no UserService
- [x] ✅ TipoContaViewModel criado
- [x] ✅ TelaTipoContaServico integrada com API
- [x] ✅ Logs de debug adicionados
- [x] ✅ Mensagem de erro 404 melhorada
- [x] ✅ Loading indicator no botão
- [ ] ⏳ Testar com novo cadastro
- [ ] ⏳ Verificar logs da API
- [ ] ⏳ Confirmar CNH funciona após correção

---

## 🆘 Troubleshooting

### Erro: "Token não encontrado"
**Solução:** Fazer login novamente

### Erro: Ainda dá 404 ao cadastrar CNH
**Causa:** Usuário cadastrado antes da correção
**Solução:** Fazer novo cadastro com outro email

### Erro: "Erro ao definir tipo de conta"
**Causa:** API pode estar fora do ar ou endpoint incorreto
**Solução:** Verificar logs e URL do endpoint

---

**Data da correção:** 11/01/2025  
**Status:** ✅ CORRIGIDO E PRONTO PARA TESTE

