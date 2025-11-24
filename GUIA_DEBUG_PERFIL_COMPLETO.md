# 🔍 DEBUG COMPLETO - PERFIL NÃO MOSTRA DADOS

## 🎯 OBJETIVO
Descobrir EXATAMENTE por que o perfil não está mostrando os dados do usuário.

---

## 📋 PREPARAÇÃO

### 1. Compile o App
```bash
.\compilar.bat
```

### 2. Abra Dois Terminais

**Terminal 1 - Logs Gerais**:
```bash
adb logcat -c
adb logcat -s PerfilPrestadorViewModel:* -v time
```

**Terminal 2 - Logs HTTP (Retrofit)**:
```bash
adb logcat -s OkHttp:* -v time
```

---

## 🧪 TESTE PASSO A PASSO

### PASSO 1: Fazer Login
1. Abra o app
2. Faça login com suas credenciais
3. **AGUARDE** aparecer a tela inicial

### PASSO 2: Verificar Token nos Logs
No Terminal 1, procure por:
```
LOGIN_DEBUG: Token recebido: eyJhbGci...
LOGIN_DEBUG: Nome do usuário: [nome]
LOGIN_DEBUG: Email salvo: [email]
```

✅ **Se aparecer**: Token foi salvo corretamente  
❌ **Se NÃO aparecer**: Problema no login, não no perfil

### PASSO 3: Clicar no Perfil
1. Clique no ícone **Perfil** na barra inferior
2. **OBSERVE** os logs no Terminal 1

---

## 📊 ANÁLISE DOS LOGS

### Cenário 1: Token Não Encontrado
```
❌ ERRO: Token é NULL!
```

**CAUSA**: Usuário não está logado ou token não foi salvo  
**SOLUÇÃO**: Faça login novamente

---

### Cenário 2: HTTP 404 (Not Found)
```
📡 Resposta HTTP: 404
║ Mensagem: Not Found
```

**CAUSA**: O endpoint `/v1/facilita/usuario/perfil` não existe no backend  
**SOLUÇÃO**: Verifique se o backend tem este endpoint implementado

**Teste manual no Postman/Insomnia**:
```
GET https://facilita-c6hhb9csgygudrdz.canadacentral-01.azurewebsites.net/v1/facilita/usuario/perfil
Authorization: Bearer SEU_TOKEN_AQUI
```

**Endpoints alternativos que podem funcionar**:
- `/v1/facilita/usuario/me`
- `/v1/facilita/usuario/{id}`
- `/v1/facilita/perfil`
- `/api/usuario/perfil`

---

### Cenário 3: HTTP 401 (Unauthorized)
```
📡 Resposta HTTP: 401
║ Mensagem: Unauthorized
```

**CAUSA**: Token inválido, expirado ou formato errado  
**SOLUÇÃO**: 
1. Verifique se o token tem o prefixo "Bearer "
2. Faça login novamente
3. Verifique configuração do backend

---

### Cenário 4: HTTP 500 (Server Error)
```
📡 Resposta HTTP: 500
║ Mensagem: Internal Server Error
```

**CAUSA**: Erro no backend (banco de dados, código, etc)  
**SOLUÇÃO**: Verifique os logs do backend

---

### Cenário 5: HTTP 200 mas Body NULL
```
📡 Resposta HTTP: 200
   Sucesso: true
   Body é null: true
```

**CAUSA**: API retorna 200 mas sem dados  
**SOLUÇÃO**: Backend deve retornar os dados do usuário

---

### Cenário 6: HTTP 200 com Dados
```
✅ SUCESSO! Dados recebidos:
║ Nome: João Silva
║ Email: joao@email.com
║ Celular: (11) 98765-4321
```

**RESULTADO**: ✅ API funcionou! Se ainda não aparece na tela, o problema é na UI.

---

## 🔧 DIAGNÓSTICO RÁPIDO

Execute este comando e copie o resultado:
```bash
adb logcat -d | findstr "PerfilPrestadorViewModel Token HTTP"
```

Me envie a saída completa!

---

## 🌐 TESTE MANUAL DA API

### Com cURL (Windows PowerShell):
```powershell
# Substitua SEU_TOKEN pelo token real
$token = "Bearer SEU_TOKEN_AQUI"
$headers = @{
    "Authorization" = $token
    "Content-Type" = "application/json"
}

Invoke-RestMethod -Uri "https://facilita-c6hhb9csgygudrdz.canadacentral-01.azurewebsites.net/v1/facilita/usuario/perfil" -Headers $headers -Method Get
```

### Resposta Esperada:
```json
{
  "id": 123,
  "nome": "João Silva",
  "email": "joao@email.com",
  "celular": "(11) 98765-4321",
  "tipo_conta": "PRESTADOR",
  "status": "ativo",
  "prestador": {
    "id": 456,
    "endereco": "Rua ABC",
    "cidade": "São Paulo",
    "estado": "SP"
  }
}
```

---

## 🎯 CHECKLIST DE DEBUG

Ao clicar no perfil, verificar nos logs:

- [ ] Token foi encontrado?
- [ ] Token tem formato correto (Bearer ...)?
- [ ] Requisição HTTP foi feita?
- [ ] Qual código HTTP retornou? (200, 401, 404, 500)
- [ ] Body da resposta tem dados?
- [ ] Dados foram parseados corretamente?
- [ ] Estado mudou para Success?

---

## 🚨 PROBLEMAS COMUNS E SOLUÇÕES

### 1. "Token é NULL"
```bash
# Verificar se token foi salvo no login
adb shell "run-as com.exemple.facilita cat /data/data/com.exemple.facilita/shared_prefs/user_prefs.xml"
```

### 2. "HTTP 404"
O endpoint não existe. Opções:
- Implementar no backend
- Mudar para endpoint correto no app

### 3. "HTTP 401"
Token inválido. Verifique:
- Token está com "Bearer " na frente?
- Token não está expirado?
- Backend valida corretamente?

### 4. "Erro de conexão"
```
Exception: Unable to resolve host
```
- Verificar internet do celular/emulador
- Verificar se backend está online
- Ping no servidor

---

## 📝 PRÓXIMOS PASSOS

1. **Execute o app**
2. **Faça login**
3. **Clique no perfil**
4. **Copie TODOS os logs** do Terminal 1
5. **Me envie os logs completos**

Vou analisar e dizer exatamente qual é o problema!

---

## ⚡ COMANDO ÚNICO PARA DEBUG

Execute isso e me envie o resultado completo:
```bash
# Limpa logs, abre app, aguarda 5 segundos, captura tudo
adb logcat -c ; timeout 60 ; adb logcat -d -s PerfilPrestadorViewModel:* OkHttp:* LOGIN_DEBUG:* TokenManager:*
```

**INSTRUÇÕES**:
1. Execute o comando
2. Aguarde aparecer "timeout"
3. Faça login no app
4. Clique no perfil
5. Aguarde 5 segundos
6. Logs aparecerão automaticamente
7. Copie TUDO e me envie!

