package org.example;

import java.util.List;
import java.util.Objects;

public class PatternMatching {

    public interface Tup {
        default String name() { return getClass().getSimpleName(); }
    }
    public record Tup2<A,B>(A a, B b) implements Tup {
    }
    record Tup3<A,B,C>(A a, B b, C c) implements Tup {
    }
    record Tup4<A,B,C,D>(A a, B b, C c, D d) implements Tup {}

    record TupTup<T>(T ... ts) implements Tup {}

    record SSTUP(String s, int i) {}
    public static void main(String[] args) {
        List<Tup> tups = List.of(new Tup2<>(1,"x"), new Tup3(2,"y", 1.3), new Tup2(1,1.3), new Tup4(3,"z",1.4, new Tup2("a", false)), new Tup2<>("x", "y"));
        for (Tup t: tups) {
            System.out.println("t = " + t);
        }
        List<? extends Tup2<?, ?>> tup2s = tups.stream()
                .map(t -> switch (t) { case Tup2<?,?> t2 -> t2; default -> null; })
                .toList();
        System.out.println("tup2s take 1");
        for (Tup2<?,?>t2 : tup2s) {
            switch(t2) {
                case Tup2<?, ?>(var a, var b) when a instanceof String -> System.out.println(STR."a = \{a} b = \{b}");
                case Tup2<?,?> tup2 -> System.out.println("tup2 = " + tup2);
                case null -> System.out.println("null");
            }
        }
        System.out.println("tup2s take 2");
        for (Tup2<? extends Object,? extends Object>t2 : tup2s) {
            switch(t2) {
                case Tup2<?,?> (String a, String b): System.out.println(STR."String String a = \{a} b = \{b}"); break;
                case Tup2<?,?> (Integer a, String b): System.out.println(STR."Integer String a = \{a} b = \{b}"); break;
                case Tup2<?,?> (Integer a, var b): System.out.println(STR."Integer ? a = \{a} b = \{b}"); break;
                case Tup2<?,?> tup2 : System.out.println("tup2 = " + tup2);
                case null : System.out.println("null");
            }
        }
        try {
            System.out.println("tup2s take 3");
            for (Tup2<? extends Object, ? extends Object> t2 : tup2s) {
                switch (t2) {
                    case Tup2<?, ?>(String a, String b) -> System.out.println(STR."String String a = \{a} b = \{b}");
                    case Tup2<?, ?>(Integer a, String b) -> System.out.println(STR."Integer String a = \{a} b = \{b}");
                    case Tup2<?, ?>(Integer a, var b) -> System.out.println(STR."Integer ? a = \{a} b = \{b}");
                    default -> System.out.println("default");
                }
            }
        } catch (NullPointerException npe) {

        }

        if (tups.get(0) instanceof Tup2<?, ?>(Integer a, String b) ) {
            System.out.println(STR."Integer String a = \{a} b = \{b}");
        }
        if (tups.get(0) instanceof Tup2<?, ?> ab ) {
            System.out.println(STR."tup ab = \{ab}");
        }
//        if (tups.get(0) instanceof Tup2<Integer,String> cd ) {
//            System.out.println(STR."tup cd = \{cd}");
//        }
        if (((Object)new SSTUP("test",5)) instanceof SSTUP sstup) {
            System.out.println(STR."sstup = \{sstup}");
        }
        if (((Object)new SSTUP("test",5)) instanceof SSTUP(String x, int i)) {
            System.out.println(STR."x i = \{x} \{i}");
        }
        if (((Object)new SSTUP("test",5)) instanceof SSTUP(var x, var i)) {
            System.out.println(STR."x i = \{x} \{i}");

        }
        Object unknown = 3; // Object unknown gir else utgang, var gir integers
        var tt = new TupTup<>(1,unknown,5,7);

        switch (tt) {
            case TupTup<?>(Integer[] integers) -> System.out.println("integers = " + integers);
            case TupTup<?>(String[] strings) -> System.out.println("strings = " + strings);
            case TupTup<?>(var elses) -> System.out.println("elses = " + elses);
            default -> System.out.println("else"); // Må ha en som ikke baserer seg på dekonstruksjon for å slippe default. Se under
        }
        switch (tt) {
            case TupTup<?>(Integer[] integers) -> System.out.println("integers = " + integers.length);
            case TupTup<?>(String[] strings) -> System.out.println("strings = " + strings.length);
            // case TupTup<?>(var  elses) -> System.out.println("elses = " + elses.getClass().getSimpleName()); // does not cover all input values
            case TupTup<?> tup -> System.out.println("tup = " + tup.getClass().getSimpleName());
        }

    }

    // Record patterns in enhanced for loop does not compile

//    record Pair<T>(T x, T y) {}
//
//    static void printPairArray(Pair[] pa) {
//        for (Pair(var first, var second) : pa) {
//            System.out.println("(" + first + ", " + second + ")");
//        }
//    }
}
