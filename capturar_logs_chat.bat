@echo off
echo ==========================================
echo CAPTURANDO LOGS DO CHAT - FACILITA APP
echo ==========================================
echo.
echo Limpando logs antigos...
adb logcat -c
echo.
echo Iniciando captura de logs...
echo Filtro: ChatSocketManager, TelaChatAoVivo, SocketIOTester, TelaDetalhes
echo.
echo Agora:
echo 1. Execute o app
echo 2. Va ate o chat
echo 3. Tente enviar mensagem
echo 4. Pressione Ctrl+C para parar a captura
echo.
echo Os logs serao salvos em: logs_chat.txt
echo.
adb logcat | findstr "ChatSocketManager TelaChatAoVivo SocketIOTester TelaDetalhes" > logs_chat.txt

