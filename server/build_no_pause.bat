@echo off
chcp 65001 >nul
set JAVA_HOME=C:\Program Files\Java\jdk-17
set JAVA="%JAVA_HOME%\bin\javac.exe"
set JAR="%JAVA_HOME%\bin\jar.exe"
set LIB=%~dp0lib\postgresql-42.7.3.jar

if not exist "%~dp0out" mkdir "%~dp0out"

%JAVA% -cp "%LIB%" -d "%~dp0out" -encoding UTF-8 ^
    "%~dp0src\com\billiards\server\protocol\PacketType.java" ^
    "%~dp0src\com\billiards\server\protocol\Packet.java" ^
    "%~dp0src\com\billiards\server\protocol\CueSkinConfig.java" ^
    "%~dp0src\com\billiards\server\DatabaseManager.java" ^
    "%~dp0src\com\billiards\server\AuthManager.java" ^
    "%~dp0src\com\billiards\server\GameRoom.java" ^
    "%~dp0src\com\billiards\server\LobbyManager.java" ^
    "%~dp0src\com\billiards\server\ClientHandler.java" ^
    "%~dp0src\com\billiards\server\ServerMain.java"

if %ERRORLEVEL% equ 0 (
    %JAR% cfe "%~dp0BilliardsServer.jar" com.billiards.server.ServerMain -C "%~dp0out" .
    echo [SERVER_BUILD_SUCCESS]
    exit /b 0
) else (
    echo [SERVER_BUILD_FAILED]
    exit /b 1
)
