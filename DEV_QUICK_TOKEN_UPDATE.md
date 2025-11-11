# 🚀 Guia Rápido - Atualizar Token (Desenvolvimento)

## 📌 Situação Atual

Você está desenvolvendo o app de **prestador de serviço** de forma modular:
- ✅ Tela de login está em **outra aplicação**
- ✅ Apps serão **integrados depois**
- ✅ Por enquanto: **token manual** no `TokenManager.kt`

---

## ⚡ Atualização Rápida (Postman)

### 1️⃣ Configurar Requisição

Abra o Postman e configure:

```
Método: POST
URL: https://servidor-facilita.onrender.com/v1/facilita/auth/login
```

### 2️⃣ Body (JSON)

Aba "Body" → "raw" → "JSON":

```json
{
  "email": "kaikedodedao@gmail.com",
  "senha": "COLOQUE_SUA_SENHA_AQUI"
}
```

### 3️⃣ Enviar e Copiar Token

Clique "Send" → Copie o valor de `"token"` da resposta

### 4️⃣ Colar no TokenManager

Arquivo: `app/src/main/java/com/exemple/facilita/utils/TokenManager.kt`

Procure por esta linha (aproximadamente linha 29):

```kotlin
private var currentToken: String = "COLE_O_TOKEN_AQUI"
```

**Substitua** o conteúdo entre as aspas pelo token novo

### 5️⃣ Executar App

Salve o arquivo → Execute o app → Teste a validação da CNH

---

## 🔥 Comando curl (Alternativa Rápida)

Se preferir usar o terminal:

### Windows (CMD):
```cmd
curl -X POST https://servidor-facilita.onrender.com/v1/facilita/auth/login -H "Content-Type: application/json" -d "{\"email\":\"kaikedodedao@gmail.com\",\"senha\":\"SUA_SENHA\"}"
```

### Windows (PowerShell):
```powershell
$body = @{email="kaikedodedao@gmail.com"; senha="SUA_SENHA"} | ConvertTo-Json
Invoke-RestMethod -Uri "https://servidor-facilita.onrender.com/v1/facilita/auth/login" -Method POST -Body $body -ContentType "application/json"
```

---

## 🛠️ Para Integração Futura (quando unir os apps)

Quando você integrar a tela de login, modifique para:

1. Após login bem-sucedido, chame:
```kotlin
TokenManager.setToken(tokenRecebidoDaAPI)
```

2. Salve também no SharedPreferences para persistir:
```kotlin
// No LoginViewModel ou similar
sharedPreferences.edit()
    .putString("auth_token", token)
    .apply()

// No TokenManager, carregue na inicialização
init {
    currentToken = sharedPreferences.getString("auth_token", "") ?: ""
}
```

---

## 🐛 Troubleshooting

### ❌ "Token expirado ou inválido"
→ Significa que precisa atualizar o token (siga passos acima)

### ❌ "Erro de conexão"
→ Verifique internet ou se a API está online: https://servidor-facilita.onrender.com/

### ❌ "Credenciais inválidas" ao fazer login
→ Verifique email/senha ou crie nova conta de teste

### ✅ Como saber quando o token expira?
→ Tokens JWT geralmente expiram em **8 horas**
→ Verifique em https://jwt.io/ colando o token → veja campo `"exp"`

---

## 📋 Checklist Rápido

- [ ] Obtive novo token via Postman/curl
- [ ] Copiei o token completo (começa com `eyJ...`)
- [ ] Colei no `TokenManager.kt` entre as aspas
- [ ] Salvei o arquivo
- [ ] Executei o app novamente
- [ ] Testei validação da CNH
- [ ] ✅ Funcionou!

---

## 💡 Dica Pro

Marque este arquivo nos favoritos do editor para acessar rapidamente quando precisar atualizar o token!

Atalho: `Ctrl + Shift + N` → digite "DEV_QUICK" → Enter

