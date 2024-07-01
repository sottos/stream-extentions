package org.example;

public class RecordExes {
    record Tup2<A, B>(A a, B b) {
    }

    record Tup3<A, B, C>(A a, B b, C c) {
        Tup3(Tup2<A, B> ab, C c) {
            this(ab.a, ab.b, c);
        }

        Tup3(A a, Tup2<B, C> bc) {
            this(a, bc.a, bc.b);
        }

        Tup3(Tup3<A, B, C> abc) {
            this(abc.a, abc.b, abc.c);
        }
    }

    public static void main(String[] args) {
        var t3 = new Tup3(1, "string", 'c');
        switch (t3) {
            case Tup3<?, ?, ?>(Integer a, String b, Character c) -> System.out.println("found a b c");
            // The below does not compile, and that is good, there is no implicit bifunction between
            // inner components and constructor parameters. Constructor may do whatever it likes.
//            case Tup3<?,?,?>(Tup2<?, ?>(String a, Integer b), Character c) -> System.out.println("found t2,c");
//            case Tup3<?,?,?>(Tup2<?,?>(String a, Integer b), Character c) -> System.out.println("found t2,c");
            default -> System.out.println("Not found");
        }

        // https://docs.oracle.com/javase/specs/jls/se21/html/jls-14.html#jls-14.11.2
        // An enhanced switch statement is one where either
        // (i) the type of the selector expression is
        //          not char, byte, short, int, Character, Byte, Short, Integer, String, or an enum type, or
        // (ii) there is a case pattern or null literal associated with the switch block.

        String x = null;
        switch (x) {
            case null:
                System.out.println("found null");
                break;
            default:
                System.out.println("not found null");
                break;
        }
        try {
            switch (x) { // Gives NPE exception since this is not an enchanced switch

                case "":
                    System.out.println("found empty");
            }
        } catch (NullPointerException e) {
            System.out.println("NPE as expected");
        }
    }


}
