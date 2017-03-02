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

    public boolean isChar(final int offset, final char c) {
        return hasChar(offset) && s.charAt(offset) == c;
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
    private boolean hasChar(final int offset) {
        return offset >= 0 && offset < s.length();
    }
}
