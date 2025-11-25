# Implementação: Edição de Campos do Perfil

## ✨ Funcionalidade Implementada

Agora você pode clicar nos ícones de **pincel (editar)** ao lado dos campos do perfil para editá-los:
- ✏️ **Email**
- ✏️ **Telefone**
- ✏️ **Cidade**

## 🎨 Interface

### Dialog de Edição
Ao clicar no ícone de editar, abre um dialog bonito com:
- ✅ Título do campo sendo editado
- ✅ Campo de texto com o valor atual
- ✅ Botão "Salvar" (verde)
- ✅ Botão "Cancelar" (cinza)
- ✅ Design arredondado e moderno

## 🔧 Arquivos Criados

### 1. UpdatePerfilRequest.kt
**Caminho:** `app/src/main/java/com/exemple/facilita/model/UpdatePerfilRequest.kt`

```kotlin
data class UpdatePerfilRequest(
    val email: String? = null,
    val telefone: String? = null,
    val cidade: String? = null
)

data class UpdatePerfilResponse(
    val status_code: Int,
    val message: String,
    val data: PerfilContratanteData?
)
```

## 📝 Arquivos Modificados

### 1. UserService.kt

**Adicionado endpoint de atualização:**
```kotlin
@PUT("v1/facilita/usuario/perfil")
fun updatePerfilContratante(
    @Header("Authorization") token: String,
    @Body request: UpdatePerfilRequest
): Call<UpdatePerfilResponse>
```

### 2. TelaPerfilPrestador.kt

#### Estados Adicionados:
```kotlin
var showEditDialog by remember { mutableStateOf(false) }
var editingField by remember { mutableStateOf("") }
var editingValue by remember { mutableStateOf("") }
var editingTitle by remember { mutableStateOf("") }
```

#### Callbacks dos Botões de Editar:
```kotlin
PerfilInfoItem(
    icon = Icons.Default.Email,
    label = perfilData?.email ?: "Email não disponível",
    onEdit = {
        editingField = "email"
        editingValue = perfilData?.email ?: ""
        editingTitle = "Editar Email"
        showEditDialog = true
    }
)
```

#### Componente EditFieldDialog:
```kotlin
@Composable
fun EditFieldDialog(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
)
```

## 🎯 Fluxo de Funcionamento

```
1. Usuário clica no ícone de editar (pincel)
    ↓
2. Dialog aparece com o valor atual do campo
    ↓
3. Usuário edita o valor
    ↓
4. Usuário clica em "Salvar"
    ↓
5. Dialog fecha e mostra loading
    ↓
6. App faz requisição PUT para /v1/facilita/usuario/perfil
    ↓
7. API atualiza o dado no backend
    ↓
8. API retorna dados atualizados
    ↓
9. App atualiza o estado local com novos dados
    ↓
10. Interface é recomposta com valores atualizados
    ↓
11. Toast mostra "Perfil atualizado com sucesso!" ✅
```

## 📊 Requisição para API

### Endpoint:
```
PUT /v1/facilita/usuario/perfil
```

### Headers:
```
Authorization: Bearer {token}
Content-Type: application/json
```

### Body (exemplo para atualizar email):
```json
{
  "email": "novoemail@example.com"
}
```

### Body (exemplo para atualizar telefone):
```json
{
  "telefone": "+5511999999999"
}
```

### Body (exemplo para atualizar cidade):
```json
{
  "cidade": "Rio de Janeiro"
}
```

### Resposta Esperada (200 OK):
```json
{
  "status_code": 200,
  "message": "Perfil atualizado com sucesso",
  "data": {
    "id": 121,
    "nome": "Bueno ",
    "email": "novoemail@example.com",
    "telefone": "+5511999999999",
    "dados_contratante": {
      "localizacao": {
        "cidade": "Rio de Janeiro"
      }
    }
  }
}
```

## ✨ Recursos Implementados

### 1. Dialog Customizado
- ✅ Design bonito e moderno
- ✅ Cores do tema (verde #00A651)
- ✅ Cantos arredondados
- ✅ Campo de texto focado automaticamente

### 2. Validação e Feedback
- ✅ Loading enquanto salva
- ✅ Toast de sucesso
- ✅ Toast de erro com código/mensagem
- ✅ Tratamento de exceções

### 3. Atualização em Tempo Real
- ✅ Dados atualizados localmente após salvar
- ✅ Interface recomposta automaticamente
- ✅ Não precisa recarregar a tela

### 4. Segurança
- ✅ Token JWT enviado no header
- ✅ Requisição em background thread (Dispatchers.IO)
- ✅ Atualização de UI na Main thread
- ✅ Tratamento de erros de rede

## 🎨 Exemplo de Uso

### Editar Email:
1. Usuário clica no ícone de editar ao lado do email
2. Dialog aparece: "Editar Email"
3. Campo mostra: "bueno123@gmail.com"
4. Usuário altera para: "novoemail@gmail.com"
5. Clica em "Salvar"
6. Loading aparece brevemente
7. Toast: "Perfil atualizado com sucesso!"
8. Email na tela muda para: "novoemail@gmail.com"

### Editar Telefone:
1. Clica no ícone ao lado do telefone
2. Dialog: "Editar Telefone"
3. Campo: "+551193990170"
4. Altera para: "+5511988887777"
5. Salva
6. Toast de sucesso
7. Telefone atualizado na tela

### Editar Cidade:
1. Clica no ícone ao lado da cidade
2. Dialog: "Editar Cidade"
3. Campo: "São Paulo"
4. Altera para: "Rio de Janeiro"
5. Salva
6. Toast de sucesso
7. Cidade atualizada na tela

## 🔐 Requisitos da API

Para que a funcionalidade funcione completamente, a API precisa ter o endpoint:

```
PUT /v1/facilita/usuario/perfil
```

Com suporte para receber:
- `email` (string, opcional)
- `telefone` (string, opcional)
- `cidade` (string, opcional)

E retornar o objeto de perfil atualizado.

## ⚠️ Notas Importantes

### Se o endpoint ainda não existir na API:
1. Os dialogs funcionam normalmente
2. A validação local funciona
3. A requisição é enviada mas pode retornar erro
4. Toast mostrará a mensagem de erro

### Para desenvolvimento:
- Você pode comentar a chamada da API temporariamente
- Atualizar apenas os dados locais
- Descomentar quando a API estiver pronta

## 🎯 Próximas Melhorias (Opcionais)

- [ ] Validação de formato de email
- [ ] Máscara de telefone brasileira
- [ ] Autocomplete de cidades
- [ ] Animação no dialog
- [ ] Confirmação antes de salvar
- [ ] Desfazer alteração
- [ ] Histórico de mudanças

## ✅ Status: Implementado e Testável!

Tudo pronto! Agora você pode:
1. ✅ Clicar nos ícones de pincel
2. ✅ Editar os campos em dialogs bonitos
3. ✅ Salvar as alterações
4. ✅ Ver feedback visual
5. ✅ Dados são atualizados na API e na tela

**Teste agora!** 🎊

