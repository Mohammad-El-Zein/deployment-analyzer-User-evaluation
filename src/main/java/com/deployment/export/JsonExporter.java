package com.deployment.export;

import com.deployment.model.Graph;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Exportiert das komplette Analyse-Ergebnis
 * als maschinenlesbare JSON Datei.
 *
 * Dieses Format kann von anderen Tools
 * (z.B. Deployment-Skripten, CI/CD Pipelines)
 * automatisch eingelesen werden.
 */
public class JsonExporter {

    public static void export(
            String filePath,
            Graph graph,
            boolean hasCycle,
            List<List<String>> cycles,
            List<String> removedEdges,
            List<String> deploymentOrder,
            List<List<String>> parallelLevels) {

        StringBuilder json = new StringBuilder();
        json.append("{\n");

        json.append("  \"services\": ")
            .append(graph.getNodeCount()).append(",\n");

        json.append("  \"hasCycle\": ")
            .append(hasCycle).append(",\n");

        // Gefundene Zyklen
        json.append("  \"sccsWithCycles\": [\n");
        if (cycles != null) {
            for (int i = 0; i < cycles.size(); i++) {
                json.append("    [")
                    .append(toJsonStringArray(cycles.get(i)))
                    .append("]");
                if (i < cycles.size() - 1) json.append(",");
                json.append("\n");
            }
        }
        json.append("  ],\n");

        // Entfernte Kanten (Feedback Arc Set)
        json.append("  \"removedEdges\": [");
        if (removedEdges != null && !removedEdges.isEmpty()) {
            json.append("\n");
            for (int i = 0; i < removedEdges.size(); i++) {
                String[] parts = removedEdges.get(i).split(" -> ");
                json.append("    {\"from\": \"")
                    .append(parts[0])
                    .append("\", \"to\": \"")
                    .append(parts[1])
                    .append("\"}");
                if (i < removedEdges.size() - 1) json.append(",");
                json.append("\n");
            }
            json.append("  ");
        }
        json.append("],\n");

        // Deployment-Reihenfolge
        json.append("  \"deploymentOrder\": [");
        if (deploymentOrder != null && !deploymentOrder.isEmpty()) {
            json.append("\n    ")
                .append(toJsonStringArray(deploymentOrder))
                .append("\n  ");
        }
        json.append("],\n");

        // Parallele Deployment-Gruppen (Level-BFS)
        json.append("  \"parallelGroups\": {");
        if (parallelLevels != null && !parallelLevels.isEmpty()) {
            json.append("\n");
            for (int i = 0; i < parallelLevels.size(); i++) {
                json.append("    \"level").append(i).append("\": [")
                    .append(toJsonStringArray(parallelLevels.get(i)))
                    .append("]");
                if (i < parallelLevels.size() - 1) json.append(",");
                json.append("\n");
            }
            json.append("  ");
        }
        json.append("}\n");

        json.append("}\n");

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(json.toString());
            System.out.println(
                " JSON Ergebnis geschrieben: " + filePath);
        } catch (IOException e) {
            System.out.println(
                " Fehler beim Schreiben der JSON Datei: "
                + e.getMessage());
        }
    }

    private static String toJsonStringArray(List<String> items) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            sb.append("\"").append(items.get(i)).append("\"");
            if (i < items.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }
}