package com.deployment.benchmark;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Liest die JMH-Ergebnis-CSV (algorithm_results.csv)
 * und sucht die Werte fuer ein bestimmtes Label
 * (z.B. "large") und einen Algorithmus (Kahn/DFS).
 */
public class CsvResultReader {

    public static class JmhResult {
        public final double score;
        public final double error;
        public final String unit;

        public JmhResult(double score, double error,
                          String unit) {
            this.score = score;
            this.error = error;
            this.unit = unit;
        }
    }

    /**
     * Sucht in der CSV nach dem Eintrag fuer
     * das gegebene Label und den Algorithmus-Namen
     * (z.B. "kahnAlgorithm" oder "dfsAlgorithm").
     *
     * Gibt null zurueck wenn kein Eintrag existiert
     * oder die Datei nicht gefunden wird.
     */
    public static JmhResult findResult(
            String csvFilePath,
            String label,
            String benchmarkMethodName) {

        try (BufferedReader reader =
                new BufferedReader(
                    new FileReader(csvFilePath))) {

            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {

                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                if (line.trim().startsWith("#")
                        || line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = splitCsvLine(line);
                if (parts.length < 8) continue;

                String benchmarkName = parts[0];
                String score = parts[4];
                String error = parts[5];
                String unit = parts[6];
                String graphSize = parts[7];

                if (benchmarkName.contains(benchmarkMethodName)
                        && graphSize.equals(label)) {

                    double scoreVal = Double.parseDouble(
                        score.replace(",", "."));
                    double errorVal = Double.parseDouble(
                        error.replace(",", "."));

                    return new JmhResult(
                        scoreVal, errorVal, unit);
                }
            }

        } catch (IOException e) {
            return null;
        }

        return null;
    }

    /**
     * Korrekter CSV-Parser der Felder in
     * Anfuehrungszeichen als EINEN Wert behandelt,
     * auch wenn darin ein Komma steht.
     */
    private static String[] splitCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                insideQuotes = !insideQuotes;
            } else if (c == ',' && !insideQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());

        return result.toArray(new String[0]);
    }
}