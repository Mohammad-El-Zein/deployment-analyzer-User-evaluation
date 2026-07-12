@echo off
setlocal enabledelayedexpansion

echo ========================================
echo   Gesamtprogramm-Benchmark
echo   (Kahn-Version vs DFS-Version)
echo ========================================
echo.

echo [1/3] Baue Projekt (mvn clean package)...
call mvn clean package -q
if errorlevel 1 (
    echo FEHLER beim Bauen! Abbruch.
    exit /b 1
)
echo Fertig!
echo.

echo [2/3] Fuehre Gesamtprogramm-Benchmark aus...
echo (Dauert ca. 2-3 Minuten - bitte warten...)
java -cp target/benchmarks.jar com.deployment.benchmark.RunFullProgramBenchmark
if errorlevel 1 (
    echo FEHLER beim Benchmark! Abbruch.
    exit /b 1
)
echo Fertig!
echo.

echo [3/3] Erstelle Zusammenfassung und Boxplot...
py create_summary.py
py create_boxplots.py
echo.

echo ========================================
echo   Fertig!
echo ========================================
echo.
echo Ergebnisse:
echo   - all_raw_results.json
echo   - summary_results.csv
echo   - boxplot_full_program_log.png

endlocal