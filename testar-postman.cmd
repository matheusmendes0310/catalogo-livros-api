@echo off
setlocal
cd /d "%~dp0"
set "PSModulePath="
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0postman\executar-testes.ps1"
set "resultado=%ERRORLEVEL%"
echo.
if "%resultado%"=="0" (
    echo Testes concluidos. Abra postman\relatorios\resultado.html para ver o relatorio.
) else (
    echo Nao foi possivel concluir os testes. Confira a mensagem acima.
)
pause
exit /b %resultado%
