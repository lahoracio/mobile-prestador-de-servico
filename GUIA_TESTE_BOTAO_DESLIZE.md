# 🧪 GUIA DE TESTE - BOTÃO DE DESLIZE PARA FINALIZAR

## 🚀 Início Rápido (5 minutos)

### 1️⃣ Compilar e Instalar
```bash
cd /Users/24122303/AndroidStudioProjects/mobile-prestador-de-servico2
./gradlew installDebug
```

### 2️⃣ Login
- **Email**: cadastro@gmail.com
- **Senha**: Senha@123

### 3️⃣ Aceitar Serviço
1. Veja a lista de serviços disponíveis
2. Toque em qualquer serviço
3. Clique em "Aceitar Serviço"

### 4️⃣ Navegar pelos Status
```
Indo para o local → Cheguei no Local
     ↓
No local → Iniciar Serviço
     ↓
Executando → Preparar Finalização
     ↓
Finalizando → 🎉 BOTÃO DE DESLIZE APARECE!
```

### 5️⃣ Testar o Botão de Deslize
1. **Veja o botão pulsando** (animação de "respiração")
2. **Leia as instruções**: "Deslize para finalizar"
3. **Arraste o círculo verde** da esquerda para direita
4. **Observe**:
   - Barra de progresso preenchendo
   - Ícone girando 360°
   - Efeito de brilho aumentando
   - Texto desaparecendo
5. **Ao chegar no final (100%)**:
   - Partículas verdes explodem
   - Ícone vira ✓
   - Botão escala 1.2x
6. **Confirmação**:
   - Toast: "✅ Serviço finalizado!"
   - Volta automaticamente após 2s

---

## 📋 Checklist de Testes

### ✅ Testes Funcionais

#### Comportamento do Botão
- [ ] Botão aparece apenas no status "FINALIZANDO"
- [ ] Pulso constante atrai atenção
- [ ] Instruções são claras e visíveis
- [ ] Arrasto é suave e responsivo
- [ ] Progresso visual corresponde ao arrasto
- [ ] Soltar antes do final volta ao início
- [ ] Completar 100% dispara finalização

#### Integração com API
- [ ] Token é enviado corretamente
- [ ] Endpoint `/servico/{id}/finalizar` é chamado
- [ ] Response 200 é tratada (sucesso)
- [ ] Response 400/403/500 são tratadas (erro)
- [ ] Timeout é respeitado
- [ ] Retry funciona em caso de erro de rede

#### Feedback ao Usuário
- [ ] Toast de sucesso aparece
- [ ] Toast de erro aparece (simular erro)
- [ ] Navegação automática funciona
- [ ] Cache do serviço é limpo
- [ ] Logs aparecem no Logcat

---

## 🎨 Testes Visuais

### Animações
- [ ] Pulso é suave (1.5s ciclo)
- [ ] Barra de progresso preenche linearmente
- [ ] Ícone gira 360° completo
- [ ] Partículas aparecem aos 95%+
- [ ] Escala final é notável (1.2x)
- [ ] Transições são fluidas (sem travamentos)

### Cores e Contraste
- [ ] Verde #00E676 é vibrante
- [ ] Contraste entre texto e fundo é adequado
- [ ] Brilhos são sutis mas perceptíveis
- [ ] Blur não compromete performance

