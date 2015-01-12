package se.eris.maven;

import org.jetbrains.annotations.NotNull;

public interface LogWrapper {

    void debug(@NotNull String message);
    void info(@NotNull String message);
    void warn(@NotNull String message);
    void error(@NotNull String message);
}
