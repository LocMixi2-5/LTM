@echo off
chcp 65001 >nul
echo ====================================================
echo   8-Ball Pool Client - Build App Image va ZIP
echo ====================================================

set JAVA_HOME=C:\Program Files\Java\jdk-17
set JPACKAGE=%JAVA_HOME%\bin\jpackage.exe
set JAVAFX_PATH=%~dp0lib\javafx-sdk-17.0.13\lib
set DIST_DIR=%~dp0dist
set DIST_EXE_DIR=%DIST_DIR%\BilliardsClient_EXE
set TEMP_INPUT=%DIST_DIR%\temp_jpackage_input

if not exist "%DIST_DIR%" mkdir "%DIST_DIR%"

if not exist "%JPACKAGE%" (
    echo [LOI] Khong tim thay jpackage tai: %JPACKAGE%
    exit /b 1
)

echo [1/4] Bien dich source code...
call "%~dp0build_no_pause.bat"
if %ERRORLEVEL% neq 0 (
    echo [LOI] Bien dich that bai!
    exit /b 1
)

echo [2/4] Chuan bi du lieu dong goi...
if exist "%TEMP_INPUT%" rmdir /s /q "%TEMP_INPUT%"
mkdir "%TEMP_INPUT%"
copy /Y "%~dp0BilliardsClient.jar" "%TEMP_INPUT%\BilliardsClient.jar" >nul

if exist "%DIST_EXE_DIR%" rmdir /s /q "%DIST_EXE_DIR%"
mkdir "%DIST_EXE_DIR%"

echo [3/4] Chay jpackage tao runtime va app image...
if exist "%~dp0BilliardsClient.ico" (
    "%JPACKAGE%" --type app-image ^
        --name BilliardsClient ^
        --input "%TEMP_INPUT%" ^
        --main-jar BilliardsClient.jar ^
        --main-class com.billiards.client.ClientMain ^
        --icon "%~dp0BilliardsClient.ico" ^
        --module-path "%JAVAFX_PATH%" ^
        --add-modules javafx.controls,javafx.fxml,javafx.media ^
        --java-options "-Dprism.order=d3d,sw" ^
        --dest "%DIST_EXE_DIR%"
) else (
    "%JPACKAGE%" --type app-image ^
        --name BilliardsClient ^
        --input "%TEMP_INPUT%" ^
        --main-jar BilliardsClient.jar ^
        --main-class com.billiards.client.ClientMain ^
        --module-path "%JAVAFX_PATH%" ^
        --add-modules javafx.controls,javafx.fxml,javafx.media ^
        --java-options "-Dprism.order=d3d,sw" ^
        --dest "%DIST_EXE_DIR%"
)

if %ERRORLEVEL% neq 0 (
    echo [LOI] jpackage that bai!
    rmdir /s /q "%TEMP_INPUT%"
    exit /b 1
)

rmdir /s /q "%TEMP_INPUT%"

echo [4/4] Sao chep native DLLs, assets va tao file ZIP phat hanh...
:: Sao chep cac DLL native bat buoc cua JavaFX de tranh loi Failed to launch JVM
if exist "%~dp0lib\javafx-sdk-17.0.13\bin\*.dll" (
    copy /Y "%~dp0lib\javafx-sdk-17.0.13\bin\*.dll" "%DIST_EXE_DIR%\BilliardsClient\runtime\bin\" >nul
    copy /Y "%~dp0lib\javafx-sdk-17.0.13\bin\*.dll" "%DIST_EXE_DIR%\BilliardsClient\" >nul
)
xcopy /E /I /Y "%~dp0assets" "%DIST_EXE_DIR%\BilliardsClient\assets" >nul
if exist "%~dp0server_ip.txt" (
    copy /Y "%~dp0server_ip.txt" "%DIST_EXE_DIR%\BilliardsClient\server_ip.txt" >nul
)
if exist "%~dp0BilliardsClient.ico" (
    copy /Y "%~dp0BilliardsClient.ico" "%DIST_EXE_DIR%\BilliardsClient\BilliardsClient.ico" >nul
)

attrib +h "%DIST_EXE_DIR%\BilliardsClient\app"
attrib +h "%DIST_EXE_DIR%\BilliardsClient\runtime"

set ZIP_FILE=%DIST_DIR%\BilliardsClient_Game.zip
if exist "%ZIP_FILE%" del /f /q "%ZIP_FILE%"
tar.exe -a -cf "%ZIP_FILE%" -C "%DIST_EXE_DIR%\BilliardsClient" .

echo [OK] Da tao xong BilliardsClient_Game.zip!
exit /b 0
