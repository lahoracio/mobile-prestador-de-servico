# 🧪 Guia de Teste - Perfil do Prestador

## ⚙️ Pré-requisitos

1. ✅ Fazer login no app como prestador
2. ✅ Ter um token JWT válido salvo
3. ✅ Conexão com a API funcionando

## 📱 Como testar

### 1. Testar Carregamento (GET)

1. Abra o app e faça login
2. Navegue para a tela de **Perfil** (ícone de perfil na barra inferior)
3. **Esperado**: 
   - Loading spinner verde aparece
   - Dados do perfil são carregados da API
   - Nome, email, telefone, cidade/estado aparecem

### 2. Testar Edição de Email (PUT)

1. Na tela de perfil, clique no ícone de **editar** ao lado do email
2. Digite um novo email
3. Clique em **Salvar**
4. **Esperado**:
   - Snackbar verde: "Email atualizado com sucesso!"
   - Email atualiza na tela automaticamente

### 3. Testar Edição de Telefone (PUT)

1. Clique no ícone de **editar** ao lado do telefone
2. Digite um novo telefone
3. Clique em **Salvar**
4. **Esperado**:
   - Snackbar verde: "Telefone atualizado com sucesso!"
   - Telefone atualiza na tela

### 4. Testar Edição de Endereço (PUT)

1. Clique no ícone de **editar** ao lado do endereço
2. Digite um novo endereço
3. Clique em **Salvar**
4. **Esperado**:
   - Snackbar verde: "Endereço atualizado com sucesso!"
   - Endereço atualiza na tela

### 5. Testar Edição de Localização (PUT)

1. Clique no ícone de **editar** ao lado da cidade/estado
2. Digite no formato: "São Paulo/SP"
3. Clique em **Salvar**
4. **Esperado**:
   - Snackbar verde: "Localização atualizada com sucesso!"
   - Cidade/Estado atualizam na tela

### 6. Testar Erros

#### Teste 1: Token expirado
1. Se o token expirar (401), deve mostrar:
   - ❌ "Sessão expirada. Faça login novamente."
   - Botão "Tentar Novamente"

#### Teste 2: Sem conexão
1. Desative o WiFi/dados
2. Tente carregar o perfil
3. **Esperado**:
   - ❌ "Erro de conexão: [mensagem]"
   - Botão "Tentar Novamente"

#### Teste 3: Cancelar edição
1. Clique em editar qualquer campo
2. Clique em **Cancelar**
3. **Esperado**:
   - Dialog fecha
   - Nada é alterado

## 🔍 Debug via Logcat

### Verificar requisições GET:
```
adb logcat | grep -i "GET.*perfil"
```

### Verificar requisições PUT:
```
adb logcat | grep -i "PUT.*perfil"
```

### Ver resposta completa:
```
adb logcat | grep -i "PerfilPrestador"
```

## 📊 Dados esperados da API

### Endpoint GET:
```
GET /v1/facilita/usuario/perfil
Headers: Authorization: Bearer [token]
```

### Resposta esperada:
```json
{
  "id": 1,
  "nome": "Nome do Prestador",
  "email": "email@exemplo.com",
  "celular": "(11) 98765-4321",
  "tipo_conta": "prestador",
  "status": "ativo",
  "prestador": {
    "id": 1,
    "endereco": "Rua ABC, 123",
    "cidade": "São Paulo",
    "estado": "SP",
    "foto_perfil": null
  }
}
```

### Endpoint PUT:
```
PUT /v1/facilita/usuario/perfil
Headers: 
  Authorization: Bearer [token]
  Content-Type: application/json
Body:
{
  "email": "novoemail@exemplo.com"
  // ou qualquer outro campo
}
```

## ✅ Checklist de Validação

- [ ] Loading aparece ao carregar
- [ ] Dados carregam corretamente
- [ ] Nome não tem ícone de editar
- [ ] Email tem ícone de editar e funciona
- [ ] Telefone tem ícone de editar e funciona
- [ ] Endereço tem ícone de editar e funciona
- [ ] Cidade/Estado tem ícone de editar e funciona
- [ ] Snackbar verde aparece no sucesso
- [ ] Snackbar vermelho aparece no erro
- [ ] Botão "Cancelar" fecha dialog
- [ ] Botão "Salvar" desabilitado se campo vazio
- [ ] Logout limpa token e volta para login
- [ ] Erro 401 mostra mensagem apropriada

## 🐛 Possíveis problemas

### Problema 1: "Token não encontrado"
**Causa**: Usuário não está logado
**Solução**: Fazer login novamente

### Problema 2: "Erro 401"
**Causa**: Token expirado
**Solução**: Fazer login novamente

### Problema 3: Dados não aparecem
**Causa**: API não retorna dados de prestador
**Solução**: Verificar se o usuário logado é realmente um prestador

### Problema 4: PUT não funciona
**Causa**: Endpoint pode não estar implementado no backend
**Solução**: Verificar logs do backend

## 📞 API Endpoints Utilizados

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/v1/facilita/usuario/perfil` | Obter dados do perfil |
| PUT | `/v1/facilita/usuario/perfil` | Atualizar dados do perfil |

## 🎯 Comportamento Esperado

1. **Ao abrir a tela**: Loading → Carrega dados → Mostra perfil
2. **Ao editar campo**: Dialog abre → Edita → Salva → Atualiza automaticamente
3. **Em caso de erro**: Mostra mensagem → Botão para tentar novamente
4. **Ao fazer logout**: Limpa token → Volta para tela de login

---

**Importante**: Certifique-se de que o backend tem esses endpoints implementados antes de testar!

