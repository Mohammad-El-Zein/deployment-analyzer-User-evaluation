package com.deployment;

import java.util.List;
import java.util.Locale;

import com.deployment.algorithm.DFSTopologicalSort;
import com.deployment.algorithm.FeedbackArcSet;
import com.deployment.algorithm.KahnAlgorithm;
import com.deployment.algorithm.LevelBFS;
import com.deployment.algorithm.TarjanAlgorithm;
import com.deployment.model.Graph;

/**
 * Zuständig für die formatierte Ausgabe
 * aller Ergebnisse
 */
public class ResultPrinter {

    private static final String LINE =
        "#=====================================#";

    private static final String THIN_LINE =
        "--------------------------------------";

    /**
     * Graph Übersicht ausgeben
     */
        public void printGraph(Graph graph) {
            System.out.println();
            System.out.println();
            System.out.println(LINE);
            System.out.println("#          GRAPH UEBERSICHT           #");
            System.out.println(LINE);
            System.out.println(
                " Services: " + graph.getNodeCount());
            System.out.println();
            System.out.println(
                " Siehe dependency_graph.svg fuer die visuelle Darstellung");
            System.out.println(
                " Siehe deployment_result.json fuer das maschinenlesbare Ergebnis");
            System.out.println(THIN_LINE + "---------------");
            System.out.println();
        }

    /**
     * Kahn Ergebnis ausgeben
     */
    public void printKahn(KahnAlgorithm kahn) {
        System.out.println();
        System.out.println(LINE);
        System.out.println("#      KAHN'S ALGORITHMUS (BFS)       #");
        System.out.println(LINE);

        if (kahn.hasCycle()) {
            System.out.println(
                " [FEHLER] Zyklus gefunden");
            System.out.println(
                " Deployment nicht moeglich.");
        } else {
            System.out.println(
                " [OK] Kein Zyklus gefunden");
            System.out.println(THIN_LINE);
            System.out.println(
                " Deployment-Reihenfolge:");
            System.out.println();
            List<String> order = kahn.getDeploymentOrder();
            for (int i = 0; i < order.size(); i++) {
                System.out.println(
                    "   " + (i + 1) + ". " + order.get(i));
            }
        }
    System.out.println(THIN_LINE);
    System.out.println(
        " Speicher : " + kahn.getMemoryFormatted());
    System.out.println(THIN_LINE);
    System.out.println();
    }

    /**
     * DFS Ergebnis ausgeben
     */
    public void printDFS(DFSTopologicalSort dfs) {
        System.out.println();
        System.out.println(LINE);
        System.out.println("#     DFS TOPOLOGISCHE SORTIERUNG     #");
        System.out.println(LINE);

        if (dfs.hasCycle()) {
            System.out.println(
                " [FEHLER] Zyklus gefunden");
            System.out.println(
                " Deployment nicht moeglich.");
        } else {
            System.out.println(
                " [OK] Kein Zyklus gefunden!");
            System.out.println(THIN_LINE);
            System.out.println(
                " Deployment-Reihenfolge:");
            System.out.println();
            List<String> order = dfs.getDeploymentOrder();
            for (int i = 0; i < order.size(); i++) {
                System.out.println(
                    "   " + (i + 1) + ". " + order.get(i));
            }
        }
            System.out.println();
            System.out.println(THIN_LINE);
            System.out.println(
                " Speicher : " + dfs.getMemoryFormatted());
            System.out.println(THIN_LINE);
            System.out.println();
    }

