# 🔧 CORREÇÃO: Chat Não Recebe Mensagens do Contratante

## ✅ PROBLEMAS CORRIGIDOS

### 1. **Bug Crítico no sendMessage**
**ANTES**: `senderUserId = targetUserId` ❌
**AGORA**: `senderUserId = senderUserId` (parâmetro correto) ✅

Este bug fazia com que as mensagens enviadas tivessem o ID do destinatário em vez do remetente, causando confusão na identificação.

### 2. **Logs Detalhados Adicionados**
✅ Logs em cada etapa do envio/recebimento
✅ Stack trace completo em erros
✅ Payload JSON completo logado
✅ Contagem de mensagens na lista

### 3. **Eventos Alternativos**
✅ Adicionado suporte para `message`
✅ Adicionado suporte para `chat_message`
✅ Adicionado suporte para `new_message`
✅ Listener genérico `*` para capturar TODOS os eventos

### 4. **Extração Robusta de Dados**
O código agora tenta múltiplas fontes para extrair dados:
- `userInfo.userName` → `senderName` → `userName`
- `userInfo.userId` → `userId` → `senderId`
- Fallbacks para evitar dados vazios

---

## 🧪 COMO TESTAR AGORA

### 1. **Limpar Cache e Recompilar**
```
Build → Clean Project
Build → Rebuild Project
```

### 2. **Abrir Logcat ANTES de testar**
Filtro recomendado:
```
ChatSocketManager | ChatViewModel
```

### 3. **Teste Passo a Passo**

#### Passo 1: Abrir o Chat
1. Entre em um pedido em andamento
2. Clique no botão "Chat"
3. **VERIFIQUE no Logcat**:
```
✅ Conectado ao servidor Socket.IO
👤 Usuário registrado: [SeuNome] (prestador)
🚪 Entrando na sala do serviço: [ID]
```

#### Passo 2: Enviar Mensagem (Você → Contratante)
1. Digite "Teste 1"
2. Clique em Enviar
3. **VERIFIQUE no Logcat**:
```
📤 Enviando mensagem para servicoId=X, sender=prestador, target=Y
📤 Conteúdo: Teste 1
✅ Mensagem adicionada localmente: sender=prestador, userId=X
```

#### Passo 3: Receber Mensagem (Contratante → Você)
1. Peça ao contratante para enviar "Teste 2"
2. **VERIFIQUE no Logcat** (CRUCIAL):
```
📩 Evento 'receive_message' recebido (principal)
📩 Payload completo: {servicoId:X, mensagem:"Teste 2", sender:"contratante", ...}
📩 Processando: servicoId=X, sender=contratante, mensagem=Teste 2
📩 Dados extraídos: userName=[Nome], userId=[ID]
✅ Mensagem processada e adicionada: 'Teste 2' de [Nome] (contratante)
✅ Total de mensagens: 2
```

#### Passo 4: Se NÃO aparecer no Logcat
Procure por:
```
🔔 Evento genérico recebido
```

Isso mostrará TODOS os eventos que estão chegando, incluindo nomes diferentes.

---

## 🔍 DIAGNÓSTICO

### Cenário A: Mensagem NÃO aparece no Logcat
**Problema**: Socket não está recebendo do servidor
**Soluções**:
1. Verificar conexão: procure por "✅ Conectado"
2. Verificar sala: procure por "🚪 Entrando na sala"
3. Verificar URL em `ChatConfig.kt`
4. Testar servidor diretamente (Postman/outra ferramenta)

### Cenário B: Mensagem aparece no Logcat mas NÃO na tela
**Problema**: UI não está observando o StateFlow
**Soluções**:
1. Verificar se `messages` está sendo coletado na tela
2. Verificar se `LazyColumn` está sendo recomposto
3. Adicionar log no `TelaChatAoVivo`:
```kotlin
LaunchedEffect(messages.size) {
    Log.d("TelaChatAoVivo", "Mensagens atualizadas: ${messages.size}")
}
```

### Cenário C: Mensagem aparece mas com dados errados
**Problema**: Mapeamento incorreto do JSON
**Soluções**:
1. Verificar o payload JSON no log
2. Ajustar extração em `processIncomingMessage`
3. Comparar com documentação da API

---

## 📊 ESTRUTURA DO PAYLOAD ESPERADO

### Enviando (send_message):
```json
{
  "servicoId": 123,
  "mensagem": "Olá!",
  "sender": "prestador",
  "targetUserId": 456,
  "senderName": "João",
  "timestamp": 1701445678000
}
```

