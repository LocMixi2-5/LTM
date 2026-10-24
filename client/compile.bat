@echo off
chcp 65001 >nul
echo ====================================================
echo   8-Ball Pool Client - Compile
echo ====================================================

set JAVA_HOME=C:\Program Files\Java\jdk-17
set JAVA=%JAVA_HOME%\bin\javac.exe
set JAVAFX_PATH=%~dp0lib\javafx-sdk-17.0.13\lib

if not exist "%JAVA%" (
    echo [LOI] Khong tim thay Java tai: %JAVA_HOME%
    pause
    exit /b 1
)

if not exist "%JAVAFX_PATH%\javafx.controls.jar" (
    echo [LOI] Khong tim thay JavaFX tai: %JAVAFX_PATH%
    pause
    exit /b 1
)

echo [Info] JavaFX: %JAVAFX_PATH%
if not exist "out" mkdir out

echo [Build] Dang compile...
"%JAVA%" --module-path "%JAVAFX_PATH%" ^
    --add-modules javafx.controls,javafx.fxml,javafx.media ^
    -sourcepath src ^
    -d out -encoding UTF-8 ^
    src\com\billiards\client\theme\Theme.java ^
    src\com\billiards\client\theme\SilverCornerPane.java ^
    src\com\billiards\client\vfx\NatureAmbience.java ^
    src\com\billiards\client\vfx\DragonCloudBackground.java ^
    src\com\billiards\client\audio\SoundManager.java ^
    src\com\billiards\client\protocol\PacketType.java ^
    src\com\billiards\client\protocol\Packet.java ^
    src\com\billiards\client\game\Ball.java ^
    src\com\billiards\client\game\PhysicsEngine.java ^
    src\com\billiards\client\game\CueSkin.java ^
    src\com\billiards\client\game\CueSkinManager.java ^
    src\com\billiards\client\NetworkClient.java ^
    src\com\billiards\client\AvatarManager.java ^
    src\com\billiards\client\scenes\SettingsDialog.java ^
    src\com\billiards\client\scenes\ServerConfigDialog.java ^
    src\com\billiards\client\scenes\FriendSearchDialog.java ^
    src\com\billiards\client\scenes\SceneManager.java ^
    src\com\billiards\client\scenes\LoginScene.java ^
    src\com\billiards\client\scenes\RegisterScene.java ^
    src\com\billiards\client\scenes\LobbyScene.java ^
    src\com\billiards\client\scenes\MatchmakingScene.java ^
    src\com\billiards\client\scenes\PreGameScene.java ^
    src\com\billiards\client\scenes\GameScene.java ^
    src\com\billiards\client\scenes\ResultScene.java ^
    src\com\billiards\client\scenes\LeaderboardScene.java ^
    src\com\billiards\client\ClientMain.java

if %ERRORLEVEL% == 0 (
    echo [Build] Compile thanh cong! Dong goi thanh JAR...
    "%JAVA_HOME%\bin\jar.exe" cfe BilliardsClient.jar com.billiards.client.ClientMain -C out .
    echo [Build] Da tao BilliardsClient.jar!
    echo Chay: run_client.bat
) else (
    echo [Build] LOI COMPILE!
    pause
    exit /b 1
)
pause
