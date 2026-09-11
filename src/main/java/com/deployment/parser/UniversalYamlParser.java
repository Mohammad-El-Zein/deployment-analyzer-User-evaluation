package com.deployment.parser;

import org.yaml.snakeyaml.Yaml;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

/**
 * Universeller Parser der Abhaengigkeiten und geschuetzten Kanten aus verschiedenen YAML-Formaten einliest.
 *
 * Unterstuetzt zwei Formate im selben Ordner
 * (auch gemischt):
 *
 * 1. Docker Compose Format:
 *    services:
 *      auth-service:
 *        depends_on: [database, redis]
 *        labels: // nur wenn geschuetzte Abhaengigkeiten vorhanden sind, wenn keine geschuetzten Abhaengigkeiten, User muss nicht etwas manuellschreiben
 *          - "deployment-analyzer.protected-dependencies=database"  
 *
 * 2. Kubernetes Deployment Format:
 *    metadata:
 *      annotations:
 *        deployment-analyzer/depends-on: "database,redis"
 *        deployment-analyzer/protected-dependencies: "database"
 *
 */
public class UniversalYamlParser {

    private static final String K8S_DEPENDS_ON_ANNOTATION =
        "deployment-analyzer/depends-on";
    private static final String K8S_PROTECTED_ANNOTATION =
        "deployment-analyzer/protected-dependencies";

    private static final String DOCKER_PROTECTED_LABEL_PREFIX =
        "deployment-analyzer.protected-dependencies=";

    private Set<String> lastProtectedEdges = new HashSet<>();

    public Map<String, List<String>> parseDirectory(
            String directoryPath) {

        Map<String, List<String>> dependencies =
            new HashMap<>();
        Set<String> protectedEdges = new HashSet<>();

        File dir = new File(directoryPath);
        File[] files = dir.listFiles(
            (d, name) -> name.endsWith(".yaml")
                      || name.endsWith(".yml"));

        if (files == null) {
            System.out.println(
                "Fehler: Ordner nicht gefunden oder leer: "
                + directoryPath);
            return dependencies;
        }

        for (File file : files) {
            try (FileInputStream fis =
                    new FileInputStream(file)) {

                Yaml yaml = new Yaml();
                Map<String, Object> data = yaml.load(fis);

                if (data == null) continue;

                if (isDockerComposeFormat(data)) {
                    parseDockerComposeFile(
                        data, dependencies, protectedEdges);
                    System.out.println(
                        "  " + file.getName()
                        + " -> Docker Compose Format erkannt");
                } else if (isKubernetesFormat(data)) {
                    parseKubernetesFile(
                        data, dependencies, protectedEdges);
                    System.out.println(
                        "  " + file.getName()
                        + " -> Kubernetes Format erkannt");
                } else {
                    System.out.println(
                        "  Warnung: " + file.getName()
                        + " hat weder Docker Compose "
                        + "noch Kubernetes Format - "
                        + "wird ignoriert.");
                }

            } catch (Exception e) {
                System.out.println(
                    "  Warnung: Konnte " + file.getName()
                    + " nicht lesen: " + e.getMessage());
            }
        }

        this.lastProtectedEdges = protectedEdges;
        return dependencies;
    }

    /**
     * Gibt die geschuetzten Kanten der zuletzt
     * geparsten Dateien zurueck. Muss NACH
     * parseDirectory() aufgerufen werden.
     */
    public Set<String> getProtectedEdges() {
        return lastProtectedEdges;
    }

    private boolean isDockerComposeFormat(
            Map<String, Object> data) {
        return data.containsKey("services");
    }

    private boolean isKubernetesFormat(
            Map<String, Object> data) {
        Object kind = data.get("kind");
        return "Deployment".equals(kind);
    }

    @SuppressWarnings("unchecked")
    private void parseDockerComposeFile(
            Map<String, Object> data,
            Map<String, List<String>> dependencies,
            Set<String> protectedEdges) {

        Map<String, Object> services =
            (Map<String, Object>) data.get("services");

        for (String serviceName : services.keySet()) {

            Map<String, Object> serviceData =
                (Map<String, Object>) services.get(
                    serviceName);

            List<String> deps = new ArrayList<>();

            if (serviceData != null &&
                serviceData.containsKey("depends_on")) {
                deps = (List<String>) serviceData.get(
                    "depends_on");
            }

            dependencies.put(serviceName, deps);

            // Protected Dependencies aus "labels" lesen
            if (serviceData != null &&
                serviceData.containsKey("labels")) {

                List<String> labels =
                    (List<String>) serviceData.get("labels");

                for (String label : labels) {
                    if (label.startsWith(
                            DOCKER_PROTECTED_LABEL_PREFIX)) {

                        String protectedValue = label
                            .substring(
                                DOCKER_PROTECTED_LABEL_PREFIX
                                    .length());

                        for (String protectedDep :
                                protectedValue.split(",")) {
                            String trimmed =
                                protectedDep.trim();
                            if (!trimmed.isEmpty()) {
                                // Kante: protectedDep -> serviceName
                                protectedEdges.add(
                                    trimmed + " -> "
                                    + serviceName);
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void parseKubernetesFile(
            Map<String, Object> data,
            Map<String, List<String>> dependencies,
            Set<String> protectedEdges) {

        Map<String, Object> metadata =
            (Map<String, Object>) data.get("metadata");
        if (metadata == null) return;

        String serviceName = (String) metadata.get("name");
        if (serviceName == null) return;

        Map<String, Object> annotations =
            (Map<String, Object>) metadata.get(
                "annotations");

        List<String> deps = new ArrayList<>();

        if (annotations != null) {

            // depends_on aus Annotation lesen
            if (annotations.containsKey(
                    K8S_DEPENDS_ON_ANNOTATION)) {

                String dependsOnValue = annotations
                    .get(K8S_DEPENDS_ON_ANNOTATION)
                    .toString();

                for (String dep : dependsOnValue.split(",")) {
                    String trimmed = dep.trim();
                    if (!trimmed.isEmpty()) {
                        deps.add(trimmed);
                    }
                }
            }

            // protected_dependencies aus Annotation lesen
            if (annotations.containsKey(
                    K8S_PROTECTED_ANNOTATION)) {

                String protectedValue = annotations
                    .get(K8S_PROTECTED_ANNOTATION)
                    .toString();

                for (String protectedDep :
                        protectedValue.split(",")) {
                    String trimmed = protectedDep.trim();
                    if (!trimmed.isEmpty()) {
                        // Kante: protectedDep -> serviceName
                        protectedEdges.add(
                            trimmed + " -> " + serviceName);
                    }
                }
            }
        }

        dependencies.put(serviceName, deps);
    }
}