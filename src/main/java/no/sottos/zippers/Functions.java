package no.sottos.zippers;

public class Functions {
    @FunctionalInterface
    public interface OneArg<T, R> {
        R apply(T t);
    }

    @FunctionalInterface
    public interface TwoArgs<A, B, R> {
        R apply(A a, B b);
    }

    @FunctionalInterface
    public interface ThreeArgs<A, B, C, R> {
        R apply(A a, B b, C c);
    }

    @FunctionalInterface
    public interface FourArgs<A, B, C, D, R> {
        R apply(A a, B b, C c, D d);
    }

    @FunctionalInterface
    public interface FiveArgs<A, B, C, D, E, R> {
        R apply(A a, B b, C c, D d, E e);
    }

    @FunctionalInterface
    public interface ArgsInArrayFunction<R> {
        R apply(Object[] args);
    }

    @SuppressWarnings("unchecked")
    public static <T, R> ArgsInArrayFunction<R> toArrayArgs(OneArg<T, R> lambda) {
        return args -> lambda.apply((T) args[0]);
    }

    @SuppressWarnings("unchecked")
    public static <A, B, R> ArgsInArrayFunction<R> toArrayArgs(TwoArgs<A, B, R> lambda) {
        return args -> lambda.apply((A) args[0], (B) args[1]);
    }

    @SuppressWarnings("unchecked")
    public static <A, B, C, R> ArgsInArrayFunction<R> toArrayArgs(ThreeArgs<A, B, C, R> lambda) {
        return args -> lambda.apply((A) args[0], (B) args[1], (C) args[2]);
    }

    @SuppressWarnings("unchecked")
    public static <A, B, C, D, R> ArgsInArrayFunction<R> toArrayArgs(FourArgs<A, B, C, D, R> lambda) {
        return args -> lambda.apply((A) args[0], (B) args[1], (C) args[2], (D) args[3]);
    }

    @SuppressWarnings("unchecked")
    public static <A, B, C, D, E, R> ArgsInArrayFunction<R> toArrayArgs(FiveArgs<A, B, C, D, E, R> lambda) {
        return args -> lambda.apply((A) args[0], (B) args[1], (C) args[2], (D) args[3], (E) args[4]);
    }

}
