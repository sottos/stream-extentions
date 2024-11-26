package org.example.simple;

import org.example.general.Tup2;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.stream.Gatherer;
import java.util.stream.Gatherer.Integrator;
import java.util.stream.Stream;

public class ZipGatherer {


    public static <T, U> Gatherer<T, ?, Tup2<T, U>> zip(Stream<U> uStream) {
        return zip(uStream, ZipWhen.WHEN_ALL_CAN_ADVANCE);
    }
    public static <T, U> Gatherer<T, ?, Tup2<T, U>> zip(Stream<U> uStream, ZipWhen zipWhen) {

        class InternalZipper {
            Iterator<U> uIterator = uStream.iterator();

            /**
             * Will be called when a T - element from the upStream is ready to be sent further down the stream pipeline.
             * Will add the T element and a U element to a Tup2 and send that to the downstream
             * @param tElement
             * @param downstream
             * @return
             */
            boolean integrate(T tElement, Gatherer.Downstream<? super Tup2<T, U>> downstream) {
                if (!downstream.isRejecting()) {
                    if (uIterator.hasNext()) {
                        return downstream.push(new Tup2<>(tElement, uIterator.next()));
                    } else if (zipWhen == ZipWhen.WHEN_AT_LEAST_ONE_CAN_ADVANCE) {
                        // More left of tStream since we have tElement
                        return downstream.push(new Tup2<>(tElement, null));
                    }
//                    try {
//                        if (getBooleanFieldValue(downstream, "downstreamProceed")) {
//                            setBooleanFieldValue(downstream, "downstreamProceed", false);
//                        }
//                    } catch (Throwable t) {
//                        System.out.println("t = " + t);
//                        throw t;
//                    }

                }
                return false;
            }

            /**
             * Will be called when either downStream.isRejecting or there are no more tElement to call integrate with.
             * @param downstream
             */
            public void finish(Gatherer.Downstream<? super Tup2<T, U>> downstream) {
                if (zipWhen == ZipWhen.WHEN_AT_LEAST_ONE_CAN_ADVANCE) {
                    while (!downstream.isRejecting()
                            && uIterator.hasNext()
                            && downstream.push(new Tup2<>(null, uIterator.next()))) {
                    }
                }
            }
        }

        return Gatherer.<T, InternalZipper, Tup2<T, U>>ofSequential(
                InternalZipper::new,
                Integrator.<InternalZipper, T, Tup2<T, U>>ofGreedy(InternalZipper::integrate),
                InternalZipper::finish
        );
    }

    public static boolean getBooleanFieldValue(Object obj, String fieldName) {
        if (obj == null || fieldName == null) {
            throw new NullPointerException("obj or fieldName is null");
        }

        Class<?> clazz = obj.getClass();

        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true); // Access private fields
                Object value = field.get(obj);

                if (value instanceof Boolean) {
                    return (Boolean) value;
                }
                throw new IllegalStateException("fieldName " + fieldName + " not Boolean in object " + obj.getClass().getName());
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass(); // Check in superclass
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("fieldName " + fieldName + " found but not accessible in object " + obj.getClass().getName());
            }
        }

        throw new IllegalStateException("fieldName " + fieldName + " not found in object " + obj.getClass().getName());

    }
    public static void setBooleanFieldValue(Object obj, String fieldName, boolean newValue) {
        if (obj == null || fieldName == null) {
            throw new NullPointerException("obj or fieldName is null");
        }

        Class<?> clazz = obj.getClass();

        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true); // Access private fields
                Object value = field.get(obj);

                if (value instanceof Boolean) {
                    field.setBoolean(obj, newValue);
                    return;
                } else {
                    throw new IllegalStateException("fieldName " + fieldName + " not Boolean in object " + obj.getClass().getName());
                }
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass(); // Check in superclass
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("fieldName " + fieldName + " found but not accessible in object " + obj.getClass().getName());
            }
        }

        throw new IllegalStateException("fieldName " + fieldName + " not found in object " + obj.getClass().getName());

    }
}
