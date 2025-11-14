```properties
# local.properties
pagbank.token=SEU_TOKEN_AQUI
```

**Option B - Use variáveis de ambiente:**
```bash
export PAGBANK_TOKEN="seu_token_aqui"
```

Depois leia no `build.gradle.kts`

---

## ❓ Ainda com Erro?

Verifique:
1. ✅ Token foi copiado completo (sem espaços extras)
2. ✅ Está usando token do **Sandbox** (não produção)
3. ✅ App foi **recompilado** após a mudança
4. ✅ Token está entre aspas duplas `"..."`
5. ✅ Conta PagBank está ativa

---

## 📚 Mais Informações

Veja o guia completo: **COMO_CONFIGURAR_PAGBANK_TOKEN.md**

---

**Última atualização:** 2025-11-14
# 🚀 GUIA RÁPIDO - Resolver Erro "unauthorized" 

## ❌ O Problema
```
Erro ao gerar QR Code PIX: {"message": "unauthorized"}
```

Isso significa que o **token do PagBank não está configurado** ou está **inválido**.

---

## ✅ Solução em 3 Passos

### Passo 1: Obter o Token

1. **Acesse:** https://dev.pagseguro.uol.com.br/
2. **Faça login** (ou crie conta gratuita)
3. **Navegue para:** Minha Conta → **Credenciais** → **Sandbox**
4. **Copie o TOKEN** (uma string longa tipo: `ABC123DEF456...`)

### Passo 2: Configurar no App

1. **Abra o arquivo:**
   ```
   app/src/main/java/com/exemple/facilita/pagbank/PagBankConfig.kt
   ```

2. **Encontre a linha 25:**
   ```kotlin
   const val TOKEN_SANDBOX = "SEU_TOKEN_SANDBOX_AQUI"
   ```

3. **Substitua por seu token:**
   ```kotlin
   const val TOKEN_SANDBOX = "ABC123DEF456GHI789JKL012MNO345PQR"
   ```
   *(Use seu token real, não este exemplo!)*

### Passo 3: Recompilar e Testar

1. **Recompile o app** (Build → Rebuild Project)
2. **Teste novamente** adicionar dinheiro
3. **Sucesso!** 🎉 O QR Code PIX será gerado

---

## 🔍 Como Verificar se Está Correto

Após configurar, o token deve:
- ✅ Ser uma string longa (30+ caracteres)
- ✅ Não conter aspas extras
- ✅ Não ser `"SEU_TOKEN_SANDBOX_AQUI"`
- ✅ Vir do ambiente **Sandbox** do PagBank

---

## 📸 Visual Guide

```
PagBank Dev Dashboard
└── Login
    └── Minha Conta
        └── Credenciais
            └── Sandbox
                └── TOKEN ← Copie este!
```

---

## 💡 Dica Pro

Para evitar expor o token no código:

**Option A - Use local.properties:**

