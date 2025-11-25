@echo off
REM Script para force push (use apenas se necessário)
REM Data: 25/11/2025

echo ========================================
echo FORCE PUSH - USO APENAS EM EMERGENCIA
echo ========================================
echo.
echo ATENCAO: Este script ira SOBRESCREVER o repositorio remoto!
echo.
echo Use este script apenas se:
echo 1. Voce e o unico desenvolvedor
echo 2. As mudancas remotas nao sao importantes
echo 3. Voce tem certeza do que esta fazendo
echo.
echo Deseja continuar? (S/N)
set /p confirma=
if /i not "%confirma%"=="S" (
    echo Operacao cancelada.
    pause
    exit /b 0
)

echo.
echo ULTIMA CHANCE! Tem certeza ABSOLUTA? (S/N)
set /p confirma2=
if /i not "%confirma2%"=="S" (
    echo Operacao cancelada.
    pause
    exit /b 0
)

echo.
echo [1/5] Verificando status...
git status
echo.

echo [2/5] Adicionando mudancas...
git add .
echo.

echo [3/5] Fazendo commit...
git commit -m "fix: Corrigidos todos erros de compilacao (force push)"
echo.

echo [4/5] Fazendo FORCE PUSH...
echo SOBRESCREVENDO repositorio remoto em 3 segundos...
timeout /t 3 /nobreak
git push --force origin main
if %ERRORLEVEL% NEQ 0 (
    echo [ERRO] Falha ao fazer force push!
    pause
    exit /b 1
)
echo.

echo [5/5] Verificacao final...
git status
echo.

echo ========================================
echo FORCE PUSH CONCLUIDO!
echo ========================================
echo.
echo IMPORTANTE: Se outros desenvolvedores estao trabalhando neste projeto,
echo eles precisarao fazer:
echo.
echo   git fetch origin
echo   git reset --hard origin/main
echo.
pause

