package no.sottos.predicates;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.function.Predicate;

import static no.sottos.predicates.Predicates.*;
import static org.junit.jupiter.api.Assertions.*;

class PredicatesTest {

    record Event(String key) {}

    // ------------------------------------------------------------
    // distinctBy
    // ------------------------------------------------------------
    @Test
    void distinctBy_returnsOnlyFirstOccurrence() {
        Predicate<Event> p = distinctBy(Event::key);

        assertTrue(p.test(new Event("a")));
        assertTrue(p.test(new Event("b")));
        assertFalse(p.test(new Event("a")));
        assertFalse(p.test(new Event("b")));
        assertTrue(p.test(new Event("c")));
    }

    // ------------------------------------------------------------
    // distinctInNObservations (Semantikk #2)
    // ------------------------------------------------------------
    @Test
    void distinctInNObservations_slidingWindowOfDistinctKeys() {
        Predicate<Event> p = distinctInNObservations(Event::key, 3);

        assertTrue(p.test(new Event("a"))); // [a]
        assertTrue(p.test(new Event("b"))); // [b,a]
        assertFalse(p.test(new Event("a"))); // [a,b]
        assertTrue(p.test(new Event("c"))); // [c,a,b]
        assertFalse(p.test(new Event("a"))); // a fortsatt i window [a,c,b]
        assertTrue(p.test(new Event("d"))); // [d,a,c] (b evicted)
        assertFalse(p.test(new Event("c"))); // [c,d,a] (none evicted)
        assertTrue(p.test(new Event("b"))); // b er ny igjen, a evicted [b,c,d]
        assertFalse(p.test(new Event("d"))); //  (none evicted)
        assertFalse(p.test(new Event("c"))); //  (none evicted)
        assertFalse(p.test(new Event("b"))); //  (none evicted)

    }

    @Test
    void distinctInNObservations_evictionOfOldestDistinctKey() {
        Predicate<Event> p = distinctInNObservations(Event::key, 2);

        assertTrue(p.test(new Event("x"))); // [x]
        assertTrue(p.test(new Event("y"))); // [y,x]
        assertFalse(p.test(new Event("x"))); // [x,y]

        // Adding z evicts y (oldest accessed key)
        assertTrue(p.test(new Event("z"))); // [z,x]

        // x is still there
        assertFalse(p.test(new Event("x"))); // [x,z]
        assertTrue(p.test(new Event("y"))); // [y,x]
        assertTrue(p.test(new Event("z"))); // [z,y]
    }

    // ------------------------------------------------------------
    // distinctByTimeWindow
    // ------------------------------------------------------------
    @Test
    void distinctByTimeWindow_basicExpiration() throws InterruptedException {
        Predicate<Event> p = distinctByTimeWindow(Event::key, Duration.ofMillis(50));

        assertTrue(p.test(new Event("a")));
        assertFalse(p.test(new Event("a"))); // still inside window

        Thread.sleep(60);

        assertTrue(p.test(new Event("a"))); // expired
    }

    @Test
    void distinctByTimeWindow_independentExpiration() throws InterruptedException {
        Predicate<Event> p = distinctByTimeWindow(Event::key, Duration.ofMillis(40));

        assertTrue(p.test(new Event("a"))); // t=0
        Thread.sleep(20);
        assertTrue(p.test(new Event("b"))); // t=20

        // DO NOT test a here — that would renew timestamp

        Thread.sleep(30); // total ~50ms

        assertTrue(p.test(new Event("a"))); // expired
        assertFalse(p.test(new Event("b"))); // still inside window
    }

    // ------------------------------------------------------------
    // distinctByTimeWindowAndNObservations
    // ------------------------------------------------------------
    @Test
    void distinctByTimeWindowAndNObservations_requiresBothTrue() throws InterruptedException {
        // Lite tidsvindu og maks 1 distinkt nøkkel i N-vinduet
        Predicate<Event> p = distinctByTimeWindowAndNObservations(
                Event::key,
                Duration.ofMillis(50),
                1
        );

        // Første a: begge vinduer sier "ny"
        assertTrue(p.test(new Event("a")));   // true

        // Andre a: begge sier "ikke ny"
        assertFalse(p.test(new Event("a")));  // false

        // La tidsvinduet utløpe for alle a-observasjoner
        Thread.sleep(60);

        // Nå legger vi inn b:
        // - N-vindu: [a] -> [a,b] -> evict a -> [b]
        // - Tidsvindu: alle a er utløpt, b er ny
        assertTrue(p.test(new Event("b")));   // true

        // Nå er:
        // - N-vindu: [b]
        // - Tidsvindu: ingen a, én b
        //
        // Når vi nå legger inn a:
        // - N-vindu: [b] -> [b,a] -> evict b -> [a]  (a er ny)
        // - Tidsvindu: ingen a fra før, så a er ny
        //
        // Begge predikater sier "true" → AND = true
        assertTrue(p.test(new Event("a")));   // true
    }

    // ------------------------------------------------------------
    // distinctByTimeWindowOrN
    // ------------------------------------------------------------
    @Test
    void distinctByTimeWindowOrNObservations_trueIfEitherPredicateTrue() throws InterruptedException {
        Predicate<Event> p = distinctByTimeWindowOrN(
                Event::key,
                Duration.ofMillis(100),
                2
        );

        assertTrue(p.test(new Event("a"))); // first
        assertFalse(p.test(new Event("a"))); // both false

        Thread.sleep(120);

        // time-window says true, N-window says false → OR = true
        assertTrue(p.test(new Event("a")));
    }

    // ------------------------------------------------------------
    // andStateful / orStateful
    // ------------------------------------------------------------
    @Test
    void andStateful_requiresAllPredicatesTrue() {
        Predicate<String> p1 = s -> s.length() > 2;
        Predicate<String> p2 = s -> s.startsWith("a");

        Predicate<String> combined = andStateful(p1, p2);

        assertTrue(combined.test("abc"));
        assertFalse(combined.test("ab"));
        assertFalse(combined.test("xbc"));
    }

    @Test
    void orStateful_requiresAtLeastOnePredicateTrue() {
        Predicate<String> p1 = s -> s.length() > 2;
        Predicate<String> p2 = s -> s.startsWith("a");

        Predicate<String> combined = orStateful(p1, p2);

        assertTrue(combined.test("abc"));
        assertTrue(combined.test("ab"));
        assertFalse(combined.test("x"));
    }
}
