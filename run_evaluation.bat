@echo off
setlocal enabledelayedexpansion

echo ========================================
echo   Deployment Analyzer - Evaluation
echo ========================================
echo.

echo [1/4] Baue Projekt (mvn clean package)...
call mvn clean package -q
if errorlevel 1 (
    echo FEHLER beim Bauen! Abbruch.
    exit /b 1
)
echo Fertig!
echo.

echo [2/4] Fuehre alle JMH Benchmarks aus (nur 1 Durchlauf)...
echo (Dauert ca. 5 Minuten - bitte warten...)
java -cp target/benchmarks.jar com.deployment.benchmark.RunAllBenchmarks
if errorlevel 1 (
    echo FEHLER bei Benchmarks! Abbruch.
    exit /b 1
)
echo Fertig!
echo.

echo [3/4] Erstelle Zusammenfassung und Boxplots...
py create_summary.py
py create_boxplots.py
echo.

echo [4/4] Fuehre interaktive Demo aus (mvn exec:java)...
call mvn exec:java -q
echo.

echo ========================================
echo   Evaluation abgeschlossen!
echo ========================================
echo.
echo Ergebnisse:
echo   - all_raw_results.json
echo   - summary_results.csv
echo   - boxplot_algorithm.png
echo   - boxplot_algorithm_log.png
echo   - boxplot_full_program_log.png
echo   - dependency_graph.svg
echo   - deployment_result.json

endlocal