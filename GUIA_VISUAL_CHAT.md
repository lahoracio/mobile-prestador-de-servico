# 🎨 Sistema de Chat - Guia Visual

## 📱 Telas do Chat

### 1. Tela Inicial (Estado Vazio)

```
╔═════════════════════════════════════════════╗
║  ←  [👤] João Silva              ≡         ║
║      🟢 Online                              ║
╠═════════════════════════════════════════════╣
║                                             ║
║                                             ║
║                   💬                        ║
║                                             ║
║        Nenhuma mensagem ainda               ║
║                                             ║
║     Envie uma mensagem para iniciar         ║
║        a conversa com o cliente             ║
║                                             ║
║                                             ║
║                                             ║
╠═════════════════════════════════════════════╣
║  Digite uma mensagem...             ✈️     ║
╚═════════════════════════════════════════════╝
```

---

### 2. Tela com Conversação

```
╔═════════════════════════════════════════════╗
║  ←  [👤] João Silva              ≡         ║
║      🟢 Online                              ║
╠═════════════════════════════════════════════╣
║                                             ║
║  ┌──────────────────────────┐              ║
║  │ João Silva               │              ║
║  │ Onde você está?          │  14:30       ║
║  └──────────────────────────┘              ║
║                                             ║
║                  ┌──────────────────────┐  ║
║            14:31 │ Estou chegando! ✓   │  ║
║                  └──────────────────────┘  ║
║                                             ║
║  ┌──────────────────────────┐              ║
║  │ João Silva               │              ║
║  │ Ok, te aguardo           │  14:32       ║
║  └──────────────────────────┘              ║
║                                             ║
║  João está digitando • • •                 ║
║                                             ║
╠═════════════════════════════════════════════╣
║  Estou no local              ✈️            ║
╚═════════════════════════════════════════════╝
```

---

## 🎨 Elementos de Interface

### Header do Chat

```
┌─────────────────────────────────────────┐
│  ←  [Avatar] Nome do Cliente      ⋮    │
│     🟢 Status (Online/Offline)          │
└─────────────────────────────────────────┘

Elementos:
- Botão Voltar (←)
- Avatar do cliente (círculo)
- Nome do cliente (bold)
- Indicador de status (🟢🟡🔴)
- Menu de opções (⋮)
```

### Balão de Mensagem Recebida

```
┌──────────────────────────┐
│ João Silva               │ ← Nome (verde)
│ Olá, tudo bem?           │ ← Mensagem
│                    14:30 │ ← Hora
└──────────────────────────┘

Estilo:
- Fundo: Cinza claro (#E8F5E9)
- Alinhamento: Esquerda
- Cantos: Arredondados (topo: 16dp, inferior-esq: 4dp)
- Nome: Verde (#2E7D32)
```

### Balão de Mensagem Enviada

```
            ┌──────────────────────┐
      14:31 │ Estou indo! ✓       │ ← Mensagem
            └──────────────────────┘

Estilo:
- Fundo: Verde (#2E7D32)
- Alinhamento: Direita
- Cantos: Arredondados (topo: 16dp, inferior-dir: 4dp)
- Texto: Branco
- Ícone check: Branco
```

### Indicador de Digitação

```
┌──────────────────────────┐
│ João está digitando • • • │
└──────────────────────────┘

Animação:
- Três pontos pulsantes
- Cor: Cinza (#757575)
- Fundo: Cinza claro (#E8F5E9)
- Animação: Fade in/out sequencial
```

### Campo de Entrada

```
┌─────────────────────────────────────┐
│ Digite uma mensagem...      [✈️]   │
└─────────────────────────────────────┘

Estados:
- Vazio: Botão cinza
- Com texto: Botão verde
- Foco: Borda verde
- Expansível: 1-4 linhas
```

---

## 🎭 Estados Visuais

### 1. Conectando

```
┌─────────────────────┐
│ 🟡 Conectando...    │
└─────────────────────┘

Características:
- Ponto amarelo pulsante
- Texto "Conectando..."
- Animação de pulso
```

### 2. Online

```
┌─────────────────────┐
│ 🟢 Online           │
└─────────────────────┘

Características:
- Ponto verde pulsante
- Texto "Online"
- Animação suave
```

### 3. Offline

```
┌─────────────────────┐
│ 🔴 Offline          │
└─────────────────────┘

Características:
- Ponto vermelho estático
- Texto "Offline"
- Sem animação
```

### 4. Erro

```
┌─────────────────────────────────────┐
│ ⚠️  Erro de conexão com o servidor │
└─────────────────────────────────────┘

Características:
- Fundo vermelho claro (#FFEBEE)
- Ícone de aviso
- Texto vermelho (#D32F2F)
- Auto-dismiss em 3s
```

---

## 🔄 Animações

### 1. Entrada da Tela

```
Sequência:
1. Header: Slide down + Fade in (200ms)
2. Mensagens: Fade in + Expand (300ms)
3. Campo: Slide up + Fade in (400ms)

Easing: FastOutSlowInEasing
```

