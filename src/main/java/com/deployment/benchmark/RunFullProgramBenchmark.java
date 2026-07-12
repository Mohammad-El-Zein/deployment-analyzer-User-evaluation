package com.deployment.benchmark;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.results.format.ResultFormatType;

/**
 * Fuehrt NUR den FullProgramBenchmark aus
 * (Gesamtlaufzeit: YAML lesen + Graph bauen +
 * Sortieren + Level-BFS, einmal mit Kahn,
 * einmal mit DFS).
 *
 * Ausfuehren mit:
 *   java -cp target/benchmarks.jar
 *        com.deployment.benchmark.RunFullProgramBenchmark
 */
public class RunFullProgramBenchmark {

    public static void main(String[] args) throws RunnerException {

        System.out.println("=== Gesamtprogramm: Kahn-Version vs DFS-Version ===");
        Options options = new OptionsBuilder()
            .include(FullProgramBenchmark.class.getSimpleName())
            .resultFormat(ResultFormatType.JSON)
            .result("all_raw_results.json")
            .build();
        new Runner(options).run();

        System.out.println();
        System.out.println("Benchmark abgeschlossen!");
        System.out.println("Ergebnis: all_raw_results.json");
    }
}