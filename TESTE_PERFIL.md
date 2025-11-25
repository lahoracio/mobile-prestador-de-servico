# 🧪 Guia de Teste - Perfil do Prestador

## ✅ Pré-requisitos

Antes de testar, certifique-se de que:
- ✅ O app está compilando sem erros
- ✅ Você tem um usuário de teste cadastrado
- ✅ O backend está rodando e acessível
- ✅ O dispositivo/emulador tem conexão com internet

---

## 📱 Passo a Passo para Testar

### 1️⃣ Fazer Login
1. Abra o aplicativo
2. Faça login com suas credenciais de teste
3. Aguarde o token ser salvo

### 2️⃣ Navegar para o Perfil
1. Na tela inicial, clique no ícone de **Perfil** na barra inferior
2. A tela de perfil deve ser aberta

### 3️⃣ Observar o Carregamento
Você verá 3 possíveis estados:

#### 🔄 Estado 1: Loading (Carregando)
```
┌─────────────────────────────┐
│                             │
│     🔄 Spinner Verde        │
│                             │
└─────────────────────────────┘
```
- **O que está acontecendo**: Requisição sendo enviada para a API
- **Tempo esperado**: 1-3 segundos

#### ✅ Estado 2: Success (Sucesso)
```
┌─────────────────────────────┐
│         Perfil              │
│      [Foto de Perfil]       │
│                             │
│  📋 Informações             │
│  • Nome: oiii               │
│  • Localização: Carapicuíba │
│  • Email: oiii@gmail.com    │
│  • Telefone: +5511961900111 │
│  • Documentos: 0            │
│  • Status: Inativo ⭕       │
│                             │
│  ⚙️ Configurações           │
│  • Alterar Senha            │
│  • Notificações             │
│  • Sair                     │
└─────────────────────────────┘
```
- **O que verificar**:
  - ✅ Nome está correto
  - ✅ Email está correto
  - ✅ Telefone está no formato internacional
  - ✅ Status mostra "Ativo" ou "Inativo"
  - ✅ Localização aparece se houver alguma cadastrada

#### ❌ Estado 3: Error (Erro)
```
┌─────────────────────────────┐
│    ⚠️ Erro ao carregar      │
│    [Mensagem de erro]       │
│                             │
│    [Tentar Novamente]       │
└─────────────────────────────┘
```
- **Possíveis erros e soluções**:

| Erro | Causa | Solução |
|------|-------|---------|
| "Token não encontrado" | Não fez login | Faça login novamente |
| "Sessão expirada" | Token expirou | Faça login novamente |
| "Endpoint não encontrado" | Backend com problema | Verifique o backend |
| "Erro de conexão" | Sem internet | Verifique a conexão |

---

## 🐛 Debug - Como Verificar os Logs

### Abrir o Logcat no Android Studio
1. Na parte inferior, clique em **Logcat**
2. No campo de filtro, digite: `PerfilPrestadorViewModel`
3. Execute o app e navegue para o perfil

### O que você deve ver nos logs:

```log
╔═══════════════════════════════════════╗
║   INICIANDO CARREGAMENTO DO PERFIL   ║
╚═══════════════════════════════════════╝

📋 PASSO 1: Verificando token...
✅ Token encontrado: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   Tamanho do token: 200 caracteres

🌐 PASSO 2: Fazendo requisição HTTP...
   URL Base: https://facilita-c6hhb9csgygudrdz.canadacentral-01.azurewebsites.net/
   Endpoint: GET /v1/facilita/usuario/perfil
   Header: Authorization: Bearer eyJhbG...
   Thread atual: DefaultDispatcher-worker-1

📡 PASSO 3: Resposta recebida
   Código HTTP: 200
   Mensagem: OK
   Sucesso: true
   Body é null: false

✅ SUCESSO! Dados recebidos:
╔════════════════════════════════════════
║ Status Code: 200
║ ID: 33
║ Nome: oiii
║ Email: oiii@gmail.com
║ Telefone: +5511961900111
║ Tipo Conta: PRESTADOR
║ Foto Perfil: NULL
║ Criado Em: 2025-11-25T19:19:45.378Z
║ Dados Prestador:
║   - ID: 9
║   - Ativo: false
║   - Documentos: 0
║   - CNH: 0
║   - Modalidades: 0
║   - Localizações: 2
║   Localizações:
║     [0] R. Vila, Lobos - 43 - Vila Olivina
║         Carapicuíba - CEP: 06365800
║     [1] Carapicuiba - Vila, Dirce - Carapicuíba - State of São Paulo
║         Brazil - CEP: 00000000
╚════════════════════════════════════════

═══════════════════════════════════════
    FIM DO CARREGAMENTO DO PERFIL
═══════════════════════════════════════
```

---

## 🧪 Cenários de Teste

