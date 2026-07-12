package com.deployment.benchmark;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Liest die vereinfachte Zusammenfassungs-CSV
 * (summary_results.csv, erzeugt von create_summary.py
 * aus den JMH Rohdaten) und sucht die Werte fuer
 * ein bestimmtes Label und Kategorie.
 *
 * Format der CSV:
 * category,graphSize,kahn,dfs,unit,faster
 */
public class CsvResultReader {

    public static class JmhResult {
        public final double kahnScore;
        public final double dfsScore;
        public final String unit;
        public final String faster;

        public JmhResult(double kahnScore, double dfsScore,
                          String unit, String faster) {
            this.kahnScore = kahnScore;
            this.dfsScore = dfsScore;
            this.unit = unit;
            this.faster = faster;
        }
    }

    /**
     * Sucht den Eintrag fuer eine bestimmte Kategorie
     * ("algorithm" oder "fullprogram") und ein Label
     * (z.B. "large").
     *
     * Gibt null zurueck wenn kein Eintrag existiert
     * oder die Datei nicht gefunden wird.
     */
    public static JmhResult findResult(
            String csvFilePath,
            String category,
            String label) {

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

                if (line.trim().isEmpty()) continue;

                String[] parts = splitCsvLine(line);
                if (parts.length < 6) continue;

                String rowCategory = parts[0];
                String rowGraphSize = parts[1];

                if (rowCategory.equals(category)
                        && rowGraphSize.equals(label)) {

                    double kahnScore = Double.parseDouble(parts[2]);
                    double dfsScore = Double.parseDouble(parts[3]);
                    String unit = parts[4];
                    String faster = parts[5];

                    return new JmhResult(
                        kahnScore, dfsScore, unit, faster);
                }
            }

        } catch (IOException e) {
            return null;
        }

        return null;
    }

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