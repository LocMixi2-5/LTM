@echo off
chcp 65001 >nul
echo ====================================================
echo   8-Ball Pool Server - Compile
echo ====================================================

set JAVA_HOME=C:\Program Files\Java\jdk-17
set JAVA=%JAVA_HOME%\bin\javac.exe
set LIB=lib\postgresql-42.7.3.jar

if not exist "%JAVA%" (
    echo [LOI] Khong tim thay Java tai: %JAVA_HOME%
    echo Tai JDK 17 tai: https://adoptium.net/
    pause
    exit /b 1
)

if not exist "%LIB%" (
    echo [LOI] Khong tim thay file: %LIB%
    echo Tai tai: https://jdbc.postgresql.org/download/
    pause
    exit /b 1
)

if not exist "out" mkdir out

echo [Build] Dang compile...
"%JAVA%" -cp "%LIB%" -sourcepath src -d out -encoding UTF-8 ^
    src\com\billiards\server\protocol\PacketType.java ^
    src\com\billiards\server\protocol\Packet.java ^
    src\com\billiards\server\protocol\CueSkinConfig.java ^
    src\com\billiards\server\DatabaseManager.java ^
    src\com\billiards\server\AuthManager.java ^
    src\com\billiards\server\GameRoom.java ^
    src\com\billiards\server\LobbyManager.java ^
    src\com\billiards\server\ClientHandler.java ^
    src\com\billiards\server\ServerMain.java

if %ERRORLEVEL% == 0 (
    echo [Build] Compile thanh cong!
    echo Chay: run_server.bat
) else (
    echo [Build] LOI COMPILE! Kiem tra lai code.
    pause
    exit /b 1
)
pause
