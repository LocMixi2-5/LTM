@echo off
chcp 65001 >nul
echo ====================================================
echo   8-Ball Pool Client - Khoi dong
echo ====================================================
set JAVA_HOME=C:\Program Files\Java\jdk-17
set JAVA="%JAVA_HOME%\bin\java.exe"
if not exist %JAVA% (
    set JAVA=java
)
set JAVAFX_PATH=%~dp0lib\javafx-sdk-17.0.13\lib

if not exist "BilliardsClient.jar" if not exist "out" (
    echo [LOI] Khong tim thay BilliardsClient.jar hoac thu muc out!
    echo Chay compile.bat truoc de tao file.
    pause
    exit /b 1
)

set HOST_PROP=
if not "%~1"=="" (
    set HOST_PROP=-Dserver.host=%~1
    echo [Info] Ket noi server chi dinh qua dong lenh: %~1:12345
) else (
    echo [Info] Su dung IP da luu trong he thong hoac tu dong tim kiem trong GUI...
)

if exist "BilliardsClient.jar" (
    %JAVA% --module-path "%JAVAFX_PATH%" ^
        --add-modules javafx.controls,javafx.fxml,javafx.media ^
        -Dprism.order=sw ^
        %HOST_PROP% ^
        -jar BilliardsClient.jar
) else (
    %JAVA% --module-path "%JAVAFX_PATH%" ^
        --add-modules javafx.controls,javafx.fxml,javafx.media ^
        -Dprism.order=sw ^
        %HOST_PROP% ^
        -cp out ^
        com.billiards.client.ClientMain
)

pause
