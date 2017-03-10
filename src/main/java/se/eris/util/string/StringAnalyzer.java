package se.eris.util.string;

import org.jetbrains.annotations.Contract;

public class StringAnalyzer {

    private final String string;

    public StringAnalyzer(final String string) {
        this.string = string;
    }

    public int length() {
        return string.length();
    }

    public boolean isCharAt(final int offset, final char c) {
        return hasChar(offset) && string.charAt(offset) == c;
    }

    public boolean isStringAt(final int offset, final String s) {
        if (offset + s.length() > string.length()) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            if (!isCharAt(i + offset, s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    @Contract(pure = true)
    private boolean hasChar(final int offset) {
        return offset >= 0 && offset < string.length();
    }

    public char charAt(final int offset) {
        return string.charAt(offset);
    }
}
