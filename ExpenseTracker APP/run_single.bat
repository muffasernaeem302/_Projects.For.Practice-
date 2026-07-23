@echo off
title ExpenseTracker Launcher
echo ============================================
echo    ExpenseTracker - Single File Application
echo ============================================
echo.

echo Compiling ExpenseTracker Single-File App...
javac ExpenseTrackerApp.java
if %errorlevel% neq 0 (
    echo.
    echo Compilation failed! Please check for errors.
    pause
    exit /b 1
)

echo Starting ExpenseTracker GUI...
start "ExpenseTracker" java ExpenseTrackerApp
echo.
echo Application launched! Close the window or press any key to exit this launcher.
pause >nul
