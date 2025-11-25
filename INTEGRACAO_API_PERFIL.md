# Integração da API de Perfil do Prestador

## 📋 Resumo das Alterações

Este documento descreve as alterações realizadas para integrar os dados do perfil do prestador vindos da API.

## 🔧 Estrutura da API

A API retorna os dados no seguinte formato:

```json
{
  "status_code": 200,
  "data": {
    "id": 33,
    "nome": "oiii",
    "email": "oiii@gmail.com",
    "telefone": "+5511961900111",
    "foto_perfil": null,
    "tipo_conta": "PRESTADOR",
    "criado_em": "2025-11-25T19:19:45.378Z",
    "carteira": null,
    "dados_prestador": {
      "id": 9,
      "ativo": false,
      "documentos": [],
      "cnh": [],
      "modalidades": [],
      "localizacoes": [
        {
          "id": 1,
          "logradouro": "R. Vila",
          "numero": "Lobos",
          "bairro": "43 - Vila Olivina",
          "cidade": "Carapicuíba",
          "cep": "06365800",
          "latitude": "-23.5480849",
          "longitude": "-46.8469512"
        }
      ]
    }
  }
}
```

## 📁 Arquivos Modificados

### 1. `PerfilPrestador.kt` (Model)

**Localização**: `app/src/main/java/com/exemple/facilita/model/PerfilPrestador.kt`

**Mudanças**:
- ✅ Criados novos modelos de dados que correspondem exatamente à estrutura da API:
  - `PerfilPrestadorResponse`: Resposta completa com `status_code` e `data`
  - `PerfilPrestadorData`: Dados do usuário prestador
  - `DadosPrestador`: Informações específicas do prestador
  - `Documento`: Documentos cadastrados
  - `CNH`: Dados da CNH
  - `Modalidade`: Modalidades de serviço
  - `LocalizacaoPrestador`: Endereços/localizações do prestador
  - `AtualizarPerfilRequest`: Request para atualizar perfil
  - `AtualizarPerfilResponse`: Resposta da atualização

**Principais campos**:
- `nome`: Nome do prestador
- `email`: E-mail do prestador
- `telefone`: Telefone no formato internacional
- `foto_perfil`: URL da foto (pode ser null)
- `tipo_conta`: Tipo da conta (PRESTADOR/CONTRATANTE)
- `dados_prestador.ativo`: Se o prestador está ativo
- `dados_prestador.localizacoes`: Lista de endereços cadastrados
- `dados_prestador.documentos`: Lista de documentos
- `dados_prestador.cnh`: Lista de CNHs
- `dados_prestador.modalidades`: Modalidades de serviço

### 2. `PerfilPrestadorViewModel.kt` (ViewModel)

**Localização**: `app/src/main/java/com/exemple/facilita/viewmodel/PerfilPrestadorViewModel.kt`

**Mudanças**:
- ✅ Atualizado para trabalhar com a nova estrutura de dados
- ✅ O método `carregarPerfil()` agora processa `apiResponse.data` ao invés do objeto direto
- ✅ Logs detalhados para debug incluindo:
  - Status code da API
  - Dados do prestador
  - Lista de localizações
  - Quantidade de documentos, CNHs e modalidades
- ✅ `PerfilUiState.Success` agora usa `PerfilPrestadorData` ao invés de `PerfilPrestadorResponse`
- ✅ Método `atualizarPerfil()` corrigido para usar `telefone` ao invés de `celular`

### 3. `TelaPerfilPrestador.kt` (UI)

**Localização**: `app/src/main/java/com/exemple/facilita/screens/TelaPerfilPrestador.kt`

**Mudanças**:
- ✅ Integração completa com o `PerfilPrestadorViewModel`
- ✅ Estados de UI implementados:
  - **Loading**: Mostra CircularProgressIndicator durante carregamento
  - **Error**: Exibe mensagem de erro com botão "Tentar Novamente"
  - **Success**: Exibe os dados do perfil vindos da API
- ✅ Dados dinâmicos exibidos:
  - Nome do prestador (não editável)
  - Localização (primeira da lista, se existir)
  - E-mail
  - Telefone
  - Quantidade de documentos
  - Status ativo/inativo com ícone colorido
- ✅ Carregamento automático ao abrir a tela via `LaunchedEffect`
- ✅ Scroll vertical para conteúdo longo

## 🎯 Fluxo de Dados

```
API → PerfilPrestadorResponse → ViewModel → UI State → TelaPerfilPrestador
```

1. **API retorna** estrutura com `status_code` e `data`
2. **ViewModel processa** e extrai `data` (PerfilPrestadorData)
3. **UI State** guarda apenas os dados necessários
4. **Tela renderiza** os dados dinamicamente

## 📊 Mapeamento de Campos na Tela

| Campo na Tela | Campo na API | Observação |
|--------------|--------------|------------|
| Nome | `data.nome` | Não editável |
| Localização | `data.dados_prestador.localizacoes[0]` | Primeira localização |
| E-mail | `data.email` | Editável |
| Telefone | `data.telefone` | Editável |
| Documentos | `data.dados_prestador.documentos.size` | Quantidade |
| Status Ativo | `data.dados_prestador.ativo` | Com ícone verde/cinza |

## 🔐 Autenticação

O token JWT é obtido automaticamente através do `TokenManager`:
```kotlin
val token = TokenManager.obterTokenComBearer(context)
```

O token é passado no header `Authorization` para o endpoint:
```
GET /v1/facilita/usuario/perfil
```

## 🐛 Debug

Para visualizar os logs detalhados, filtrar por:
```
Tag: PerfilPrestadorViewModel
```

Os logs incluem:
- ✅ Verificação de token
- ✅ URL e headers da requisição
- ✅ Resposta HTTP completa
- ✅ Dados parseados
- ✅ Localizações cadastradas
- ✅ Erros detalhados

## ✅ Próximos Passos (Opcional)

1. **Múltiplas Localizações**: Adicionar tela para visualizar todas as localizações
2. **Edição de Perfil**: Implementar formulário de edição usando `atualizarPerfil()`
3. **Upload de Foto**: Implementar seleção e upload de foto de perfil
4. **Documentos**: Tela para visualizar e adicionar documentos
5. **Modalidades**: Tela para gerenciar modalidades de serviço

## 📝 Notas Importantes

- ⚠️ O campo `foto_perfil` pode ser `null`, então sempre verificar antes de usar
- ⚠️ A lista de `localizacoes` pode estar vazia, usar `.firstOrNull()` com segurança
- ⚠️ O `dados_prestador` pode ser `null` para contas que não são de prestador
- ✅ Todos os campos são deserializados automaticamente pelo Gson
- ✅ Anotações `@SerializedName` garantem compatibilidade com snake_case da API

## 🎨 UI/UX

- **Loading**: Spinner verde centralizado
- **Erro**: Mensagem vermelha com botão de retry
- **Sucesso**: Cards brancos com informações organizadas
- **Status Ativo**: Ícone verde para ativo, cinza para inativo
- **Scroll**: Conteúdo rolável para telas pequenas

## 🔄 Atualização de Dados

Para recarregar os dados do perfil:
```kotlin
viewModel.carregarPerfil(context)
```

Isso pode ser chamado:
- Na abertura da tela (automático via LaunchedEffect)
- Após atualização do perfil
- No botão "Tentar Novamente" em caso de erro
- Em pull-to-refresh (se implementado)

