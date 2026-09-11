package com.deployment;

import com.deployment.algorithm.*;
import com.deployment.model.Graph;
import com.deployment.parser.YamlParser;
import java.util.*;
import java.util.Locale;

/**
 * Evaluation: Vergleich Kahn vs DFS
 * Jeder Test wird 10 mal wiederholt und wird durchschnittliche Laufzeit und Speicherverbrauch berechnet.
 */
public class EvaluationRunner {

    private static final int RUNS = 10;

    public static void main(String[] args) {

        String[] testFiles = {
            "simple.yaml",
            "medium.yaml",
            "large.yaml",
            "xlarge.yaml",
            "xxlarge.yaml"
        };

        System.out.println(
            "#=====================================#");
        System.out.println(
            "#          EVALUATION RUNNER          #");
        System.out.println(
            "#   Wiederholungen pro Test: " + RUNS + "        #");
        System.out.println(
            "#=====================================#");
        System.out.println();

        for (String fileName : testFiles) {

            String filePath =
                "src/main/resources/examples/"
                + fileName;

            try {
                // YAML einlesen
                YamlParser parser = new YamlParser();
                Map<String, List<String>> deps =
                    parser.parse(filePath);

                if (deps.isEmpty()) continue;

                // Graph aufbauen
                Graph graph = new Graph(deps);

                // Kahn 10 mal ausführen
                long kahnTotal = 0;
                long kahnMemory = 0;

                for (int run = 0; run < RUNS; run++) {
                    KahnAlgorithm kahn =
                        new KahnAlgorithm();
                    kahn.execute(graph);
                    kahnTotal += kahn.getExecutionTime();
                    kahnMemory += kahn.getMemoryUsed();
                }

                long kahnAvg = kahnTotal / RUNS;
                long kahnMemAvg = kahnMemory / RUNS;

                // DFS 10 mal ausführen
                long dfsTotal = 0;
                long dfsMemory = 0;

                for (int run = 0; run < RUNS; run++) {
                    DFSTopologicalSort dfs =
                        new DFSTopologicalSort();
                    dfs.execute(graph);
                    dfsTotal += dfs.getExecutionTime();
                    dfsMemory += dfs.getMemoryUsed();
                }

                long dfsAvg = dfsTotal / RUNS;
                long dfsMemAvg = dfsMemory / RUNS;

                String schneller =
                    kahnAvg < dfsAvg ? "Kahn" : "DFS";

                String wenigerSpeicher =
                    kahnMemAvg < dfsMemAvg ? "Kahn" : "DFS";

                // Ausgabe
                System.out.println(
                    "#=====================================#");
                System.out.printf(
                    "# Datei: %-30s #%n", fileName);
                System.out.println(
                    "#=====================================#");
                System.out.println();
                System.out.printf(
                    " Services: %d%n",
                    graph.getNodeCount());
                System.out.println(
                    "--------------------------------------");
                System.out.printf(
                    " %-18s %-18s %-18s%n",
                    "", "Kahn", "DFS");
                System.out.println(
                    "--------------------------------------");
                System.out.printf(
                    " %-18s %-18s %-18s%n",
                    "Laufzeit (avg)",
                    kahnAvg + " ns",
                    dfsAvg + " ns");
                System.out.printf(
                    " %-18s %-18s %-18s%n",
                    "in ms",
                    String.format(Locale.US, "%.3f ms",
                        kahnAvg / 1_000_000.0),
                    String.format(Locale.US, "%.3f ms",
                        dfsAvg / 1_000_000.0));
                System.out.printf(
                    " %-18s %-18s %-18s%n",
                    "Speicher (avg)",
                    String.format(Locale.US, "%.2f KB",
                        kahnMemAvg / 1024.0),
                    String.format(Locale.US, "%.2f KB",
                        dfsMemAvg / 1024.0));
                System.out.println(
                    "--------------------------------------");
                System.out.printf(
                    " Schneller        : %s%n", schneller);
                System.out.printf(
                    " Weniger Speicher : %s%n",
                    wenigerSpeicher);
                System.out.println(
                    "--------------------------------------");
                System.out.println();

            } catch (Exception e) {
                System.out.println(
                    " Fehler: " + e.getMessage());
            }
        }

        System.out.println(
            "#=====================================#");
        System.out.println(
            "#      Evaluation abgeschlossen       #");
        System.out.println(
            "#=====================================#");
    }
}