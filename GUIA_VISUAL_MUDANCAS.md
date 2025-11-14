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

    // ⬇️ ATUALIZADO: Botões ⬇️
    
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = { metodoPagamento = null },
            modifier = Modifier.weight(1f)
        ) {
            Text("Voltar")
        }

        Button(
            onClick = {
                // ✅ NOVO: Usa o método correto
                val valorDouble = valor.replace(",", ".").toDoubleOrNull() ?: 0.0
                val pixQrCodeData = viewModel.qrCodePix.value
                
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
                containerColor = Color(0xFFFF9800)  // ✅ Laranja
            )
        ) {
            Icon(
                Icons.Default.CheckCircle,
                null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text("✅ Simular Pagamento")  // ✅ Novo texto
        }
    }
}
```

---

## 📊 Comparação Visual

### ANTES:
```
┌────────────────────────────────┐
│  QR Code                       │
│  Código PIX                    │
├────────────────────────────────┤
│  [Voltar]  [Já Paguei]        │
└────────────────────────────────┘
```

### DEPOIS:
```
┌────────────────────────────────┐
│  QR Code                       │
│  Código PIX                    │
├────────────────────────────────┤
│  ⚠️  Modo Simulado Ativo      │ ← NOVO
│  Clique no botão abaixo...    │
├────────────────────────────────┤
│  [Voltar] [✅ Simular Pag.]   │ ← MUDOU
└────────────────────────────────┘
```

---

## 🎨 Mudanças de Cor

| Elemento | Antes | Depois |
|----------|-------|--------|
| Botão principal | 🟢 Verde #00B14F | 🟠 Laranja #FF9800 |
| Card de aviso | ❌ Não existia | 🟠 Laranja claro #FFF3E0 |
| Ícone de aviso | ❌ Não existia | 🟠 Laranja #FF9800 |

---

## 🔧 Checklist de Mudanças

Para fazer a integração, você precisa:

- [ ] **1. Adicionar** o card laranja (modo simulado)
- [ ] **2. Mudar** o texto do botão: "Já Paguei" → "✅ Simular Pagamento"
- [ ] **3. Mudar** a cor do botão: Verde → Laranja
- [ ] **4. Mudar** a ação do botão: Usar `confirmarDepositoSimulado()`
- [ ] **5. Adicionar** ícone no botão (CheckCircle)

---

## 💻 Código Completo para Copiar e Colar

Use este código completo para a seção dos botões:

```kotlin
// Modo Simulado Card
OutlinedCard(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
) {
    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.SimCard, null, tint = Color(0xFFFF9800), modifier = Modifier.size(32.dp))
        Spacer(Modifier.height(8.dp))
        Text("⚠️ Modo Simulado Ativo", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF9800))
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
Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    OutlinedButton(onClick = { metodoPagamento = null }, modifier = Modifier.weight(1f)) {
        Text("Voltar")
    }
    Button(
        onClick = {
            val valorDouble = valor.replace(",", ".").toDoubleOrNull() ?: 0.0
            val pixQrCodeData = viewModel.qrCodePix.value
            if (pixQrCodeData?.id != null) {
                viewModel.confirmarDepositoSimulado(pixQrCodeData.id!!, valorDouble)
            }
            mensagemSucesso = true
        },
        modifier = Modifier.weight(1f),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
    ) {
        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(4.dp))
        Text("✅ Simular Pagamento")
    }
}
```

---

## ✅ Pronto!

Com essas mudanças, seu app vai ter:

1. ✅ QR Code estático visual
2. ✅ Card laranja explicativo
3. ✅ Botão "✅ Simular Pagamento" destacado
4. ✅ Confirmação instantânea ao clicar
5. ✅ Notificação de sucesso
6. ✅ Saldo atualizado

---

**🎉 Agora é só copiar e colar o código acima no lugar certo! 🎉**
# 🎯 GUIA VISUAL - Adicionar Botão Simular Pagamento

## 📍 Localização Exata

No seu arquivo `TelaCarteira.kt`, dentro da função `DialogDepositoSimplificado`, procure pela seção que mostra o QR Code PIX.

---

## 🔍 ANTES (Como está agora)

```kotlin
// QR Code gerado com sucesso
Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text("Pagar com PIX", fontSize = 22.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(8.dp))
    Text(
        "R$ ${valor.replace(".", ",")}",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF00B14F)
    )

    Spacer(Modifier.height(16.dp))

    // QR Code
    Card(...) {
        Box(...) {
            Icon(Icons.Default.QrCode2, ...)
        }
    }

    // Código PIX
    OutlinedCard(...) {
        Column(...) {
            Text("Código PIX:")
            Text(pixQrCode!!.take(40) + "...")
        }
    }

    Spacer(Modifier.height(12.dp))

    // ⬇️ ATENÇÃO: ALTERE AQUI ABAIXO ⬇️
    
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = { metodoPagamento = null },
            modifier = Modifier.weight(1f)
        ) {
            Text("Voltar")
        }

        Button(
            onClick = {
                val valorDouble = valor.replace(",", ".").toDoubleOrNull() ?: 0.0
                viewModel.confirmarPagamentoPix(valorDouble)  // ❌ MÉTODO ANTIGO
                mensagemSucesso = true
            },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00B14F)  // ❌ Verde
            )
        ) {
            Text("Já Paguei")  // ❌ Texto antigo
        }
    }
}
```

---

## ✅ DEPOIS (Como deve ficar)

```kotlin
// QR Code gerado com sucesso
Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text("Pagar com PIX", fontSize = 22.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(8.dp))
    Text(
        "R$ ${valor.replace(".", ",")}",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF00B14F)
    )

    Spacer(Modifier.height(16.dp))

    // QR Code
    Card(...) {
        Box(...) {
            Icon(Icons.Default.QrCode2, ...)
        }
    }

    // Código PIX
    OutlinedCard(...) {
        Column(...) {
            Text("Código PIX:")
            Text(pixQrCode!!.take(40) + "...")
        }
    }

    Spacer(Modifier.height(12.dp))

    // ⬇️ NOVO: Card de Modo Simulado ⬇️
    
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3E0)  // ✅ Fundo laranja claro
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

