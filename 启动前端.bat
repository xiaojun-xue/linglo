@echo off
title IPD Platform - Frontend
echo ================================================
echo  IPD研发管理平台 - 前端服务启动脚本
echo ================================================
echo.
echo 启动 Vite 开发服务器...
echo.
cd /d "%~dp0src\frontend"
pnpm run dev
pause
