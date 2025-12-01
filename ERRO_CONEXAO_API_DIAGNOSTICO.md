# 🔴 ERRO DE CONEXÃO COM API - DIAGNÓSTICO E SOLUÇÕES

## 🐛 PROBLEMA IDENTIFICADO

### Erro:
```
java.net.ConnectException: Failed to connect to facilita-c6hhb9csgygudrdz.canadacentral-01.azurewebsites.net/20.48.204.7:443
Caused by: android.system.ErrnoException: isConnected failed: ECONNREFUSED (Connection refused)
```

### O que significa:
- ❌ Servidor Azure **recusando** conexões na porta 443 (HTTPS)
- ❌ Timeout após 60 segundos tentando conectar
- ❌ Erro tanto em IPv4 (20.48.204.7) quanto IPv6

### Causas possíveis:

1. **Servidor Fora do Ar** ⚠️
   - Azure App Service parado/em manutenção
   - Créditos esgotados
   - Plano gratuito expirado

2. **Problemas de Rede** 🌐
   - Firewall bloqueando
   - DNS não resolvendo
   - Problemas na Azure

3. **Configuração do App Service** ⚙️
   - App desligado
   - Slot de deployment errado
   - Configuração de SSL/TLS

---

## ✅ CORREÇÃO APLICADA NO CÓDIGO

### RetrofitFactory.kt - Retry Automático

#### ANTES:
```kotlin
private val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(60, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    // Sem retry automático
    .build()
```

#### AGORA:
```kotlin
private val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .retryOnConnectionFailure(true)  // ✅ Retry automático!
    .build()
```

**Benefícios**:
- ✅ Tenta reconectar automaticamente
- ✅ Timeout reduzido (30s em vez de 60s)
- ✅ Melhor experiência do usuário

---

## 🔍 COMO VERIFICAR O SERVIDOR

### 1. Teste Manual no Navegador

Abra no navegador:
```
https://facilita-c6hhb9csgygudrdz.canadacentral-01.azurewebsites.net/
```

**Resultado Esperado**:
- ✅ **Sucesso**: Página carrega (mesmo que seja erro 404 é bom sinal)
- ❌ **Erro**: "Site can't be reached" ou timeout

### 2. Teste com cURL (CMD/PowerShell)

```cmd
curl -I https://facilita-c6hhb9csgygudrdz.canadacentral-01.azurewebsites.net/
```

**Resultado Esperado**:
- ✅ **Sucesso**: Retorna headers HTTP (mesmo 404)
- ❌ **Erro**: "Could not resolve host" ou timeout

### 3. Verifique no Azure Portal

1. Acesse: https://portal.azure.com
2. Vá em **App Services**
3. Encontre: `facilita-c6hhb9csgygudrdz`
4. **Verifique**:
   - ✅ Status: **Running** (verde)
   - ❌ Status: **Stopped** (vermelho)

### 4. Logs do Azure

No Azure Portal → App Service → **Monitoring** → **Log stream**

Veja se há erros ou se o app está rodando.

---

## 🛠️ SOLUÇÕES POSSÍVEIS

### Solução 1: Reiniciar App Service (Azure)

**No Azure Portal**:
1. App Services → `facilita-c6hhb9csgygudrdz`
2. Clique em **Restart**
3. Aguarde 2-3 minutos
4. Teste novamente

### Solução 2: Verificar Plano/Créditos

**No Azure Portal**:
1. App Services → `facilita-c6hhb9csgygudrdz`
2. **Settings** → **Scale up (App Service plan)**
3. Verifique se há créditos disponíveis
4. Se necessário, upgrade ou adicione créditos

### Solução 3: Verificar Configurações SSL

**No Azure Portal**:
1. App Services → `facilita-c6hhb9csgygudrdz`
2. **Settings** → **TLS/SSL settings**
3. Verifique:
   - HTTPS Only: **On**
   - TLS Version: **1.2** (mínimo)

### Solução 4: Usar URL Alternativa (Temporário)

Se o problema persistir, você pode:

1. **Deploy em outro servidor** (Heroku, Railway, Vercel)
2. **Usar localhost** para testes (com ngrok)
3. **Mudar para outra região** do Azure

#### Como mudar URL no app:

