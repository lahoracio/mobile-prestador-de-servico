# 🚀 TESTE RÁPIDO - Carteira Integrada com PagBank

## ✅ Tudo Pronto! Vamos Testar!

---

## 🎮 TESTE 1: Depósito via PIX (2 minutos)

### **Passo a Passo:**

1. **Abra o app e vá para Carteira**
   ```
   Tela inicial → Carteira (ícone carteira na navbar)
   ```

2. **Observe o saldo inicial**
   ```
   Saldo Disponível: R$ 1.500,00 (simulado)
   ```

3. **Clique em "Adicionar"**
   ```
   Botões de ação → Adicionar (ícone +)
   ```

4. **Digite o valor**
   ```
   Digite: 50
   (R$ 50,00)
   ```

5. **Gere o QR Code**
   ```
   Clique: "Gerar QR Code PIX"
   Aguarde: 1.5 segundos
   ```

6. **QR Code aparece!**
   ```
   ✅ Tela mostra:
   - QR Code (imagem)
   - Código copia e cola
   - Botão copiar
   ```

7. **Aguarde a confirmação automática**
   ```
   Contador: 5... 4... 3... 2... 1...
   ✅ Notificação verde: "Depósito confirmado! R$ 50,00"
   ```

8. **Volte para a Carteira**
   ```
   Botão Voltar → Carteira
   ```

9. **Verifique:**
   ```
   ✅ Saldo: R$ 1.550,00 (era 1.500 + 50)
   ✅ Histórico: Nova transação
   ✅ Status: Concluído ✅
   ```

---

## 💸 TESTE 2: Saque (2 minutos)

### **Passo a Passo:**

1. **Na tela da Carteira, clique em "Sacar"**
   ```
   Botões de ação → Sacar (ícone ↓)
   ```

2. **Digite o valor**
   ```
   Digite: 30
   (R$ 30,00)
   ```

3. **Selecione a conta bancária**
   ```
   Se não tiver conta:
   1. Clique "Adicionar Conta"
   2. Preencha dados fake
   3. Salve
   
   Se já tiver:
   1. Selecione da lista
   ```

4. **Solicite o saque**
   ```
   Clique: "Solicitar Saque"
   ```

5. **Observe o saldo ser bloqueado**
   ```
   Antes:
   Disponível: R$ 1.550,00
   Bloqueado: R$ 0,00
   
   Depois (imediato):
   Disponível: R$ 1.520,00
   Bloqueado: R$ 30,00
   ```

6. **Aguarde a confirmação (3 segundos)**
   ```
   Contador: 3... 2... 1...
   ✅ Notificação verde: "Saque confirmado! R$ 30,00"
   ```

7. **Verifique:**
   ```
   ✅ Saldo: R$ 1.520,00
   ✅ Bloqueado: R$ 0,00 (desbloqueado)
   ✅ Histórico: Nova transação
   ✅ Status: Concluído ✅
   ```

---

## 🔄 TESTE 3: Sincronização (30 segundos)

### **Passo a Passo:**

1. **Observe o topo da tela**
   ```
   Deve mostrar:
   "⚡ Sincronizando..."
   ```

2. **Após 1 segundo:**
   ```
   "✓ Sincronizado há 0s"
   ```

3. **Aguarde 10 segundos e observe:**
   ```
   "✓ Sincronizado há 10s"
   ```

4. **Clique no ícone Refresh (⟳)**
   ```
   Topo direito → Botão Refresh
   ```

5. **Observe:**
   ```
   "⚡ Sincronizando..."
   Após 1s:
   "✓ Sincronizado há 0s"
   ```

6. **Aguarde 30 segundos sem fazer nada:**
   ```
   Deve sincronizar automaticamente:
   "⚡ Sincronizando..."
   "✓ Sincronizado há 0s"
   ```

---

## 📋 TESTE 4: Histórico (1 minuto)

### **Passo a Passo:**

1. **Na tela da Carteira, role para baixo**
   ```
   Ver "Transações Recentes"
   ```

2. **Observe as transações:**
   ```
   Deve mostrar:
   
   💰 Depósito via PIX
   R$ 50,00        ✅ Concluído
   14/11/2025 15:30
   
   💸 Saque para [Banco]
   R$ 30,00        ✅ Concluído
   14/11/2025 15:28
   ```

3. **Clique em uma transação:**
   ```
   Ver detalhes completos:
   - ID da transação
   - Data/Hora
   - Valor
   - Status
   - Descrição
   ```

---

## 📊 Checklist de Verificação

### ✅ **Depósito PIX**
- [ ] QR Code aparece em ~1.5s
- [ ] Código copia e cola funciona
- [ ] Auto-confirmação em 5s
- [ ] Saldo atualizado
- [ ] Transação no histórico
- [ ] Notificação verde exibida

### ✅ **Saque**
- [ ] Valida saldo insuficiente
- [ ] Saldo bloqueado imediatamente
- [ ] Auto-confirmação em 3s
- [ ] Saldo desbloqueado após confirmação
- [ ] Transação no histórico
- [ ] Notificação verde exibida

