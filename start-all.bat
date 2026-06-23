@echo off
echo ==========================================
echo   AIChat 一键启动脚本
echo ==========================================

REM === 1. MySQL（通常已作为服务运行，尝试启动） ===
echo [1/5] MySQL...
net start MySQL80 >nul 2>&1
echo MySQL 服务已就绪

REM === 2. Redis ===
echo [2/5] Redis...
REM 请将下一行的路径改为你的 redis-server.exe 真实路径
start "Redis" /min "D:\tools\Redis-7.4.0-Windows-x64-msys2\redis-server.exe"
echo Redis 已启动

REM === 3. RocketMQ ===
echo [3/5] RocketMQ...
REM 请将下一行的路径改为你的 RocketMQ bin 目录
start "RocketMQ-NameServer" /min cmd /c "cd /d D:\tools\rocketmq-all-5.5.0-bin-release\bin && mqnamesrv.cmd"
timeout /t 3 >nul
start "RocketMQ-Broker" /min cmd /c "cd /d D:\tools\rocketmq-all-5.5.0-bin-release\bin && mqbroker.cmd -n localhost:9876 -c ../conf/broker.conf"
echo RocketMQ 已启动

REM === 4. Elasticsearch ===
echo [4/5] Elasticsearch...
REM 请将下一行的路径改为你的 Elasticsearch bin 目录
start "Elasticsearch" /min cmd /c "cd /d D:\tools\elasticsearch-9.4.2-windows-x86_64\bin && elasticsearch.bat"
echo Elasticsearch 已启动

REM === 等待中间件就绪 ===
echo 等待中间件启动（30秒）...
timeout /t 30 >nul

REM === 5. 后端 ===
echo [5/5] 启动后端和前端...
cd /d D:\AIChat\projects\aichat
start "AIChat-Backend" /min cmd /c "mvn spring-boot:run"
echo 后端正在启动...

REM === 6. 前端 ===
cd /d D:\AIChat\projects\aichat\frontend
start "AIChat-Frontend" /min cmd /c "npm run dev"
echo 前端正在启动...

echo ==========================================
echo  全部服务已启动！
echo  后端: http://localhost:8080
echo  前端: http://localhost:5173
echo  请等待 10-20 秒后访问前端页面。
echo ==========================================
pause