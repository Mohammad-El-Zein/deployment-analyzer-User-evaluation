package com.deployment.benchmark;

import com.deployment.algorithm.DFSTopologicalSort;
import com.deployment.algorithm.KahnAlgorithm;
import com.deployment.algorithm.LevelBFS;
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
 * JMH Benchmark für das GESAMTE Programm:
 * YAML einlesen -> Graph aufbauen ->
 * Kahn -> DFS -> Level-BFS
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 2)
@State(Scope.Benchmark)
public class FullProgramBenchmark {

    @Param({"simple", "medium", "large", "xlarge", "xxlarge", "xxxlarge", "xxxxlarge", "showcase-10-services"})
    private String graphSize;

    private String filePath;

    private static final Map<String, String> FILES = Map.of(
        "simple",  "src/main/resources/examples/simple.yaml",
        "medium",  "src/main/resources/examples/medium.yaml",
        "large",   "src/main/resources/examples/large.yaml",
        "xlarge",  "src/main/resources/examples/xlarge.yaml",
        "xxlarge", "src/main/resources/examples/xxlarge.yaml",
        "xxxlarge", "src/main/resources/examples/xxxlarge.yaml",
        "xxxxlarge", "src/main/resources/examples/xxxxlarge.yaml",
        "showcase-10-services", "src/main/resources/examples/showcase-10-services.yaml"
    );

    @Setup(Level.Trial)
    public void setup() {
        filePath = FILES.get(graphSize);
    }

    /**
     * Misst das GESAMTE Programm:
     * YAML lesen + Graph bauen + Kahn + 
     * DFS + Level-BFS
     */
    @Benchmark
    public void fullProgramWithKahn() {

        // Schritt 1: YAML einlesen
        YamlParser parser = new YamlParser();
        Map<String, List<String>> dependencies =
            parser.parse(filePath);

        // Schritt 2: Graph aufbauen
        Graph graph = new Graph(dependencies);

        // Schritt 3: Kahn ausführen
        KahnAlgorithm kahn = new KahnAlgorithm();
        kahn.executeForBenchmark(graph);

        // Schritt 4: Level-BFS ausführen
        LevelBFS levelBFS = new LevelBFS();
        levelBFS.execute(graph);
    }

    /**
     * Gleiche Messung aber mit DFS statt Kahn
     */
    @Benchmark
    public void fullProgramWithDFS() {

        YamlParser parser = new YamlParser();
        Map<String, List<String>> dependencies =
            parser.parse(filePath);

        Graph graph = new Graph(dependencies);

        DFSTopologicalSort dfs = new DFSTopologicalSort();
        dfs.executeForBenchmark(graph);

        LevelBFS levelBFS = new LevelBFS();
        levelBFS.execute(graph);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(FullProgramBenchmark.class.getSimpleName())
            .resultFormat(org.openjdk.jmh.results.format.ResultFormatType.CSV)
            .result("full_program_results.csv")
            .build();

        new Runner(opt).run();
    }
}