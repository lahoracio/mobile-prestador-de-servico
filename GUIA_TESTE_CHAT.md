# 🚀 Guia Rápido - Como Testar o Chat

## ⚡ Início Rápido (5 minutos)

### Pré-requisitos
- ✅ App compilado e instalado
- ✅ Internet ativa
- ✅ Conta de prestador logada
- ✅ Pelo menos um serviço aceito

---

## 📱 Passo a Passo para Testar

### 1️⃣ Aceitar um Serviço (se ainda não tem)

```
1. Abra o app
2. Vá para "Início"
3. Aguarde notificação de novo serviço
4. Clique em "Aceitar Serviço"
```

### 2️⃣ Abrir o Chat

**Método 1: Pela Tela de Detalhes**
```
1. Na tela "Serviço Aceito"
2. Role para baixo até "Cliente"
3. Clique no botão verde "Chat"
```

**Método 2: Pela Tela de Localização**
```
1. Na tela de acompanhamento (mapa)
2. Clique no botão azul flutuante (ícone de chat)
   - Fica acima do botão verde de localização
```

### 3️⃣ Verificar Conexão

```
Aguarde alguns segundos até:
✓ Status mudar para "Online"
✓ Ponto verde aparecer no header
✓ Tela carregar completamente
```

### 4️⃣ Enviar Primeira Mensagem

```
1. Clique no campo "Digite uma mensagem..."
2. Digite: "Olá! Estou a caminho"
3. Pressione o botão de enviar (✈️) OU Enter

Resultado esperado:
✓ Mensagem aparece do lado direito (verde)
✓ Horário da mensagem aparece embaixo
✓ Ícone de check (✓) aparece
```

### 5️⃣ Testar Indicador de Digitação

```
1. Peça para o contratante começar a digitar
   (ou use o app do contratante se tiver acesso)

Resultado esperado:
✓ Aparece "Fulano está digitando..."
✓ Três pontinhos animados aparecem
✓ Desaparece quando parar de digitar
```

### 6️⃣ Receber Mensagem

```
1. Contratante envia mensagem

Resultado esperado:
✓ Mensagem aparece do lado esquerdo (cinza)
✓ Nome do contratante aparece em verde
✓ Scroll automático para nova mensagem
✓ Horário da mensagem correto
```

---

## ✅ Checklist de Funcionalidades

### Conexão
- [ ] Status "Conectando..." aparece
- [ ] Status muda para "Online"
- [ ] Ponto verde no header
- [ ] Avatar do contratante carregado

### Envio de Mensagens
- [ ] Campo de texto funciona
- [ ] Botão de enviar fica verde ao digitar
- [ ] Mensagem aparece do lado direito
- [ ] Cor verde para minhas mensagens
- [ ] Timestamp correto
- [ ] Ícone de check aparece

### Recebimento de Mensagens
- [ ] Mensagem aparece do lado esquerdo
- [ ] Cor cinza claro para mensagens recebidas
- [ ] Nome do remetente aparece
- [ ] Timestamp correto
- [ ] Scroll automático funciona

### Indicador de Digitação
- [ ] Aparece quando outro usuário digita
- [ ] Nome correto do usuário
- [ ] Animação dos pontos funciona
- [ ] Desaparece após envio/timeout

### Interface
- [ ] Header bonito e funcional
- [ ] Botão voltar funciona
- [ ] Balões de mensagem bem formatados
- [ ] Cores agradáveis
- [ ] Animações suaves
- [ ] Campo de texto expansível (até 4 linhas)

### Navegação
- [ ] Abrir chat da tela de detalhes
- [ ] Abrir chat da tela de localização
- [ ] Voltar para tela anterior
- [ ] Dados corretos em todas as rotas

### Estado Vazio
- [ ] Mensagem "Nenhuma mensagem ainda" quando vazio
- [ ] Ícone de chat aparece
- [ ] Texto de incentivo aparece
- [ ] Desaparece após primeira mensagem

### Erros
- [ ] Mensagem de erro aparece se não conectar
- [ ] Card vermelho para erros
- [ ] Erro desaparece após alguns segundos
- [ ] Retry automático funciona

---

## 🐛 O que Fazer se Algo Der Errado

### Chat não abre
```
✓ Verifique se serviço foi aceito corretamente
✓ Verifique logs: adb logcat | findstr "Chat"
✓ Reinicie o app
✓ Limpe o cache do app
```

### Não conecta
```
✓ Verifique internet do celular
✓ Teste URL: https://facilita-c6hhb9csgygudrdz.canadacentral-01.azurewebsites.net
✓ Aguarde até 10 segundos
✓ Force close e reabra
```

### Mensagens não enviam
```
✓ Verifique status "Online"
✓ Verifique se campo não está vazio
✓ Tente enviar novamente
✓ Verifique logs de erro
```

### App trava
```
✓ Capture logs: adb logcat > crash.log
✓ Limpe cache
✓ Desinstale e reinstale
✓ Contate suporte
```

