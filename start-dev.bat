@echo off
echo ==========================================================
echo   Iniciando Buzon UCC - Voz Estudiantil (Full Stack)
echo ==========================================================

start "Buzon UCC - Backend (Spring Boot :8081)" cmd /k "cd /d %~dp0backend && C:\Users\Valentina\.m2\wrapper\dists\apache-maven-3.9.16\0daed3be3ebd1c706f0e69e8b07c6b73f5cc4ea3dfce72a8d0ec2e849ca2ddb0\bin\mvn.cmd spring-boot:run"

timeout /t 5 /nobreak >nul

start "Buzon UCC - Frontend (Vite :5173)" cmd /k "cd /d %~dp0frontend && npm.cmd run dev"

echo.
echo ==========================================================
echo   Aplicacion iniciada en ventanas separadas:
echo   - Frontend: http://localhost:5173
echo   - Backend:  http://localhost:8081
echo   - Swagger:  http://localhost:8081/api/v1/swagger-ui
echo ==========================================================
