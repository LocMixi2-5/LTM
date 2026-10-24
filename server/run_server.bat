@echo off
chcp 65001 >nul
echo ====================================================
echo   8-Ball Pool Server - Khoi dong
echo ====================================================
set JAVA_HOME=C:\Program Files\Java\jdk-17
set JAVA=%JAVA_HOME%\bin\java.exe
set LIB=lib\postgresql-42.7.3.jar

if not exist "out" (
    echo [LOI] Chua compile! Chay compile.bat truoc.
    pause
    exit /b 1
)

if not exist "%LIB%" (
    echo [CANH BAO] Khong co postgresql.jar - server se chay offline (khong luu DB)
)

echo [Server] Dang khoi dong tren cong 12345...
"%JAVA%" -cp "out;%LIB%" com.billiards.server.ServerMain
pause