---

## 📊 Logs Úteis

### Ver Logs do Chat
```bash
# Windows
adb logcat -c  # Limpar logs
adb logcat | findstr "Chat"

# Linux/Mac
adb logcat -c
adb logcat | grep Chat
```

### Logs Importantes

**Conexão estabelecida:**
```
ChatSocketManager: ✅ Conectado ao servidor Socket.IO
```

**Mensagem enviada:**
```
ChatSocketManager: 📤 Enviando mensagem: Olá
ChatSocketManager: ✅ Mensagem enviada com sucesso
```

**Mensagem recebida:**
```
ChatSocketManager: 📩 Mensagem recebida: {...}
ChatSocketManager: ✅ Mensagem adicionada: Olá
```

**Erro:**
```
ChatSocketManager: ❌ Erro de conexão: ...
```

---

## 🎯 Cenários de Teste Completos

### Teste 1: Conversa Básica (5 min)
```
1. Prestador aceita serviço
2. Abre chat
3. Envia: "Olá, estou indo para o local"
4. Contratante responde: "Ok, obrigado!"
5. Prestador envia: "Chego em 10 minutos"
6. Verifica todas as mensagens aparecem corretamente
```

### Teste 2: Múltiplas Mensagens (5 min)
```
1. Envia 5 mensagens seguidas rapidamente
2. Verifica todas aparecem
3. Verifica ordem correta
4. Verifica scroll automático funciona
5. Verifica timestamps corretos
```

### Teste 3: Reconexão (5 min)
```
1. Chat funcionando normalmente
2. Desativa WiFi/Dados
3. Verifica status muda para "Offline"
4. Tenta enviar mensagem (deve falhar)
5. Reativa WiFi/Dados
6. Aguarda reconexão (até 10s)
7. Envia mensagem novamente
8. Verifica mensagem é enviada
```

### Teste 4: Navegação (3 min)
```
1. Abre chat pela tela de detalhes
2. Envia mensagem
3. Volta com botão back
4. Vai para tela de localização
5. Abre chat pelo FAB
6. Verifica histórico mantém mensagens
7. Envia nova mensagem
```

### Teste 5: Estado Vazio (2 min)
```
1. Abre chat novo (sem mensagens)
2. Verifica estado vazio aparece
3. Verifica texto e ícone corretos
4. Envia primeira mensagem
5. Verifica estado vazio desaparece
```

---

## 📸 Screenshots Esperados

### Tela Inicial do Chat
```
┌─────────────────────────────────┐
│ ← [Avatar] João Silva     ⋮     │
│   Online                        │
├─────────────────────────────────┤
│                                 │
│  [Estado Vazio]                 │
│  💬 Ícone                       │
│  Nenhuma mensagem ainda         │
│  Envie uma mensagem para...     │
│                                 │
├─────────────────────────────────┤
│ [Digite uma mensagem...]  ✈️    │
└─────────────────────────────────┘
```

### Chat com Mensagens
```
┌─────────────────────────────────┐
│ ← [Avatar] João Silva     ⋮     │
│   Online                        │
├─────────────────────────────────┤
│                                 │
│ ┌─João Silva──────────┐         │
│ │ Onde você está?     │ 14:30   │
│ └─────────────────────┘         │
│                                 │
│         ┌─────────────────────┐ │
│   14:31 │ Estou chegando! ✓  │ │
│         └─────────────────────┘ │
│                                 │
│ João está digitando...          │
│                                 │
├─────────────────────────────────┤
│ [Olá! Estou indo]  ✈️           │
└─────────────────────────────────┘
```

---

## 🎓 Dicas Profissionais

### Para Melhor Experiência
1. ✅ Use frases curtas e objetivas
2. ✅ Responda rapidamente
3. ✅ Mantenha profissionalismo
4. ✅ Use o indicador de digitação (digite devagar)

### Evite
1. ❌ Enviar mensagens muito longas
2. ❌ Spam de mensagens
3. ❌ Fechar chat durante conversa ativa
4. ❌ Usar com internet instável

---

## 📞 Precisa de Ajuda?

### Contato Rápido
- 📧 Email: suporte@facilita.com
- 💬 Chat: disponível no app
- 📱 WhatsApp: +55 11 9999-9999

### Documentação
- 📖 Documentação Completa: `SISTEMA_CHAT_COMPLETO.md`
- 🔧 Troubleshooting: Seção de problemas comuns
- 📊 API Docs: Documentação do backend

---

## ✨ Resultado Final

Após seguir este guia, você deve ter:

✅ Chat funcionando perfeitamente
✅ Conexão estável
✅ Mensagens enviando e recebendo
✅ Interface bonita e responsiva
✅ Indicador de digitação funcional
✅ Navegação fluida

**Tempo estimado:** 15-20 minutos para teste completo

---

**Bons testes! 🚀**

_Última atualização: 2025-01-28_

