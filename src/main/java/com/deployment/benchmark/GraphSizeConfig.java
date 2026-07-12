package com.deployment.benchmark;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Zentrale Zuordnung: Label (wie in JMH @Param) 
 * <-> YAML Dateipfad.
 *
 * WICHTIG: Diese Map wird sowohl von den 
 * Benchmark-Klassen (AlgorithmBenchmark, 
 * FullProgramBenchmark) als auch von Main.java 
 * genutzt, damit die CSV-Ergebnisse automatisch 
 * der richtigen YAML-Datei zugeordnet werden können.
 *
 * Neue Testdatei hinzufuegen:
 * 1. Hier einen neuen Eintrag ergaenzen
 * 2. mvn clean package
 * 3. Benchmark einmal ausfuehren (erzeugt CSV-Eintrag)
 * 4. Danach zeigt mvn exec:java automatisch 
 *    die praezisen JMH-Werte fuer diese Datei
 */
public class GraphSizeConfig {

    public static final Map<String, String> LABEL_TO_FILE =
        new LinkedHashMap<>();

    static {
        LABEL_TO_FILE.put("simple",
            "src/main/resources/examples/simple.yaml");
        LABEL_TO_FILE.put("medium",
            "src/main/resources/examples/medium.yaml");
        LABEL_TO_FILE.put("large",
            "src/main/resources/examples/large.yaml");
        LABEL_TO_FILE.put("xlarge",
            "src/main/resources/examples/xlarge.yaml");
        LABEL_TO_FILE.put("xxlarge",
            "src/main/resources/examples/xxlarge.yaml");
        LABEL_TO_FILE.put("xxxlarge",
            "src/main/resources/examples/xxxlarge.yaml");
        LABEL_TO_FILE.put("xxxxlarge",
            "src/main/resources/examples/xxxxlarge.yaml");

        // Neue eigene Datei hier registrieren, z.B.:
        // LABEL_TO_FILE.put("realworld",
        //     "src/main/resources/examples/realworld.yaml");
    }

    /**
     * Findet das Label zu einem gegebenen Dateipfad.
     * Gibt null zurueck wenn die Datei nicht 
     * registriert ist.
     */
    public static String getLabelForFile(String filePath) {
        String normalized = filePath.replace("\\", "/");
        for (Map.Entry<String, String> entry :
                LABEL_TO_FILE.entrySet()) {
            if (entry.getValue().replace("\\", "/")
                    .equals(normalized)) {
                return entry.getKey();
            }
        }
        return null;
    }
}