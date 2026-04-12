@echo off
title IPD Platform - Backend
echo ================================================
echo  IPD研发管理平台 - 后端服务启动脚本
echo ================================================
echo.

:: 设置 Java 和 Maven 路径
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot"
set "MAVEN_HOME=C:\ProgramData\chocolatey\lib\maven\apache-maven-3.9.14"
set "PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%"

echo [1/3] 检查 Java...
java -version
echo.

echo [2/3] 检查 Maven...
mvn -version
echo.

echo [3/3] 启动 Spring Boot...
echo 编译并启动中（首次运行需要下载依赖，请耐心等待...）
echo.

cd /d "%~dp0src\backend"
mvn spring-boot:run

pause
