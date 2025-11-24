@echo off
echo ========================================
echo CAPTURANDO LOGS DO CRASH - PERFIL
echo ========================================
echo.

echo Limpando logs antigos...
adb logcat -c

echo.
echo ========================================
echo LOGS PRONTOS!
echo Agora clique no perfil no app...
echo ========================================
echo.

adb logcat -v time PerfilPrestadorViewModel:V TelaPerfilPrestador:V AndroidRuntime:E *:E

pause

