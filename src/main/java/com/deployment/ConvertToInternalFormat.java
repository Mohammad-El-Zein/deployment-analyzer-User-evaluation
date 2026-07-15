package com.deployment;

import com.deployment.parser.UniversalYamlParser;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Standalone-Tool: Liest Kubernetes/Docker Compose
 * Dateien aus einem Ordner und konvertiert sie
 * automatisch in das interne YAML-Format des
 * Deployment Analyzers - inklusive geschuetzter
 * Abhaengigkeiten (protected_dependencies).
 *
 * Nutzung:
 *   java -cp target/classes com.deployment.ConvertToInternalFormat
 *        <input-ordner> <output-datei>
 */
public class ConvertToInternalFormat {

    public static void main(String[] args) {

        if (args.length < 2) {
            System.out.println(
                "Nutzung: java -cp target/classes "
                + "com.deployment.ConvertToInternalFormat "
                + "<input-ordner> <output-datei>");
            return;
        }

        String inputDir = args[0];
        String outputFile = args[1];

        System.out.println(
            "Lese Kubernetes/Docker Compose Dateien aus: "
            + inputDir);
        System.out.println();

        UniversalYamlParser parser =
            new UniversalYamlParser();
        Map<String, List<String>> dependencies =
            parser.parseDirectory(inputDir);
        Set<String> protectedEdges =
            parser.getProtectedEdges();

        if (dependencies.isEmpty()) {
            System.out.println();
            System.out.println(
                "Keine Abhaengigkeiten gefunden!");
            return;
        }

        try (FileWriter writer =
                new FileWriter(outputFile)) {

            writer.write("version: \"3\"\n");
            writer.write("services:\n");

            for (String service : dependencies.keySet()) {
                writer.write("  " + service + ":\n");

                List<String> deps =
                    dependencies.get(service);

                if (deps.isEmpty()) {
                    writer.write("    depends_on: []\n");
                } else {
                    writer.write("    depends_on:\n");
                    for (String dep : deps) {
                        writer.write("      - " + dep + "\n");
                    }
                }

                List<String> protectedForThisService =
                    findProtectedForService(
                        service, protectedEdges);

                if (!protectedForThisService.isEmpty()) {
                    writer.write("    protected_dependencies:\n");
                    for (String protectedDep :
                            protectedForThisService) {
                        writer.write(
                            "      - " + protectedDep + "\n");
                    }
                }
            }

            System.out.println(
                "Erfolgreich konvertiert: " + outputFile);
            System.out.println(
                "Services gefunden: " + dependencies.size());
            System.out.println(
                "Geschuetzte Kanten gefunden: "
                + protectedEdges.size());
            System.out.println();
            System.out.println(
                "Naechster Schritt: Trage in Main.java ein:");
            System.out.println(
                "  String filePath = \"" + outputFile + "\";");

        } catch (Exception e) {
            System.out.println(
                "Fehler beim Schreiben: " + e.getMessage());
        }
    }

    private static List<String> findProtectedForService(
            String service, Set<String> protectedEdges) {

        List<String> result = new ArrayList<>();

        for (String edge : protectedEdges) {
            String[] parts = edge.split(" -> ");
            if (parts.length == 2
                    && parts[1].equals(service)) {
                result.add(parts[0]);
            }
        }

        return result;
    }
}