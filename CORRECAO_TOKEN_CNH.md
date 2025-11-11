# 🔧 Correção do Bug de Token Inválido ao Cadastrar CNH

## 🐛 Problema Identificado

Quando o prestador tentava cadastrar a CNH, recebia o erro **"Token inválido"**, mesmo após fazer login com sucesso.

### Causa Raiz
O token JWT estava sendo enviado **DUPLICADO** com o prefixo "Bearer":

**Antes (INCORRETO):**
```
Authorization: Bearer Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Esperado (CORRETO):**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Por que acontecia?

1. Na `TelaCNH.kt` (linha 217), o código chamava:
   ```kotlin
   val token = TokenManager.obterTokenComBearer(context)
   ```
   Isso retornava: `"Bearer {token}"`

2. No `CNHViewModel.kt` (linha 44), o código adicionava "Bearer" novamente:
   ```kotlin
   val response = service.cadastrarCNH("Bearer $token", body)
   ```
   Resultado: `"Bearer Bearer {token}"` ❌

---

## ✅ Solução Implementada

### Arquivos Modificados:

#### 1. **TelaCNH.kt**
- **Mudança:** Trocar `obterTokenComBearer()` por `obterToken()`
- **Linha ~217:**
  ```kotlin
  // ANTES
  val token = TokenManager.obterTokenComBearer(context)
  
  // DEPOIS
  val token = TokenManager.obterToken(context)
  ```

#### 2. **CNHViewModel.kt**
- **Mudança:** Adicionar logs de debug para rastrear problemas
- **Novos logs:**
  - Token recebido (primeiros 20 caracteres)
  - Request body enviado
  - Header Authorization
  - Erros HTTP detalhados

---

## 🧪 Como Testar

### Pré-requisitos:
1. Usuário deve estar **logado** como PRESTADOR
2. Token deve estar **válido** (não expirado)

### Passo a Passo:

1. **Abra o app** e faça login como prestador
   - Email: `kaikedodedao@gmail.com`
   - Senha: (sua senha)

2. **Navegue até Completar Perfil** → **CNH com EAR**

3. **Preencha os dados:**
   - Número da CNH: `12345678901` (11 dígitos)
   - Categoria: `B`, `AB`, `C`, `D` ou `E`
   - Validade: `2030-12-31` (formato YYYY-MM-DD)
   - Possui EAR: `Sim`
   - Pontuação: `10`

4. **Clique em "Validar CNH"**

5. **Verifique os logs no Logcat:**
   - Filtro: `TELA_CNH` ou `CNH_DEBUG`
   - Você deve ver:
     ```
     TELA_CNH: Token obtido: eyJhbGciOiJIUzI1NiI...
     CNH_DEBUG: Iniciando cadastro de CNH
     CNH_DEBUG: Token recebido (primeiros 20 chars): eyJhbGciOiJIUzI1NiI...
     CNH_DEBUG: Enviando request: CNHRequest(...)
     CNH_DEBUG: Header Authorization: Bearer eyJhbGciOiJIUzI...
     CNH_DEBUG: Resposta recebida: CNHResponse(...)
     ```

6. **Resultado esperado:**
   - ✅ Mensagem: "CNH cadastrada com sucesso!"
   - ✅ Redirecionamento automático para tela de completar perfil
   - ✅ Item "CNH com EAR" marcado como validado

---

## 🔍 Debugging

### Se ainda der erro de token inválido:

1. **Verifique se o token está salvo:**
   ```kotlin
   val token = TokenManager.obterToken(context)
   Log.d("DEBUG", "Token: $token")
   ```

2. **Verifique se o token não expirou:**
   - Tokens JWT geralmente expiram em 24h
   - Se expirou, faça login novamente

3. **Verifique os logs do Retrofit:**
   - O `HttpLoggingInterceptor` está configurado com `Level.BODY`
   - Você verá a requisição completa no Logcat

4. **Teste a API manualmente:**
   - Use Postman ou curl:
     ```bash
     curl -X POST https://servidor-facilita.onrender.com/v1/facilita/prestador/cnh \
       -H "Authorization: Bearer SEU_TOKEN_AQUI" \
       -H "Content-Type: application/json" \
       -d '{
         "numero_cnh": "12345678901",
         "categoria": "B",
         "validade": "2030-12-31",
         "possui_ear": true
       }'
     ```

---

## 📋 Checklist de Validação

- [x] Token não está mais duplicado
- [x] Logs de debug adicionados
- [x] Validação de formato de data (YYYY-MM-DD)
- [x] Validação de 11 dígitos na CNH
- [x] Conversão Sim/Não para Boolean
- [x] Tratamento de erros HTTP (401, 400, 404, 500)
- [x] Mensagens de erro amigáveis
- [x] Redirecionamento após sucesso
- [x] Integração com PerfilViewModel

---

## 🎯 Endpoint da API

**POST** `https://servidor-facilita.onrender.com/v1/facilita/prestador/cnh`

### Headers:
```
Authorization: Bearer {token}
Content-Type: application/json
```

### Body:
```json
{
  "numero_cnh": "12345678901",
  "categoria": "B",
  "validade": "2030-12-31",
  "possui_ear": true
}
```

### Response (Sucesso - 200):
```json
{
  "message": "CNH cadastrada com sucesso",
  "cnh": {
    "id": 1,
    "id_prestador": 116,
    "numero_cnh": "12345678901",
    "categoria": "B",
    "validade": "2030-12-31",
    "possui_ear": true,
    "pontuacao_atual": 0,
    "data_criacao": "2025-01-11T10:30:00.000Z"
  }
}
```

### Response (Erro - 401):
```json
{
  "erro": "Token inválido ou expirado"
}
```

---

## ⚠️ Observações Importantes

1. **Token é obtido automaticamente do login/cadastro**
   - Não precisa inserir manualmente
   - É salvo no SharedPreferences via TokenManager

2. **O campo "pontuacao_atual" não é enviado**
   - A API pode aceitar, mas não é obrigatório
   - Se quiser adicionar, modifique o `CNHRequest.kt`

3. **EAR é obrigatório para prestadores**
   - A tela já informa isso
   - O campo "possuiEAR" deve ser `true` para exercer atividade remunerada

4. **Formato da data é crítico**
   - Use sempre `YYYY-MM-DD` (ex: 2030-12-31)
   - O app valida com regex antes de enviar

---

## 🚀 Status: CORRIGIDO ✅

Data da correção: 11/01/2025

