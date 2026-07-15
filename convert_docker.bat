@echo off
setlocal enabledelayedexpansion

set INPUT_DIR=src\main\resources\examples\docker-manifests
set OUTPUT_FILE=converted-docker.yaml

echo ========================================
echo   Docker Compose zu internem Format
echo ========================================
echo.

if not exist "target\classes" (
    echo [1/3] Baue Projekt ^(einmalig^)...
    call mvn clean package -q
    echo Fertig!
    echo.
)

if not exist "target\dependency" (
    echo [2/3] Lade Abhaengigkeiten ^(einmalig^)...
    call mvn dependency:copy-dependencies -DoutputDirectory=target/dependency -q
    echo Fertig!
    echo.
)

echo [3/3] Konvertiere Docker Compose Dateien aus: %INPUT_DIR%
java -cp "target/classes;target/dependency/*" com.deployment.ConvertToInternalFormat %INPUT_DIR% %OUTPUT_FILE%

echo.
echo ========================================
echo   Fertig!
echo ========================================
echo Naechster Schritt: Trage in Main.java ein:
echo   String filePath = "%OUTPUT_FILE%";

endlocal