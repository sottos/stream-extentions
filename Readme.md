# Zippers, GathererDSL, and Pipeline
Utilities for advanced Java Stream composition

## Zippers — Combining multiple streams
Zippers are the main feature of this project. They provide several ways to merge two or more streams into a single stream of combined elements.

They allow merging of infinite streams.

### Zipper variants
- **Spliterator-based (`zip`)** — used at the start of a pipeline.

```java
var result = Zippers.zip(
        NumberedString::new,
        Stream.iterate(1, x -> x + 1),
        Stream.of("One", "Two", "Three")
).toList();
```

- **Gatherer-based (`zipWith`)** — used mid‑pipeline via `.gather(...)`.

```java
var result = Stream.of("One", "Two", "Three")
        .gather(Zippers.zipWith(
                (s, i) -> new NumberedString(i, s),
                Stream.iterate(1, x -> x + 1)
        ))
        .toList();
```

- **Collector-based (`zipped`)** — used as a terminal operation.

```java
var result = Stream.of("One", "Two", "Three")
        .collect(Zippers.zipped(
                Collectors.toList(),
                (s, i) -> new NumberedString(i, s),
                Stream.iterate(1, x -> x + 1)
        ));
```

**All of the above examples have the same result**
```java
[
  NumberedString(1, "One"),
  NumberedString(2, "Two"),
  NumberedString(3, "Three"),
]
```

### Termination behavior
- `WHEN_ALL_HAVE_DATA` — stop when the shortest stream ends (default)
- `WHEN_AT_LEAST_ONE_HAVE_DATA` — continue until all streams are empty, filling missing values with `null`

Zippers support merging of 2–5 streams, plus a varargs version.

---

## GathererDSL — Gatherer versions of common Stream operations
Provides gatherer equivalents of: `map`, `filter`, `flatMap`, `takeWhile`, `dropWhile`, `limit`, `skip`, `peek`.

Example:

```java
var result = Stream.of(1, 2, 3, 4)
        .gather(GathererDSL.filter(x -> x % 2 == 0))
        .gather(GathererDSL.map(x -> x * 10))
        .toList();
// [20, 40]
```

---

## Pipeline — Composable gatherer chains
A fluent builder for reusable, stateless gatherer pipelines.

```java
var pipeline = Pipeline.<Integer>start()
        .map(x -> x + 1)
        .filter(x -> x % 2 == 0)
        .toGatherer();

var result = Stream.of(1, 2, 3)
        .gather(pipeline)
        .toList();
// [2]
```

Pipelines can be combined using `.then(...)`.

---

## Requirements
- Java 24+ (for the Gatherer API)