### Responsividade
- [ ] Funciona em portrait
- [ ] Funciona em landscape
- [ ] Adapta-se a telas pequenas (< 5")
- [ ] Adapta-se a telas grandes (tablets)

---

## 🐛 Testes de Erro

### Cenários de Erro Comuns

#### 1. Token Expirado
```
Simular: Esperar 8 horas sem usar app
Resultado esperado: "Token não encontrado. Faça login novamente."
```

#### 2. Sem Internet
```
Simular: Desligar WiFi/Dados móveis
Resultado esperado: "Erro ao finalizar serviço" + mensagem de rede
```

#### 3. Serviço Já Finalizado
```
Simular: Finalizar mesmo serviço 2x (cache + API)
Resultado esperado: Erro 400 do backend
```

#### 4. Usuário Não Autorizado
```
Simular: Token de outro prestador
Resultado esperado: Erro 403 "Acesso negado"
```

---

## 📊 Testes de Performance

### Métricas

| Métrica | Alvo | Como Medir |
|---------|------|------------|
| FPS durante animação | ≥ 55 | GPU Monitor |
| Tempo de resposta API | < 2s | Logcat timestamps |
| Uso de memória | < +10MB | Memory Profiler |
| CPU durante animação | < 30% | CPU Profiler |

### Stress Tests
- [ ] Arrastar rápido 10x seguidas
- [ ] Arrastar devagar por 30s
- [ ] Arrastar e soltar 20x
- [ ] Rotacionar tela durante arrasto
- [ ] App em segundo plano e voltar

---

## 🔍 Logs para Verificar

### Logcat Filters
```
Tag: ServicoViewModel
Level: Debug, Info, Error
```

### Logs Esperados (Sucesso)
```
🏁 FINALIZANDO SERVIÇO
   ServicoId: 89
🔑 Token obtido: eyJhbGciOiJIUzI1NiIs...
📡 Chamando API PATCH /servico/89/finalizar
📡 Resposta recebida:
   Status Code: 200
✅ Serviço finalizado com sucesso!
   Mensagem: Serviço finalizado com sucesso
📦 Serviço removido do cache
✅ Callback onSuccess executado
```

### Logs Esperados (Erro)
```
❌ Erro ao finalizar serviço
   Código: 400
   Mensagem: Prestador já possui um serviço em andamento
   Body: {"status_code":400,"message":"..."}
```

---

## 📱 Testes em Diferentes Dispositivos

### Configurações Recomendadas

| Dispositivo | OS | Resolução | Status |
|-------------|----|-----------| -------|
| Pixel 5 | Android 13 | 1080x2340 | ✅ |
| Samsung S21 | Android 14 | 1080x2400 | 🔄 |
| Xiaomi Redmi | Android 12 | 720x1600 | 🔄 |
| Tablet 10" | Android 13 | 1200x1920 | 🔄 |

---

## 🎯 Casos de Uso Reais

### Cenário 1: Fluxo Completo Feliz
```
1. Prestador aceita serviço #89
2. Vai até local (status "INDO_BUSCAR")
3. Chega no local (status "NO_LOCAL")
4. Inicia execução (status "EXECUTANDO")
5. Prepara finalização (status "FINALIZANDO")
6. Arrasta botão até o final
7. ✅ Serviço finalizado
8. Volta para lista de serviços
```

### Cenário 2: Cliente Confirma Rapidamente
```
1. Prestador finaliza serviço
2. Cliente recebe notificação
3. Cliente confirma em < 1min
4. Prestador recebe notificação de pagamento
5. Saldo é atualizado na carteira
```

### Cenário 3: Arrasto Incompleto
```
1. Prestador arrasta até 50%
2. Solta o dedo
3. Botão volta ao início (animação elástica)
4. Pode tentar novamente
```

### Cenário 4: Erro de Rede
```
1. Prestador arrasta até 100%
2. Sem internet
3. Toast: "Erro ao finalizar serviço"
4. Botão volta ao estado inicial
5. Prestador pode tentar quando tiver internet
```

---

## 🔧 Troubleshooting

### Problema: Botão não aparece
**Causa**: Status não é "FINALIZANDO"
**Solução**: Navegar pelos status corretamente

### Problema: Arrasto não funciona
**Causa**: Gestos conflitantes
**Solução**: Verificar outros listeners na tela

### Problema: Animação trava
**Causa**: Dispositivo com pouca memória
**Solução**: Reduzir partículas ou blur

### Problema: API retorna 403
**Causa**: Serviço não pertence ao prestador
**Solução**: Verificar token e ID do prestador

### Problema: Toast não aparece
**Causa**: Context inválido
**Solução**: Verificar se context é Main/Activity

---

## 📈 Métricas de Sucesso

### KPIs
- **Taxa de conclusão**: > 95%
- **Tempo médio para finalizar**: < 3s
- **Taxa de erro**: < 2%
- **Satisfação do usuário**: > 4.5/5

### Analytics Recomendados
```kotlin
// Firebase Analytics
analytics.logEvent("swipe_to_finish_completed") {
    param("service_id", servicoId)
    param("time_taken_ms", timeTaken)
    param("attempts", attempts)
}
```

---

## ✅ Resultado Final do Teste

### Após Completar Todos os Testes

```
✅ Funcionalidade: PASS
✅ Animações: PASS
✅ API Integration: PASS
✅ Error Handling: PASS
✅ Performance: PASS
✅ UX: PASS

Status: 🟢 APROVADO PARA PRODUÇÃO
```

---

## 📞 Reportar Bugs

### Template
```
**Descrição**: O que aconteceu?
**Passos para reproduzir**:
1. ...
2. ...
3. ...
**Resultado esperado**: ...
**Resultado obtido**: ...
**Screenshots/Vídeo**: [anexar]
**Dispositivo**: Modelo, OS, Versão
**Logs**: [copiar do Logcat]
```

---

## 🎓 Dicas para Testers

1. **Teste no dispositivo real**, não só emulador
2. **Varie a velocidade** do arrasto
3. **Teste com internet lenta** (throttling)
4. **Rotacione a tela** durante operações
5. **Simule interrupções** (ligações, notificações)
6. **Teste em horários de pico** da API
7. **Documente tudo** com screenshots

---

## 🏁 Checklist Final

Antes de marcar como completo:

- [ ] Todos os testes funcionais passaram
- [ ] Todos os testes visuais passaram
- [ ] Todos os cenários de erro foram testados
- [ ] Performance está adequada
- [ ] Documentação foi atualizada
- [ ] Logs de debug são informativos
- [ ] UX é intuitiva
- [ ] Código está comentado
- [ ] README foi atualizado
- [ ] Time foi treinado

---

## 🎉 Próximos Passos

1. ✅ Testar localmente
2. 🔄 Code review
3. 🔄 QA testing
4. 🔄 Beta testing
5. 🔄 Deploy staging
6. 🔄 Monitor métricas
7. 🔄 Deploy produção

---

**Data de criação**: 27/11/2025
**Última atualização**: 27/11/2025
**Versão**: 1.0
**Status**: 🟢 Pronto para uso

