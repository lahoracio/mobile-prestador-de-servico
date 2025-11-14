# 🔐 Como Configurar o Token do PagBank

## ❗ IMPORTANTE
O erro **"unauthorized"** ocorre porque você precisa configurar um token válido do PagBank.

## 📋 Passo a Passo

### 1. Criar Conta no PagBank (Sandbox)

1. Acesse: https://dev.pagseguro.uol.com.br/
2. Clique em "Criar conta gratuita" ou faça login
3. Entre no ambiente **Sandbox** (Testes)

### 2. Obter o Token de API

1. No Dashboard do PagBank, vá em:
   - **Minha Conta** → **Credenciais** → **Sandbox**
   
2. Você verá:
   - **Token** (é este que precisamos!)
   - **Public Key** (também copie, pode ser útil)

3. Copie o **Token** que se parece com:
   ```
   XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
   ```

### 3. Configurar no App

Abra o arquivo:
```
app/src/main/java/com/exemple/facilita/pagbank/PagBankConfig.kt
```

E substitua esta linha:
```kotlin
const val TOKEN_SANDBOX = "SEU_TOKEN_SANDBOX_AQUI"
```

Por:
```kotlin
const val TOKEN_SANDBOX = "SEU_TOKEN_REAL_AQUI"
```

**Exemplo:**
```kotlin
const val TOKEN_SANDBOX = "ABC123DEF456GHI789JKL012MNO345PQR"
```

### 4. Configurar Public Key (Opcional)

Se precisar, também substitua:
```kotlin
const val PUBLIC_KEY_SANDBOX = "SUA_PUBLIC_KEY_AQUI"
```

### 5. Testar

Após configurar o token:
1. Recompile o app
2. Teste adicionar dinheiro novamente
3. O QR Code PIX deve ser gerado com sucesso! ✅

---

## 🔒 Segurança para Produção

⚠️ **NUNCA** commite tokens em código fonte!

Para produção, use uma das opções:

### Opção 1: BuildConfig (Recomendado)

No `build.gradle.kts` do módulo app:
```kotlin
android {
    defaultConfig {
        buildConfigField("String", "PAGBANK_TOKEN", "\"${System.getenv("PAGBANK_TOKEN")}\"")
    }
}
```

E no código:
```kotlin
const val TOKEN_SANDBOX = BuildConfig.PAGBANK_TOKEN
```

### Opção 2: local.properties

Adicione no `local.properties`:
```
pagbank.token=SEU_TOKEN_AQUI
```

E leia no `build.gradle.kts`:
```kotlin
val localProperties = Properties()
localProperties.load(FileInputStream(rootProject.file("local.properties")))

android {
    defaultConfig {
        buildConfigField("String", "PAGBANK_TOKEN", "\"${localProperties["pagbank.token"]}\"")
    }
}
```

### Opção 3: Backend

O mais seguro é:
1. Nunca expor o token no app
2. Criar um backend próprio
3. O backend faz as chamadas ao PagBank
4. O app chama apenas seu backend

---

## 📚 Documentação Oficial

- PagBank Sandbox: https://dev.pagseguro.uol.com.br/
- API Reference: https://dev.pagseguro.uol.com.br/reference/
- Criar cobrança PIX: https://dev.pagseguro.uol.com.br/reference/criar-cobranca-pix

---

## ❓ Problemas Comuns

### Erro: "unauthorized"
- ✅ Verifique se o token está correto
- ✅ Verifique se está usando token do **Sandbox** no ambiente de testes
- ✅ Confirme que copiou o token completo (sem espaços)

### Erro: "Invalid token format"
- ✅ Token não pode conter aspas extras
- ✅ Token deve ser uma string contínua

### Erro: "Account not found"
- ✅ Certifique-se de estar usando credenciais do **Sandbox**
- ✅ Verifique se a conta PagBank está ativa

---

## 🎯 Checklist Final

- [ ] Criei conta no PagBank Dev
- [ ] Acessei o Dashboard → Credenciais → Sandbox
- [ ] Copiei o Token
- [ ] Editei `PagBankConfig.kt`
- [ ] Recompilei o app
- [ ] Testei a funcionalidade
- [ ] Funcionou! 🎉

---

**Última atualização:** 2025-11-14

