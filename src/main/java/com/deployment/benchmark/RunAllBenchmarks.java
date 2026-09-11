package com.deployment.benchmark;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.results.format.ResultFormatType;

/**
 * Fuehrt ALLE Benchmarks (Algorithm + FullProgram).
 *
 * Ausfuehren mit:
 *   java -cp target/benchmarks.jar
 *        com.deployment.benchmark.RunAllBenchmarks
 */
public class RunAllBenchmarks {

    public static void main(String[] args) throws RunnerException {

        System.out.println("=== Fuehre alle Benchmarks aus (JSON Export) ===");
        Options jsonOptions = new OptionsBuilder()
            .include(AlgorithmBenchmark.class.getSimpleName())
            .include(FullProgramBenchmark.class.getSimpleName())
            .resultFormat(ResultFormatType.JSON)
            .result("all_raw_results.json")
            .build();
        new Runner(jsonOptions).run();

        System.out.println();
        System.out.println("Benchmarks abgeschlossen!");
        System.out.println("Ergebnis: all_raw_results.json");
    }
}