    public void printComparison(
        KahnAlgorithm kahn,
        DFSTopologicalSort dfs,
        Graph graph,
        String currentFilePath) {

        System.out.println();
        System.out.println(LINE);
        System.out.println("#        VERGLEICH: KAHN vs DFS       #");
        System.out.println(LINE);
        System.out.println();
        System.out.println(
            " Graph-Groesse: "
            + graph.getNodeCount()
            + " Services");
        System.out.println();

        String label = com.deployment.benchmark
            .GraphSizeConfig.getLabelForFile(currentFilePath);

        String csvPath = "algorithm_results.csv";

        com.deployment.benchmark.CsvResultReader.JmhResult
            kahnJmh = null;
        com.deployment.benchmark.CsvResultReader.JmhResult
            dfsJmh = null;

        if (label != null) {
            kahnJmh = com.deployment.benchmark
                .CsvResultReader.findResult(
                    csvPath, label, "kahnAlgorithm");
            dfsJmh = com.deployment.benchmark
                .CsvResultReader.findResult(
                    csvPath, label, "dfsAlgorithm");
        }

        if (kahnJmh != null && dfsJmh != null) {

            System.out.println(
                " [JMH] Wissenschaftlich praezise Werte in Mikrosekunden "
                + "(aus " + csvPath + "):");
            System.out.println(THIN_LINE + "-------------------------");
            System.out.printf(
                " %-15s %-25s %-25s%n",
                "", "Kahn", "DFS");
            System.out.println(THIN_LINE + "-------------------------");
            System.out.printf(
                " %-15s %-25s %-25s%n",
                "Laufzeit",
                String.format(java.util.Locale.US,
                    "%.3f +/- %.3f %s",
                    kahnJmh.score, kahnJmh.error, kahnJmh.unit),
                String.format(java.util.Locale.US,
                    "%.3f +/- %.3f %s",
                    dfsJmh.score, dfsJmh.error, dfsJmh.unit));

            String faster = kahnJmh.score < dfsJmh.score
                ? "Kahn" : "DFS";
            System.out.println(THIN_LINE + "-------------------------");
            System.out.println(" Schneller (JMH): " + faster);
            System.out.println(THIN_LINE + "-------------------------");

        } else {

            System.out.println(
                " [Hinweis] Keine JMH-Benchmark-Daten fuer");
            System.out.println(
                " diese Datei gefunden. Zeige einfache");
            System.out.println(
                " Messung (nicht wissenschaftlich praezise):");
            System.out.println(THIN_LINE);
            System.out.printf(
                " %-15s %-20s %-20s%n",
                "", "Kahn", "DFS");
            System.out.println(THIN_LINE + "-------------------------");
            System.out.printf(
                " %-15s %-20s %-20s%n",
                "Laufzeit",
                kahn.getExecutionTime() + " ns",
                dfs.getExecutionTime() + " ns");
            System.out.printf(
                " %-15s %-20s %-20s%n",
                "Speicher",
                kahn.getMemoryFormatted(),
                dfs.getMemoryFormatted());

            String faster = kahn.getExecutionTime()
                < dfs.getExecutionTime() ? "Kahn" : "DFS";
            System.out.println();
            System.out.println(THIN_LINE);
            System.out.println(" Schneller (einfach): " + faster);
            System.out.println(
                " Fuer praezise Werte: fuege diese Datei");
            System.out.println(
                " zu GraphSizeConfig.java hinzu und fuehre");
            System.out.println(
                " den Benchmark einmal aus!");
            System.out.println(THIN_LINE);
        }

        System.out.println();
    }

    /**
     * Zeigt den Gesamtprogramm-Laufzeitvergleich
     * (Kahn-Version vs DFS-Version) basierend auf
     * den JMH FullProgramBenchmark Ergebnissen.
    */
    public void printFullProgramComparison(String currentFilePath) {

        System.out.println();
        System.out.println(LINE);
        System.out.println("#   GESAMTPROGRAMM: KAHN vs DFS        #");
        System.out.println(LINE);
        System.out.println();

        String label = com.deployment.benchmark
            .GraphSizeConfig.getLabelForFile(currentFilePath);

        String csvPath = "full_program_results.csv";

        com.deployment.benchmark.CsvResultReader.JmhResult
            kahnJmh = null;
        com.deployment.benchmark.CsvResultReader.JmhResult
            dfsJmh = null;

        if (label != null) {
            kahnJmh = com.deployment.benchmark
                .CsvResultReader.findResult(
                    csvPath, label, "fullProgramWithKahn");
            dfsJmh = com.deployment.benchmark
                .CsvResultReader.findResult(
                    csvPath, label, "fullProgramWithDFS");
        }

        if (kahnJmh != null && dfsJmh != null) {

            System.out.println(
                " [JMH] Gesamtlaufzeit in Millisekunden "
                + "(aus " + csvPath + "):");
            System.out.println(
                "(YAML lesen + Graph aufbauen + Sortieren + Level-BFS):");
            System.out.println(THIN_LINE + "-------------------------");
            System.out.printf(
                " %-15s %-25s %-25s%n",
                "", "mit Kahn", "mit DFS");
            System.out.println(THIN_LINE + "-------------------------");
            System.out.printf(
                " %-15s %-25s %-25s%n",
                "Laufzeit",
                String.format(java.util.Locale.US,
                    "%.3f +/- %.3f %s",
                    kahnJmh.score, kahnJmh.error, kahnJmh.unit),
                String.format(java.util.Locale.US,
                    "%.3f +/- %.3f %s",
                    dfsJmh.score, dfsJmh.error, dfsJmh.unit));

            String faster = kahnJmh.score < dfsJmh.score
                ? "Kahn-Version" : "DFS-Version";
            System.out.println(THIN_LINE + "-------------------------");
            System.out.println(
                " Schneller (Gesamtprogramm): " + faster);
            System.out.println(THIN_LINE + "-------------------------");

        } else {
            System.out.println(
                " [Hinweis] Keine JMH-Gesamtprogramm-Daten");
            System.out.println(
                " fuer diese Datei gefunden.");
            System.out.println(
                " Fuege diese Datei zu GraphSizeConfig.java");
            System.out.println(
                " hinzu und fuehre FullProgramBenchmark aus!");
        }

        System.out.println();
    }
    /**
     * Tarjan Ergebnis ausgeben
     */
    public void printTarjan(TarjanAlgorithm tarjan) {
        System.out.println();
        System.out.println(LINE);
        System.out.println("#        TARJAN'S ALGORITHMUS         #");
        System.out.println(LINE);
        System.out.println();

        if (!tarjan.hasCycles()) {
            System.out.println(
                " [OK] Kein Zyklus gefunden!");
        } else {
            System.out.println(
                " [FEHLER] Zyklen gefunden: "
                + tarjan.getCycles().size());
            System.out.println(THIN_LINE);
            System.out.println();

            List<List<String>> cycles = tarjan.getCycles();
            for (int i = 0; i < cycles.size(); i++) {
                System.out.println(
                    " Zyklus " + (i + 1) + ": "
                    + cycles.get(i));
            }
        }
        System.out.println();
        System.out.println();
    }

