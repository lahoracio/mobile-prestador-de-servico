# 🔧 GUIA DE DEBUG - Chat ao Vivo

## ✅ Correções Aplicadas:

### 1. **URL do Servidor Corrigida**
- ❌ Antes: `https://facilita-c6hhb9csgygudrdz.canadacentral-01.azurewebsites.net`
- ✅ Agora: `https://servidor-facilita.onrender.com`

### 2. **Logs Detalhados Adicionados**
Agora você pode ver no Logcat o que está acontecendo:
- `🔌 Tentando conectar...`
- `✅ Socket conectado com sucesso`
- `👤 Usuário autenticado`
- `🔗 Conectado ao serviço`
- `📤 Emitindo send_message com payload`
- `✅ Mensagem enviada com sucesso!`
- `📥 Mensagem recebida`
- `❌ Erro ao...`

### 3. **Callbacks de Confirmação**
- `onSuccess()` - Quando mensagem é enviada
- `onError(String)` - Quando há erro

### 4. **Verificação Periódica de Conexão**
- Verifica status a cada 2 segundos
- Atualiza o indicador visual automaticamente

### 5. **Tratamento de Erros Melhorado**
- Mensagem de erro clara quando offline
- Logs detalhados de cada etapa

## 📱 Como Testar:

### **Passo 1: Abrir o Logcat**
No Android Studio:
1. Vá em **View** > **Tool Windows** > **Logcat**
2. No filtro, digite: `ChatSocketManager` ou `TelaChatAoVivo`
3. Execute o app

### **Passo 2: Entrar no Chat**
1. Aceite um serviço
2. Vá em "Detalhes do Serviço"
3. Clique em "Chat ao vivo"
4. **Observe os logs!**

### **Passo 3: Verificar Conexão**
No Logcat você deve ver:
```
D/ChatSocketManager: 🔌 Tentando conectar ao servidor WebSocket...
D/ChatSocketManager: ✅ Socket conectado com sucesso
D/ChatSocketManager: Evento user_connected enviado: {"userId":X,"userType":"prestador","userName":"Nome"}
D/ChatSocketManager: Entrou na sala do serviço: X
```

Se ver `❌ Erro ao conectar`, anote o erro e me mostre!

### **Passo 4: Enviar Mensagem**
1. Digite uma mensagem
2. Clique no botão verde
3. **Observe os logs!**

Você deve ver:
```
D/ChatSocketManager: Tentando enviar mensagem...
D/ChatSocketManager: Socket conectado? true
D/ChatSocketManager: ServiceId: X, TargetUserId: X
D/ChatSocketManager: Mensagem: Sua mensagem
D/ChatSocketManager: 📤 Emitindo send_message com payload: {...}
D/ChatSocketManager: ✅ Mensagem enviada com sucesso!
D/TelaChatAoVivo: ✅ Mensagem enviada com sucesso!
```

Se ver `❌ Socket não está conectado`, significa que perdeu a conexão!

## 🐛 Problemas Comuns e Soluções:

### **❌ "Socket não está conectado"**
**Causa:** O socket desconectou ou não conectou ainda
**Solução:**
1. Verifique se o servidor está online
2. Verifique sua internet
3. Aguarde alguns segundos e tente novamente

### **❌ "Erro ao conectar: [erro]"**
**Causa:** Servidor inacessível ou problema de rede
**Solução:**
1. Verifique se a URL está correta
2. Teste no navegador: https://servidor-facilita.onrender.com
3. Verifique permissão de INTERNET no AndroidManifest

### **❌ Mensagem não chega ao contratante**
**Causa:** Problema no backend ou contratante não está na sala
**Solução:**
1. Verifique se o `targetUserId` está correto
2. Verifique logs do servidor
3. Teste com outro usuário

### **❌ Status sempre "Offline"**
**Causa:** Socket não está conectando
**Solução:**
1. Veja o Logcat para ver o erro exato
2. Verifique URL do servidor
3. Reinicie o app

## 📊 Checklist de Debug:

- [ ] Logcat aberto e filtrando `ChatSocketManager`
- [ ] Ver log "🔌 Tentando conectar..."
- [ ] Ver log "✅ Socket conectado com sucesso"
- [ ] Ver log "Entrou na sala do serviço"
- [ ] Status muda para "Online" (bolinha verde)
- [ ] Ao digitar e enviar:
  - [ ] Ver log "Tentando enviar mensagem..."
  - [ ] Ver log "Socket conectado? true"
  - [ ] Ver log "📤 Emitindo send_message"
  - [ ] Ver log "✅ Mensagem enviada com sucesso!"
  - [ ] Mensagem aparece na tela (lado direito, verde claro)

## 🧪 Teste Completo:

1. **Abrir Logcat** com filtro `ChatSocketManager`
2. **Entrar no chat** de um serviço
3. **Copiar TODOS os logs** que aparecem
4. **Tentar enviar mensagem**
5. **Copiar os logs do envio**
6. **Me mostrar os logs** se houver problema

## 📋 Informações para Reportar Erro:

Se continuar com problema, me envie:
1. **Logs do Logcat** (filtro: ChatSocketManager)
2. **Status mostrado na tela** (Online/Offline)
3. **O que acontece ao clicar enviar**
4. **Versão do Android** que está testando

## 🔍 Comandos Úteis do Logcat:

```
# Ver apenas logs do Chat
adb logcat | grep ChatSocketManager

# Ver apenas logs de erro
adb logcat | grep "ChatSocketManager.*ERROR"

# Salvar logs em arquivo
adb logcat -d > logs_chat.txt
```

## ✅ Se Tudo Funcionar:

Você verá:
1. ✅ Bolinha verde "Online"
2. ✅ Mensagens aparecem instantaneamente
3. ✅ Logs sem erros
4. ✅ Mensagens chegam no app do contratante

## 🎯 Próximo Passo:

**TESTE AGORA** e me mostre os logs do Logcat! 📱🔍

