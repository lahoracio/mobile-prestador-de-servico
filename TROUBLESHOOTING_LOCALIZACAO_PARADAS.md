# 🔧 TROUBLESHOOTING: Localização em Tempo Real + Paradas

## 🐛 Problemas Reportados

### Problema 1: Localização não está sendo enviada em tempo real
❌ Sua localização não chega para o contratante

### Problema 2: Não mostra origem, paradas e destino
❌ Quando aceita serviço, não aparecem os pontos solicitados

---

## ✅ Correções Aplicadas

### 1. Logs de Debug para Envio de Localização

**Adicionado:**
```kotlin
// Verificar conexão antes de enviar
val connected = locationManager.isConnected()
if (connected != isConnected) {
    isConnected = connected
    Log.d("🔄 Status de conexão atualizado: $isConnected")
}

// Enviar com logs
if (isConnected) {
    Log.d("📤 Enviando localização via WebSocket...")
    locationManager.updateLocation(...)
    Log.d("✅ Localização enviada!")
} else {
    Log.e("❌ WebSocket não conectado")
}
```

### 2. Extração Completa de Origem, Paradas e Destino

**Agora extrai:**
- 🟢 ORIGEM (tipo = "ORIGEM")
- 🟠 PARADAS (tipo = "PARADA")
- 🔴 DESTINO (tipo = "DESTINO")

---

## 🧪 Como Testar

### Teste 1: Verificar se Localização Está Sendo Enviada

**Abra Logcat:**
```bash
adb logcat -s TelaAcompanhamento:D LocationSocketManager:D
```

**Execute o app e aguarde GPS detectar sua localização.**

**Você DEVE ver:**
```
📍 Minha localização atualizada: -23.55, -46.63
🔄 Status de conexão atualizado: true
📤 Enviando localização via WebSocket...
✅ Localização enviada!
```

**Se aparecer:**
```
❌ WebSocket não conectado, não enviou localização
```

**Significa:** WebSocket não está conectado!

---

### Teste 2: Verificar Origem, Paradas e Destino

**Logs esperados ao abrir mapa:**
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📋 EXTRAINDO PARADAS DO SERVIÇO
   Total de pontos: 4

   🟢 ORIGEM: Shopping Center (-23.55, -46.63)
   🟠 PARADA 1: Casa da amiga (-23.56, -46.64)
   🟠 PARADA 2: Farmácia (-23.57, -46.65)
   🔴 DESTINO: Hospital (-23.58, -46.66)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

✅ Destino definido: Hospital
✅ Serviço tem origem e destino, aguardando GPS para traçar rota...
```

**Se NÃO aparecer:**
- API não está retornando `paradas` no ServicoDetalhe
- Verificar resposta da API

---

## 🔍 Diagnóstico Passo a Passo

### Problema: Localização Não é Enviada

#### Passo 1: Verificar Conexão WebSocket
```
Procure no log:
✅ Socket de localização conectado!
```

**Se NÃO aparecer:**
- URL do servidor está incorreta
- Servidor offline
- Problema de internet

#### Passo 2: Verificar GPS
```
Procure no log:
✅ Rastreamento GPS iniciado
📍 Minha localização atualizada: ...
```

**Se NÃO aparecer:**
- Permissão de localização negada
- GPS desligado
- Aguardar alguns segundos

#### Passo 3: Verificar Status de Conexão
```
Procure no log:
🔄 Status de conexão atualizado: true
```

**Se aparecer `false`:**
- WebSocket desconectou
- Tentar reconectar

#### Passo 4: Verificar Envio
```
Procure no log:
📤 Enviando localização via WebSocket...
✅ Localização enviada!
```

**Se aparecer:**
```
❌ WebSocket não conectado
```
**Problema:** `isConnected` é `false`

---

### Problema: Não Mostra Origem, Paradas e Destino

#### Causa 1: API não retorna `paradas`

**Verificar:**
```bash
adb logcat | grep "EXTRAINDO PARADAS"
```

**Se NÃO aparecer nada:**
- `servicoDetalhe.paradas` é `null` ou vazio
- API não está enviando campo `paradas`

**Solução:** Verificar resposta da API:
```
GET /v1/facilita/servico/{id}

Deve retornar:
{
  "data": {
    "id": 23,
    "paradas": [
      {"tipo": "ORIGEM", "lat": -23.55, "lng": -46.63, ...},
      {"tipo": "PARADA", "lat": -23.56, "lng": -46.64, ...},
      {"tipo": "DESTINO", "lat": -23.58, "lng": -46.66, ...}
    ]
  }
}
```

#### Causa 2: ServicoDetalhe não está sendo carregado

**Verificar no MainActivity:**
```kotlin
LaunchedEffect(servicoId) {
    servicoViewModel.carregarServico(servicoId, context)
}
```

**Procure no log:**
```
🔍 CARREGANDO SERVIÇO
   ServicoId: 23

