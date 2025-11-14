# 📋 INSTRUÇÕES - Integrar Código da TelaCarteira

## ✅ O que você precisa fazer:

Seu código da `TelaCarteira.kt` que você enviou já está **quase perfeito**! Ele tem um layout excelente com QR Code e tudo funcionando.

Para adicionar o **botão "Simular Pagamento"**, você só precisa fazer **UMA pequena modificação**:

---

## 🔧 Modificação Necessária

### **Localize esta parte do seu código:**

No dialog de depósito, onde tem o QR Code PIX e os botões, você tem algo assim:

```kotlin
Row(
    Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
) {
    OutlinedButton(
        onClick = {
            metodoPagamento = null
        },
        modifier = Modifier.weight(1f)
    ) {
        Text("Voltar")
    }

    Button(
        onClick = {
            // Confirmar pagamento PIX
            val valorDouble = valor.replace(",", ".").toDoubleOrNull() ?: 0.0
            viewModel.confirmarPagamentoPix(valorDouble)
            mensagemSucesso = true
        },
        modifier = Modifier.weight(1f),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF00B14F)
        )
    ) {
        Text("Já Paguei")
    }
}
```

---

### **Substitua por este código:**

```kotlin
// Card de Modo Simulado - NOVO!
OutlinedCard(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
        containerColor = Color(0xFFFFF3E0)
    )
) {
    Column(
        Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.SimCard,
            null,
            tint = Color(0xFFFF9800),
            modifier = Modifier.size(32.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "⚠️ Modo Simulado Ativo",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF9800)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Clique no botão abaixo para simular que você já realizou o pagamento",
            fontSize = 12.sp,
            color = Color(0xFF424242),
            textAlign = TextAlign.Center
        )
    }
}

Spacer(Modifier.height(12.dp))

// Botões
Row(
    Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
) {
    OutlinedButton(
        onClick = {
            metodoPagamento = null
        },
        modifier = Modifier.weight(1f)
    ) {
        Text("Voltar")
    }

    Button(
        onClick = {
            // ✅ NOVO: Confirmar pagamento PIX SIMULADO
            val valorDouble = valor.replace(",", ".").toDoubleOrNull() ?: 0.0
            val pixQrCodeData = viewModel.qrCodePix.value
            
            // Usar o método correto do ViewModel
            if (pixQrCodeData?.id != null) {
                viewModel.confirmarDepositoSimulado(
                    pixQrCodeData.id!!,
                    valorDouble
                )
            }
            mensagemSucesso = true
        },
        modifier = Modifier.weight(1f),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFF9800) // Laranja para destaque
        )
    ) {
        Icon(
            Icons.Default.CheckCircle,
            null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text("✅ Simular Pagamento")
    }
}
```

---

## 🎨 Como Vai Ficar

```
┌────────────────────────────────────┐
│  Pagar com PIX                     │
│  R$ 50,00                          │
├────────────────────────────────────┤
│                                    │
│         [QR CODE]                  │
│                                    │
│  Escaneie o QR Code...            │
├────────────────────────────────────┤
│  Código PIX:                       │
│  00020126330014br...              │
├────────────────────────────────────┤
│  ⚠️  Modo Simulado Ativo          │  ← NOVO!
│  Clique no botão abaixo para      │
│  simular que você já pagou        │
├────────────────────────────────────┤
│  [Voltar] [✅ Simular Pagamento]  │  ← NOVO!
└────────────────────────────────────┘
```

---

## 📝 Mudanças Resumidas

### **O que mudou:**

1. ✅ **Adicionado**: Card laranja explicando modo simulado
2. ✅ **Mudado**: Botão "Já Paguei" → "✅ Simular Pagamento"
3. ✅ **Mudado**: Cor do botão: Verde → Laranja (#FF9800)
4. ✅ **Mudado**: Ação do botão: Agora chama `viewModel.confirmarDepositoSimulado()`

### **O que NÃO mudou:**

- ✅ Layout geral
- ✅ QR Code
- ✅ Código PIX
- ✅ Botão "Voltar"
- ✅ Toda a estrutura do dialog

---

## 🔍 Onde Fazer a Mudança

**Procure por:** `"Já Paguei"` no seu código

Ou procure pela seção onde tem:
- O QR Code (ícone `Icons.Default.QrCode2`)
- O código PIX exibido
- Os botões "Voltar" e outro botão

---

## ✅ Resultado Esperado

Depois dessa mudança:

1. **Usuário adiciona R$ 50**
2. **QR Code aparece** (igual antes)
3. **Código PIX aparece** (igual antes)
4. **🆕 Card laranja aparece** explicando modo simulado
5. **🆕 Botão laranja** "✅ Simular Pagamento"
6. **Usuário clica** no botão laranja
7. **Saldo é adicionado** instantaneamente
8. **Notificação verde** "✅ Depósito confirmado! R$ 50,00"

---

## 💡 Dica Rápida

Se você não encontrar exatamente o código, procure por:

```kotlin
viewModel.confirmarPagamentoPix(valorDouble)
```

E substitua por:

```kotlin
viewModel.confirmarDepositoSimulado(
    pixQrCodeData.id!!,
    valorDouble
)
```

---

## 🚀 Pronto!

Faça essa única mudança e seu código vai funcionar perfeitamente com o botão de simular pagamento! 

**Todo o resto do seu código está perfeito e não precisa mudar!** ✅

---

**Precisa de ajuda para localizar o código exato? Me avise!**

