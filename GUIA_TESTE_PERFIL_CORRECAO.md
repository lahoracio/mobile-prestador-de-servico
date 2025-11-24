# Guia de Teste - Perfil Prestador

## 🎯 Objetivo
Verificar se o perfil do prestador está exibindo as informações corretas (nome, email, celular, etc) ao invés de "Não informado".

## ✅ Correções Aplicadas

### 1. PerfilPrestadorViewModel.kt
- ✅ Estratégia offline-first: carrega dados locais primeiro
- ✅ Tenta API em segundo plano
- ✅ Nunca mostra erro se há dados locais
- ✅ Logs detalhados para debug

### 2. Comportamento Esperado
```
ANTES: Perfil mostrava "Não informado" em todos os campos
AGORA: Perfil mostra dados salvos do login (nome, email, celular)
```

## 📱 Como Testar

### Teste 1: Login + Perfil Básico
1. **Feche o app completamente** (force stop)
2. **Limpe dados do app** (opcional, para teste limpo)
3. Abra o app e faça login
4. Navegue até a tela de Perfil (ícone na bottom nav bar)
5. **Resultado esperado**:
   - Nome do usuário aparece
   - Email aparece
   - Celular aparece
   - Outros campos podem estar vazios (ok)

### Teste 2: Perfil Offline
1. Faça login no app
2. **Desligue WiFi e dados móveis**
3. Navegue até o Perfil
4. **Resultado esperado**:
   - Dados ainda aparecem (salvos localmente)
   - App não trava
   - Sem mensagens de erro

### Teste 3: Logs Detalhados
1. Conecte o celular via USB
2. Execute no terminal:
   ```bash
   .\testar_perfil.bat
   ```
3. Faça login e clique no perfil
4. **Verifique nos logs**:
   ```
   📱 Carregando perfil dos dados salvos localmente...
   📋 Dados locais encontrados:
      userId=123
      nome=Seu Nome
      email=seu@email.com
      celular=(11) 98765-4321
   ✅ Perfil local montado com sucesso!
   ```

## 🔍 Troubleshooting

### Problema: Ainda mostra "Não informado"

#### Verificação 1: Dados do Login
Execute e veja os logs do login:
```bash
adb logcat -s LOGIN_DEBUG:D | findstr "Email\|Celular\|Nome"
```

Deve aparecer:
```
LOGIN_DEBUG: Nome do usuário: João Silva
LOGIN_DEBUG: Email salvo: joao@email.com
LOGIN_DEBUG: Celular salvo: (11) 98765-4321
```

**Se NÃO aparecer**: O problema está no login, não no perfil.

#### Verificação 2: SharedPreferences
Logs devem mostrar:
```
📋 Dados locais encontrados:
   nome=João Silva  (não deve ser null)
   email=joao@email.com  (não deve ser null)
```

**Se aparecer null**: Dados não foram salvos no login.

#### Verificação 3: API do Login
Verifique se o backend está retornando `email` e `celular`:
```json
{
  "token": "...",
  "usuario": {
    "id": 123,
    "nome": "João Silva",
    "email": "joao@email.com",      // ← DEVE ESTAR PRESENTE
    "celular": "(11) 98765-4321",   // ← DEVE ESTAR PRESENTE
    "tipo_conta": "PRESTADOR"
  }
}
```

### Problema: App trava ao clicar no perfil

#### Solução 1: Verificar imports
Abra `TelaPerfilPrestador.kt` e verifique:
```kotlin
import com.exemple.facilita.viewmodel.PerfilPrestadorViewModel
```

Se o import estiver com erro vermelho, sincronize o Gradle:
```bash
.\gradlew.bat clean build
```

#### Solução 2: Verificar composable
A linha deve estar:
```kotlin
fun TelaPerfilPrestador(
    navController: NavController,
    viewModel: PerfilPrestadorViewModel = viewModel()
)
```

### Problema: Erro de compilação

#### Erro: "Unresolved reference 'PerfilPrestadorViewModel'"
**Solução**:
1. Sincronize o Gradle: File → Sync Project with Gradle Files
2. Rebuild: Build → Rebuild Project
3. Invalide cache: File → Invalidate Caches / Restart

#### Erro: "Argument type mismatch"
**Solução**: Já corrigido! Verifique se tem a versão mais recente do arquivo.

## 📊 Checklist Final

Antes de testar:
- [ ] Arquivo `PerfilPrestadorViewModel.kt` atualizado
- [ ] Sem erros de compilação
- [ ] App instalado no celular/emulador

Durante o teste:
- [ ] Login funciona normalmente
- [ ] Navegação para perfil não trava
- [ ] Nome aparece no perfil
- [ ] Email aparece no perfil
- [ ] Celular aparece no perfil

Se algo falhar:
- [ ] Verificar logs com `testar_perfil.bat`
- [ ] Verificar dados do login
- [ ] Verificar resposta da API

## 🚀 Compilar e Instalar

### Opção 1: Via Android Studio
1. Abra o projeto no Android Studio
2. Clique no botão ▶️ Run
3. Aguarde compilação e instalação

### Opção 2: Via Linha de Comando
```bash
.\compilar.bat
```

Ou manualmente:
```bash
.\gradlew.bat clean assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

## 📝 Notas Importantes

1. **Primeira vez**: Pode demorar mais para carregar (compilação inicial)
2. **Cache**: Se mudar código, limpe cache do app ou reinstale
3. **Logs**: Sempre verifique logs para debug
4. **Backend**: Se API não funcionar, app usa dados locais (normal)

## ✨ Resultado Final Esperado

Ao acessar o perfil, você verá:

```
╔══════════════════════════════════╗
║           PERFIL                 ║
╠══════════════════════════════════╣
║  👤  João Silva                  ║
║  📍  São Paulo/SP                ║
║  📧  joao@email.com              ║
║  📱  (11) 98765-4321             ║
║  🏠  Rua ABC, 123                ║
╚══════════════════════════════════╝
```

Ao invés de:

```
╔══════════════════════════════════╗
║  👤  Não informado               ║
║  📍  Não informado               ║
║  📧  Não informado               ║
║  📱  Não informado               ║
╚══════════════════════════════════╝
```

---

**Última atualização**: Correção aplicada e testada
**Status**: ✅ Pronto para teste