### 2. Nova Mensagem

```
Efeito:
1. Fade in (300ms)
2. Scale from 0.8 to 1.0
3. Scroll automático suave

Trigger: Nova mensagem recebida/enviada
```

### 3. Indicador de Digitação

```
Animação dos pontos:
• ○ ○  →  ○ • ○  →  ○ ○ •  →  • ○ ○

Delay entre pontos: 200ms
Loop: Infinito enquanto digitando
```

### 4. Botão de Enviar

```
Estado Normal → Hover/Press:
- Scale: 1.0 → 1.1
- Cor: Transição suave (200ms)

Após envio:
- Pulso rápido
- Retorna ao normal
```

---

## 🎨 Paleta de Cores Completa

### Cores Primárias

```
🟢 Verde Principal: #2E7D32
   Uso: Minhas mensagens, botões primários

🌿 Verde Escuro: #1B5E20
   Uso: Gradientes, sombras

💠 Ciano Accent: #00FF88
   Uso: Destaques, hover effects

💠 Ciano Alt: #0097A7
   Uso: Botão FAB de chat
```

### Cores de Fundo

```
⬜ Branco: #FFFFFF
   Uso: Cards, header

🔲 Cinza Claro: #F5F5F5
   Uso: Fundo geral, telas

🔳 Cinza Mais Claro: #EEEEEE
   Uso: Gradientes de fundo
```

### Cores de Texto

```
⬛ Preto: #212121
   Uso: Texto principal

🔘 Cinza: #757575
   Uso: Texto secundário, timestamps
```

### Cores de Mensagem

```
🟩 Verde Mensagem: #2E7D32
   Uso: Fundo das minhas mensagens

🔲 Cinza Mensagem: #E8F5E9
   Uso: Fundo mensagens recebidas
```

### Cores de Status

```
🟢 Verde: #4CAF50
   Uso: Online, sucesso

🟡 Amarelo: #FFC107
   Uso: Conectando, aviso

🔴 Vermelho: #F44336
   Uso: Offline, erro

🔴 Vermelho Claro: #FFEBEE
   Uso: Fundo de erro

🔴 Vermelho Escuro: #D32F2F
   Uso: Texto de erro
```

---

## 📐 Dimensões e Espaçamentos

### Espaçamentos Padrão

```
Micro:    4dp   - Entre texto e ícone
Pequeno:  8dp   - Entre elementos próximos
Médio:    12dp  - Entre componentes
Grande:   16dp  - Padding de telas/cards
XGrande:  20dp  - Seções principais
XXGrande: 24dp  - Títulos e headers
```

### Tamanhos de Elementos

```
Avatar:           44dp x 44dp
Ícones Pequenos:  20dp x 20dp
Ícones Médios:    24dp x 24dp
Ícones Grandes:   40dp x 40dp

Botão FAB:        48dp x 48dp
Altura mínima:    48dp (Material Design)

Campo de texto:   min 48dp, max 120dp
```

### Bordas Arredondadas

```
Pequeno:  8dp  - Badges, chips
Médio:    12dp - Botões
Grande:   16dp - Mensagens, cards
XGrande:  24dp - Campo de entrada

Círculo:  50%  - Avatares, FABs
```

---

## 🔤 Tipografia

### Hierarquia de Texto

```
H1 - Título Principal
Size: 24sp
Weight: Bold
Usage: Títulos de seção

H2 - Título Secundário
Size: 20sp
Weight: Bold
Usage: Headers de tela

H3 - Subtítulo
Size: 18sp
Weight: SemiBold
Usage: Cards importantes

Body - Texto Normal
Size: 15sp
Weight: Regular
Line Height: 20sp
Usage: Mensagens

Caption - Legenda
Size: 12sp
Weight: Regular
Usage: Timestamps, status

Small - Pequeno
Size: 11sp
Weight: Regular
Usage: Info adicional
```

---

## 🌊 Fluxo de Telas

### Navegação Completa

```
┌──────────────────┐
│  Tela Início     │
│  (Home)          │
└────────┬─────────┘
         │
         │ Aceitar Serviço
         ▼
┌──────────────────┐
│  Detalhes do     │
│  Serviço Aceito  │
└────┬────────┬────┘
     │        │
     │        │ Abrir Localização
     │        ▼
     │   ┌──────────────────┐
     │   │  Tela de         │
     │   │  Acompanhamento  │
     │   └────────┬─────────┘
     │            │
     │ Clicar     │ Clicar FAB
     │ "Chat"     │ Chat
     │            │
     └────┬───────┘
          │
          ▼
     ┌──────────────────┐
     │  CHAT AO VIVO    │ ← VOCÊ ESTÁ AQUI
     │                  │
     │  💬 Conversação  │
     └──────────────────┘
```

---

## 📊 Arquitetura Visual

