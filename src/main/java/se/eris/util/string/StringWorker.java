package se.eris.util.string;

import java.util.List;

class StringWorker {

    private final StringBuilder stringBuilder = new StringBuilder();
    private final StringAnalyzer stringAnalyzer;
    private int index;

    StringWorker(final String s) {
        this.stringAnalyzer = new StringAnalyzer(s);
        index = 0;
    }

    private void step() {
        stringBuilder.append(stringAnalyzer.charAt(index));
        index++;
    }

    public void insert(final String s) {
        stringBuilder.append(s);
    }

    private boolean isDone() {
        return index >= stringAnalyzer.length();
    }

    /**
     * Iterates character by character once replacing using the first matching replacement.
     * @param replacements
     */
    public void replaceInOrder(final List<Replacement> replacements) {
        while (!isDone()) {
            if (!replaceFirstMatching(replacements)) {
                step();
            }
        }
    }

    private boolean replaceFirstMatching(final List<Replacement> replacements) {
        for (final Replacement replacement : replacements) {
            if (replaceIfMatch(replacement)) {
                return true;
            }
        }
        return false;
    }

    private boolean replaceIfMatch(final Replacement replacement) {
        final String replace = replacement.getReplace();
        if (stringAnalyzer.isStringAt(index, replace)) {
            stringBuilder.append(replacement.getWith());
            index += replace.length();
            return true;
        }
        return false;
    }

    public String toString() {
        return stringBuilder.toString();
    }


}
