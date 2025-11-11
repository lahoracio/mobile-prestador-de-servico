# 🐛 Debug Guide - CNH Token Issue

## 🔍 Como Verificar se o Problema Foi Resolvido

### 1. Verificar Token no SharedPreferences

#### Via Logcat:
```kotlin
// Adicione temporariamente na TelaCNH antes de enviar:
val token = TokenManager.obterToken(context)
android.util.Log.d("DEBUG_TOKEN", "Token completo: $token")
android.util.Log.d("DEBUG_TOKEN", "Token com Bearer: ${TokenManager.obterTokenComBearer(context)}")
```

#### Via Device File Explorer (Android Studio):
```
1. Android Studio → View → Tool Windows → Device File Explorer
2. Navegar para: /data/data/com.exemple.facilita/shared_prefs/
3. Abrir: user_prefs.xml ou FacilitaPrefs.xml
4. Verificar se "auth_token" ou "token" existe
```

---

## 🧪 Teste Manual da API

### 1. Obter Token Válido:

```bash
curl -X POST https://servidor-facilita.onrender.com/v1/facilita/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "kaikedodedao@gmail.com",
    "senha": "SUA_SENHA"
  }'
```

**Resposta esperada:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MTE2LCJ0aXBvX2NvbnRhIjoiUFJFU1RBRE9SIiwiZW1haWwiOiJrYWlrZWRvZGVkYW9AZ21haWwuY29tIiwiaWF0IjoxNzM2NTk4MDAwLCJleHAiOjE3MzY2MjY4MDB9.SIGNATURE",
  "usuario": {
    "id": 116,
    "nome": "Kaike",
    "email": "kaikedodedao@gmail.com",
    "tipo_conta": "PRESTADOR"
  }
}
```

### 2. Testar CNH com Token Obtido:

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

**Resposta esperada (200 OK):**
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
    "pontuacao_atual": 0
  }
}
```

**Se retornar 401:**
```json
{
  "erro": "Token inválido ou expirado"
}
```
→ O token expirou ou está incorreto

---

## 📱 Debug no App

### Habilitar Logs Detalhados do Retrofit:

Já está configurado no `RetrofitFactory.kt`:
```kotlin
private val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
}
```

### Filtros de Logcat Úteis:

#### Ver todos os logs do app:
```
Tag: facilita
```

#### Ver apenas logs de CNH:
```
Tag: CNH_DEBUG | CNH_ERROR | TELA_CNH
```

#### Ver requisições HTTP:
```
Tag: OkHttp
```

#### Ver requisições completas:
```
Tag: HttpLoggingInterceptor
```

---

## 🔧 Checklist de Debug

### Antes de Testar:
- [ ] Build foi bem-sucedido
- [ ] APK foi instalado no dispositivo
- [ ] App foi reiniciado após instalação
- [ ] Logcat está aberto e filtrando

### Durante Login:
- [ ] Login retorna token
- [ ] Token é salvo no SharedPreferences
- [ ] Log mostra: "Token salvo verificado: eyJ..."

### Durante Cadastro CNH:
- [ ] Token é recuperado corretamente
- [ ] Log mostra: "Token obtido: eyJ..."
- [ ] Log mostra: "Header Authorization: Bearer eyJ..."
- [ ] **NÃO** mostra: "Bearer Bearer" (ERRO!)

### Após Enviar:
- [ ] Log mostra: "Resposta recebida: CNHResponse(...)"
- [ ] **OU** Log mostra: "Erro HTTP 401/400/500"

---

## 🚨 Cenários de Erro e Soluções

### Erro 1: "Token está nulo ou vazio"

**Causa:** Token não foi salvo após login

**Debug:**
```kotlin
val token = TokenManager.obterToken(context)
Log.d("DEBUG", "Token: $token")
```

**Solução:**
1. Verificar se `TokenManager.salvarToken()` é chamado após login
2. Verificar permissões do SharedPreferences
3. Fazer login novamente

---

### Erro 2: "Token expirado ou inválido" (401)

**Causa:** Token expirou (8 horas) ou está incorreto

**Debug:**
```bash
# Decodificar token JWT (use jwt.io)
# Verificar campo "exp" (expiration)
```

**Solução:**
1. Fazer logout e login novamente
2. Obter novo token via API
3. Verificar se o relógio do dispositivo está correto

---

### Erro 3: "Bearer Bearer" aparece nos logs

**Causa:** O bug NÃO foi corrigido

**Verificar:**
```kotlin
// TelaCNH.kt - Deve estar assim:
val token = TokenManager.obterToken(context)  // SEM "ComBearer"

// CNHViewModel.kt - Deve estar assim:
service.cadastrarCNH("Bearer $token", body)  // COM "Bearer "
```

