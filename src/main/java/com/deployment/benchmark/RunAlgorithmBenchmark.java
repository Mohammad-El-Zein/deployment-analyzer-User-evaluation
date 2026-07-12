package com.deployment.benchmark;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.results.format.ResultFormatType;

/**
 * Fuehrt NUR den AlgorithmBenchmark aus
 * (isolierter Vergleich Kahn vs DFS,
 * ohne YAML-Parsing/Graph-Konstruktion).
 *
 * Ausfuehren mit:
 *   java -cp target/benchmarks.jar
 *        com.deployment.benchmark.RunAlgorithmBenchmark
 */
public class RunAlgorithmBenchmark {

    public static void main(String[] args) throws RunnerException {

        System.out.println("=== Isolierte Algorithmen: Kahn vs DFS ===");
        Options options = new OptionsBuilder()
            .include(AlgorithmBenchmark.class.getSimpleName())
            .resultFormat(ResultFormatType.JSON)
            .result("all_raw_results.json")
            .build();
        new Runner(options).run();

        System.out.println();
        System.out.println("Benchmark abgeschlossen!");
        System.out.println("Ergebnis: all_raw_results.json");
    }
}