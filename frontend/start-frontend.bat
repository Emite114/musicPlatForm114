@echo off
setlocal

cd /d "%~dp0"
title Music Platform Frontend Dev Server

echo ==========================================
echo   Music Platform Frontend
echo ==========================================
echo.

where npm.cmd >nul 2>nul
if errorlevel 1 (
  echo [ERROR] 未找到 npm.cmd，请先安装 Node.js。
  echo.
  pause
  exit /b 1
)

if not exist "node_modules" (
  echo [INFO] 首次运行，正在安装前端依赖...
  call npm.cmd install
  if errorlevel 1 (
    echo.
    echo [ERROR] 依赖安装失败。
    pause
    exit /b 1
  )
)

echo [INFO] 正在启动前端开发服务器...
echo [INFO] 启动后请在浏览器访问 http://localhost:5173/#/
echo.

call npm.cmd run dev

echo.
echo [INFO] 前端服务已退出。
pause
