# 🐛 DEBUG DO CRASH - Tela de Perfil

## ✅ Correções Aplicadas

### 1. **Estado Inicial Seguro**
- Mudou de `Loading` para `Idle` no início
- Evita tentativas de carregamento antes da tela estar pronta

### 2. **Try-Catch Robusto**
- Adicionado tratamento de exceção no `LaunchedEffect`
- Captura erros de rede específicos (timeout, DNS, conexão)

### 3. **Logs Detalhados**
- Adicionados logs em todas as etapas do carregamento
- Facilita identificação do problema

### 4. **Proteção contra Multiple Loads**
- Flag `hasLoadedOnce` previne carregamentos duplicados

## 🔍 Como Debugar o Crash

### Passo 1: Capturar Logs
Execute o arquivo `capturar_logs_perfil.bat`:

```bash
cd C:\Users\joelm\StudioProjects\mobile-prestador-de-servico
capturar_logs_perfil.bat
```

### Passo 2: Reproduzir o Crash
1. Abra o app
2. Faça login
3. Clique no ícone de **Perfil** na barra inferior
4. Observe os logs no terminal

### Passo 3: Analisar Logs

#### Logs Esperados (sucesso):
```
D/TelaPerfilPrestador: Iniciando carregamento do perfil...
D/PerfilPrestadorViewModel: Iniciando carregamento do perfil...
D/PerfilPrestadorViewModel: Token obtido: presente
D/PerfilPrestadorViewModel: Fazendo requisição para API...
D/PerfilPrestadorViewModel: Resposta recebida - código: 200
D/PerfilPrestadorViewModel: Perfil carregado com sucesso: [Nome]
```

#### Possíveis Erros e Soluções:

##### Erro 1: Token não encontrado
```
W/PerfilPrestadorViewModel: Token não encontrado
```
**Solução**: Fazer logout e login novamente

##### Erro 2: UnknownHostException
```
E/PerfilPrestadorViewModel: Erro de DNS/Host não encontrado
```
**Solução**: Verificar conexão com internet

##### Erro 3: SocketTimeoutException
```
E/PerfilPrestadorViewModel: Timeout na conexão
```
**Solução**: API está demorando muito. Verificar se o servidor está online.

##### Erro 4: 404 Not Found
```
D/PerfilPrestadorViewModel: Resposta recebida - código: 404
```
**Solução**: Endpoint `/v1/facilita/usuario/perfil` não existe no backend

##### Erro 5: 500 Internal Server Error
```
D/PerfilPrestadorViewModel: Resposta recebida - código: 500
```
**Solução**: Erro no servidor. Verificar logs do backend.

## 🧪 Teste Manual

### Teste 1: Verificar se o token existe
1. Abra o app
2. Navegue para qualquer tela que funciona
3. Use `adb shell` para verificar SharedPreferences:

```bash
adb shell
run-as com.exemple.facilita
cat shared_prefs/user_prefs.xml
```

Deve mostrar algo como:
```xml
<string name="auth_token">eyJhbGc...</string>
```

### Teste 2: Testar endpoint manualmente
Use Postman ou curl:

```bash
curl -X GET "https://facilita-c6hhb9csgygudrdz.canadacentral-01.azurewebsites.net/v1/facilita/usuario/perfil" \
  -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

## 🔧 Possíveis Causas do Crash

### Causa 1: Endpoint não implementado
**Sintoma**: Erro 404
**Verificação**: Testar endpoint manualmente
**Solução**: Implementar endpoint no backend

### Causa 2: Modelo de dados incompatível
**Sintoma**: Erro de parsing JSON
**Verificação**: Ver logs com "JsonSyntaxException"
**Solução**: Ajustar modelo `PerfilPrestadorResponse`

### Causa 3: Token inválido/expirado
**Sintoma**: Erro 401
**Verificação**: Ver resposta "Unauthorized"
**Solução**: Fazer login novamente

### Causa 4: Falta de permissão de internet
**Sintoma**: Crash imediato
**Verificação**: AndroidManifest.xml tem `INTERNET` permission?
**Solução**: Já está configurado no projeto

### Causa 5: Problema de inicialização do ViewModel
**Sintoma**: Crash antes de fazer requisição
**Verificação**: Erro antes do log "Iniciando carregamento"
**Solução**: Já corrigido com estado `Idle`

## 📱 Teste Alternativo: Modo Mock

Se o endpoint não existir, podemos criar um modo de teste:

### Opção A: Usar dados mockados
Comentar a chamada da API e usar dados fixos temporariamente.

### Opção B: Verificar se existe outro endpoint
Talvez o backend use:
- `/api/usuario/perfil`
- `/usuario/perfil`
- `/v1/prestador/perfil`

## ✅ Checklist de Verificação

- [ ] App compila sem erros
- [ ] Consegue fazer login
- [ ] Token é salvo após login
- [ ] Outras telas funcionam normalmente
- [ ] Tem internet
- [ ] Logs aparecem quando clica em perfil
- [ ] Ver qual é o erro específico nos logs

## 🚀 Próximos Passos

1. **Execute o capturar_logs_perfil.bat**
2. **Reproduza o crash**
3. **Copie os logs** do terminal
4. **Compartilhe os logs** para análise detalhada

---

**Status**: Correções aplicadas. Aguardando logs do crash para diagnóstico preciso.

