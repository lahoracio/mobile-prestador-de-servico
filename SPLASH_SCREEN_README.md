# 🎨 Splash Screen - Facilita

## 📱 Implementação Completa da Tela de Splash

### ✅ O que foi implementado:

1. **SplashScreen.kt** - Tela de splash com animações incríveis:
   - ✨ Animações de rotação e morfologia
   - 🎆 Explosão de partículas com efeito de rastro
   - 🌀 Espirais e anéis rotativos em 3D
   - 💫 Logo central com efeito glassmorphism
   - 🔥 Texto com efeito neon
   - 🎨 Gradiente dinâmico de fundo
   - ⚡ Círculos pulsantes

2. **MainActivity.kt** - Navegação configurada:
   - A splash screen é a tela inicial do app (`startDestination = "splash_screen"`)
   - Após 3.5 segundos de animação, navega para `tela_login`

### 🎬 Fluxo de Animação:

1. **Fase 1** (1s): Morphing e entrada dos anéis
2. **Fase 2** (0.4s): Logo aparece com bounce effect
3. **Fase 3** (0.6s): Texto aparece com fade-in
   - Logo "Facilita" com efeito neon
   - Badge "PRESTADOR" em destaque (verde neon com texto preto)
   - Subtítulo: "Ganhe dinheiro fazendo entregas"
4. **Fase 4** (0.8s): Explosão de partículas
5. **Fase 5** (0.5s): Delay antes da navegação
6. **Navegação**: Redireciona para tela de login

### 🎯 Identificação de Prestador:

O app agora deixa claro que é para prestadores com:
- ✅ Badge verde neon "PRESTADOR" em destaque
- ✅ Texto: "Ganhe dinheiro fazendo entregas"
- ✅ Visual profissional e moderno

### 🎨 Cores Utilizadas:

- **Verde Primário**: `#019D31`
- **Verde Neon**: `#00FF47`
- **Verde Médio**: `#00b14f`
- **Preto**: Background base
- **Branco**: Texto principal

### 🔧 Personalização:

Para alterar o destino após a splash, edite o arquivo `SplashScreen.kt` na linha:

```kotlin
// Navegação
navController.navigate("tela_login") {
    popUpTo("splash_screen") { inclusive = true }
}
```

Para alterar a duração da splash, modifique os valores de `delay()` e `animationSpec` nos `LaunchedEffect`.

### 📦 Dependências:

Todas as dependências já estão incluídas no Jetpack Compose:
- `androidx.compose.animation:animation-core`
- `androidx.compose.foundation`
- `androidx.compose.ui`

### 🚀 Como Usar:

O app já está configurado! Basta:

1. Compilar o projeto
2. Executar no dispositivo/emulador
3. A splash screen aparecerá automaticamente ao iniciar o app

### 📝 Notas:

- A splash screen usa 60 partículas animadas para o efeito de explosão
- Todas as animações são otimizadas para performance
- O logo tem rotação infinita nos detalhes internos
- Os anéis têm efeito de perspectiva 3D simulada

---

**Criado em**: 13 de Novembro de 2025
**App**: Facilita - Prestador de Serviços

