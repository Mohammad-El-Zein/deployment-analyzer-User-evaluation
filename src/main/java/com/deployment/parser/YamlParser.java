package com.deployment.parser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.yaml.snakeyaml.Yaml;

/**
 * Liest eine YAML Datei ein und extrahiert alle Services,
 * ihre Abhängigkeiten und optional geschützte
 * (nicht löschbare) Abhängigkeiten.
 *
 * Schritt 1: YamlParser liest YAML
 *          ↓
 *          gibt zurück:
 *          dependencies: Service -> Liste seiner Abhängigkeiten
 *          protectedEdges: Set von "dep -> service" Kanten
 *          die vom Feedback Arc Set NICHT entfernt
 *          werden duerfen
 *          ↓
 * Schritt 2: Graph nimmt dependencies
 * Schritt 3: FeedbackArcSet nutzt protectedEdges
 */
public class YamlParser {

    // Zuletzt geparste geschuetzte Kanten
    // (Format: "from -> to", z.B. "user-service -> auth-service")
    private Set<String> lastProtectedEdges = new HashSet<>();

    /**
     * Liest die YAML Datei und gibt eine Map zurück:
     * Service → Liste seiner Abhängigkeiten
     */
    public Map<String, List<String>> parse(String filePath) {

        Map<String, List<String>> dependencies = new HashMap<>();
        Set<String> protectedEdges = new HashSet<>();

        try {
            Yaml yaml = new Yaml();
            FileInputStream file = new FileInputStream(filePath);

            Map<String, Object> data = yaml.load(file);

            if (data == null || !data.containsKey("services")) {
                System.out.println(
                    "Fehler: Kein 'services' in YAML gefunden!");
                return dependencies;
            }

            Map<String, Object> services =
                (Map<String, Object>) data.get("services");

            for (String serviceName : services.keySet()) {

                Map<String, Object> serviceData =
                    (Map<String, Object>) services.get(serviceName);

                // depends_on holen
                List<String> deps = new ArrayList<>();

                if (serviceData != null &&
                    serviceData.containsKey("depends_on")) {
                    deps = (List<String>) serviceData.get("depends_on");
                }

                dependencies.put(serviceName, deps);

                // protected_dependencies holen (optional)
                if (serviceData != null &&
                    serviceData.containsKey(
                        "protected_dependencies")) {

                    List<String> protectedDeps =
                        (List<String>) serviceData.get(
                            "protected_dependencies");

                    for (String protectedDep : protectedDeps) {
                        // Die Kante im Graph geht von
                        // protectedDep -> serviceName
                        // (weil serviceName depends_on protectedDep)
                        String edge = protectedDep
                            + " -> " + serviceName;
                        protectedEdges.add(edge);
                    }
                }
            }

        } catch (FileNotFoundException e) {
            System.out.println(
                "Fehler: YAML Datei nicht gefunden: " + filePath);
        } catch (Exception e) {
            System.out.println(
                "Fehler beim Einlesen: " + e.getMessage());
        }

        this.lastProtectedEdges = protectedEdges;
        return dependencies;
    }

    /**
     * Gibt die geschuetzten Kanten der zuletzt
     * geparsten Datei zurueck. Muss NACH parse()
     * aufgerufen werden.
     */
    public Set<String> getProtectedEdges() {
        return lastProtectedEdges;
    }
}