Edite `RetrofitFactory.kt`:
```kotlin
private val retrofit: Retrofit = Retrofit.Builder()
   .baseUrl("https://SEU_NOVO_SERVIDOR.com/")  // ← Mudar aqui
   .client(okHttpClient)
   .addConverterFactory(GsonConverterFactory.create(gson))
    .build()
```

---

## 🧪 TESTE RÁPIDO DE CONECTIVIDADE

### Android App - Adicionar Ping Test

Você pode adicionar um botão de teste na tela de login:

```kotlin
// TelaLogin.kt
Button(onClick = {
    viewModel.testarConexao()
}) {
    Text("Testar Conexão")
}

// LoginViewModel.kt
fun testarConexao() {
    viewModelScope.launch {
        try {
            val response = userService.ping() // endpoint de teste
            Log.d("PING", "✅ Servidor OK: $response")
        } catch (e: Exception) {
            Log.e("PING", "❌ Servidor offline: ${e.message}")
        }
    }
}
```

---

## 📊 CHECKLIST DE DIAGNÓSTICO

Execute na ordem:

- [ ] 1. Teste no navegador (desktop)
- [ ] 2. Verifique Azure Portal (status do app)
- [ ] 3. Tente reiniciar o App Service
- [ ] 4. Verifique logs no Azure
- [ ] 5. Teste com cURL
- [ ] 6. Verifique firewall/antivírus
- [ ] 7. Teste em rede diferente (dados móveis)
- [ ] 8. Verifique créditos/plano do Azure

---

## 🔧 MELHORIAS NO CÓDIGO (JÁ APLICADAS)

### 1. Retry Automático ✅
```kotlin
.retryOnConnectionFailure(true)
```

### 2. Timeouts Reduzidos ✅
```kotlin
.connectTimeout(30, TimeUnit.SECONDS)  // Era 60s, agora 30s
```

### 3. Tratamento de Erro na UI (Próximo Passo)

Adicione um Toast/Dialog quando login falhar:

```kotlin
// LoginViewModel.kt
catch (e: Exception) {
    when (e) {
        is ConnectException -> {
            _errorMessage.value = "❌ Servidor offline. Tente novamente mais tarde."
        }
        is SocketTimeoutException -> {
            _errorMessage.value = "⏱️ Tempo esgotado. Verifique sua internet."
        }
        else -> {
            _errorMessage.value = "❌ Erro: ${e.message}"
        }
    }
}
```

---

## 💡 DICAS IMPORTANTES

### Para Testes em Desenvolvimento:

1. **Use emulador/dispositivo real na mesma rede**
2. **Configure ngrok** se backend local:
   ```bash
   ngrok http 8080
   ```
   Depois use a URL do ngrok no app

3. **Desabilite temporariamente HTTPS** (apenas dev):
   ```kotlin
   .baseUrl("http://SEU_IP:8080/")  // HTTP para testes
   ```

### Para Produção:

1. ✅ Sempre use HTTPS
2. ✅ Configure SSL válido
3. ✅ Monitore uptime (UptimeRobot, Pingdom)
4. ✅ Configure retry e fallback

---

## 🚨 MENSAGEM PARA O USUÁRIO

Quando der erro de conexão, mostre:

```
┌─────────────────────────────────────┐
│  ⚠️ Servidor Temporariamente        │
│     Indisponível                    │
│                                     │
│  Por favor, tente novamente em      │
│  alguns minutos.                    │
│                                     │
│  [Tentar Novamente] [Cancelar]     │
└─────────────────────────────────────┘
```

---

## 📱 STATUS ATUAL

### Código:
✅ **Retry automático** implementado
✅ **Timeouts otimizados**
⚠️ **Servidor Azure** precisa ser verificado

### Próximos Passos:

1. **Verifique o Azure Portal** (status do servidor)
2. **Reinicie o App Service** se necessário
3. **Teste a URL** no navegador
4. **Compile e teste** o app novamente

---

## 🔗 LINKS ÚTEIS

- Azure Portal: https://portal.azure.com
- Azure Status: https://status.azure.com
- Teste de DNS: https://www.whatsmydns.net
- Teste de SSL: https://www.ssllabs.com/ssltest/

---

**Data**: 01/12/2025  
**Status**: ⚠️ **PROBLEMA DE INFRAESTRUTURA**  
**Ação**: Verificar Azure Portal e reiniciar App Service

🔧 **O código está correto. O problema é no servidor Azure que precisa ser verificado/reiniciado.**

