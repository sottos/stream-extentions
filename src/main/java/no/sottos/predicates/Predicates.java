package no.sottos.predicates;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.function.Function.identity;

public class Predicates {
    public static <T, U> Predicate<T> distinctBy(Function<? super T, U> keyExtractor) {
        Set<U> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    public static <T, U> Predicate<T> distinctInNObservations(
            Function<? super T, U> keyExtractor,
            int maxDistinctKeys) {

        Map<U, Boolean> window = new LinkedHashMap<>(maxDistinctKeys, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<U, Boolean> eldest) {
                return size() > maxDistinctKeys;
            }
        };

        return t -> {
            U key = keyExtractor.apply(t);
            boolean isNew = !window.containsKey(key);
            window.put(key, Boolean.TRUE); // put oppdaterer access-order selv ved eksisterende nøkkel
            return isNew;
        };
    }

    public static <T, U> Predicate<T> distinctByTimeWindow(
            Function<? super T, U> keyExtractor,
            Duration window) {

        long windowMs = window.toMillis();
        Map<U, Long> lastSeen = new LinkedHashMap<>(16, 0.75f, true);

        return t -> {
            long now = System.currentTimeMillis();
            long cutoff = now - windowMs;
            U key = keyExtractor.apply(t);

            // Rydd ut alle nøkler som ikke er sett innen vinduet.
            // Iterasjonsordenen er "eldst sist-sett først", så vi kan stoppe
            // ved første ikke-utgåtte oppføring.
            var it = lastSeen.entrySet().iterator();
            while (it.hasNext() && it.next().getValue() < cutoff) {
                it.remove();
            }

            boolean isNew = !lastSeen.containsKey(key);
            lastSeen.put(key, now); // oppdaterer tidsstempel og flytter til "nyeste"
            return isNew;
        };
    }
    /**
     * Kombinerer {@link #distinctInNObservations} og {@link #distinctByTimeWindow} med AND-logikk:
     * en nøkkel regnes som "ny" først når den ikke er sett verken
     * - blant de {@code maxSize} sist sette unike nøklene, OG
     * - innenfor det gitte tidsvinduet {@code window}
     * <p>
     * Begge kriteriene må altså være oppfylt samtidig for at noe skal regnes som duplikat.
     * Effekten er at en nøkkel anses som "ny" igjen så snart <em>ett</em> av de to vinduene
     * har "glemt" den — selv om det andre vinduet fortsatt husker den.
     * <p>
     * <b>Viktig:</b> Begge de underliggende predikatene evalueres alltid (ingen kortslutning,
     * jf. {@link #andStateful}), så begge sin interne tilstand (telling/tidsstempler)
     * oppdateres ved hvert kall, uavhengig av resultatet.
     *
     * @param keyExtractor funksjon som henter ut nøkkelen som skal vurderes for duplikat
     * @param window tidsvinduet en nøkkel "huskes" innenfor
     * @param maxSize maks antall distinkte nøkler som "huskes" samtidig
     * @return et stateful predikat som returnerer {@code true} hvis nøkkelen er ny
     *         iht. begge kriteriene
     */
    public static <T, U> Predicate<T> distinctByTimeWindowAndNObservations(
            Function<? super T, U> keyExtractor,
            Duration window,
            int maxSize) {
        Predicate<U> countPredicate = distinctInNObservations(identity(), maxSize);
        Predicate<U> durationPredicate = distinctByTimeWindow(identity(), window);

        return t -> andStateful(
                countPredicate,
                durationPredicate
        ).test(keyExtractor.apply(t));
    }
    /**
     * Kombinerer {@link #distinctInNObservations} og {@link #distinctByTimeWindow} med OR-logikk:
     * en nøkkel regnes som "ny" hvis den ikke er sett
     * - blant de {@code maxSize} sist sette unike nøklene, ELLER
     * - innenfor det gitte tidsvinduet {@code window}
     * <p>
     * Det er altså tilstrekkelig at <em>ett</em> av kriteriene anser nøkkelen som ny.
     * Dette gir en strengere duplikat-sperre enn AND-varianten: en nøkkel må være "glemt"
     * av <em>begge</em> vinduene før den slipper gjennom som ny igjen.
     * <p>
     * <b>Viktig:</b> Begge de underliggende predikatene evalueres alltid (ingen kortslutning,
     * jf. {@link #orStateful}), så begge sin interne tilstand (telling/tidsstempler)
     * oppdateres ved hvert kall, uavhengig av resultatet.
     *
     * @param keyExtractor funksjon som henter ut nøkkelen som skal vurderes for duplikat
     * @param window tidsvinduet en nøkkel "huskes" innenfor
     * @param maxSize maks antall distinkte nøkler som "huskes" samtidig
     * @return et stateful predikat som returnerer {@code true} hvis nøkkelen er ny
     *         iht. minst ett av kriteriene
     */
    public static <T, U> Predicate<T> distinctByTimeWindowOrN(
            Function<? super T, U> keyExtractor,
            Duration window,
            int maxSize) {
        Predicate<U> countPredicate = distinctInNObservations(identity(), maxSize);
        Predicate<U> durationPredicate = distinctByTimeWindow(identity(), window);

        return t -> orStateful(
                countPredicate,
                durationPredicate
        ).test(keyExtractor.apply(t));
    }

    @SafeVarargs
    public static <T> Predicate<T> andStateful(Predicate<T>... predicates) {
        Objects.requireNonNull(predicates);
        if (predicates.length == 0) {
            throw new IllegalArgumentException("Need at least one predicate");
        }

        return t -> {
            boolean result = true;
            for (Predicate<T> p : predicates) {
                result &= p.test(t); // always evaluates each test, no short circuit here
            }
            return result;
        };
    }

    @SafeVarargs
    public static <T> Predicate<T> orStateful(Predicate<T>... predicates) {
        Objects.requireNonNull(predicates);
        if (predicates.length == 0) {
            throw new IllegalArgumentException("Need at least one predicate");
        }

        return t -> {
            boolean result = false;
            for (Predicate<T> p : predicates) {
                result |= p.test(t); // always evaluates each test, no short circuit here
            }
            return result;
        };
    }

    @SafeVarargs
    public static <T> Predicate<T> andShortCircuit(Predicate<T>... predicates) {
        Objects.requireNonNull(predicates);
        if (predicates.length == 0) {
            throw new IllegalArgumentException("Need at least one predicate");
        }

        return t -> {
            for (Predicate<T> p : predicates) {
                if (!p.test(t)) {
                    return false; // stops on first false test
                }
            }
            return true;
        };
    }
    @SafeVarargs
    public static <T> Predicate<T> orShortCircuit(Predicate<T>... predicates) {
        Objects.requireNonNull(predicates);
        if (predicates.length == 0) {
            throw new IllegalArgumentException("Need at least one predicate");
        }

        return t -> {
            for (Predicate<T> p : predicates) {
                if (p.test(t)) {
                    return true; // stops on first true test
                }
            }
            return false;
        };
    }


}
