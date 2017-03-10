package se.eris.util.string;

public class Replacement {
    private final String replace;
    private final String with;

    public Replacement(final String replace, final String with) {
        this.replace = replace;
        this.with = with;
    }

    public String getReplace() {
        return replace;
    }

    public String getWith() {
        return with;
    }
}
