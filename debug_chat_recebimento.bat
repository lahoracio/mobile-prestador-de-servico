@echo off
echo ========================================
echo DEBUG CHAT - RECEBIMENTO DE MENSAGENS
echo ========================================
echo.
echo Logs sendo capturados em tempo real...
echo.
echo LEGENDA:
echo   [CONECTAR] - Conexao estabelecida
echo   [USUARIO]  - Usuario registrado
echo   [SALA]     - Entrou na sala
echo   [ENVIAR]   - Mensagem enviada
echo   [RECEBER]  - Mensagem recebida
echo   [ERRO]     - Erro encontrado
echo.
echo Pressione Ctrl+C para parar
echo ========================================
echo.

adb logcat -c
adb logcat | findstr /C:"ChatSocketManager" /C:"ChatViewModel" /C:"TelaChatAoVivo" /C:"📩" /C:"📤" /C:"✅" /C:"❌" /C:"👤" /C:"🚪" /C:"🔔"

