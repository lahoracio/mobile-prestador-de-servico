# 🧪 GUIA DE TESTE - API Correta Implementada

## ✅ Status: Pronto para Teste

**Build:** ✅ Sucesso  
**Endpoint:** `POST /v1/facilita/prestador`  
**APK:** `app/build/outputs/apk/debug/app-debug.apk`

---

## ⚠️ IMPORTANTE: NOVO CADASTRO OBRIGATÓRIO

### Por quê precisa ser novo cadastro?

Se você já tinha cadastro antes desta correção:
- ❌ Seu usuário não está registrado como prestador no backend
- ❌ O token antigo não tem `tipo_conta`
- ❌ CNH ainda vai dar erro 404

**Solução:** Fazer NOVO cadastro com outro email!

---

## 📱 PASSO A PASSO COMPLETO

### 1️⃣ INSTALAR APK

```bash
# Localização do APK:
app/build/outputs/apk/debug/app-debug.apk

# Instalar via ADB:
adb install app/build/outputs/apk/debug/app-debug.apk

# Ou arrastar o APK para o emulador
```

---

### 2️⃣ FAZER NOVO CADASTRO

**Abrir app → Tela de Cadastro**

Preencher:
```
Nome: Teste Prestador API
Email: teste_prestador_api_2025@gmail.com
Senha: 123456
CPF: 12345678901
Telefone: 11999999999
```

**Clicar em "Cadastrar"**

**✅ Esperado:**
- Toast: "Usuário cadastrado com sucesso!"
- Navega automaticamente para: **Tela Permissão de Localização**

---

### 3️⃣ ACEITAR PERMISSÕES DE LOCALIZAÇÃO

**Tela:** "Seja bem-vindo!"

**Clicar em "Permitir"**

**O que acontece:**
1. Android solicita permissões de localização
   - ✅ Aceitar "Permitir enquanto usa o app"
   
2. Android solicita ativar GPS (se desativado)
   - ✅ Aceitar ativar GPS
   
3. **Loading aparece** no botão "Permitir"
   - ⏳ Aguardar (2-5 segundos)

**✅ Esperado:**
- Loading some
- Toast: "Prestador criado com sucesso!"
- Navega para: **Tela Tipo de Veículo**

---

### 4️⃣ VERIFICAR LOGS (Android Studio)

**Abrir:** Android Studio → Logcat

**Filtrar por:** `PRESTADOR_DEBUG`

**Logs esperados:**
```logcat
D/PERMISSAO_LOC: GPS ativado, obtendo localização...
D/PERMISSAO_LOC: Localização obtida: [-23.5489, -46.6388]
D/PERMISSAO_LOC: Token: eyJhbGciOiJIUzI1NiI...
D/PRESTADOR_DEBUG: Iniciando criação de prestador
D/PRESTADOR_DEBUG: Token: eyJhbGciOiJIUzI1NiI...
D/PRESTADOR_DEBUG: Localização: [-23.5489, -46.6388]
D/PRESTADOR_DEBUG: Resposta: Prestador criado com sucesso!
D/PRESTADOR_DEBUG: Novo token recebido: eyJhbGciOiJIUzI1NiI...
D/PERMISSAO_LOC: Novo token salvo: eyJhbGciOiJIUzI1NiI...
```

**❌ Se aparecer erro:**
```logcat
E/PRESTADOR_ERROR: Erro HTTP 401: Token inválido
→ Token expirou, fazer novo cadastro

E/PRESTADOR_ERROR: Erro HTTP 500: Erro no servidor
→ Servidor pode estar fora do ar, aguardar

E/PERMISSAO_LOC: Location é null
→ GPS não conseguiu obter localização, tentar novamente
```

---

### 5️⃣ CONTINUAR FLUXO ATÉ CNH

#### A. Tipo de Veículo
- Escolher: **Moto** (ou Carro/Bicicleta)
- Clicar: "Continuar"

#### B. Informações do Veículo
Preencher:
```
Marca: Honda
Modelo: CG 160
Ano: 2023
Placa: ABC1D23
```
- Clicar: "Cadastrar Modalidade"

#### C. Completar Perfil
- Tela aparece com menu de itens
- Clicar: **"CNH com EAR"**

---

### 6️⃣ CADASTRAR CNH (MOMENTO DA VERDADE! 🎯)

**Tela:** Cadastro de CNH

Preencher:
```
Número da CNH: 12345678901 (11 dígitos)
Categoria: B
Validade: 2030-12-31
Possui EAR: Sim
Pontuação: 10
```

**Clicar: "Validar CNH"**

**✅ ESPERADO (SUCESSO):**
- Toast: "CNH cadastrada com sucesso!"
- Volta para: Tela Completar Perfil
- Item "CNH com EAR" aparece como: ✅ Validado

**❌ SE DER ERRO:**

**Erro 404:**
```
"Prestador não encontrado. Certifique-se de ter escolhido 'Prestador de Serviço' no tipo de conta."
```
**Causa:** Usuário não foi criado como prestador no backend  
**Solução:** 
1. Verificar logs: `PRESTADOR_DEBUG`
2. Confirmar que API foi chamada na tela de permissão
3. Se não foi, fazer novo cadastro e tentar novamente

**Erro 401:**
```
"Token expirado ou inválido. Faça login novamente."
```
**Causa:** Token expirou (8 horas)  
**Solução:** Fazer login novamente

**Erro 400:**
```
"Dados inválidos. Verifique as informações da CNH."
```
**Causa:** Formato incorreto dos dados  
**Solução:** 
- CNH: Apenas 11 dígitos numéricos
- Data: Formato YYYY-MM-DD

