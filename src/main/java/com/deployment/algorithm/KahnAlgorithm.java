package com.deployment.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.deployment.model.Graph;

/**
 * Kahn's Algorithmus - BFS-basierte Topologische Sortierung
 */
public class KahnAlgorithm {

    private List<String> deploymentOrder;
    private boolean hasCycle;
    private long executionTime;
    private long memoryUsed;

    public KahnAlgorithm() {
        deploymentOrder = new ArrayList<>();
        hasCycle = false;
        executionTime = 0;
        memoryUsed = 0;
    }

    public void execute(Graph graph) {

        Runtime runtime = Runtime.getRuntime();

        // Speicher vor dem Algorithmus
        runtime.gc();
        long memoryBefore = runtime.totalMemory()
                          - runtime.freeMemory();

        // Laufzeit Start
        long startTime = System.nanoTime();

        // Schritt 1: Kopie von In-Degree
        Map<String, Integer> inDegree =
            new HashMap<>(graph.getInDegree());

        // Schritt 2: Queue aufbauen
        Queue<String> queue = new LinkedList<>();
        for (String node : graph.getNodes()) {
            if (inDegree.get(node) == 0) {
                queue.add(node);
            }
        }

        // Schritt 3: Hauptschleife
        while (!queue.isEmpty()) {
            String current = queue.poll();
            deploymentOrder.add(current);

            for (String neighbor : 
                    graph.getNeighbors(current)) {
                inDegree.put(neighbor,
                    inDegree.get(neighbor) - 1);
                if (inDegree.get(neighbor) == 0) {
                    queue.add(neighbor);
                }
            }
        }

        // Schritt 4: Zyklus prüfen
        if (deploymentOrder.size() != 
                graph.getNodeCount()) {
            hasCycle = true;
        }

        // Laufzeit Ende
        executionTime = System.nanoTime() - startTime;

        // Speicher nach dem Algorithmus
        long memoryAfter = runtime.totalMemory()
                         - runtime.freeMemory();
        memoryUsed = memoryAfter - memoryBefore;
    }

        /**
     * Reine Ausführung für Benchmarking (JMH)
     * OHNE Memory-Messung und OHNE runtime.gc()
     * damit die Laufzeitmessung nicht verfälscht wird
     */
    public void executeForBenchmark(Graph graph) {

        Map<String, Integer> inDegree =
            new HashMap<>(graph.getInDegree());

        Queue<String> queue = new LinkedList<>();
        for (String node : graph.getNodes()) {
            if (inDegree.get(node) == 0) {
                queue.add(node);
            }
        }

        while (!queue.isEmpty()) {
            String current = queue.poll();
            deploymentOrder.add(current);

            for (String neighbor : 
                    graph.getNeighbors(current)) {
                inDegree.put(neighbor,
                    inDegree.get(neighbor) - 1);
                if (inDegree.get(neighbor) == 0) {
                    queue.add(neighbor);
                }
            }
        }

        if (deploymentOrder.size() != 
                graph.getNodeCount()) {
            hasCycle = true;
        }
    }

    // Getter
    public List<String> getDeploymentOrder() {
        return deploymentOrder;
    }

    public boolean hasCycle() {
        return hasCycle;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public long getMemoryUsed() {
        return memoryUsed;
    }

    // Speicher in lesbarem Format
    public String getMemoryFormatted() {
        if (memoryUsed < 1024) {
            return memoryUsed + " B";
        } else if (memoryUsed < 1024 * 1024) {
            return String.format("%.2f KB",
                memoryUsed / 1024.0);
        } else {
            return String.format("%.2f MB",
                memoryUsed / (1024.0 * 1024.0));
        }
    }

    // Laufzeit in lesbarem Format
    public String getTimeFormatted() {
        return executionTime + " ns ("
            + String.format("%.3f",
                executionTime / 1_000_000.0)
            + " ms)";
    }
}