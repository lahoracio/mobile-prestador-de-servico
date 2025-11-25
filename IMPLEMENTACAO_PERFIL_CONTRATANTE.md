# Implementação do Perfil do Contratante

## Resumo
Foi implementada a funcionalidade de carregar e exibir os dados do perfil do contratante a partir da API, extraindo as informações do JSON e populando os campos na tela de perfil.

## Arquivos Criados

### 1. DadosContratante.kt
**Caminho:** `app/src/main/java/com/exemple/facilita/model/DadosContratante.kt`

Modelo para dados específicos do contratante:
- id
- cpf
- necessidade
- localizacao (objeto Localizacao - já existia em Servico.kt)

### 2. PerfilContratanteResponse.kt
**Caminho:** `app/src/main/java/com/exemple/facilita/model/PerfilContratanteResponse.kt`

Modelo para a resposta completa da API:
```kotlin
data class PerfilContratanteResponse(
    val status_code: Int,
    val data: PerfilContratanteData
)

data class PerfilContratanteData(
    val id: Int,
    val nome: String,
    val email: String,
    val telefone: String,
    val foto_perfil: String?,
    val tipo_conta: String,
    val criado_em: String,
    val carteira: String?,
    val dados_contratante: DadosContratante
)
```

## Arquivos Modificados

### 1. UserService.kt
**Caminho:** `app/src/main/java/com/exemple/facilita/sevice/UserService.kt`

Adicionado endpoint para buscar perfil do contratante:
```kotlin
@GET("v1/facilita/usuario/{id}")
fun getPerfilContratante(
    @Path("id") id: Int,
    @Header("Authorization") token: String
): Call<PerfilContratanteResponse>
```

### 2. TelaPerfilPrestador.kt
**Caminho:** `app/src/main/java/com/exemple/facilita/screens/TelaPerfilPrestador.kt`

#### Mudanças implementadas:

1. **Importações adicionadas:**
   - `LocalContext`
   - `RetrofitFactory`
   - `TokenManager`
   - `PerfilContratanteData`
   - `kotlinx.coroutines.launch`

2. **Estados adicionados:**
   ```kotlin
   var perfilData by remember { mutableStateOf<PerfilContratanteData?>(null) }
   var isLoading by remember { mutableStateOf(true) }
   var errorMessage by remember { mutableStateOf<String?>(null) }
   ```

3. **Carregamento de dados:**
   - Implementado `LaunchedEffect` para carregar dados ao iniciar a tela
   - Obtém `userId` e `token` do `TokenManager`
   - Faz chamada à API usando `RetrofitFactory.userService`
   - Trata erros e estados de loading

4. **Exibição de dados dinâmicos:**
   Os campos agora exibem dados reais da API:
   - **Nome:** `perfilData?.nome`
   - **Cidade:** `perfilData?.dados_contratante?.localizacao?.cidade`
   - **Email:** `perfilData?.email`
   - **Telefone:** `perfilData?.telefone`

5. **Indicadores visuais:**
   - Loading spinner enquanto carrega
   - Mensagem de erro se falhar
   - Valores padrão caso dados não estejam disponíveis

## Exemplo de JSON Suportado

```json
{
    "status_code": 200,
    "data": {
        "id": 121,
        "nome": "Bueno ",
        "email": "bueno123@gmail.com",
        "telefone": "+551193990170",
        "foto_perfil": null,
        "tipo_conta": "CONTRATANTE",
        "criado_em": "2025-11-11T15:19:35.335Z",
        "carteira": null,
        "dados_contratante": {
            "id": 66,
            "cpf": "32087349053",
            "necessidade": "NENHUMA",
            "localizacao": {
                "id": 1,
                "logradouro": "Av. Paulista",
                "numero": "1000",
                "bairro": "Bela Vista",
                "cidade": "São Paulo",
                "cep": "01310-100",
                "latitude": "-23.564",
                "longitude": "-46.652"
            }
        }
    }
}
```

## Campos Extraídos e Exibidos

1. ✅ **Nome:** `data.nome` → Exibido no campo "Pessoa"
2. ✅ **Cidade:** `data.dados_contratante.localizacao.cidade` → Exibido no campo "Localização"
3. ✅ **Email:** `data.email` → Exibido no campo "Email"
4. ✅ **Telefone:** `data.telefone` → Exibido no campo "Telefone"

## Fluxo de Funcionamento

1. Usuário acessa a tela de perfil
2. Sistema obtém o ID do usuário e token de autenticação do `TokenManager`
3. Faz requisição GET para `/v1/facilita/usuario/{id}` com o token no header
4. API retorna os dados do perfil em formato JSON
5. Dados são parseados e armazenados no estado `perfilData`
6. Interface é atualizada automaticamente com os dados reais
7. Caso ocorra erro, mensagem é exibida ao usuário

## Tratamento de Erros

- ✅ Verifica se usuário está autenticado
- ✅ Valida se token está disponível
- ✅ Trata resposta de erro da API
- ✅ Captura exceções de rede
- ✅ Exibe mensagens de erro amigáveis
- ✅ Mostra loading durante requisição

## Próximos Passos (Opcional)

- [ ] Implementar refresh pull-to-refresh
- [ ] Adicionar cache local dos dados
- [ ] Implementar edição de campos
- [ ] Adicionar upload de foto de perfil
- [ ] Validação de campos editados

