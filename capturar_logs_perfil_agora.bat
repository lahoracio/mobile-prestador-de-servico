@echo off
echo ========================================
echo CAPTURAR LOGS - PERFIL APENAS
echo ========================================
echo.
echo INSTRUCOES:
echo 1. Certifique-se que o app esta aberto
echo 2. VA ATE A TELA INICIAL (nao clique no perfil ainda)
echo 3. Pressione qualquer tecla aqui
echo 4. DEPOIS clique no PERFIL no app
echo.
pause

echo.
echo Limpando logs antigos...
adb logcat -c

echo.
echo ========================================
echo MONITORANDO LOGS DO PERFIL
echo Clique no PERFIL no app AGORA!
echo ========================================
echo.

adb logcat -s PerfilPrestadorViewModel:* OkHttp:* -v time

pause

