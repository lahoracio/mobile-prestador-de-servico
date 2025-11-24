# 🧪 Guia de Teste: Ver Detalhes do Serviço

## 📋 Pré-requisitos

1. ✅ App instalado no dispositivo/emulador
2. ✅ Usuário logado como prestador
3. ✅ Ao menos um serviço aceito e em andamento

## 🔍 Passos para Testar

### Teste 1: Navegação da Tela de Serviços

1. **Abra o app** e faça login como prestador
2. **Navegue** para a tela "Serviços" (ícone na bottom bar)
3. **Verifique** se aparecem cards de serviços em andamento
4. **Clique** em qualquer card de serviço
5. **Resultado esperado:** 
   - Deve abrir a tela de detalhes do serviço
   - Deve mostrar informações do cliente (nome, telefone)
   - Deve mostrar categoria e descrição do serviço
   - Deve mostrar valor do serviço
   - Deve ter botões "Ligar" e "Chat ao vivo"

### Teste 2: Verificar Logs no Logcat

Após clicar em "Ver detalhes", filtre o Logcat por `ServicoViewModel`:

**Logs esperados:**
```
D/ServicoViewModel: 🔍 Carregando serviço ID: 123
D/ServicoViewModel: ✅ Serviço encontrado no cache
```

**OU (primeira vez):**
```
D/ServicoViewModel: 🔍 Carregando serviço ID: 123
D/ServicoViewModel: 📡 Serviço não está no cache, buscando da API...
D/ServicoViewModel: ✅ Serviço carregado da API com sucesso
```

**Logs de erro (se houver problema):**
```
E/ServicoViewModel: ❌ Erro ao carregar serviço: 404
E/ServicoViewModel: ❌ Token não encontrado
E/ServicoViewModel: ❌ Exceção ao carregar serviço: ...
```

### Teste 3: Performance do Cache

1. **Abra** os detalhes de um serviço (primeira vez)
2. **Volte** para a tela de serviços
3. **Abra** novamente os detalhes do mesmo serviço
4. **Resultado esperado:**
   - Primeira vez: pode demorar um pouco (busca da API)
   - Segunda vez: deve ser instantâneo (usa cache)

### Teste 4: Funcionalidades na Tela de Detalhes

Quando os detalhes aparecerem:

1. **Botão Ligar:**
   - Clique e verifique se abre o discador do telefone
   - Número deve estar preenchido

2. **Botão Chat ao vivo:**
   - Clique e verifique se abre o chat
   - Deve carregar conversa com o cliente

3. **Botão "Iniciar navegação"** (se houver localização):
   - Deve abrir app de mapas com destino

## ❌ Problemas Comuns e Soluções

### Problema: "Serviço não encontrado"

**Possíveis causas:**
1. Serviço não está no cache E context não foi passado
2. Serviço foi deletado do backend
3. ID do serviço está incorreto

**Solução:**
- Verifique os logs do `ServicoViewModel`
- Certifique-se que o serviço existe na API
- Teste com um serviço que você acabou de aceitar

### Problema: Tela fica em loading infinito

**Possíveis causas:**
1. Token expirado
2. API não responde
3. Erro de rede

**Solução:**
- Verifique o Logcat por erros de rede
- Faça logout e login novamente
- Verifique se a API está online

### Problema: Aparece erro 401 Unauthorized

**Causa:** Token expirado ou inválido

**Solução:**
1. Faça logout
2. Faça login novamente
3. Teste novamente

## 📊 Comandos de Debug

### Ver todos os logs do app
```bash
adb logcat | findstr "com.exemple.facilita"
```

### Ver apenas logs do ServicoViewModel
```bash
adb logcat | findstr "ServicoViewModel"
```

### Ver requisições HTTP
```bash
adb logcat | findstr "OkHttp"
```

### Limpar cache do app (para testar sem cache)
```bash
adb shell pm clear com.exemple.facilita
```

## ✅ Checklist de Validação

- [ ] Detalhes aparecem quando clico no card
- [ ] Nome do cliente aparece corretamente
- [ ] Telefone do cliente aparece
- [ ] Categoria do serviço aparece
- [ ] Descrição do serviço aparece
- [ ] Valor do serviço aparece
- [ ] Botão "Ligar" funciona
- [ ] Botão "Chat" funciona
- [ ] Cache funciona (segunda vez é mais rápido)
- [ ] Não há crashes
- [ ] Não há erros no Logcat

## 🎯 Métricas de Sucesso

✅ **100% OK** = Todos os itens do checklist passam
⚠️ **Parcial** = Alguns itens falharam
❌ **Falhou** = Não abre a tela de detalhes

---

**Última atualização:** 24/11/2025
**Versão testada:** Debug
**Plataforma:** Android

