# 🔐 Como Obter um Novo Token JWT

## ⚠️ Problema Atual
O token JWT está **EXPIRADO** (era válido até 06/11/2025).

Quando você tenta validar a CNH, o app mostra "Token inválido" porque o servidor rejeita tokens expirados.

---

## ✅ Solução: Obter um Novo Token

### Opção 1: Usando Postman (Recomendado)

1. **Abra o Postman** (ou baixe em: https://www.postman.com/downloads/)

2. **Configure a requisição:**
   - Método: `POST`
   - URL: `https://servidor-facilita.onrender.com/v1/facilita/auth/login`
   - Aba "Body" → Selecione "raw" e "JSON"

3. **Cole este JSON no Body:**
```json
{
  "email": "kaikedodedao@gmail.com",
  "senha": "SUA_SENHA_AQUI"
}
```
   ⚠️ **Substitua "SUA_SENHA_AQUI" pela senha real da conta**

4. **Clique em "Send"**

5. **Copie o token da resposta:**
   A resposta será algo assim:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MTE2LCJ0aXBvX2NvbnRhIjoiUFJFU1RBRE9SIiwiZW1haWwiOiJrYWlrZWRvZGVkYW9AZ21haWwuY29tIiwiaWF0IjoxNzMxMzI1MDA1LCJleHAiOjE3MzEzNTM4MDV9.NOVO_TOKEN_AQUI"
}
```

6. **Cole o novo token no arquivo TokenManager.kt:**
   - Abra: `app/src/main/java/com/exemple/facilita/utils/TokenManager.kt`
   - Linha 29: substitua o valor de `currentToken` pelo novo token
   - Salve o arquivo

---

### Opção 2: Usando curl (Terminal/CMD)

```bash
curl -X POST https://servidor-facilita.onrender.com/v1/facilita/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"kaikedodedao@gmail.com\",\"senha\":\"SUA_SENHA_AQUI\"}"
```

---

### Opção 3: Criar Tela de Login no App (Recomendado a Longo Prazo)

Em vez de atualizar manualmente o token toda vez, implemente uma tela de login que:

1. Faça o POST para `/v1/facilita/auth/login`
2. Salve o token no SharedPreferences ou DataStore
3. Atualize o TokenManager automaticamente

---

## 🔍 Como Verificar se o Token é Válido

1. Acesse: https://jwt.io/
2. Cole o token no campo "Encoded"
3. Verifique na seção "Payload":
   - `tipo_conta` deve ser `"PRESTADOR"`
   - `exp` (expiração) deve ser uma data **FUTURA**
   - `email` deve ser o correto

Exemplo de payload válido:
```json
{
  "id": 116,
  "tipo_conta": "PRESTADOR",
  "email": "kaikedodedao@gmail.com",
  "iat": 1731325005,
  "exp": 1731353805  ← Esta data deve ser no FUTURO
}
```

Para converter o timestamp `exp` para data legível:
- Acesse: https://www.epochconverter.com/
- Cole o número do `exp`
- Veja a data de expiração

---

## ✅ Após Obter o Novo Token

1. **Atualize o TokenManager.kt** com o novo token
2. **Compile e execute o app novamente**
3. **Tente validar a CNH** - agora deve funcionar!

---

## 📌 Arquivos Corrigidos

Os seguintes arquivos já foram corrigidos para melhorar o tratamento de erros:

✅ **TelaCNH.kt**: Agora valida o token antes de chamar a API
✅ **CNHViewModel.kt**: Mostra mensagens específicas para token expirado (erro 401)

Quando o token estiver expirado, você verá: 
> "Token expirado ou inválido. Faça login novamente."

---

## 🆘 Não Sabe a Senha?

Se você não tem a senha da conta `kaikedodedao@gmail.com`:

1. Verifique se há um endpoint de "esqueci minha senha"
2. Ou crie uma nova conta de teste:
   - Provavelmente: `POST /v1/facilita/auth/cadastro` ou similar
3. Ou entre em contato com quem configurou a API

---

## 📞 Suporte

Se continuar tendo problemas:
- Verifique se a API está online: https://servidor-facilita.onrender.com/
- Confira os logs do Logcat para erros detalhados
- Certifique-se de que está usando o email/senha corretos

