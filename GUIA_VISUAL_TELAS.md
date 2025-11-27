# 🎨 Guia Visual das Telas - Fluxo de Serviço

## 📱 1. Tela de Detalhes do Serviço Aceito

### Layout
```
┌─────────────────────────────────────┐
│  ← Serviço Aceito                   │  ← Header com gradiente
├─────────────────────────────────────┤
│                                     │
│   ┌───────────────────────────┐    │
│   │    ✓  [Glow Effect]       │    │  ← Card de sucesso animado
│   │                           │    │
│   │   Serviço Aceito!         │    │
│   │ Confira todos detalhes    │    │
│   └───────────────────────────┘    │
│                                     │
│   ┌───────────────────────────┐    │
│   │ 👤 Cliente                │    │  ← Card do cliente
│   │ ─────────────────────     │    │
│   │ 👤 Nome: João Silva       │    │
│   │ 📞 Tel: (11) 99999-9999   │    │
│   │ 📧 Email: joao@email.com  │    │
│   │                           │    │
│   │ [📞 Ligar]  [💬 Chat]     │    │  ← Botões de ação
│   └───────────────────────────┘    │
│                                     │
│   ┌───────────────────────────┐    │
│   │ 📝 Detalhes do Serviço    │    │  ← Card do serviço
│   │ ─────────────────────     │    │
│   │ 📁 Categoria: Transporte  │    │
│   │ 📋 Descrição: ...         │    │
│   │ ⏱️ Tempo: 45 minutos      │    │
│   │                           │    │
│   │  💰 Valor: R$ 50,00       │    │  ← Valor em destaque
│   └───────────────────────────┘    │
│                                     │
│   ┌───────────────────────────┐    │
│   │ 📍 Localização            │    │  ← Card de localização
│   │ ─────────────────────     │    │
│   │ 🏠 Rua ABC, 123           │    │
│   │ 📌 Centro, São Paulo-SP   │    │
│   │ 📮 CEP: 01234-567         │    │
│   │                           │    │
│   │ [🧭 Abrir Navegação]      │    │  ← Botão Google Maps
│   └───────────────────────────┘    │
│                                     │
│ [Paradas se houver...]             │
│                                     │
├─────────────────────────────────────┤
│ ┌─────────────────────────────┐    │
│ │ ➡️ Prosseguir para o Pedido │    │  ← Botão flutuante
│ └─────────────────────────────┘    │
└─────────────────────────────────────┘
```

