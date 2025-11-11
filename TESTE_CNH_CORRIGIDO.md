# 🧪 Guia de Teste - CNH Corrigida

## ✅ Build Status: SUCESSO
Data: 11/01/2025
Tempo de build: 26 segundos

---

## 🚀 Como Testar a Correção

### 1️⃣ Preparação
Certifique-se de que:
- [ ] O app está instalado no dispositivo/emulador
- [ ] Você tem as credenciais de um prestador
- [ ] Há conexão com a internet

### 2️⃣ Passo a Passo

#### A. Fazer Login
```
1. Abra o app
2. Tela de Login
3. Email: kaikedodedao@gmail.com
4. Senha: (sua senha)
5. Clique em "Entrar"
```

#### B. Navegar para CNH
```
1. Na tela inicial → Menu lateral ou "Completar Perfil"
2. Clique em "CNH com EAR"
3. Você verá o formulário de cadastro de CNH
```

#### C. Preencher CNH
```
- Número da CNH: 12345678901 (11 dígitos)
- Categoria: B (ou AB, C, D, E)
- Validade: 2030-12-31 (formato YYYY-MM-DD)
- Possui EAR: Sim
- Pontuação: 10
```

#### D. Validar
```
1. Clique no botão "Validar CNH"
2. Aguarde a resposta (2-5 segundos)
3. Verifique a mensagem
```

### 3️⃣ Resultados Esperados

✅ **SUCESSO:**
```
- Mensagem: "CNH cadastrada com sucesso!"
- Retorna automaticamente para tela de perfil
- Item "CNH com EAR" aparece como validado
```

❌ **Se der erro:**
```
- "Token expirado ou inválido" → Faça logout e login novamente
- "Dados inválidos" → Verifique formato da data e número da CNH
- "Erro de conexão" → Verifique internet
```

---

## 🔍 Verificar Logs (Android Studio)

### Abrir Logcat:
1. Android Studio → Logcat (parte inferior)
2. Filtrar por: `TELA_CNH` ou `CNH_DEBUG`

### Logs Esperados ao Clicar em "Validar CNH":

```logcat
D/TELA_CNH: Token obtido: eyJhbGciOiJIUzI1NiI...
D/CNH_DEBUG: Iniciando cadastro de CNH
D/CNH_DEBUG: Token recebido (primeiros 20 chars): eyJhbGciOiJIUzI1NiI...
D/CNH_DEBUG: Enviando request: CNHRequest(numero_cnh=12345678901, categoria=B, validade=2030-12-31, possui_ear=true)
D/CNH_DEBUG: Header Authorization: Bearer eyJhbGciOiJIUzI...
D/CNH_DEBUG: Resposta recebida: CNHResponse(message=CNH cadastrada com sucesso, cnh=CNHData(...))
```

### Se Houver Erro (HTTP 401):

```logcat
E/CNH_ERROR: Erro HTTP 401: {"erro":"Token inválido ou expirado"}
```
**Solução:** Faça login novamente

---

## 🧪 Teste Avançado (Postman)

Se quiser testar a API diretamente:

### 1. Obter Token Manualmente:

```bash
POST https://servidor-facilita.onrender.com/v1/facilita/auth/login
Content-Type: application/json

{
  "email": "kaikedodedao@gmail.com",
  "senha": "sua_senha"
}
```

**Resposta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "usuario": {
    "id": 116,
    "nome": "Kaike",
    "email": "kaikedodedao@gmail.com",
    "tipo_conta": "PRESTADOR"
  }
}
```

### 2. Cadastrar CNH:

```bash
POST https://servidor-facilita.onrender.com/v1/facilita/prestador/cnh
Authorization: Bearer {TOKEN_DO_PASSO_1}
Content-Type: application/json

{
  "numero_cnh": "12345678901",
  "categoria": "B",
  "validade": "2030-12-31",
  "possui_ear": true
}
```

**Resposta Esperada (200 OK):**
```json
{
  "message": "CNH cadastrada com sucesso",
  "cnh": {
    "id": 1,
    "id_prestador": 116,
    "numero_cnh": "12345678901",
    "categoria": "B",
    "validade": "2030-12-31",
    "possui_ear": true,
    "pontuacao_atual": 0,
    "data_criacao": "2025-01-11T10:30:00.000Z"
  }
}
```

---

## ❓ Troubleshooting

### Problema: "Token está nulo ou vazio"
**Causa:** Token não foi salvo após login
**Solução:** 
1. Faça logout
2. Limpe os dados do app (Configurações → Apps → Facilita → Limpar dados)
3. Faça login novamente

### Problema: "Token expirado"
**Causa:** Tokens JWT expiram em ~8 horas
**Solução:** Faça login novamente

### Problema: "Erro de conexão"
**Causa:** Servidor pode estar lento (Render free tier)
**Solução:** Aguarde alguns segundos e tente novamente

### Problema: "Dados inválidos"
**Causa:** Formato incorreto dos campos
**Validar:**
- CNH: Exatamente 11 dígitos numéricos
- Data: Formato YYYY-MM-DD (ex: 2030-12-31)
- Categoria: Uma das opções (A, B, AB, C, D, E)

---

## 📊 Checklist Final

Antes de considerar testado:

- [ ] Login funciona e salva o token
- [ ] Token é recuperado corretamente na tela CNH
- [ ] Validações locais funcionam (11 dígitos, formato data)
- [ ] Requisição é enviada com header correto (Bearer token)
- [ ] Mensagem de sucesso é exibida
- [ ] Redirecionamento funciona após sucesso
- [ ] Erros são tratados adequadamente
- [ ] Logs aparecem no Logcat

---

## 📝 Notas Importantes

1. **Token não é mais duplicado:** A correção garante que o header seja `Bearer {token}` e não `Bearer Bearer {token}`

2. **Logs detalhados:** Todos os passos são logados para facilitar debug

3. **Integração completa:** O token usado é o mesmo do login/cadastro, não precisa inserir manualmente

4. **Validação no frontend:** O app valida os dados antes de enviar para API (economia de requisições)

---

## ✅ Status: PRONTO PARA TESTE
Build: Sucesso ✓
Correções: Aplicadas ✓
Logs: Implementados ✓