**Solução:**
1. Verificar se as mudanças foram salvas
2. Fazer clean e rebuild:
   ```bash
   .\gradlew.bat clean
   .\gradlew.bat assembleDebug
   ```
3. Reinstalar o APK

---

### Erro 4: "Dados inválidos" (400)

**Causa:** Formato incorreto dos dados

**Verificar:**
- CNH: Apenas números, 11 dígitos
- Data: YYYY-MM-DD (ex: 2030-12-31)
- Categoria: Uma das opções válidas

**Debug:**
```kotlin
Log.d("DEBUG", "CNH: $numeroCNH (length: ${numeroCNH.length})")
Log.d("DEBUG", "Validade: $validade (matches: ${validade.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))})")
```

---

### Erro 5: "Erro de conexão"

**Causa:** Sem internet ou servidor fora do ar

**Verificar:**
1. Conexão do dispositivo
2. Status do servidor: https://servidor-facilita.onrender.com/
3. Firewall/VPN bloqueando

**Debug:**
```kotlin
try {
    val response = service.cadastrarCNH(...)
} catch (e: IOException) {
    Log.e("DEBUG", "Erro de rede: ${e.message}")
}
```

---

## 🎓 Entendendo o Token JWT

### Estrutura:
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MTE2LCJ0aXBvX2NvbnRhIjoiUFJFU1RBRE9SIiwiZW1haWwiOiJrYWlrZWRvZGVkYW9AZ21haWwuY29tIiwiaWF0IjoxNzM2NTk4MDAwLCJleHAiOjE3MzY2MjY4MDB9.SIGNATURE
│                Header                  │                           Payload                                  │ Signature │
```

### Decodificar (use jwt.io):

**Payload exemplo:**
```json
{
  "id": 116,
  "tipo_conta": "PRESTADOR",
  "email": "kaikedodedao@gmail.com",
  "iat": 1736598000,  // Issued At (timestamp)
  "exp": 1736626800   // Expiration (timestamp)
}
```

### Verificar Expiração:
```javascript
// Converter timestamp para data:
new Date(1736626800 * 1000)
// Resultado: 2025-01-11T18:00:00.000Z
```

---

## 🧰 Ferramentas Úteis

### 1. JWT Decoder
- **Site:** https://jwt.io/
- **Uso:** Cole o token para ver o payload

### 2. Postman
- **Site:** https://www.postman.com/
- **Uso:** Testar API manualmente

### 3. Logcat (Android Studio)
- **Menu:** View → Tool Windows → Logcat
- **Filtros:** Por tag, package, ou regex

### 4. Device File Explorer
- **Menu:** View → Tool Windows → Device File Explorer
- **Uso:** Ver arquivos do app (SharedPreferences)

### 5. Network Profiler (Android Studio)
- **Menu:** View → Tool Windows → Profiler
- **Uso:** Ver requisições HTTP em tempo real

---

## 📊 Tabela de Códigos HTTP

| Código | Significado | Causa Comum | Solução |
|--------|-------------|-------------|---------|
| 200 | OK | Sucesso | ✅ Tudo certo |
| 400 | Bad Request | Dados inválidos | Verificar formato dos campos |
| 401 | Unauthorized | Token inválido/expirado | Fazer login novamente |
| 403 | Forbidden | Sem permissão | Verificar tipo_conta (deve ser PRESTADOR) |
| 404 | Not Found | Endpoint errado | Verificar URL da API |
| 500 | Internal Server Error | Erro no servidor | Aguardar ou contatar suporte |

---

## 🔄 Fluxo de Debugging

```
1. Fazer login no app
   ↓
2. Verificar no Logcat: "Token salvo verificado: eyJ..."
   ↓
3. Ir para tela CNH
   ↓
4. Verificar no Logcat: "Token obtido: eyJ..."
   ↓
5. Preencher formulário
   ↓
6. Clicar em "Validar CNH"
   ↓
7. Verificar no Logcat: "Enviando request: CNHRequest(...)"
   ↓
8. Verificar no Logcat: "Header Authorization: Bearer eyJ..."
   ↓
9. IMPORTANTE: NÃO deve ter "Bearer Bearer"
   ↓
10. Verificar resposta:
    - Sucesso: "Resposta recebida: CNHResponse(...)"
    - Erro: "Erro HTTP 401/400/500: {...}"
```

---

## 📞 Suporte

Se após seguir todos os passos ainda houver problemas:

1. **Coletar logs:**
   ```
   - Logcat completo (filtro: facilita)
   - Código HTTP do erro
   - Mensagem de erro exata
   ```

2. **Verificar API manualmente:**
   - Testar login via Postman
   - Testar CNH via Postman com token obtido
   - Comparar resultado do app vs Postman

3. **Verificar código:**
   - TelaCNH.kt linha ~217
   - CNHViewModel.kt linha ~44
   - TokenManager.kt

---

**Última atualização:** 11/01/2025

