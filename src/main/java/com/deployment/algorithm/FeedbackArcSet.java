package com.deployment.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.deployment.model.Graph;

public class FeedbackArcSet {

    private List<String> edgesToRemove;
    private long executionTime;

    // Zyklen die nicht aufgeloest werden konnten
    // weil alle beteiligten Kanten geschuetzt sind
    private List<List<String>> unresolvableSccsWithCycles;

    public FeedbackArcSet() {
        edgesToRemove = new ArrayList<>();
        executionTime = 0;
        unresolvableSccsWithCycles = new ArrayList<>();
    }

    /**
     * Fuehrt Feedback Arc Set OHNE geschuetzte Kanten aus.
     * (Abwaertskompatibel zur alten Nutzung)
     */
    public void execute(Graph graph,
                        List<List<String>> sccsWithCycles) {
        execute(graph, sccsWithCycles, new HashSet<>());
    }

    /**
     * Fuehrt Feedback Arc Set aus. Kanten in
     * protectedEdges (Format "from -> to") werden
     * NIEMALS als Loesungsvorschlag entfernt.
     *
     * Falls ein Zyklus ausschliesslich aus
     * geschuetzten Kanten besteht, kann er nicht
     * aufgeloest werden - das wird in
     * unresolvableSccsWithCycles vermerkt.
     */
    public void execute(Graph graph,
                        List<List<String>> sccsWithCycles,
                        Set<String> protectedEdges) {

        long startTime = System.nanoTime();

        Map<String, List<String>> adjCopy = new HashMap<>();
        for (String node : graph.getNodes()) {
            adjCopy.put(node,
                new ArrayList<>(graph.getNeighbors(node)));
        }

        List<List<String>> remainingSccsWithCycles =
            new ArrayList<>(sccsWithCycles);

        while (!remainingSccsWithCycles.isEmpty()) {

            Map<String, Integer> edgeCount = new HashMap<>();

            for (List<String> scc : remainingSccsWithCycles) {
                Set<String> sccNodes = new HashSet<>(scc);

                for (String startNode : scc) {
                    findCyclesInSCC(
                        startNode, startNode,
                        sccNodes, adjCopy,
                        new ArrayList<>(),
                        edgeCount);
                }
            }

            // Geschuetzte Kanten aus den Kandidaten
            // entfernen - sie duerfen nicht
            // vorgeschlagen werden!
            for (String protectedEdge : protectedEdges) {
                edgeCount.remove(protectedEdge);
            }

            String bestEdge = null;
            int maxCount = 0;

            for (String edge : edgeCount.keySet()) {
                if (edgeCount.get(edge) > maxCount) {
                    maxCount = edgeCount.get(edge);
                    bestEdge = edge;
                }
            }

            if (bestEdge != null) {
                edgesToRemove.add(bestEdge);

                String[] parts = bestEdge.split(" -> ");
                String from = parts[0];
                String to = parts[1];
                adjCopy.get(from).remove(to);

                remainingSccsWithCycles.removeIf(scc -> {
                    Set<String> sccNodes = new HashSet<>(scc);

                    for (String node : scc) {
                        boolean hasEdgeInCycle = false;
                        for (String neighbor : adjCopy
                                .getOrDefault(node,
                                    new ArrayList<>())) {
                            if (sccNodes.contains(
                                    neighbor)) {
                                hasEdgeInCycle = true;
                                break;
                            }
                        }
                        if (!hasEdgeInCycle) return true;
                    }
                    return false;
                });

            } else {
                // Keine loeschbare Kante mehr gefunden!
                // Alle verbleibenden Zyklen bestehen
                // nur noch aus geschuetzten Kanten.
                unresolvableSccsWithCycles.addAll(remainingSccsWithCycles);
                break;
            }
        }

        executionTime = System.nanoTime() - startTime;
    }

    private void findCyclesInSCC(
            String current,
            String start,
            Set<String> sccNodes,
            Map<String, List<String>> adj,
            List<String> path,
            Map<String, Integer> edgeCount) {

        for (String neighbor : adj.getOrDefault(
                current, new ArrayList<>())) {

            if (!sccNodes.contains(neighbor)) continue;

            String edge = current + " -> " + neighbor;

            if (neighbor.equals(start)
                    && !path.isEmpty()) {
                edgeCount.put(edge,
                    edgeCount.getOrDefault(edge, 0) + 1);
                for (int i = 0; i < path.size() - 1; i++) {
                    String e = path.get(i)
                        + " -> " + path.get(i + 1);
                    edgeCount.put(e,
                        edgeCount.getOrDefault(e, 0) + 1);
                }
                if (!path.isEmpty()) {
                    String e = path.get(path.size() - 1)
                        + " -> " + neighbor;
                    edgeCount.put(e,
                        edgeCount.getOrDefault(e, 0) + 1);
                }
                return;
            }

            if (!path.contains(neighbor)) {
                path.add(neighbor);
                findCyclesInSCC(
                    neighbor, start, sccNodes,
                    adj, path, edgeCount);
                path.remove(neighbor);
            }
        }
    }

    public Map<String, List<String>> getCleanAdjacencyList(
        Graph graph) {

        Map<String, List<String>> cleanAdj = new HashMap<>();

        for (String node : graph.getNodes()) {
            cleanAdj.put(node,
                new ArrayList<>(graph.getNeighbors(node)));
        }

        for (String edge : edgesToRemove) {
            String[] parts = edge.split(" -> ");
            String from = parts[0];
            String to = parts[1];
            cleanAdj.get(from).remove(to);
        }

        return cleanAdj;
    }

    public List<String> getEdgesToRemove() {
        return edgesToRemove;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public boolean hasUnresolvableSccsWithCycles() {
        return !unresolvableSccsWithCycles.isEmpty();
    }

    public List<List<String>> getUnresolvableSccsWithCycles() {
        return unresolvableSccsWithCycles;
    }

    public void printResult() {
        if (edgesToRemove.isEmpty()
                && unresolvableSccsWithCycles.isEmpty()) {
            System.out.println(
                " Keine Kanten zu entfernen!");
        } else {

            if (!edgesToRemove.isEmpty()) {
                System.out.println(
                    " Feedback Arc Set Loesung:");
                System.out.println(
                    " Entferne folgende Abhaengigkeiten:");
                for (String edge : edgesToRemove) {
                    System.out.println("   - " + edge);
                }
            }

            if (unresolvableSccsWithCycles.isEmpty()) {
                System.out.println(
                    " Nach Entfernung: Kein Zyklus mehr!");
            } else {
                System.out.println();
                System.out.println(
                    " [WARNUNG] Folgende Zyklen konnten NICHT");
                System.out.println(
                    " aufgeloest werden, da alle beteiligten");
                System.out.println(
                    " Kanten als geschuetzt markiert sind:");
                for (List<String> cycle : unresolvableSccsWithCycles) {
                    System.out.println("   - " + cycle);
                }
            }

            System.out.println(
                " Laufzeit: " + executionTime + " ns");
        }
    }
}