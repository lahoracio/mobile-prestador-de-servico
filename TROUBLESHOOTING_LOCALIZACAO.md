# 🔧 TROUBLESHOOTING: Localização em Tempo Real

## 🐛 Problemas Identificados

### 1. **"Skipped 571 frames! The application may be doing too much work on its main thread"**
**Causa:** MapView sendo inicializado de forma pesada na Main Thread

**Solução Aplicada:**
- ✅ Otimizado AndroidView factory
- ✅ Adicionado update lifecycle
- ✅ Simplificado criação do mapa

---

### 2. **Localização do Contratante Não Aparece**
**Possíveis Causas:**
1. Contratante não está enviando localização
2. Evento WebSocket com nome diferente
3. Sala do serviço não está sendo criada corretamente

**Soluções Aplicadas:**
- ✅ Adicionado listener `onAnyEvent` para capturar TODOS os eventos
- ✅ Adicionado listener `joined_servico` para confirmar entrada na sala
- ✅ Logs detalhados quando localização é recebida
- ✅ Marcador do contratante adicionado imediatamente ao receber localização

---

## 🧪 Como Diagnosticar

### Passo 1: Verificar Conexão WebSocket

**O que procurar no Logcat:**
```
✅ Socket de localização conectado!
📤 user_connected enviado: {"userId":3,"userType":"prestador",...}
🔗 Entrou na sala do serviço: 23
```

**Se NÃO aparecer:**
- WebSocket não conectou
- Verificar URL do servidor
- Verificar internet

---

### Passo 2: Verificar Eventos Recebidos

**Novo log adicionado (captura TUDO):**
```
🔔 EVENTO RECEBIDO: connect
🔔 EVENTO RECEBIDO: user_connected
🔔 EVENTO RECEBIDO: joined_servico
🔔 EVENTO RECEBIDO: location_updated  ← ESSE É O IMPORTANTE!
```

**O que fazer:**
1. Abra o Logcat
2. Filtre por: `LocationSocketManager`
3. Procure por `🔔 EVENTO RECEBIDO`
4. Veja TODOS os eventos que estão chegando

**Cenários:**

#### ✅ Se aparecer `location_updated`:
- Perfeito! O evento está chegando
- Problema é no processamento

#### ❌ Se NÃO aparecer `location_updated`:
- **Causa:** Contratante não está enviando localização
- **Solução:** Verificar app do contratante

---

### Passo 3: Verificar GPS do Prestador

**O que procurar:**
```
✅ Rastreamento GPS iniciado
📍 Minha localização: -23.55, -46.63
📤 Enviando localização: Lat: -23.55, Lng: -46.63
```

**Se NÃO aparecer:**
- Permissão de localização negada
- GPS desligado
- Problema com FusedLocationProviderClient

---

### Passo 4: Verificar Busca de Rota

**O que procurar:**
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📍 LOCALIZAÇÃO DO CONTRATANTE RECEBIDA
   Nome: Kaike Bueno
   Lat: -23.56
   Lng: -46.64
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

🗺️ Iniciando busca de rota...
   Origem (você): -23.55, -46.63
   Destino (contratante): -23.56, -46.64