📡 Status Code: 200
✅ Serviço carregado da API com sucesso
```

**Se NÃO aparecer:**
- ServicoViewModel não está sendo chamado
- Erro ao carregar serviço

---

## 📋 Checklist de Diagnóstico

### WebSocket e Envio de Localização

Execute e marque:

- [ ] Vê: `✅ Socket de localização conectado!`
- [ ] Vê: `✅ Rastreamento GPS iniciado`
- [ ] Vê: `📍 Minha localização atualizada: ...`
- [ ] Vê: `🔄 Status de conexão atualizado: true`
- [ ] Vê: `📤 Enviando localização via WebSocket...`
- [ ] Vê: `✅ Localização enviada!`

**Se TODOS marcados:** ✅ Localização está sendo enviada!

**Se faltou algum:** Use os logs para identificar onde parou

---

### Origem, Paradas e Destino

Execute e marque:

- [ ] Vê: `📋 EXTRAINDO PARADAS DO SERVIÇO`
- [ ] Vê: `Total de pontos: X` (X > 0)
- [ ] Vê: `🟢 ORIGEM: ...`
- [ ] Vê: `🟠 PARADA 1: ...` (se houver paradas)
- [ ] Vê: `🔴 DESTINO: ...`
- [ ] Vê marcadores no mapa (verde, laranja, vermelho)
- [ ] Vê rota azul conectando os pontos

**Se TODOS marcados:** ✅ Paradas estão sendo exibidas!

**Se faltou algum:** Verificar API

---

## 🚨 Problemas Comuns

### Problema: "WebSocket não conectado"

**Causa:** Socket desconectou ou nunca conectou

**Solução 1:** Verificar URL
```kotlin
// LocationSocketManager.kt
private const val SOCKET_URL = "wss://facilita-...azurewebsites.net"
```

**Solução 2:** Verificar servidor
- Testar URL no browser
- Ver se servidor está online

**Solução 3:** Reiniciar app
- Fechar completamente
- Abrir novamente

---

### Problema: "API não retorna paradas"

**Causa:** Campo `paradas` não está na resposta da API

**Solução:** Adicionar paradas no backend

**Exemplo de resposta correta:**
```json
{
  "status_code": 200,
  "data": {
    "id": 23,
    "paradas": [
      {
        "id": 1,
        "ordem": 1,
        "tipo": "ORIGEM",
        "lat": -23.55052,
        "lng": -46.633308,
        "descricao": "Shopping Center",
        "endereco_completo": "Av. Paulista, 1000"
      },
      {
        "id": 2,
        "ordem": 2,
        "tipo": "PARADA",
        "lat": -23.56052,
        "lng": -46.643308,
        "descricao": "Casa da amiga"
      },
      {
        "id": 3,
        "ordem": 3,
        "tipo": "DESTINO",
        "lat": -23.58052,
        "lng": -46.663308,
        "descricao": "Hospital"
      }
    ]
  }
}
```

---

### Problema: GPS Não Detecta Localização

**Causa 1:** Permissão negada
- Configurações → Apps → Facilita → Permissões → Localização → Sempre

**Causa 2:** GPS desligado
- Ativar GPS nas configurações do celular

**Causa 3:** Esperando satélite
- Aguardar 10-30 segundos
- Ir para área aberta (fora de prédios)

---

## 🎯 Próximos Passos

### 1. Execute o App

### 2. Abra Logcat Filtrado:
```bash
adb logcat -s TelaAcompanhamento:D LocationSocketManager:D ServicoViewModel:D
```

### 3. Aceite um Serviço e Entre no Mapa

### 4. Me Envie os Logs Mostrando:

**Para Problema 1 (Envio de Localização):**
```
- Linha: "Socket de localização conectado"
- Linha: "Minha localização atualizada"
- Linha: "Status de conexão atualizado"
- Linha: "Enviando localização" OU "WebSocket não conectado"
```

**Para Problema 2 (Paradas):**
```
- Linha: "EXTRAINDO PARADAS DO SERVIÇO"
- Linha: "Total de pontos: X"
- Linhas: "ORIGEM", "PARADA", "DESTINO"
```

**Com os logs, identifico exatamente qual é o problema!**

---

**Data:** 2025-11-24  
**Status:** ⚠️ **AGUARDANDO LOGS PARA DIAGNÓSTICO**

**Correções Aplicadas:**
- ✅ Logs detalhados para envio de localização
- ✅ Verificação constante de conexão WebSocket
- ✅ Extração completa de origem, paradas e destino
- ✅ Logs detalhados de todas as paradas

