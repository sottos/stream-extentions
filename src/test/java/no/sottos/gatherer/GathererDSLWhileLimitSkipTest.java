package no.sottos.gatherer;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Gatherer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GathererDSLWhileLimitSkipTest {

    static private <T> List<T> run(Gatherer<T, ?, T> g, List<T> input) {
        return input.stream().gather(g).toList();
    }

    @Test
    void testTakeWhile() {
        List<Integer> data = List.of(1, 2, 3, 0, 4, 5);

        var expected = data.stream()
                .takeWhile(x -> x < 3)
                .toList();

        var actual = run(GathererDSL.takeWhile(x -> x < 3), data);

        assertEquals(expected, actual);
    }

    @Test
    void testDropWhile() {
        List<Integer> data = List.of(1, 2, 3, 0, 4, 5);

        var expected = data.stream()
                .dropWhile(x -> x < 3)
                .toList();

        var actual = run(GathererDSL.dropWhile(x -> x < 3), data);

        assertEquals(expected, actual);
    }

    @Test
    void testLimit() {
        List<Integer> data = List.of(10, 20, 30, 40, 50);

        var expected = data.stream()
                .limit(3)
                .toList();

        var actual = run(GathererDSL.limit(3), data);

        assertEquals(expected, actual);
    }

    @Test
    void testSkip() {
        List<Integer> data = List.of(10, 20, 30, 40, 50);

        var expected = data.stream()
                .skip(2)
                .toList();

        var actual = run(GathererDSL.skip(2), data);

        assertEquals(expected, actual);
    }

    @Test
    void testTakeWhileEmptyResult() {
        List<Integer> data = List.of(5, 6, 7);

        var expected = data.stream()
                .takeWhile(x -> x < 0)
                .toList();

        var actual = run(GathererDSL.takeWhile(x -> x < 0), data);

        assertEquals(expected, actual);
    }

    @Test
    void testDropWhileAllDropped() {
        List<Integer> data = List.of(1, 2, 3);

        var expected = data.stream()
                .dropWhile(x -> x < 10)
                .toList();

        var actual = run(GathererDSL.dropWhile(x -> x < 10), data);

        assertEquals(expected, actual);
    }

    @Test
    void testLimitZero() {
        List<Integer> data = List.of(1, 2, 3);

        var expected = data.stream()
                .limit(0)
                .toList();

        var actual = run(GathererDSL.limit(0), data);

        assertEquals(expected, actual);
    }

    @Test
    void testSkipMoreThanSize() {
        List<Integer> data = List.of(1, 2, 3);

        var expected = data.stream()
                .skip(10)
                .toList();

        var actual = run(GathererDSL.skip(10), data);

        assertEquals(expected, actual);
    }

    @Test
    void testCombinedOperations() {
        List<Integer> data = List.of(1, 2, 3, 4, 5, 6);

        var expected = data.stream()
                .dropWhile(x -> x < 3)
                .takeWhile(x -> x < 6)
                .skip(1)
                .limit(2)
                .toList();

        var actual = data.stream()
                .gather(GathererDSL.dropWhile(x -> x < 3))
                .gather(GathererDSL.takeWhile(x -> x < 6))
                .gather(GathererDSL.skip(1))
                .gather(GathererDSL.limit(2))
                .toList();

        assertEquals(expected, actual);
    }
}