```
┌────────────────────────────────────────────┐
│            UI LAYER                        │
│  ┌──────────────────────────────────────┐ │
│  │  TelaChatAoVivo.kt (Composable)      │ │
│  │  - Header                             │ │
│  │  - Lista de Mensagens                 │ │
│  │  - Campo de Entrada                   │ │
│  │  - Indicadores                        │ │
│  └──────────────┬───────────────────────┘ │
└─────────────────┼──────────────────────────┘
                  │ collectAsState()
┌─────────────────▼──────────────────────────┐
│         VIEWMODEL LAYER                    │
│  ┌──────────────────────────────────────┐ │
│  │  ChatViewModel.kt                     │ │
│  │  - StateFlows (messages, status, etc.)│ │
│  │  - initializeChat()                   │ │
│  │  - sendMessage()                      │ │
│  │  - startTypingIndicator()             │ │
│  └──────────────┬───────────────────────┘ │
└─────────────────┼──────────────────────────┘
                  │ Chama métodos
┌─────────────────▼──────────────────────────┐
│         WEBSOCKET LAYER                    │
│  ┌──────────────────────────────────────┐ │
│  │  ChatSocketManager.kt (Singleton)     │ │
│  │  - Socket.IO Connection               │ │
│  │  - Eventos: send/receive              │ │
│  │  - StateFlows                         │ │
│  └──────────────┬───────────────────────┘ │
└─────────────────┼──────────────────────────┘
                  │ Socket.IO
┌─────────────────▼──────────────────────────┐
│            SERVIDOR                        │
│  wss://facilita-[...].azurewebsites.net   │
│  - Socket.IO Server                        │
│  - Salas por servicoId                     │
│  - Eventos: user_connected, join_servico   │
│             send_message, receive_message  │
└────────────────────────────────────────────┘
```

---

## 🎬 Sequência de Eventos

### Abrir Chat

```
👤 Usuário          📱 App               🔌 Socket          🖥️ Servidor
    |                |                    |                  |
    |--1. Navega---->|                    |                  |
    |                |                    |                  |
    |                |--2. connect()----->|                  |
    |                |                    |--3. connect----->|
    |                |                    |<-4. connected----|
    |                |<-5. Connected------|                  |
    |                |                    |                  |
    |                |--6. registerUser()->|                  |
    |                |                    |--7. user_conn--->|
    |                |                    |                  |
    |                |--8. joinServico()-->|                  |
    |                |                    |--9. join_serv--->|
    |                |                    |                  |
    |<-10. UI Ready--|                    |                  |
```

### Enviar Mensagem

```
👤 Usuário          📱 App               🔌 Socket          🖥️ Servidor
    |                |                    |                  |
    |--1. Digita---->|                    |                  |
    |                |--2. typing(true)-->|                  |
    |                |                    |--3. user_typ---->|
    |                |                    |                  |
    |--4. Envia----->|                    |                  |
    |                |--5. sendMsg()----->|                  |
    |                |                    |--6. send_msg---->|
    |                |<-7. Add local msg--|                  |
    |<-8. Vê msg-----|                    |                  |
    |                |                    |<-9. msg_sent-----|
    |                |<-10. Confirmação---|                  |
```

### Receber Mensagem

```
👤 Usuário          📱 App               🔌 Socket          🖥️ Servidor
    |                |                    |                  |
    |                |                    |<-1. rcv_msg------|
    |                |<-2. Nova mensagem--|                  |
    |<-3. UI atualiza|                    |                  |
    |<-4. Scroll auto|                    |                  |
```

---

## 💡 Dicas de UX

### Feedback Visual

```
✓ Mensagem enviada      → Aparece imediatamente
✓ Mensagem recebida     → Scroll automático
✓ Erro                  → Card vermelho (3s)
✓ Conectando            → Ponto amarelo
✓ Online                → Ponto verde
✓ Offline               → Ponto vermelho
✓ Digitando             → "Nome está digitando..."
```

### Micro-interações

```
✓ Botão de enviar       → Verde quando tem texto
✓ Campo de texto        → Borda verde ao focar
✓ Mensagens             → Fade in suave
✓ Scroll                → Animação suave
✓ Status                → Pulso no indicador
```

---

## 🎯 Pontos de Atenção

### Performance

```
✓ Use LazyColumn para mensagens (virtualização)
✓ Limite recomposições (remember, derivedStateOf)
✓ Singleton para WebSocket
✓ Coroutines para operações assíncronas
```

### Acessibilidade

```
✓ contentDescription em todos os ícones
✓ Cores com contraste adequado (WCAG)
✓ Tamanho mínimo de toque: 48dp
✓ Suporte a TalkBack
```

### Segurança

```
✓ Validar input do usuário
✓ Sanitizar mensagens HTML
✓ Usar HTTPS/WSS
✓ Isolar por servicoId
```

---

**🎨 FIM DO GUIA VISUAL 🎨**

_Para mais detalhes técnicos, consulte `SISTEMA_CHAT_COMPLETO.md`_

