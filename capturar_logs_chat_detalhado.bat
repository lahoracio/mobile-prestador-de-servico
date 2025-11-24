@echo off
echo ================================
echo    CAPTURANDO LOGS DO CHAT
echo ================================
echo.
echo Pressione CTRL+C para parar
echo.
echo Aguardando logs...
echo ================================
echo.

adb logcat -c
adb logcat -s ChatSocketManager:D TelaChatAoVivo:D SocketIOTester:D | findstr /V "EGL_emulation"