🗺️ Buscando rota de -23.55,-46.63 para -23.56,-46.64
✅ Rota encontrada com 1 opções
🎨 Desenhando rota no mapa...
✅ Rota desenhada: 5.2 km, 12 mins
```

**Se aparecer erro:**
```
❌ Sua localização ainda não está disponível. Aguardando GPS...
```
**Solução:** Aguardar GPS pegar sua localização primeiro

---

## 📋 Checklist de Diagnóstico

### ✅ Pré-requisitos:
- [ ] Permissão de localização concedida
- [ ] GPS ligado
- [ ] Internet conectada
- [ ] Google Maps API Key configurada

### ✅ WebSocket:
- [ ] Socket conectou (`Socket de localização conectado!`)
- [ ] Entrou na sala (`Entrou na sala do serviço: X`)
- [ ] Vê eventos chegando (`🔔 EVENTO RECEBIDO`)

### ✅ GPS Prestador:
- [ ] GPS iniciou (`Rastreamento GPS iniciado`)
- [ ] Localização detectada (`Minha localização: ...`)
- [ ] Enviando localização (`Enviando localização: ...`)

### ✅ Localização Contratante:
- [ ] Evento `location_updated` chegou
- [ ] Lat/Lng válidos (não 0, 0)
- [ ] Marcador adicionado no mapa

### ✅ Rota:
- [ ] Origem (prestador) disponível
- [ ] Destino (contratante) disponível
- [ ] Directions API chamada
- [ ] Rota desenhada no mapa

---

## 🔍 Comandos de Debug

### Filtrar Logcat por Tag:
```
adb logcat -s TelaAcompanhamento:D LocationSocketManager:D DirectionsService:D
```

### Ver TODOS os eventos WebSocket:
```
adb logcat | grep "🔔 EVENTO RECEBIDO"
```

### Ver localização recebida:
```
adb logcat | grep "📍 LOCALIZAÇÃO DO CONTRATANTE"
```

### Ver rota sendo desenhada:
```
adb logcat | grep "🎨 Desenhando rota"
```

---

## 🛠️ Soluções Rápidas

### Problema: "Skipped frames"
**Solução:** ✅ Já corrigido! MapView otimizado.

### Problema: Não recebe localização do contratante
**Passo 1:** Verificar se contratante está enviando
```kotlin
// No app do CONTRATANTE, verificar se ele está chamando:
locationManager.updateLocation(servicoId, lat, lng, userId)
```

**Passo 2:** Verificar nome do evento
```
// Procure no log:
🔔 EVENTO RECEBIDO: location_updated  ← Nome correto
🔔 EVENTO RECEBIDO: new_location      ← Nome diferente? Ajustar!
```

**Passo 3:** Verificar sala do serviço
```
// Prestador e contratante devem estar na MESMA sala
Prestador: join_servico("23")
Contratante: join_servico("23")  ← MESMO ID!
```

### Problema: Rota não aparece
**Causa 1:** Google Maps API Key inválida
```xml
<!-- Verificar: app/src/main/res/values/strings.xml -->
<string name="google_maps_key">SUA_API_KEY_AQUI</string>
```

**Causa 2:** Directions API não habilitada
- Ir em: https://console.cloud.google.com
- APIs & Services → Library
- Procurar: "Directions API"
- Clicar em "Enable"

**Causa 3:** Sua localização não está disponível
```
❌ Sua localização ainda não está disponível. Aguardando GPS...
```
**Solução:** Aguardar alguns segundos para GPS inicializar

---

## 📱 Teste Manual

### Teste 1: Ver Eventos WebSocket
1. Abra o app
2. Entre no mapa
3. Abra Logcat
4. Procure por: `🔔 EVENTO RECEBIDO`
5. Anote TODOS os eventos que aparecem

### Teste 2: Simular Localização do Contratante
Se você não tem o app do contratante, pode testar enviando manualmente:

1. Abra o código do `LocationSocketManager`
2. No `LaunchedEffect`, adicione teste:
```kotlin
// TESTE: Simular localização após 5 segundos
LaunchedEffect(Unit) {
    delay(5000) // Aguarda 5s
    
    // Simula localização do contratante
    val testLat = -23.56
    val testLng = -46.64
    
    onLocationUpdated(testLat, testLng, "Teste Contratante", "")
}
```

3. Se funcionar → Problema é que contratante não está enviando
4. Se NÃO funcionar → Problema é no código de processamento

---

## 🎯 Resultado Esperado

### Logs de Sucesso:
```
🚀 Conectando ao WebSocket de localização...
   ServicoId: 23
   UserId: 3

🔧 Configurando Socket.IO para localização...
✅ Socket criado com sucesso
🔌 Conectando ao WebSocket de localização...

✅ Socket de localização conectado!
📤 user_connected enviado: {"userId":3,...}
🔗 Entrou na sala do serviço: 23

✅ Rastreamento GPS iniciado
📍 Minha localização: -23.55, -46.63
📤 Enviando localização: Lat: -23.55

🔔 EVENTO RECEBIDO: location_updated
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📍 LOCALIZAÇÃO DO CONTRATANTE RECEBIDA
   Nome: Kaike Bueno
   Lat: -23.56
   Lng: -46.64
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📍 Adicionando marcador do contratante no mapa

🗺️ Iniciando busca de rota...
   Origem (você): -23.55, -46.63
   Destino (contratante): -23.56, -46.64

🗺️ Buscando rota de -23.55,-46.63 para -23.56,-46.64
✅ Rota encontrada com 1 opções

🎨 Desenhando rota no mapa...
✅ Rota desenhada: 5.2 km, 12 mins
```

---

## 📞 Próximo Passo

**Execute o app novamente e me envie os logs filtrados:**

```bash
adb logcat -s TelaAcompanhamento:D LocationSocketManager:D DirectionsService:D
```

**Com os logs, poderei ver exatamente:**
1. ✅ Se WebSocket conectou
2. ✅ Quais eventos estão chegando
3. ✅ Se localização está sendo recebida
4. ✅ Se rota está sendo calculada

---

**Data:** 2025-11-24  
**Status:** ⚠️ **AGUARDANDO LOGS PARA DIAGNÓSTICO**

