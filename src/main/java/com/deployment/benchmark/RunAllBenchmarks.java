package com.deployment.benchmark;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.results.format.ResultFormatType;

/**
 * Fuehrt ALLE Benchmarks (Algorithm + FullProgram)
 * in EINEM JVM-Prozess aus und exportiert
 * gleichzeitig CSV UND JSON.
 *
 * Das spart erheblich Zeit gegenueber separaten
 * Aufrufen, da der JVM-Start-Overhead nur einmal
 * anfaellt.
 *
 * Ausfuehren mit:
 *   java -cp target/benchmarks.jar 
 *        com.deployment.benchmark.RunAllBenchmarks
 */
public class RunAllBenchmarks {

    public static void main(String[] args) throws RunnerException {

        // Lauf 1: Beide Benchmark-Klassen zusammen,
        // Ergebnis als CSV
        Options csvOptions = new OptionsBuilder()
            .include(AlgorithmBenchmark.class.getSimpleName())
            .include(FullProgramBenchmark.class.getSimpleName())
            .resultFormat(ResultFormatType.CSV)
            .result("all_results.csv")
            .build();

        System.out.println("=== Lauf 1/2: CSV Export ===");
        new Runner(csvOptions).run();

        // Lauf 2: Gleiche Benchmarks nochmal,
        // aber JSON Export (fuer Boxplots mit Rohdaten)
        Options jsonOptions = new OptionsBuilder()
            .include(AlgorithmBenchmark.class.getSimpleName())
            .include(FullProgramBenchmark.class.getSimpleName())
            .resultFormat(ResultFormatType.JSON)
            .result("all_raw_results.json")
            .build();

        System.out.println("=== Lauf 2/2: JSON Export ===");
        new Runner(jsonOptions).run();
    }
}