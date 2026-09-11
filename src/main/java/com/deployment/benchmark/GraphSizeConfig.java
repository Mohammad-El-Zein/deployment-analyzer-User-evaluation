package com.deployment.benchmark;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Diese Klasse registriert die YAML-Dateien, die fuür die Benchmark-Tests
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
            LABEL_TO_FILE.put("showcase-10-services",
            "src/main/resources/examples/showcase-10-services.yaml");

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