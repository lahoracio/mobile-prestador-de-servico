# Fluxo de Cadastro de Modalidades (Tipo de Veículo)

## 📋 Resumo
Sistema completo para o prestador escolher até 2 tipos de veículos, preencher informações detalhadas e enviar para a API.

## 🔄 Fluxo de Navegação

1. **TelaCompletarPerfilPrestador** → Clica em "Informações do veículo"
2. **TelaTipoVeiculo** → Escolhe 1 ou 2 tipos de veículo (MOTO, CARRO, BICICLETA)
3. **TelaInformacoesVeiculo** → Preenche informações de cada veículo selecionado
4. **Envio para API** → Cadastra as modalidades
5. **Volta para TelaCompletarPerfilPrestador** → Marca "Informações do veículo" como ✅ (verde)

## 📁 Arquivos Modificados/Criados

### 1. `model/ModalidadeRequest.kt` ✅
- **Atualizado** para aceitar objetos completos `ModalidadeItem`
- Campos: `tipo`, `modelo_veiculo`, `ano_veiculo`, `possui_seguro`, `compartimento_adequado`, `revisao_em_dia`, `antecedentes_criminais`

### 2. `screens/TelaTipoVeiculo.kt` ✅
- **Criado** do zero
- Permite selecionar até 2 tipos de veículos
- Cards interativos para MOTO, CARRO e BICICLETA
- Validação de limite de seleção
- Navegação para `TelaInformacoesVeiculo` passando os tipos selecionados

### 3. `screens/TelaInformacoesVeiculo.kt` ✅
- **Atualizado** para receber parâmetros `tiposVeiculo`
- Formulário dinâmico para cada veículo selecionado
- Navegação entre veículos (Anterior/Próximo)
- Integração com `ModalidadeViewModel`
- Marca documento como validado no `PerfilViewModel`
- Volta automaticamente para `TelaCompletarPerfilPrestador` após sucesso

### 4. `viewmodel/ModalidadeViewModel.kt` ✅
- **Atualizado** para aceitar `List<ModalidadeItem>`
- Gerencia estados: `mensagem` e `modalidadesCadastradas`
- Tratamento de erros (HTTP, Conexão, Genérico)

### 5. `MainActivity.kt` ✅
- **Adicionado** rota `tela_tipo_veiculo`
- **Atualizado** rota `tela_veiculo/{tiposVeiculo}` para aceitar parâmetros
- **Adicionado** rota `tela_documentos`

### 6. `screens/TelaCompletarPerfilPrestador.kt` ✅
- **Atualizado** rota de "Informações do veículo" para `tela_tipo_veiculo`

## 🔧 Como Funciona

### Passo 1: Seleção de Tipos
```kotlin
// TelaTipoVeiculo.kt
// Usuário seleciona até 2 tipos de veículo
// Ao clicar em "Continuar", navega para:
navController.navigate("tela_veiculo/MOTO,CARRO")
```

### Passo 2: Preenchimento de Informações
```kotlin
// TelaInformacoesVeiculo.kt
// Recebe os tipos: "MOTO,CARRO"
// Cria formulários dinâmicos para cada tipo
// Usuário navega entre os formulários (Anterior/Próximo)
```

### Passo 3: Envio para API
```kotlin
// Ao clicar em "Finalizar" no último veículo
val modalidades = veiculosInfo.map { v ->
    ModalidadeItem(
        tipo = v.tipo,
        modelo_veiculo = v.modelo,
        ano_veiculo = v.ano.toIntOrNull(),
        possui_seguro = v.possuiSeguro.lowercase() == "sim",
        compartimento_adequado = v.compartimento.lowercase() == "sim",
        revisao_em_dia = v.revisao.lowercase() == "sim",
        antecedentes_criminais = v.antecedentes.lowercase() == "sim"
    )
}
modalidadeViewModel.cadastrarModalidades(token, modalidades)
```

### Passo 4: Request API
```json
POST https://servidor-facilita.onrender.com/v1/facilita/prestador/modalidades
Authorization: Bearer {token}
Content-Type: application/json

{
  "modalidades": [
    {
      "tipo": "MOTO",
      "modelo_veiculo": "Honda CG 160",
      "ano_veiculo": 2020,
      "possui_seguro": true,
      "compartimento_adequado": true,
      "revisao_em_dia": true,
      "antecedentes_criminais": true
    },
    {
      "tipo": "CARRO",
      "modelo_veiculo": "Fiat Uno",
      "ano_veiculo": 2018,
      "possui_seguro": true,
      "compartimento_adequado": true,
      "revisao_em_dia": true,
      "antecedentes_criminais": true
    }
  ]
}
```

### Passo 5: Response API
```json
{
  "message": "Modalidades adicionadas com sucesso!",
  "modalidades": [
    {
      "id": 6,
      "id_prestador": 8,
      "tipo": "MOTO"
    },
    {
      "id": 5,
      "id_prestador": 8,
      "tipo": "CARRO"
    }
  ]
}
```

### Passo 6: Após Sucesso
```kotlin
// ModalidadeViewModel detecta sucesso
_modalidadesCadastradas.value = true

// TelaInformacoesVeiculo observa e:
LaunchedEffect(modalidadesCadastradas) {
    if (modalidadesCadastradas) {
        // 1. Marca como validado
        perfilViewModel.marcarComoValidado("Informações do veículo")
        
        // 2. Volta para completar perfil
        navController.navigate("tela_completar_perfil_prestador") {
            popUpTo("tela_tipo_veiculo") { inclusive = true }
        }
    }
}
```

## 🎨 UI/UX

### TelaTipoVeiculo
- Cards clicáveis para cada tipo de veículo
- Indicador visual de seleção (fundo verde + ícone)
- Limite de 2 veículos com mensagem de erro
- Botão "Continuar" habilitado apenas com seleção

### TelaInformacoesVeiculo
- Indicador de progresso: "Informações do veículo (1/2)"
- Tipo do veículo atual em destaque
- Campos:
  - Modelo
  - Ano
  - Possui seguro? (sim/não)
  - Compartimento adequado? (sim/não)
  - Revisão em dia? (sim/não)
  - Sem antecedentes criminais? (sim/não)
- Botões de navegação:
  - "Anterior" (se não for o primeiro)
  - "Próximo" (se houver mais veículos)
  - "Finalizar" (no último veículo)
- Mensagens de erro/sucesso

## 🔐 Autenticação
Token hardcoded em `TelaInformacoesVeiculo.kt`:
```kotlin
val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**⚠️ IMPORTANTE:** Substitua por um sistema de autenticação real usando SharedPreferences ou similar.

## ✅ Checklist de Validação
- [x] Seleção de até 2 veículos
- [x] Navegação para TelaInformacoesVeiculo com tipos
- [x] Formulários dinâmicos por veículo
- [x] Envio para API com formato correto
- [x] Tratamento de erros
- [x] Marcação de "Informações do veículo" como válido (verde)
- [x] Volta automática para TelaCompletarPerfilPrestador

## 🧪 Testes Sugeridos
1. Selecionar 1 veículo → preencher → enviar
2. Selecionar 2 veículos → preencher ambos → enviar
3. Tentar selecionar 3 veículos → verificar mensagem de erro
4. Verificar navegação entre formulários
5. Verificar se marca como validado após sucesso
6. Testar com conexão offline → verificar mensagem de erro

## 📝 Notas
- Campos sim/não são case-insensitive ("sim", "SIM", "Sim")
- Ano é convertido para Int (se inválido, envia null)
- Modelo pode ser vazio (envia null)
- Todos os campos booleanos podem ser null na API