### ✅ **Sincronização**
- [ ] Indicador no topo funciona
- [ ] Contador atualiza a cada segundo
- [ ] Botão refresh manual funciona
- [ ] Auto-sync a cada 30s
- [ ] Saldo atualiza corretamente

### ✅ **Interface**
- [ ] Animações suaves
- [ ] Cores corretas (verde #019D31)
- [ ] Ícones apropriados
- [ ] Textos legíveis
- [ ] Botões responsivos

---

## 🐛 Se Algo Der Errado

### **Problema: QR Code não aparece**
```
Solução:
1. Verifique Logcat: "PagBankRepository"
2. Deve mostrar: "⚠️ MODO SIMULADO - Gerando QR Code fake"
3. Se não mostrar, rebuild o app
```

### **Problema: Saldo não atualiza**
```
Solução:
1. Clique no botão Refresh (⟳)
2. Verifique Logcat: "CarteiraViewModel"
3. Deve mostrar: "✅ Saldo sincronizado"
```

### **Problema: Transação não aparece**
```
Solução:
1. Aguarde 5s (depósito) ou 3s (saque)
2. Role a lista para baixo
3. Pull to refresh na lista
```

### **Problema: App crashou**
```
Solução:
1. Verifique Logcat para erro específico
2. Rebuild Project
3. Clean Project
4. Invalidate Caches / Restart
```

---

## 📱 Fluxo Completo (5 minutos)

```
1. Abrir App
   ↓
2. Ir para Carteira
   ↓
3. Ver saldo inicial: R$ 1.500,00
   ↓
4. Adicionar → R$ 50,00 → Gerar PIX
   ↓
5. Aguardar 5s → Confirmado
   ↓
6. Novo saldo: R$ 1.550,00
   ↓
7. Sacar → R$ 30,00 → Solicitar
   ↓
8. Aguardar 3s → Confirmado
   ↓
9. Saldo final: R$ 1.520,00
   ↓
10. Ver histórico: 2 transações ✅
```

---

## 🎨 O Que Você Deve Ver

### **Tela da Carteira:**
```
┌────────────────────────────────────┐
│ Minha Carteira        ⟳           │
│ ✓ Sincronizado há 5s               │
├────────────────────────────────────┤
│                                    │
│  Saldo Disponível          🏦      │
│  R$ 1.520,00                       │
│  Bloqueado: R$ 0,00               │
│                                    │
├────────────────────────────────────┤
│ 💰      💸      🏦                 │
│ Adicionar  Sacar   Contas          │
├────────────────────────────────────┤
│ Transações Recentes                │
│                                    │
│ 💰 Depósito via PIX                │
│ R$ 50,00           ✅ Concluído    │
│ 14/11/2025 15:30                   │
│                                    │
│ 💸 Saque para Banco do Brasil      │
│ R$ 30,00           ✅ Concluído    │
│ 14/11/2025 15:28                   │
└────────────────────────────────────┘
```

---

## 📝 Logs Esperados

### **Logcat (Filter: "CarteiraViewModel"):**
```
🔄 Iniciando sincronização com PagBank...
✅ Saldo sincronizado: R$ 1500.0
✅ Sincronização concluída
✅ Depósito criado: DEP_1731606000000
⚠️ MODO SIMULADO - Auto-confirmando pagamento em 5s...
💰 Confirmando depósito simulado: DEP_1731606000000
✅ Depósito confirmado: +R$ 50,00
✅ Saque solicitado: SAQ_1731606100000
⚠️ MODO SIMULADO - Auto-confirmando saque em 3s...
💸 Confirmando saque simulado: SAQ_1731606100000
✅ Saque confirmado: -R$ 30,00
```

---

## ⏱️ Tempo Estimado por Teste

| Teste | Tempo |
|-------|-------|
| Depósito PIX | 2 min |
| Saque | 2 min |
| Sincronização | 30s |
| Histórico | 1 min |
| **TOTAL** | **~6 min** |

---

## ✅ Resultado Esperado

Ao final dos testes você deve ter:

```
✅ Saldo inicial: R$ 1.500,00
✅ Após depósito: R$ 1.550,00 (+50)
✅ Após saque: R$ 1.520,00 (-30)
✅ 2 transações concluídas no histórico
✅ Sincronização funcionando
✅ Todas as notificações exibidas
```

---

## 🎉 Pronto!

Se tudo funcionou conforme descrito acima:

```
┌────────────────────────────────────┐
│  🎊 PARABÉNS! 🎊                   │
│                                    │
│  Sua carteira está 100% integrada  │
│  com o PagBank Sandbox!            │
│                                    │
│  ✅ Depósitos funcionando          │
│  ✅ Saques funcionando             │
│  ✅ Sincronização ativa            │
│  ✅ Modo simulado perfeito         │
│                                    │
│  Agora é só curtir! 🚀             │
└────────────────────────────────────┘
```

---

**🎮 BOM TESTE!**

