package se.eris.util.string;

import java.util.ArrayList;
import java.util.List;

public class StringReplacer {

    public static StringReplacer.Builder init() {
        return StringReplacer.Builder.init();
    }

    private final String prefix;
    private final String suffix;
    private final List<Replacement> replacements = new ArrayList<>();

    public StringReplacer(final String prefix, final List<Replacement> replacements, final String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.replacements.addAll(replacements);
    }

    public String apply(final String s) {
        final StringWorker worker = new StringWorker(s);
        worker.insert(prefix);
        worker.replace(replacements);
        worker.insert(suffix);
        return worker.toString();
    }

    public static class Builder {

        public static Builder init() {
            return new Builder();
        }

        private String prefix = "";
        private String suffix = "";
        private final List<Replacement> replacements = new ArrayList<>();

        public Builder prefix(final String prefix) {
            this.prefix = prefix;
            return this;
        }

        public Builder suffix(final String suffix) {
            this.suffix = suffix;
            return this;
        }

        public Builder add(final Replacement replacement) {
            replacements.add(replacement);
            return this;
        }

        public Builder add(final String replace, final String with) {
            return add(new Replacement(replace, with));
        }

        public StringReplacer build() {
            return new StringReplacer(prefix, replacements, suffix);
        }

    }
}
