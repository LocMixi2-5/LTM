@echo off
chcp 65001 >nul
echo ====================================================
echo   8-Ball Pool - Dong goi 1 FILE EXE DUY NHAT
echo ====================================================

set CSC=C:\Windows\Microsoft.NET\Framework64\v4.0.30319\csc.exe

if not exist "%CSC%" (
    echo [LOI] Khong tim thay csc.exe tai: %CSC%
    exit /b 1
)

echo [1/2] Tao runtime va goi du lieu game...
call "%~dp0make_exe.bat"
if %ERRORLEVEL% neq 0 (
    echo [LOI] make_exe that bai!
    exit /b 1
)

echo [2/2] Dang dong goi tat ca vao 1 file EXE duy nhat: dist\8BallPool_Game.exe...
"%CSC%" /target:winexe ^
    /r:System.Windows.Forms.dll ^
    /r:System.IO.Compression.FileSystem.dll ^
    /r:System.IO.Compression.dll ^
    /resource:"%~dp0dist\BilliardsClient_Game.zip",GameArchive.zip ^
    /out:"%~dp0dist\8BallPool_Game.exe" ^
    "%~dp0StandaloneLauncher.cs"

if %ERRORLEVEL% equ 0 (
    copy /Y "%~dp0dist\8BallPool_Game.exe" "%~dp0..\8BallPool_Game.exe" >nul
    echo.
    echo ====================================================
    echo   DONG GOI THANH CONG 1 FILE EXE DUY NHAT!
    echo   1. Thu muc goc: %~dp0..\8BallPool_Game.exe
    echo   2. Thu muc dist: %~dp0dist\8BallPool_Game.exe
    echo ====================================================
    exit /b 0
) else (
    echo [LOI] Dong goi that bai!
    exit /b 1
)
