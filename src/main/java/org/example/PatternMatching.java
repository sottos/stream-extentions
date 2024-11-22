package org.example;

import org.example.general.*;

import java.util.List;

public class PatternMatching {

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
                case Tup2<?, ?>(var a, var b) when a instanceof String -> System.out.println("a = "+a+" b = "+b);
                case Tup2<?,?> tup2 -> System.out.println("tup2 = " + tup2);
                case null -> System.out.println("null");
            }
        }
        System.out.println("tup2s take 2");
        for (Tup2<? extends Object,? extends Object>t2 : tup2s) {
            switch(t2) {
                case Tup2<?,?> (String a, String b): System.out.println("String String a = "+a+" b = "+b); break;
                case Tup2<?,?> (Integer a, String b): System.out.println("Integer String a = "+a+" b = "+b); break;
                case Tup2<?,?> (Integer a, var b): System.out.println("Integer ? a = "+a+" b = "+b); break;
                case Tup2<?,?> tup2 : System.out.println("tup2 = " + tup2);
                case null : System.out.println("null");
            }
        }
        try {
            System.out.println("tup2s take 3");
            for (Tup2<? extends Object, ? extends Object> t2 : tup2s) {
                switch (t2) {
                    case Tup2<?, ?>(String a, String b) -> System.out.println("String String a = "+a+" b = "+b);
                    case Tup2<?, ?>(Integer a, String b) -> System.out.println("Integer String a = "+a+" b = "+b);
                    case Tup2<?, ?>(Integer a, var b) -> System.out.println("Integer ? a = "+a+" b = "+b);
                    default -> System.out.println("default");
                }
            }
        } catch (NullPointerException npe) {

        }

        if (tups.get(0) instanceof Tup2<?, ?>(Integer a, String b) ) {
            System.out.println("Integer String a = "+a+" b = "+b);
        }
        if (tups.get(0) instanceof Tup2<?, ?> ab ) {
            System.out.println("tup ab = "+ab);
        }
//        if (tups.get(0) instanceof Tup2<Integer,String> cd ) {
//            System.out.println("tup cd = \{cd}");
//        }
        if (((Object)new SSTUP("test",5)) instanceof SSTUP sstup) {
            System.out.println("sstup = " + sstup);
        }
        if (((Object)new SSTUP("test",5)) instanceof SSTUP(String x, int i)) {
            System.out.println("x i = "+x+" "+i);
        }
        if (((Object)new SSTUP("test",5)) instanceof SSTUP(var x, var i)) {
            System.out.println("x i = "+x+" "+i);

        }
        Object unknown = 3; // Object unknown gir else utgang, var gir integers
        var tt = new TupN<>(1,unknown,5,7);

        switch (tt) {
            case TupN<?>(Integer[] integers) -> System.out.println("integers = " + integers);
            case TupN<?>(String[] strings) -> System.out.println("strings = " + strings);
            case TupN<?>(var elses) -> System.out.println("elses = " + elses);
            default -> System.out.println("else"); // Må ha en som ikke baserer seg på dekonstruksjon for å slippe default. Se under
        }
        switch (tt) {
            case TupN<?>(Integer[] integers) -> System.out.println("integers = " + integers.length);
            case TupN<?>(String[] strings) -> System.out.println("strings = " + strings.length);
            // case TupN<?>(var  elses) -> System.out.println("elses = " + elses.getClass().getSimpleName()); // does not cover all input values
            case TupN<?> tup -> System.out.println("tup = " + tup.getClass().getSimpleName());
        }

    }

    // Record patterns in enhanced for loop does not compile

//    record Pair<T>(T x, T y) {}
//
//    static <T> void printPairArray(Pair<T>[] pa) {
//        for (Pair(T first, T second) : pa) {
//            System.out.println("(" + first + ", " + second + ")");
//        }
//    }
}