---

## 🔍 CHECKLIST DE VALIDAÇÃO

### Durante o Cadastro:
- [ ] ✅ Cadastro retorna token
- [ ] ✅ Token é salvo no SharedPreferences
- [ ] ✅ Navega para tela de permissão

### Durante Permissão de Localização:
- [ ] ✅ Permissões são aceitas
- [ ] ✅ GPS é ativado
- [ ] ✅ Loading aparece no botão
- [ ] ✅ Logs mostram: "Localização obtida"
- [ ] ✅ Logs mostram: "Prestador criado com sucesso!"
- [ ] ✅ Logs mostram: "Novo token recebido"
- [ ] ✅ Logs mostram: "Novo token salvo"
- [ ] ✅ Navega para tela de tipo de veículo

### Durante Cadastro de CNH:
- [ ] ✅ Formulário é preenchido
- [ ] ✅ Logs mostram: "Iniciando cadastro de CNH"
- [ ] ✅ Logs mostram: "Header Authorization: Bearer ..."
- [ ] ✅ Logs **NÃO** mostram: "Bearer Bearer" (duplicado)
- [ ] ✅ Logs mostram: "Resposta recebida: CNHResponse"
- [ ] ✅ Toast: "CNH cadastrada com sucesso!"
- [ ] ✅ Volta para tela de completar perfil
- [ ] ✅ Item CNH marcado como validado

---

## 📊 CENÁRIOS DE TESTE

### ✅ Cenário 1: Fluxo Completo (Caminho Feliz)
```
1. Novo cadastro → Token salvo ✅
2. Permissão GPS → API chamada ✅
3. Prestador criado → Novo token salvo ✅
4. Tipo veículo → Escolhido ✅
5. Info veículo → Cadastrado ✅
6. CNH → Cadastrada com sucesso ✅
```

### ⚠️ Cenário 2: Usuário Antigo
```
1. Login com usuário antigo
2. Tenta cadastrar CNH
3. ❌ Erro 404: Prestador não encontrado
4. Solução: Fazer novo cadastro
```

### ⚠️ Cenário 3: GPS Desativado
```
1. Novo cadastro
2. Permissão GPS → GPS desativado
3. Android solicita ativar
4. ✅ Usuário ativa → API é chamada
5. ❌ Usuário não ativa → Não avança
```

### ⚠️ Cenário 4: Sem Internet
```
1. Novo cadastro
2. Permissão GPS → Sem conexão
3. ❌ Erro: "Erro de conexão. Verifique sua internet."
4. Solução: Conectar internet e tentar novamente
```

### ⚠️ Cenário 5: Token Expirado
```
1. Cadastro feito há 8+ horas
2. Tenta cadastrar CNH
3. ❌ Erro 401: Token expirado
4. Solução: Fazer login novamente
```

---

## 🐛 TROUBLESHOOTING

### 🔴 Problema: Loading infinito na tela de permissão

**Causa:** API não está respondendo ou erro na chamada

**Debug:**
1. Verificar Logcat: `PRESTADOR_ERROR`
2. Ver mensagem de erro específica
3. Verificar se servidor está online: https://servidor-facilita.onrender.com/

**Soluções:**
- Servidor fora: Aguardar alguns minutos (Render free tier)
- Erro 401: Token inválido, fazer novo cadastro
- Erro 500: Problema no backend, contatar suporte

---

### 🔴 Problema: GPS não obtém localização

**Causa:** GPS não consegue triangular posição

**Debug:**
```logcat
E/PERMISSAO_LOC: Location é null
```

**Soluções:**
1. Sair para rua (GPS precisa ver céu)
2. Usar emulador com localização mockada:
   - Android Studio → Extended Controls → Location
   - Definir lat/lng manualmente
3. Verificar permissões no Android

---

### 🔴 Problema: CNH ainda dá erro 404

**Causa:** API não foi chamada na tela de permissão

**Debug:**
1. Verificar logs: `PRESTADOR_DEBUG`
2. Procurar: "Prestador criado com sucesso!"
3. Se não encontrar → API não foi chamada

**Soluções:**
1. Fazer NOVO cadastro (não usar usuário antigo)
2. Aceitar todas as permissões
3. Aguardar API processar (loading)
4. Verificar se novo token foi salvo

---

## ✅ CONFIRMAÇÃO DE SUCESSO

### Você saberá que funcionou quando:

1. **Logs mostram:**
   ```
   D/PRESTADOR_DEBUG: Prestador criado com sucesso!
   D/PRESTADOR_DEBUG: Novo token recebido: ...
   D/PERMISSAO_LOC: Novo token salvo: ...
   ```

2. **Toast aparece:**
   ```
   "Prestador criado com sucesso!"
   ```

3. **Navega automaticamente** para tipo de veículo

4. **CNH cadastra sem erro:**
   ```
   "CNH cadastrada com sucesso!"
   ```

5. **Item CNH fica verde** na tela de completar perfil

---

## 📞 REPORTAR RESULTADO

Após testar, favor reportar:

### ✅ Se funcionou:
```
✅ SUCESSO!
- Prestador criado: SIM
- Novo token salvo: SIM
- CNH cadastrada: SIM
- Sem erros
```

### ❌ Se deu erro:
```
❌ ERRO
- Erro encontrado: [código HTTP e mensagem]
- Logs: [copiar logs do Logcat]
- Passo onde parou: [ex: "Loading infinito na permissão"]
```

---

**Data:** 11/01/2025  
**Versão:** API Correta v1.0  
**Status:** ✅ Pronto para Teste

