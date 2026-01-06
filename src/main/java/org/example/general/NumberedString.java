package org.example.general;

public record NumberedString(int i, String s) {
    @SuppressWarnings("MethodNameSameAsClassName")
    public static NumberedString newNS(int i, String s) {
        return new NumberedString(i, s);
    }
}
