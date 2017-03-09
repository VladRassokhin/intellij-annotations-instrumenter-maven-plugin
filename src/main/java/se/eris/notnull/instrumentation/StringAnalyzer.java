package se.eris.notnull.instrumentation;

import org.jetbrains.annotations.Contract;

public class StringAnalyzer {

    private final String s;

    public StringAnalyzer(final String s) {
        this.s = s;
    }

    public int length() {
        return s.length();
    }

    public boolean isCharAt(final int offset, final char c) {
        return hasChar(offset) && s.charAt(offset) == c;
    }

    public boolean isStringAt(final int offset, final String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!isCharAt(i + offset, s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    @Contract(pure = true)
    private boolean hasChar(final int offset) {
        return offset >= 0 && offset < s.length();
    }

    public char charAt(final int offset) {
        return s.charAt(offset);
    }
}
