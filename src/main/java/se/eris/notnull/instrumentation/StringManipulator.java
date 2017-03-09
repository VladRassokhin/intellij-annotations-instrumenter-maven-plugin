package se.eris.notnull.instrumentation;

class StringManipulator {

    private final StringBuilder stringBuilder = new StringBuilder();
    private final StringAnalyzer stringAnalyzer;
    private int index;

    StringManipulator(final String s) {
        this.stringAnalyzer = new StringAnalyzer(s);
        index = 0;
    }

    public void step() {
        stringBuilder.append(stringAnalyzer.charAt(index));
        index++;
    }

    public void stepOver() {
        index++;
    }

    public void insert(final String s) {
        stringBuilder.append(s);
    }

    public boolean replaceIfMatch(final Replacement replacement) {
        final String replace = replacement.getReplace();
        if (stringAnalyzer.isStringAt(index, replace)) {
            stringBuilder.append(replacement.getWith());
            index += replace.length();
            return true;
        }
        return false;
    }

    public boolean isDone() {
        return index >= stringAnalyzer.length();
    }

    public String toString() {
        return stringBuilder.toString();
    }

}
