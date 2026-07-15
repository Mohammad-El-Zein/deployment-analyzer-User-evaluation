package com.deployment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.deployment.algorithm.DFSTopologicalSort;
import com.deployment.algorithm.FeedbackArcSet;
import com.deployment.algorithm.KahnAlgorithm;
import com.deployment.algorithm.LevelBFS;
import com.deployment.algorithm.TarjanAlgorithm;
import com.deployment.export.GraphvizExporter;
import com.deployment.export.JsonExporter;
import com.deployment.model.Graph;
import com.deployment.parser.YamlParser;
import com.deployment.validator.Validator;

public class Main {   

    public static void main(String[] args) {

        String filePath ="src/main/resources/examples/medium.yaml"; // muss angepasst werden, je nachdem, welche Datei analysiert werden soll

        ResultPrinter printer = new ResultPrinter();

        System.out.println(
            "╔════════════════════════════════════╗");
        System.out.println(
            "║       DEPLOYMENT ANALYZER          ║");
        System.out.println(
            "╚════════════════════════════════════╝");

        System.out.println(
            "\nLese YAML: " + filePath);
        YamlParser parser = new YamlParser();
        Map<String, List<String>> dependencies =
            parser.parse(filePath);

        Set<String> protectedEdges =
            parser.getProtectedEdges();

        Validator validator = new Validator();
        try {
            validator.validateFile(filePath);
            validator.validateDependencies(dependencies);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            return;
        }

        Graph graph = new Graph(dependencies);
        printer.printGraph(graph);

        KahnAlgorithm kahn = new KahnAlgorithm();
        kahn.execute(graph);
        printer.printKahn(kahn);

        DFSTopologicalSort dfs = new DFSTopologicalSort();
        dfs.execute(graph);
        printer.printDFS(dfs);

        if (!kahn.hasCycle() && !dfs.hasCycle()) {
            printer.printComparison(kahn, dfs, graph, filePath);
        }

        if (kahn.hasCycle() || dfs.hasCycle()) {

            TarjanAlgorithm tarjan = new TarjanAlgorithm();
            tarjan.execute(graph);
            printer.printTarjan(tarjan);

            FeedbackArcSet fas = new FeedbackArcSet();
            fas.execute(graph, tarjan.getCycles(), protectedEdges);
            printer.printFAS(fas);

            if (fas.hasUnresolvableCycles()) {

                GraphvizExporter.export(
                    graph, protectedEdges, null,
                    "dependency_graph.dot",
                    "dependency_graph.svg");

                JsonExporter.export(
                    "deployment_result.json",
                    graph, true, tarjan.getCycles(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>());

                System.out.println();
                System.out.println(
                    "╔════════════════════════════════════╗");
                System.out.println(
                    "║   Analyse abgebrochen - Deployment ║");
                System.out.println(
                    "║   aufgrund geschuetzter Zyklen     ║");
                System.out.println(
                    "║   nicht moeglich!                  ║");
                System.out.println(
                    "╚════════════════════════════════════╝");
                return;
            }

            LevelBFS levelBFS = new LevelBFS();
            levelBFS.executeWithAdjacency(
                graph,
                fas.getCleanAdjacencyList(graph));
            printer.printLevelBFS(levelBFS);

            GraphvizExporter.export(
                graph, protectedEdges,
                fas.getEdgesToRemove(),
                "dependency_graph.dot",
                "dependency_graph.svg");

            JsonExporter.export(
                "deployment_result.json",
                graph, true, tarjan.getCycles(),
                fas.getEdgesToRemove(),
                new ArrayList<>(), // Kahn hat Zyklus, keine Reihenfolge
                levelBFS.getLevels());

        } else {

            LevelBFS levelBFS = new LevelBFS();
            levelBFS.execute(graph);
            printer.printLevelBFS(levelBFS);
            printer.printFullProgramComparison(filePath);

            GraphvizExporter.export(
                graph, protectedEdges, null,
                "dependency_graph.dot",
                "dependency_graph.svg");

            JsonExporter.export(
                "deployment_result.json",
                graph, false, Collections.emptyList(),
                Collections.emptyList(),
                kahn.getDeploymentOrder(),
                levelBFS.getLevels());
        }

        System.out.println();
        System.out.println(
            "╔════════════════════════════════════╗");
        System.out.println(
            "║      Analyse abgeschlossen         ║");
        System.out.println(
            "╚════════════════════════════════════╝");
        
    }

}