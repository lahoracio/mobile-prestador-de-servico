# 🎭 MODO SIMULADO - PagBank Repository

## ✅ **PROBLEMA RESOLVIDO!**

Agora você pode testar a carteira **SEM precisar configurar o token do PagBank**!

---

## 🎮 Como Funciona

### **MODO SIMULADO Ativado** (Padrão)

O repositório está configurado com:
```kotlin
private val MODO_SIMULADO = true
```

Isso significa que **todas as operações são simuladas localmente**, sem fazer chamadas reais à API do PagBank.

---

## 🧪 O Que é Simulado

### ✅ **1. Gerar QR Code PIX**
- ✅ Retorna QR Code fake instantaneamente
- ✅ Não precisa de token
- ✅ Funciona offline
- ✅ Simula delay de 1.5 segundos (realista)

**QR Code gerado:**
```
00020126330014br.gov.bcb.pix0111123456789015204000053039865802BR5913Facilita App6009SAO PAULO62070503***63041D3D
```

### ✅ **2. Consultar Status PIX**
- ✅ Status inicial: `WAITING`
- ✅ Após 30 segundos: muda para `PAID` automaticamente
- ✅ Simula pagamento bem-sucedido

### ✅ **3. Cartão de Crédito**
- ✅ Cartão `4111111111111111` → **APROVADO** ✅
- ✅ Qualquer outro cartão → **RECUSADO** ❌
- ✅ Simula delay de 2 segundos

### ✅ **4. Saques/Transferências**
- ✅ Sempre retorna sucesso
- ✅ Status: `PROCESSING`
- ✅ Simula delay de 1 segundo

### ✅ **5. Consultar Saldo**
- ✅ Saldo disponível: **R$ 1.500,00**
- ✅ Saldo bloqueado: **R$ 50,00**
- ✅ Simula delay de 0.5 segundos

### ✅ **6. Cancelar Cobrança**
- ✅ Sempre retorna sucesso
- ✅ Status: `CANCELED`

---

## 🔄 Como Alternar Entre Modos

### Para usar MODO SIMULADO (Testes):
```kotlin
// Em PagBankRepository.kt (linha 23)
private val MODO_SIMULADO = true
```

### Para usar MODO REAL (Produção):
```kotlin
// Em PagBankRepository.kt (linha 23)
private val MODO_SIMULADO = false
```

⚠️ **Quando usar MODO REAL, você PRECISA configurar o token em `PagBankConfig.kt`**

---

## 🧪 Testando

### Teste 1: Adicionar Dinheiro via PIX
1. Abra a tela "Adicionar Dinheiro"
2. Digite qualquer valor (ex: R$ 50,00)
3. Clique em "Gerar QR Code PIX"
4. ✅ QR Code será gerado instantaneamente!
5. ✅ Você verá o código copia e cola do PIX

### Teste 2: Cartão de Crédito (Aprovado)
1. Use o cartão: `4111 1111 1111 1111`
2. CVV: qualquer (ex: 123)
3. Validade: qualquer futura
4. ✅ Pagamento será **APROVADO**

### Teste 3: Cartão de Crédito (Recusado)
1. Use qualquer outro número de cartão
2. ❌ Pagamento será **RECUSADO**

### Teste 4: Saque
1. Adicione uma conta bancária
2. Solicite um saque
3. ✅ Saque será processado com sucesso

---

## 📊 Vantagens do Modo Simulado

| Vantagem | Descrição |
|----------|-----------|
| 🚀 **Rápido** | Testa sem depender da API |
| 🔒 **Seguro** | Não precisa de token real |
| 💰 **Grátis** | Não gasta créditos da API |
| 🌐 **Offline** | Funciona sem internet |
| 🐛 **Debug** | Facilita encontrar bugs |
| 👨‍💻 **Dev** | Desenvolva mais rápido |

---

## 🎯 Quando Usar Cada Modo

### Use MODO SIMULADO quando:
- ✅ Estiver desenvolvendo
- ✅ Testando layout/UI
- ✅ Não tiver token do PagBank
- ✅ Quiser testar offline
- ✅ Estiver debugando

### Use MODO REAL quando:
- ✅ Estiver em produção
- ✅ Testando integração real
- ✅ Validando webhook
- ✅ Testando com dinheiro real
- ✅ Homologação final

---

## 🔍 Logs

O modo simulado gera logs úteis:

```
D/PagBankRepository: ⚠️ MODO SIMULADO - Gerando QR Code fake
D/PagBankRepository: ✅ QR Code simulado gerado com sucesso
D/PagBankRepository: ⚠️ MODO SIMULADO - Processando cartão fake
D/PagBankRepository: ✅ Cartão simulado aprovado
```

Procure por `⚠️ MODO SIMULADO` nos logs do Logcat!

---

## 🚀 Migração para Produção

Quando estiver pronto para produção:

1. **Configure o token:**
   ```kotlin
   // PagBankConfig.kt
   const val TOKEN_SANDBOX = "SEU_TOKEN_REAL_AQUI"
   ```

2. **Desative o modo simulado:**
   ```kotlin
   // PagBankRepository.kt
   private val MODO_SIMULADO = false
   ```

3. **Teste com sandbox primeiro:**
   ```kotlin
   const val IS_SANDBOX = true
   ```

4. **Depois mude para produção:**
   ```kotlin
   const val IS_SANDBOX = false
   const val TOKEN_PRODUCTION = "SEU_TOKEN_PRODUCAO"
   ```

---

## 💡 Dicas

### Cartões de Teste (Quando usar modo real sandbox)
```
Aprovado: 4111 1111 1111 1111
Recusado: 4000 0000 0000 0002
CVV: qualquer 3 dígitos
Validade: qualquer data futura
```

### PIX de Teste
- No modo real sandbox, o PIX expira em 10 minutos
- Use o app PagBank Sandbox para simular pagamento

---

## 📚 Arquivos Relacionados

- `PagBankRepository.kt` - Contém a lógica simulada
- `PagBankConfig.kt` - Configurações e token
- `COMO_CONFIGURAR_PAGBANK_TOKEN.md` - Como obter token real
- `ERRO_UNAUTHORIZED_PAGBANK_SOLUCAO.md` - Resolver erro de token

---

## ❓ FAQ

**P: Por que usar modo simulado?**
R: Para desenvolver e testar sem depender da API real do PagBank.

**P: O modo simulado salva os dados?**
R: Não, os dados só existem durante a execução. Use `CarteiraLocalRepository` para persistir.

**P: Posso usar em produção?**
R: NÃO! Modo simulado é apenas para desenvolvimento.

**P: Como sei se está em modo simulado?**
R: Procure por logs `⚠️ MODO SIMULADO` e mensagens na UI com "(SIMULADO)".

**P: O QR Code simulado funciona para pagar?**
R: Não, é apenas um texto de exemplo.

---

**Última atualização:** 2025-11-14
**Versão:** 1.0
**Autor:** Sistema de Carteira Facilita

