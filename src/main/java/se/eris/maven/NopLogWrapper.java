package se.eris.maven;

import org.jetbrains.annotations.NotNull;

public class NopLogWrapper implements LogWrapper {

    @Override
    public void debug(@NotNull final String message) {
    }

    @Override
    public void info(@NotNull final String message) {
    }

    @Override
    public void warn(@NotNull final String message) {
    }

    @Override
    public void error(@NotNull final String message) {
    }

}
