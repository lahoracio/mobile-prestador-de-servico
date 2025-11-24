@echo off
echo ========================================
echo TESTE ENDPOINT - GET PERFIL
echo ========================================
echo.
echo Este script testa o endpoint do perfil
echo.
echo INSTRUCOES:
echo 1. Certifique-se que o app esta rodando
echo 2. Faca login no app
echo 3. Pressione qualquer tecla para capturar o token
echo.
pause

echo.
echo Capturando token dos logs...
echo.

adb logcat -d | findstr "Token:" > temp_token.txt
type temp_token.txt

echo.
echo.
echo Agora vou monitorar os logs do perfil em tempo real...
echo Clique no PERFIL no app AGORA!
echo.
echo Pressione Ctrl+C para parar
echo.

adb logcat -c
adb logcat -s PerfilPrestadorViewModel:* RetrofitFactory:* OkHttp:*

pause

