package se.eris.notnull.instrumentation;

import org.jetbrains.annotations.Contract;

public class StringWorker {

    private final String s;

    public StringWorker(final String s) {
        this.s = s;
    }

    public int length() {
        return s.length();
    }

    public boolean isChar(final int i, final char c) {
        return hasChar(i) && s.charAt(i) == c;
    }

    public boolean isString(final int offset, final String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!isChar(i + offset, s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    @Contract(pure = true)
    private boolean hasChar(final int i) {
        return i >= 0 && i < s.length();
    }
}
