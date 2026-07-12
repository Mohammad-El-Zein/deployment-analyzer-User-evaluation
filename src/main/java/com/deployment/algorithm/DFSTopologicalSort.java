package com.deployment.algorithm;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.deployment.model.Graph;

/**
 * DFS-basierte Topologische Sortierung
 */
public class DFSTopologicalSort {

    private List<String> deploymentOrder;
    private boolean hasCycle;
    private long executionTime;
    private long memoryUsed;

    private static final int WHITE = 0;
    private static final int GRAY  = 1;
    private static final int BLACK = 2;

    private Map<String, Integer> color;
    private Deque<String> stack;

    public DFSTopologicalSort() {
        deploymentOrder = new ArrayList<>();
        hasCycle = false;
        executionTime = 0;
        memoryUsed = 0;
        color = new HashMap<>();
        stack = new ArrayDeque<>();
    }

    public void execute(Graph graph) {

        Runtime runtime = Runtime.getRuntime();

        // Speicher vor dem Algorithmus
        runtime.gc();
        long memoryBefore = runtime.totalMemory()
                          - runtime.freeMemory();

        // Laufzeit Start
        long startTime = System.nanoTime();

        // Alle Knoten auf Weiß
        for (String node : graph.getNodes()) {
            color.put(node, WHITE);
        }

        // DFS für jeden unbesuchten Knoten
        for (String node : graph.getNodes()) {
            if (color.get(node) == WHITE) {
                dfsVisit(node, graph);
            }
            if (hasCycle) break;
        }

        // Stack in Liste umwandeln
        if (!hasCycle) {
            while (!stack.isEmpty()) {
                deploymentOrder.add(stack.pop());
            }
        }

        // Laufzeit Ende
        executionTime = System.nanoTime() - startTime;

        // Speicher nach dem Algorithmus
        long memoryAfter = runtime.totalMemory()
                         - runtime.freeMemory();
        memoryUsed = memoryAfter - memoryBefore;
    }

    private void dfsVisit(String node, Graph graph) {

        color.put(node, GRAY);

        for (String neighbor : 
                graph.getNeighbors(node)) {
            if (color.get(neighbor) == GRAY) {
                hasCycle = true;
                return;
            }
            if (color.get(neighbor) == WHITE) {
                dfsVisit(neighbor, graph);
            }
            if (hasCycle) return;
        }

        color.put(node, BLACK);
        stack.push(node);
    }

        /**
     * Reine Ausführung für Benchmarking (JMH)
     * OHNE Memory-Messung und OHNE runtime.gc()
     */
    public void executeForBenchmark(Graph graph) {

        for (String node : graph.getNodes()) {
            color.put(node, WHITE);
        }

        for (String node : graph.getNodes()) {
            if (color.get(node) == WHITE) {
                dfsVisit(node, graph);
            }
            if (hasCycle) break;
        }

        if (!hasCycle) {
            while (!stack.isEmpty()) {
                deploymentOrder.add(stack.pop());
            }
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