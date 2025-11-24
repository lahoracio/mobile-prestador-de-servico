# 🔄 SOLUÇÃO TEMPORÁRIA - Perfil com Dados Mock

Se a API ainda não está implementada ou está com problemas, você pode usar esta versão temporária com dados mockados para testar a interface.

## Como usar:

### 1. Substituir temporariamente o ViewModel

Adicione este método ao `PerfilPrestadorViewModel.kt`:

```kotlin
fun carregarPerfilMock() {
    viewModelScope.launch {
        try {
            Log.d(TAG, "Carregando perfil MOCK para teste...")
            _uiState.value = PerfilUiState.Loading
            
            // Simula delay de rede
            kotlinx.coroutines.delay(1000)
            
            // Dados mockados
            val perfilMock = PerfilPrestadorResponse(
                id = 1,
                nome = "Prestador Teste",
                email = "prestador@teste.com",
                celular = "(11) 98765-4321",
                tipoConta = "prestador",
                status = "ativo",
                prestador = PrestadorInfo(
                    id = 1,
                    endereco = "Rua Teste, 123",
                    cidade = "São Paulo",
                    estado = "SP",
                    fotoPerfil = null
                )
            )
            
            _uiState.value = PerfilUiState.Success(perfilMock)
            Log.d(TAG, "Perfil MOCK carregado com sucesso!")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao carregar perfil mock", e)
            _uiState.value = PerfilUiState.Error("Erro: ${e.message}")
        }
    }
}
```

### 2. Usar o método mock na tela

No `TelaPerfilPrestador.kt`, trocar:

```kotlin
// De:
viewModel.carregarPerfil(context)

// Para:
viewModel.carregarPerfilMock()
```

### 3. Testar

Agora a tela deve abrir sem crash e mostrar dados de teste.

## ✅ Quando usar modo Mock

- ✅ Para testar a interface sem depender da API
- ✅ Para desenvolvimento offline
- ✅ Quando a API ainda não está pronta
- ✅ Para demonstrações

## ❌ Remover depois

Quando a API estiver funcionando:
1. Remover o método `carregarPerfilMock()`
2. Voltar a usar `carregarPerfil(context)`
3. Testar com dados reais

---

**Observação**: Esta é apenas uma solução temporária para testes!

