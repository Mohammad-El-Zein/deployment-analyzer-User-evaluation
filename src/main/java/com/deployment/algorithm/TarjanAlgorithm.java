package com.deployment.algorithm;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.deployment.model.Graph;

/**
 * Tarjan's Algorithmus
 * Findet alle Strongly Connected Components
 * Identifiziert genau welche Services einen Zyklus bilden
 */
public class TarjanAlgorithm {

    // Alle gefundenen Zyklen
    private List<List<String>> sccsWithCycles;

    // Laufzeit messen
    private long executionTime;

    private int index;

    // Discovery Index für jeden Knoten
    private Map<String, Integer> disc;

    // Lowlink für jeden Knoten
    private Map<String, Integer> low;

    // Ist Knoten auf dem Stack?
    private Map<String, Boolean> onStack;

    private Deque<String> stack;

    public TarjanAlgorithm() {
        sccsWithCycles = new ArrayList<>();
        executionTime = 0;
        index = 0;
        disc = new HashMap<>();
        low = new HashMap<>();
        onStack = new HashMap<>();
        stack = new ArrayDeque<>();
    }

    /**
     * Führt Tarjan's Algorithmus aus
     */
    public void execute(Graph graph) {

        // Laufzeit Start
        long startTime = System.nanoTime();

        // Alle Knoten initialisieren
        for (String node : graph.getNodes()) {
            disc.put(node, -1); // -1 = noch nicht besucht
            low.put(node, -1);
            onStack.put(node, false);
        }

        // Tarjan für jeden unbesuchten Knoten
        for (String node : graph.getNodes()) {
            if (disc.get(node) == -1) {
                tarjanVisit(node, graph);
            }
        }

        // Laufzeit Ende
        executionTime = System.nanoTime() - startTime;
    }

    /**
     * Rekursiver Tarjan Besuch
     */
    private void tarjanVisit(String node, Graph graph) {

        // Index und Lowlink setzen
        disc.put(node, index);
        low.put(node, index);
        index++;

        // Knoten auf Stack legen
        stack.push(node);
        onStack.put(node, true);

        // Alle Nachfolger besuchen
        for (String neighbor : graph.getNeighbors(node)) {

            if (disc.get(neighbor) == -1) {
                // Noch nicht besucht dann tiefer gehen
                tarjanVisit(neighbor, graph);

                // Lowlink aktualisieren
                low.put(node,
                        Math.min(low.get(node), low.get(neighbor)));

            } else if (onStack.get(neighbor)) {
                // Knoten ist auf Stack → Zyklus kann gibt es
                low.put(node,
                        Math.min(low.get(node), disc.get(neighbor)));
            }
        }

        // Prüfen ob node Startpunkt einer SCC ist
        if (low.get(node).equals(disc.get(node))) {

            // SCC aus Stack holen
            List<String> scc = new ArrayList<>();
            String current;

            do {
                current = stack.pop();
                onStack.put(current, false);
                scc.add(current);
            } while (!current.equals(node));

            // Nur Zyklen speichern (mehr als 1 Knoten)
            if (scc.size() > 1) {
                sccsWithCycles.add(scc);
            }
        }
    }

    // Getter
    public List<List<String>> getSccsWithCycles() {
        return sccsWithCycles;
    }

    public boolean hasCycles() {
        return !sccsWithCycles.isEmpty();
    }

    public long getExecutionTime() {
        return executionTime;
    }

    // Ergebnis ausgeben
    public void printResult() {
        if (sccsWithCycles.isEmpty()) {
            System.out.println("Kein Zyklus gefunden!");
        } else {
            System.out.println("Zyklen gefunden:");
            for (int i = 0; i < sccsWithCycles.size(); i++) {
                System.out.println(
                        "SCC mit Zyklus " + (i + 1) + ": " + sccsWithCycles.get(i));
            }
            System.out.println(
                    " Laufzeit: " + executionTime + " ns");
        }
    }
}