@echo off
setlocal enabledelayedexpansion

echo ========================================
echo   Deployment Analyzer - Demo
echo   (Graph, Reihenfolge, Visualisierung)
echo ========================================
echo.

echo [1/2] Baue Projekt (mvn clean package)...
call mvn clean package -q
if errorlevel 1 (
    echo FEHLER beim Bauen! Abbruch.
    exit /b 1
)
echo Fertig!
echo.

echo [2/2] Fuehre Programm aus (mvn exec:java)...
call mvn exec:java -q
echo.

echo ========================================
echo   Fertig!
echo ========================================
echo.
echo Ergebnisse:
echo   - dependency_graph.svg  (Graph-Visualisierung)
echo   - deployment_result.json  (maschinenlesbares Ergebnis)
echo.
echo Hinweis: Fuer praezise JMH-Werte im Vergleich
echo zuerst run_algorithm_benchmark.bat und/oder
echo run_full_program_benchmark.bat ausfuehren!

endlocal