### Recebendo (receive_message):
```json
{
  "servicoId": 123,
  "mensagem": "Tudo bem?",
  "sender": "contratante",
  "userInfo": {
    "userId": 456,
    "userName": "Maria",
    "userType": "contratante"
  },
  "timestamp": 1701445678000
}
```

**OU** (formato alternativo):
```json
{
  "servicoId": 123,
  "mensagem": "Tudo bem?",
  "sender": "contratante",
  "userId": 456,
  "senderName": "Maria",
  "timestamp": 1701445678000
}
```

---

## 🛠️ COMANDOS DE DEBUG

### 1. Filtrar apenas eventos de mensagem:
```
adb logcat | findstr "📩 📤"
```

### 2. Filtrar eventos de conexão:
```
adb logcat | findstr "✅ 🚪 👤"
```

### 3. Filtrar erros:
```
adb logcat | findstr "❌ ERROR"
```

### 4. Ver TUDO do chat:
```
adb logcat -s ChatSocketManager:D ChatViewModel:D TelaChatAoVivo:D
```

---

## 🎯 CHECKLIST DE VERIFICAÇÃO

### Antes de testar:
- [ ] Clean + Rebuild executado
- [ ] App reinstalado no dispositivo
- [ ] Logcat aberto e configurado
- [ ] Filtro correto aplicado

### Durante o teste:
- [ ] Conexão estabelecida (log "✅ Conectado")
- [ ] Usuário registrado (log "👤 Usuário registrado")
- [ ] Sala joinada (log "🚪 Entrando na sala")
- [ ] Mensagem enviada (log "📤 Enviando mensagem")
- [ ] Confirmação local (log "✅ Mensagem adicionada localmente")

### Para receber mensagem:
- [ ] Evento receive_message detectado (log "📩 Evento 'receive_message'")
- [ ] Payload recebido (log "📩 Payload completo")
- [ ] Mensagem processada (log "✅ Mensagem processada")
- [ ] Mensagem aparece na tela

---

## 🚨 SE AINDA NÃO FUNCIONAR

### 1. Verificar servidor está emitindo corretamente
Peça ao time de backend para:
- Confirmar que o evento `receive_message` está sendo emitido
- Verificar que a sala está correta: `servicoId.toString()`
- Confirmar estrutura do payload JSON
- Testar com outro cliente (Postman, navegador, etc.)

### 2. Testar com outro usuário
- Crie duas contas
- Abra o chat em dois dispositivos/emuladores
- Envie mensagens de ambos os lados
- Compare logs

### 3. Versão do Socket.IO
Verificar compatibilidade:
- Cliente Android: `io.socket:socket.io-client:2.1.0`
- Servidor: deve ser compatível (1.x ou 2.x)

---

## 📝 ARQUIVOS MODIFICADOS

### ChatSocketManager.kt
- ✅ Corrigido `senderUserId` no sendMessage
- ✅ Adicionado método `processIncomingMessage`
- ✅ Adicionado listeners alternativos (`message`, `chat_message`, `new_message`)
- ✅ Adicionado listener genérico `*`
- ✅ Logs detalhados em todos os pontos

### ChatViewModel.kt
- ✅ Passar `senderUserId` correto ao chamar sendMessage
- ✅ Logs detalhados no envio

---

## ✅ RESULTADO ESPERADO

### No Logcat (quando funcionar):
```
📩 Evento 'receive_message' recebido (principal)
📩 Payload completo: {...}
📩 Processando: servicoId=123, sender=contratante, mensagem=Olá!
📩 Dados extraídos: userName=Maria, userId=456
✅ Mensagem processada e adicionada: 'Olá!' de Maria (contratante)
✅ Total de mensagens: 2
```

### Na Tela:
```
┌─────────────────────────────────────────┐
│  ┌──────────────┐                       │
│  │ Olá!         │  Maria                │ ← Mensagem do contratante
│  │              │  14:30                │
│  └──────────────┘                       │
│                                         │
│                  ┌──────────────┐       │
│                  │ Oi! Tudo bem?│ ✓     │ ← Sua mensagem
│                  └──────────────┘ 14:31 │
└─────────────────────────────────────────┘
```

---

## 🎉 PRONTO PARA TESTAR!

**Compile o projeto e siga os passos de teste acima.**

**ATENÇÃO**: Mantenha o Logcat aberto o tempo todo para ver os logs em tempo real!

---

**Data**: 01/12/2025
**Versão**: 2.0 - Correção de Recebimento
**Status**: ✅ PRONTO PARA TESTE

