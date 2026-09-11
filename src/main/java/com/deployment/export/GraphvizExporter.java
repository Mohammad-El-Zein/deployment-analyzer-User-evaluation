package com.deployment.export;

import com.deployment.model.Graph;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;

/**
 * Exportiert den Abhaengigkeitsgraph als
 * Graphviz DOT Datei und rendert daraus
 * automatisch ein SVG Bild.
 */
public class GraphvizExporter {

    public static void export(
            Graph graph,
            Set<String> protectedEdges,
            List<String> removedEdges,
            String dotFilePath,
            String svgFilePath) {

        try (FileWriter writer = new FileWriter(dotFilePath)) {

            writer.write("digraph DeploymentGraph {\n");
            writer.write("    rankdir=TB;\n");
            writer.write("    nodesep=0.6;\n");
            writer.write("    ranksep=1.2;\n");
            writer.write("    node [shape=ellipse, style=filled, fontsize=10];\n");
            writer.write("\n");

            writer.write("    labelloc=\"t\";\n");
            writer.write("    label=\"A ---> B = A vor B (B depends_on A)\";\n");
            writer.write("    fontsize=14;\n");
            writer.write("\n");

            writer.write("    legend [shape=none, margin=0, label=<\n");
            writer.write("        <TABLE BORDER=\"1\" CELLBORDER=\"1\" "
                    + "CELLSPACING=\"0\" CELLPADDING=\"4\">\n");
            writer.write("        <TR><TD COLSPAN=\"2\"><B>Legende</B></TD></TR>\n");
            writer.write("        <TR><TD BGCOLOR=\"white\">     </TD>"
                    + "<TD ALIGN=\"LEFT\">In-Degree 0 (Basis-Service)</TD></TR>\n");
            writer.write("        <TR><TD BGCOLOR=\"palegreen\">     </TD>"
                    + "<TD ALIGN=\"LEFT\">In-Degree 1</TD></TR>\n");
            writer.write("        <TR><TD BGCOLOR=\"gold\">     </TD>"
                    + "<TD ALIGN=\"LEFT\">In-Degree 2</TD></TR>\n");
            writer.write("        <TR><TD BGCOLOR=\"orange\">     </TD>"
                    + "<TD ALIGN=\"LEFT\">In-Degree 3</TD></TR>\n");
            writer.write("        <TR><TD BGCOLOR=\"orangered\">     </TD>"
                    + "<TD ALIGN=\"LEFT\">In-Degree 4+</TD></TR>\n");

            writer.write("        </TABLE>\n");
            writer.write("    >];\n");
            writer.write("\n");

            for (String node : graph.getNodes()) {
                int inDegree = graph.getInDegreeOf(node);
                String color = getColorForInDegree(inDegree);
                writer.write("    \"" + node
                        + "\" [fillcolor=\"" + color + "\"];\n");
            }
            writer.write("\n");

            for (String node : graph.getNodes()) {
                for (String neighbor : graph.getNeighbors(node)) {

                    String edgeKey = node + " -> " + neighbor;
                    boolean isRemoved = removedEdges != null
                            && removedEdges.contains(edgeKey);
                    boolean isProtected = protectedEdges != null
                            && protectedEdges.contains(edgeKey);

                    String style;
                    if (isRemoved) {
                        style = " [style=dashed, color=gray, "
                                + "label=\"entfernt\", fontcolor=gray]";
                    } else if (isProtected) {
                        style = " [color=red, penwidth=2.5, "
                                + "label=\"geschuetzt\", fontcolor=red]";
                    } else {
                        style = "";
                    }

                    writer.write("    \"" + node + "\" -> \""
                            + neighbor + "\"" + style + ";\n");
                }
            }

            writer.write("}\n");

            System.out.println(
                    " DOT Datei geschrieben: " + dotFilePath);

        } catch (IOException e) {
            System.out.println(
                    " Fehler beim Schreiben der DOT Datei: "
                            + e.getMessage());
            return;
        }

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "dot", "-Tsvg", dotFilePath, "-o", svgFilePath);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println(
                        " Graph-Visualisierung erstellt: "
                                + svgFilePath);
            } else {
                System.out.println(
                        " [Hinweis] Graphviz 'dot' Befehl "
                                + "fehlgeschlagen. Exit Code: " + exitCode);
                System.out.println(
                        " Graphviz Ausgabe: " + output.toString());
            }

        } catch (IOException | InterruptedException e) {
            System.out.println(
                    " [Hinweis] Graphviz 'dot' Befehl nicht "
                            + "verfuegbar: " + e.getMessage());
            System.out.println(
                    " Installiere Graphviz von "
                            + "https://graphviz.org/download/");
        }
    }

    private static String getColorForInDegree(int inDegree) {
        switch (inDegree) {
            case 0:
                return "white";
            case 1:
                return "palegreen";
            case 2:
                return "gold";
            case 3:
                return "orange";
            default:
                return "orangered";
        }
    }
}