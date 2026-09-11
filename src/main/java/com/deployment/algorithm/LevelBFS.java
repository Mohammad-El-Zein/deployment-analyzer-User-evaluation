package com.deployment.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.deployment.model.Graph;

/**
 * Level-BFS Algorithmus
 * Erweiterung von Kahn's Algorithmus
 * Findet parallele Deployment-Gruppen
 */
public class LevelBFS {

    // Alle Levels mit ihren Services
    // Level 0: [database, redis]
    // Level 1: [auth-service]
    // usw.
    private List<List<String>> levels;

    // Laufzeit messen
    private long executionTime;

    // Geschätzte Zeitersparnis
    private int sequentialTime;
    private int parallelTime;

    public LevelBFS() {
        levels = new ArrayList<>();
        executionTime = 0;
        sequentialTime = 0;
        parallelTime = 0;
    }

    /**
     * Führt Level-BFS aus
     * Kann mit normalem Graph oder
     * bereinigtem Graph (nach FAS) laufen
     */
    public void execute(Graph graph) {
        executeWithAdjacency(
                graph, 
                graph.getAdjacencyList()
            );
    }

    /**
     * Level-BFS mit angepasster Adjazenzliste
     * Wird nach Feedback Arc Set genutzt
     */
    public void executeWithAdjacency(
            Graph graph,
            Map<String, List<String>> adjacencyList) {

        // Laufzeit Start
        long startTime = System.nanoTime();

        // Kopie von In-Degree erstellen
        Map<String, Integer> inDegree = new HashMap<>();
        for (String node : graph.getNodes()) {
            inDegree.put(node, 0);
        }

        // In-Degree neu berechnen
        // basierend auf der Adjazenzliste
        for (String node : graph.getNodes()) {
            for (String neighbor : adjacencyList
                    .getOrDefault(node, new ArrayList<>())) {
                inDegree.put(neighbor,
                        inDegree.get(neighbor) + 1);
            }
        }

        // Queue mit allen In-Degree = 0
        Queue<String> queue = new LinkedList<>();
        for (String node : graph.getNodes()) {
            if (inDegree.get(node) == 0) {
                queue.add(node);
            }
        }

        // Level-BFS Hauptschleife
        while (!queue.isEmpty()) {

            // Alle Services im aktuellen Level
            List<String> currentLevel = new ArrayList<>();
            int levelSize = queue.size();

            // Nimm ALLE Services des aktuellen Levels
            for (int i = 0; i < levelSize; i++) {
                String current = queue.poll();
                currentLevel.add(current);

                // Nachfolger verarbeiten
                for (String neighbor : adjacencyList
                        .getOrDefault(current,
                                new ArrayList<>())) {
                    inDegree.put(neighbor,
                            inDegree.get(neighbor) - 1);

                    if (inDegree.get(neighbor) == 0) {
                        queue.add(neighbor);
                    }
                }
            }

            // Level hinzufügen
            levels.add(currentLevel);
        }

        // Zeitersparnis berechnen
        calculateTimeSaving();

        // Laufzeit Ende
        executionTime = System.nanoTime() - startTime;
    }

    /**
     * Berechnet theoretische Zeitersparnis
     * Annahme: jeder Service braucht 1 Zeiteinheit
     */
    private void calculateTimeSaving() {

        // Sequenziell: alle Services nacheinander
        int totalServices = 0;
        for (List<String> level : levels) {
            totalServices += level.size();
        }
        sequentialTime = totalServices;

        // Parallel: nur Anzahl der Levels
        parallelTime = levels.size();
    }

    // Getter
    public List<List<String>> getLevels() {
        return levels;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public int getSequentialTime() {
        return sequentialTime;
    }

    public int getParallelTime() {
        return parallelTime;
    }

    // Ergebnis ausgeben
    public void printResult() {
        System.out.println(
                " Parallele Deployment-Gruppen (Level-BFS):");

        for (int i = 0; i < levels.size(); i++) {
            System.out.println(
                    "Level " + i + " (parallel starten): "
                            + levels.get(i));
        }

        System.out.println();
        System.out.println(
                " Zeitvergleich:");
        System.out.println(
                "Sequenziell: " + sequentialTime
                        + " Zeiteinheiten");
        System.out.println(
                "Parallel:    " + parallelTime
                        + " Zeiteinheiten");
        System.out.println(
                "Ersparnis:   "
                        + (sequentialTime - parallelTime)
                        + " Zeiteinheiten ("
                        + Math.round(
                                (1.0 - (double) parallelTime / sequentialTime)
                                        * 100)
                        + "%)");
        System.out.println(
                "   Laufzeit: " + executionTime + " ns");
    }
}