    /**
     * Feedback Arc Set Ergebnis ausgeben
     */
    public void printFAS(FeedbackArcSet fas) {
    System.out.println();
    System.out.println(LINE);
    System.out.println("#          FEEDBACK ARC SET           #");
    System.out.println(LINE);
    System.out.println();

    if (fas.getEdgesToRemove().isEmpty()
            && !fas.hasUnresolvableCycles()) {
        System.out.println(
            " [OK] Keine Kanten zu entfernen!");
    } else {

        if (!fas.getEdgesToRemove().isEmpty()) {
            System.out.println(
                " Entferne folgende Abhaengigkeiten:");
            System.out.println();
            for (String edge : fas.getEdgesToRemove()) {
                System.out.println(
                    "   [X] " + edge);
            }
            System.out.println();
        }

        if (!fas.hasUnresolvableCycles()) {
            System.out.println(
                " [OK] Nach Entfernung: Kein Zyklus mehr!");
        } else {
            System.out.println(
                " [FEHLER] Folgende Zyklen KONNTEN NICHT");
            System.out.println(
                " aufgeloest werden - alle beteiligten");
            System.out.println(
                " Kanten sind als 'protected_dependencies'");
            System.out.println(
                " markiert:");
            System.out.println();
            for (List<String> cycle :
                    fas.getUnresolvableCycles()) {
                System.out.println("   - " + cycle);
            }
            System.out.println();
            System.out.println(
                " URSACHE: Der Benutzer hat Abhaengigkeiten");
            System.out.println(
                " als 'protected_dependencies' geschuetzt,");
            System.out.println(
                " die gleichzeitig einen Zyklus bilden.");
            System.out.println(
                " Ein Zyklus kann nicht bestehen bleiben");
            System.out.println(
                " und gleichzeitig aufgeloest werden.");
            System.out.println();
            System.out.println(
                " => DEPLOYMENT NICHT MOEGLICH.");
            System.out.println(
                " Bitte entfernen Sie den Schutz von");
            System.out.println(
                " mindestens einer Abhaengigkeit im Zyklus,");
            System.out.println(
                " oder aendern Sie die Architektur.");
        }
    }

       System.out.println();
   }

    /**
     * Level-BFS Ergebnis ausgeben
     */
    public void printLevelBFS(LevelBFS levelBFS) {
        System.out.println();
        System.out.println(LINE);
        System.out.println("#      LEVEL-BFS PARALLELISIERUNG     #");
        System.out.println(LINE);
        System.out.println();
        System.out.println(
            " Parallele Deployment-Gruppen:");
        System.out.println();

        List<List<String>> levels = levelBFS.getLevels();
        for (int i = 0; i < levels.size(); i++) {
            System.out.printf(
                "   Level %-3d -> %s%n",
                i, levels.get(i));
        }

        System.out.println();
        System.out.println(THIN_LINE);
        System.out.println(" Zeitvergleich:");
        System.out.println();
        System.out.println(
            "   Sequenziell : "
            + levelBFS.getSequentialTime()
            + " Zeiteinheiten");
        System.out.println(
            "   Parallel    : "
            + levelBFS.getParallelTime()
            + " Zeiteinheiten");

        int saving = levelBFS.getSequentialTime()
                   - levelBFS.getParallelTime();
        int percent = Math.round(
            (1.0f - (float) levelBFS.getParallelTime()
            / levelBFS.getSequentialTime()) * 100);

        System.out.println(
            "   Ersparnis   : "
            + saving + " Zeiteinheiten ("
            + percent + "%)");
        System.out.println();
        System.out.println(THIN_LINE );
        System.out.println();
    }

    
}