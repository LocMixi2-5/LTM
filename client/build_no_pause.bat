@echo off
chcp 65001 >nul
set JAVA_HOME=C:\Program Files\Java\jdk-17
set JAVAC="%JAVA_HOME%\bin\javac.exe"
set JAR="%JAVA_HOME%\bin\jar.exe"
set JAVAFX_PATH=%~dp0lib\javafx-sdk-17.0.13\lib

if not exist "%~dp0out" mkdir "%~dp0out"

%JAVAC% --module-path "%JAVAFX_PATH%" ^
    --add-modules javafx.controls,javafx.fxml,javafx.media ^
    -d "%~dp0out" -encoding UTF-8 ^
    "%~dp0src\com\billiards\client\theme\Theme.java" ^
    "%~dp0src\com\billiards\client\theme\SilverCornerPane.java" ^
    "%~dp0src\com\billiards\client\vfx\NatureAmbience.java" ^
    "%~dp0src\com\billiards\client\vfx\DragonCloudBackground.java" ^
    "%~dp0src\com\billiards\client\audio\SoundManager.java" ^
    "%~dp0src\com\billiards\client\protocol\PacketType.java" ^
    "%~dp0src\com\billiards\client\protocol\Packet.java" ^
    "%~dp0src\com\billiards\client\game\Ball.java" ^
    "%~dp0src\com\billiards\client\game\PhysicsEngine.java" ^
    "%~dp0src\com\billiards\client\game\CueSkin.java" ^
    "%~dp0src\com\billiards\client\game\CueSkinManager.java" ^
    "%~dp0src\com\billiards\client\NetworkClient.java" ^
    "%~dp0src\com\billiards\client\AvatarManager.java" ^
    "%~dp0src\com\billiards\client\scenes\SettingsDialog.java" ^
    "%~dp0src\com\billiards\client\scenes\ServerConfigDialog.java" ^
    "%~dp0src\com\billiards\client\scenes\FriendSearchDialog.java" ^
    "%~dp0src\com\billiards\client\scenes\SceneManager.java" ^
    "%~dp0src\com\billiards\client\scenes\LoginScene.java" ^
    "%~dp0src\com\billiards\client\scenes\RegisterScene.java" ^
    "%~dp0src\com\billiards\client\scenes\LobbyScene.java" ^
    "%~dp0src\com\billiards\client\scenes\MatchmakingScene.java" ^
    "%~dp0src\com\billiards\client\scenes\PreGameScene.java" ^
    "%~dp0src\com\billiards\client\scenes\GameScene.java" ^
    "%~dp0src\com\billiards\client\scenes\ResultScene.java" ^
    "%~dp0src\com\billiards\client\scenes\LeaderboardScene.java" ^
    "%~dp0src\com\billiards\client\ClientMain.java"

if %ERRORLEVEL% equ 0 (
    %JAR% cfe "%~dp0BilliardsClient.jar" com.billiards.client.ClientMain -C "%~dp0out" .
    echo [BUILD_SUCCESS]
    exit /b 0
) else (
    echo [BUILD_FAILED]
    exit /b 1
)
