package no.sottos.gatherer;

import java.util.stream.Stream;

import no.sottos.gatherer.GathererDSL.Pipeline;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Gatherer;

import static org.junit.jupiter.api.Assertions.*;

public class PipelineTest {

    // Helper: run a gatherer on a stream and collect to list
    private static <IN, OUT> List<OUT> run(Gatherer<IN, Void, OUT> g, List<IN> input) {
        return input.stream().gather(g).toList();
    }

    @Test
    void testIdentityStart() {
        var pipeline = Pipeline.<Integer>start().toGatherer();
        var result = run(pipeline, List.of(1, 2, 3));
        assertEquals(List.of(1, 2, 3), result);
    }

    @Test
    void testMap() {
        var pipeline = Pipeline.<Integer>start()
                .map(x -> x * 2)
                .toGatherer();

        var result = run(pipeline, List.of(1, 2, 3));
        assertEquals(List.of(2, 4, 6), result);
    }

    @Test
    void testFilter() {
        Predicate<Integer> even = x -> x % 2 == 0;

        var pipeline = Pipeline.<Integer>start()
                .filter(even)
                .toGatherer();

        var result = run(pipeline, List.of(1, 2, 3, 4));
        assertEquals(List.of(2, 4), result);
    }

    @Test
    void testFlatMap() {
        var pipeline = Pipeline.<Integer>start()
                .flatMap(x -> Stream.of(x, x * 10))
                .toGatherer();

        var result = run(pipeline, List.of(1, 2));
        assertEquals(List.of(1, 10, 2, 20), result);
    }

    @Test
    void testPeek() {
        var sb = new StringBuilder();

        var pipeline = Pipeline.<Integer>start()
                .peek(sb::append)
                .toGatherer();

        var result = run(pipeline, List.of(1, 2, 3));

        assertEquals("123", sb.toString());
        assertEquals(List.of(1, 2, 3), result);
    }

    @Test
    void testGather() {
        // A simple stateless gatherer that triples values
        Gatherer<Integer, Void, Integer> triple =
                Gatherer.of(
                        (_, item, downstream) -> downstream.push(item * 3),
                        (_, _) -> {
                        }
                );

        var pipeline = Pipeline.<Integer>start()
                .gather(triple)
                .toGatherer();

        var result = run(pipeline, List.of(1, 2, 3));
        assertEquals(List.of(3, 6, 9), result);
    }

    @Test
    void testCombinedPipeline() {
        var pipeline = Pipeline.<Integer>start()
                .map(x -> x + 1)          // 1→2, 2→3, 3→4
                .filter(x -> x % 2 == 0)  // keep even
                .flatMap(x -> Stream.of(x, x * 100))
                .toGatherer();

        var result = run(pipeline, List.of(1, 2, 3));

        assertEquals(List.of(
                2, 200,   // from 1→2
                4, 400    // from 3→4
        ), result);
    }

    @Test
    void testPipelineIsReusable() {
        var pipeline = Pipeline.<Integer>start()
                .map(x -> x + 1)
                .filter(x -> x % 2 == 0)
                .toGatherer();

        var input1 = List.of(1, 2, 3, 4);
        var input2 = List.of(1, 2, 3, 4);

        var result1 = run(pipeline, input1);
        var result2 = run(pipeline, input2);

        assertEquals(result1, result2);
        assertEquals(List.of(2, 4), result1);
    }

    @Test
    void testPipelineWorksOnDifferentInputs() {
        var pipeline = Pipeline.<Integer>start()
                .map(x -> x * 10)
                .filter(x -> x >= 30)
                .toGatherer();

        var resultA = run(pipeline, List.of(1, 2, 3, 4));
        var resultB = run(pipeline, List.of(5, 6));

        assertEquals(List.of(30, 40), resultA);
        assertEquals(List.of(50, 60), resultB);
    }

    @Test
    void testPipelineParallelReuse() {
        var pipeline = Pipeline.<Integer>start()
                .map(x -> x + 1)
                .flatMap(x -> Stream.of(x, x * 2))
                .toGatherer();

        var input = List.of(1, 2, 3);

        var result1 = run(pipeline, input);
        var result2 = run(pipeline, input.parallelStream().toList());

        assertEquals(result1, result2);
    }

    @Test
    void testCombineTwoPipelines() {
        var p1 = Pipeline.<Integer>start()
                .map(x -> x + 1);   // 1→2, 2→3, 3→4

        var p2 = Pipeline.<Integer>start()
                .map(x -> x * 10);  // 2→20, 3→30, 4→40

        var combined = p1.then(p2).toGatherer();

        var result = run(combined, List.of(1, 2, 3));

        assertEquals(List.of(20, 30, 40), result);
    }
    @Test
    void testCombinedPipelineIsReusable() {
        var p1 = Pipeline.<Integer>start().map(x -> x + 1);
        var p2 = Pipeline.<Integer>start().filter(x -> x % 2 == 0);

        var combined = p1.then(p2).toGatherer();

        var r1 = run(combined, List.of(1, 2, 3, 4));
        var r2 = run(combined, List.of(1, 2, 3, 4));

        assertEquals(r1, r2);
        assertEquals(List.of(2, 4), r1);
    }
    @Test
    void testCombineThreePipelines() {
        var p1 = Pipeline.<Integer>start().map(x -> x + 1);      // +1
        var p2 = Pipeline.<Integer>start().filter(x -> x > 2);   // >2
        var p3 = Pipeline.<Integer>start().flatMap(x -> Stream.of(x, x * 100));

        var combined = p1.then(p2).then(p3).toGatherer();

        var result = run(combined, List.of(1, 2, 3));

        assertEquals(List.of(
                3, 300,   // from 2→3
                4, 400    // from 3→4
        ), result);
    }
    @Test
    void filterThenToUpperAndFlatmap() {
        var pipeline =
                Pipeline.<String>start()
                        .filter(s -> s.length() > 2)
                        .map(String::toUpperCase)
                        .flatMap(s -> Stream.of(s, s + "!"))
                        .toGatherer(); // Gatherer<String, Void, String>

        var resultList = Stream.of("a", "bb", "ccc", "dddd")
                .gather(pipeline).toList();
        assertEquals(List.of("CCC","CCC!","DDDD","DDDD!"), resultList);
    }
}