### Cores e Efeitos
- **Background:** Gradiente vertical (azul escuro profundo)
- **Cards:** Semi-transparentes com glassmorphism
- **Ícones:** Verde neon (#00E676) e Ciano (#00E5FF)
- **Texto:** Branco com diferentes opacidades
- **Animações:** 
  - Pulso no ícone ✓
  - Fade in sequencial dos cards
  - Glow effect no fundo

---

## 📱 2. Tela de Pedido em Andamento

### Layout
```
┌─────────────────────────────────────┐
│ ← Pedido #123        ⏱️ 00:15      │  ← Header com timer
│   Em andamento                      │
├─────────────────────────────────────┤
│                                     │
│   ┌───────────────────────────┐    │
│   │    🚗  [Pulse Animation]  │    │  ← Card de status animado
│   │                           │    │
│   │    Indo para o local      │    │
│   │ Navegue até o endereço    │    │
│   └───────────────────────────┘    │
│                                     │
│   ┌───────────────────────────┐    │
│   │ Progresso do Serviço      │    │  ← Timeline de status
│   │                           │    │
│   │  ●━━○━━○━━○              │    │
│   │  │  │  │  │              │    │
│   │  ✓  →  ○  ○              │    │
│   │  │     │                  │    │
│   │ Indo  No Local            │    │
│   │       Executando          │    │
│   │       Concluir            │    │
│   └───────────────────────────┘    │
│                                     │
│   ┌───────────────────────────┐    │
│   │ João Silva                │    │  ← Card do cliente
│   │ Cliente                   │    │
│   │                [📞] [💬]  │    │  ← Ações rápidas
│   └───────────────────────────┘    │
│                                     │
│   ┌───────────────────────────┐    │
│   │ 📍 Localização do Serviço │    │  ← Card de localização
│   │                           │    │
│   │ Rua ABC, 123              │    │
│   │ Centro, São Paulo-SP      │    │
│   │                           │    │
│   │ [🧭 Abrir Navegação]      │    │
│   └───────────────────────────┘    │
│                                     │
│   ┌───────────────────────────┐    │
│   │ Detalhes do Serviço       │    │  ← Card de detalhes
│   │                           │    │
│   │ Categoria: Transporte     │    │
│   │ Descrição: ...            │    │
│   │ Tempo: 45 minutos         │    │
│   │                           │    │
│   │ Valor      R$ 50,00       │    │
│   └───────────────────────────┘    │
│                                     │
├─────────────────────────────────────┤
│ ┌─────────────────────────────┐    │
│ │ 📍 Cheguei no Local         │    │  ← Botão contextual
│ └─────────────────────────────┘    │
└─────────────────────────────────────┘
```

### Estados da Timeline

#### 1. Indo para o Local
```
●━━○━━○━━○
│  
🚗 Indo para o local [ATIVO]
   No local
   Executando serviço
   Concluir serviço

Botão: [📍 Cheguei no Local]
```

#### 2. No Local
```
●━━●━━○━━○
   │  
   📍 No local [ATIVO]
      Executando serviço
      Concluir serviço

Botão: [🔨 Iniciar Serviço]
```

#### 3. Executando Serviço
```
●━━●━━●━━○
      │  
      🔨 Executando serviço [ATIVO]
         Concluir serviço

Botão: [✓ Preparar Finalização]
```

#### 4. Finalizando
```
●━━●━━●━━●
         │  
         ✓ Concluir serviço [ATIVO]

Botão: [✓ Concluir Serviço]
```

---

## 🎨 Paleta de Cores Detalhada

### Cores Primárias
```
Primary Green:   #00E676  ████  Verde neon brilhante
Dark Green:      #00C853  ████  Verde escuro
Accent Cyan:     #00E5FF  ████  Ciano elétrico
Accent Orange:   #FF9800  ████  Laranja vibrante
Red Location:    #FF5252  ████  Vermelho localização
```

### Cores de Background
```
Background Dark:  #0A0E27  ████  Azul muito escuro
Surface Dark:     #1A1F3A  ████  Azul escuro médio
Card Background:  #252D47  ████  Azul acinzentado
```

### Cores de Texto
```
White 100%:       #FFFFFF  ████  Texto principal
White 80%:        #FFFFFF  ████  Texto secundário
White 60%:        #FFFFFF  ████  Labels
White 30%:        #FFFFFF  ████  Desabilitado
```

---

## 🎭 Animações Detalhadas

### 1. Entrada da Tela (TelaDetalhesServicoAceito)
```kotlin
// Sequência de animações
0ms   → Header: slideInVertically + fadeIn
200ms → Card de sucesso: scaleIn + fadeIn
300ms → Card cliente: slideInHorizontally (esquerda)
400ms → Card serviço: slideInHorizontally (direita)
500ms → Card localização: slideInVertically
600ms → Botão flutuante: slideInVertically + fadeIn
```

### 2. Ícone de Sucesso
```
[Pulso Contínuo]
Scale: 1.0 ↔ 1.1 (1000ms)
Glow: 0.3 ↔ 0.8 (1500ms)
```

### 3. Background Decorativo
```
Círculo 1: Rotação 360° (3000ms) + Blur 80dp
Círculo 2: Rotação -360° (4000ms) + Blur 60dp
```

### 4. Timeline de Status
```
Status Completo:  ● Verde sólido
Status Ativo:     ● Verde + Border pulsante
Status Futuro:    ○ Cinza transparente
Linha Completa:   ━ Verde
Linha Futura:     ━ Cinza
```

---

## 📐 Espaçamentos e Tamanhos

### Padding/Margin Padrão
```
Screen padding:    20dp
Card padding:      20-28dp
Between cards:     20dp
Icon size small:   20dp
Icon size medium:  24dp
Icon size large:   48dp
Button height:     56dp
```

### Border Radius
```
Cards:            20-24dp
Buttons:          12-16dp
Small elements:   12dp
Circles:          50% (CircleShape)
```

### Elevação
```
Cards:            2-8dp
Buttons:          8-12dp
Floating button:  12-16dp
Header:           8dp
```

---

## 🎯 Elementos Interativos

### Botões Primários
```
┌────────────────────────┐
│  🔵  Texto do Botão    │  ← Ícone + Texto
└────────────────────────┘
- Height: 56dp
- Corners: 12-16dp
- Background: Gradiente ou cor sólida
- Elevation: 8dp
- Font: Bold, 16-18sp
```

### Botões de Ação Rápida
```
  ┌────┐
  │ 📞 │  ← IconButton circular
  └────┘
- Size: 48dp
- Background: Cor com alpha 0.2
- Icon size: 24dp
```

### Cards de Informação
```
┌─────────────────────────┐
│ 🎯 Título do Card       │  ← Header com ícone
│ ─────────────────       │  ← Divider opcional
│                         │
│ Conteúdo...             │
│                         │
└─────────────────────────┘
- Border: 1dp, white 10%
- Background: Semi-transparente
- Shadow: Soft
```

---

## 🔄 Fluxo de Interação

### Jornada do Usuário

```
[Início] 
   ↓
[Lista de Serviços]
   ↓ [Aceitar]
[Tela Detalhes Serviço Aceito]
   ↓ [Visualizar informações]
   ↓ [Contato com cliente opcional]
   ↓ [Abrir navegação opcional]
   ↓ [Prosseguir]
[Tela Pedido em Andamento]
   ↓ [Cheguei no Local]
   ↓ [Iniciar Serviço]
   ↓ [Preparar Finalização]
   ↓ [Concluir Serviço]
   ↓ [Confirmar]
[Tela Início] + Toast de sucesso
```

### Ações Disponíveis por Tela

**Detalhes Serviço Aceito:**
- ← Voltar
- 📞 Ligar para cliente
- 💬 Abrir chat (em dev)
- 🧭 Abrir navegação
- ➡️ Prosseguir

**Pedido em Andamento:**
- ← Voltar (com confirmação)
- 📞 Ligar para cliente
- 💬 Abrir chat (em dev)
- 🧭 Abrir navegação
- ✓ Avançar status
- ✅ Concluir serviço

---

## 💡 Feedback Visual

### Estados dos Elementos

**Normal:**
- Opacidade: 100%
- Scale: 1.0

**Hover/Pressed:**
- Opacidade: 80%
- Scale: 0.95

**Disabled:**
- Opacidade: 50%
- Saturação: 0%

**Loading:**
- Circular Progress Indicator
- Shimmer effect nos cards

**Success:**
- Verde brilhante
- Ícone de check
- Animação de celebração

**Error:**
- Vermelho
- Ícone de alerta
- Mensagem descritiva

---

## 📱 Responsividade

### Breakpoints
```
Small:  < 360dp  → Padding reduzido
Medium: 360-600dp → Layout padrão
Large:  > 600dp  → Cards mais largos
```

### Adaptações
- Textos: Scaling automático
- Imagens: Aspect ratio mantido
- Cards: Width máximo de 600dp
- Padding: Proporcional ao tamanho

---

## 🎬 Transições Entre Telas

### Navegação Forward
```
[Tela A] ──────────────► [Tela B]
         Slide Right
         Fade In
         300ms
```

### Navegação Back
```
[Tela B] ◄────────────── [Tela A]
         Slide Left
         Fade Out
         300ms
```

---

**🎨 Design System: Futurista e Moderno**
**⚡ Performance: Otimizado**
**📱 Compatibilidade: Android 7.0+**