### ✅ Teste 1: Carregamento Bem-sucedido
**Passos**:
1. Faça login
2. Navegue para o perfil
3. Aguarde o carregamento

**Resultado esperado**:
- ✅ Loading aparece por 1-3 segundos
- ✅ Dados do perfil são exibidos
- ✅ Informações estão corretas

---

### ✅ Teste 2: Token Expirado
**Passos**:
1. Faça login
2. Espere 1 hora (ou force expiração)
3. Navegue para o perfil

**Resultado esperado**:
- ❌ Mensagem: "Sessão expirada. Faça login novamente."
- ✅ Botão "Tentar Novamente" aparece
- ✅ Clicar nele mantém o erro (token ainda expirado)

---

### ✅ Teste 3: Sem Internet
**Passos**:
1. Desative Wi-Fi e dados móveis
2. Navegue para o perfil

**Resultado esperado**:
- ❌ Mensagem de erro de conexão
- ✅ Botão "Tentar Novamente" aparece
- ✅ Reative internet e clique em "Tentar Novamente"
- ✅ Dados são carregados com sucesso

---

### ✅ Teste 4: Múltiplas Localizações
**Passos**:
1. Certifique-se de ter 2+ localizações cadastradas
2. Navegue para o perfil
3. Observe qual localização é exibida

**Resultado esperado**:
- ✅ Primeira localização da lista é exibida
- ✅ Formato: "Cidade - Bairro"

---

### ✅ Teste 5: Prestador Ativo vs Inativo
**Passos**:
1. Teste com prestador `ativo: true`
2. Teste com prestador `ativo: false`

**Resultado esperado**:
- ✅ Ativo: Ícone verde ✅ + texto "Conta Ativa" (verde)
- ✅ Inativo: Ícone cinza ⭕ + texto "Conta Inativa" (cinza)

---

### ✅ Teste 6: Sem Localizações
**Passos**:
1. Use um prestador com `localizacoes: []`
2. Navegue para o perfil

**Resultado esperado**:
- ✅ Item "Localização" não aparece
- ✅ Outros dados aparecem normalmente
- ✅ Sem crashes

---

### ✅ Teste 7: Dados Prestador NULL
**Passos**:
1. Use uma conta CONTRATANTE (não prestador)
2. Navegue para o perfil

**Resultado esperado**:
- ✅ Nome, email, telefone aparecem
- ✅ Items específicos de prestador não aparecem
- ✅ Sem crashes

---

## 🔍 Checklist de Verificação

Após os testes, verifique:

- [ ] Dados são carregados da API (não hardcoded)
- [ ] Loading aparece durante requisição
- [ ] Erros são tratados com mensagem clara
- [ ] Token é enviado no header Authorization
- [ ] Logs detalhados aparecem no Logcat
- [ ] Status ativo/inativo é exibido corretamente
- [ ] Localização aparece se existir
- [ ] Telefone está no formato correto
- [ ] Scroll funciona para conteúdo longo
- [ ] Botão "Tentar Novamente" funciona
- [ ] UI não trava durante carregamento

---

## 🚨 Problemas Comuns e Soluções

### ❌ Problema: "Token não encontrado"
**Causa**: Usuário não fez login ou token foi apagado
**Solução**: Fazer login novamente

### ❌ Problema: "Endpoint não encontrado" (404)
**Causa**: Backend não tem o endpoint `/v1/facilita/usuario/perfil`
**Solução**: Verificar se o backend está atualizado

### ❌ Problema: Dados não aparecem mas não há erro
**Causa**: Parsing falhou silenciosamente
**Solução**: Verificar logs para ver a estrutura da resposta

### ❌ Problema: App trava ao abrir perfil
**Causa**: Operação de rede na Main Thread
**Solução**: Verificar se está usando `Dispatchers.IO`

### ❌ Problema: Localização não aparece
**Causa**: Lista de localizações vazia
**Solução**: Normal se o prestador não cadastrou localizações

---

## 📊 Validação de Dados

Compare os dados na tela com os dados no Logcat:

| Campo | Na Tela | No Log |
|-------|---------|--------|
| Nome | "oiii" | `║ Nome: oiii` |
| Email | "oiii@gmail.com" | `║ Email: oiii@gmail.com` |
| Telefone | "+5511961900111" | `║ Telefone: +5511961900111` |
| Status | "Conta Inativa" | `║   - Ativo: false` |

---

## ✅ Teste Passou?

Se todos os itens acima funcionaram corretamente, a integração está **100% funcional**! 🎉

---

## 📝 Próximos Passos

Após validar que está funcionando:
1. ✅ Testar em diferentes dispositivos
2. ✅ Testar com conexão lenta
3. ✅ Implementar cache de dados (opcional)
4. ✅ Adicionar pull-to-refresh (opcional)
5. ✅ Implementar edição de perfil

