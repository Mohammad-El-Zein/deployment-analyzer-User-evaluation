package com.deployment.benchmark;

import com.deployment.algorithm.DFSTopologicalSort;
import com.deployment.algorithm.KahnAlgorithm;
import com.deployment.model.Graph;
import com.deployment.parser.YamlParser;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * JMH Benchmark für Kahn's Algorithmus und
 * DFS-basierte topologische Sortierung.
 *
 * JMH führt automatisches Warmup durch und
 * misst die Laufzeit statistisch korrekt
 * (Median, Mittelwert, Standardabweichung).
 *
 * Ausführen mit:
 *   mvn clean package
 *   java -jar target/benchmarks.jar
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 2)
@State(Scope.Benchmark)
public class AlgorithmBenchmark {

    // Graphgröße: wird für jede Größe separat ausgeführt
    @Param({"simple", "medium", "large", "xlarge", "xxlarge", "xxxlarge", "xxxxlarge"})
    private String graphSize;

    private Graph graph;

    // Dateiname pro Graphgröße
    private static final Map<String, String> FILES = Map.of(
        "simple",  "src/main/resources/examples/simple.yaml",
        "medium",  "src/main/resources/examples/medium.yaml",
        "large",   "src/main/resources/examples/large.yaml",
        "xlarge",  "src/main/resources/examples/xlarge.yaml",
        "xxlarge", "src/main/resources/examples/xxlarge.yaml",
        "xxxlarge", "src/main/resources/examples/xxxlarge.yaml",
        "xxxxlarge", "src/main/resources/examples/xxxxlarge.yaml"  
    );

    @Setup(Level.Trial)
    public void setup() {
        YamlParser parser = new YamlParser();
        Map<String, List<String>> dependencies =
            parser.parse(FILES.get(graphSize));
        graph = new Graph(dependencies);
    }

    @Benchmark
    public void kahnAlgorithm() {
        KahnAlgorithm kahn = new KahnAlgorithm();
        kahn.executeForBenchmark(graph);
    }

    @Benchmark
    public void dfsAlgorithm() {
        DFSTopologicalSort dfs = new DFSTopologicalSort();
        dfs.executeForBenchmark(graph);
    }

    /**
     * Startet die Benchmarks direkt aus der IDE/Maven heraus,
     * ohne das Shade-Jar zu bauen. Ergebnis wird zusätzlich
     * als CSV exportiert für die Boxplot-Erstellung.
     */
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(AlgorithmBenchmark.class.getSimpleName())
            .resultFormat(org.openjdk.jmh.results.format.ResultFormatType.CSV)
            .result("benchmark_results.csv")
            .build();

        new Runner(opt).run();
    }
}