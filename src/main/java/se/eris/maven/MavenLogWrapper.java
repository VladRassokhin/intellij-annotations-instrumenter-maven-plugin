package se.eris.maven;

import org.apache.maven.plugin.logging.Log;
import org.jetbrains.annotations.NotNull;

public class MavenLogWrapper implements LogWrapper {

    @NotNull
    private final Log logger;

    public MavenLogWrapper(@NotNull final Log logger) {
        this.logger = logger;
    }

    @Override
    public void debug(@NotNull final String message) {
        logger.debug(message);
    }

    @Override
    public void info(@NotNull final String message) {
        logger.info(message);
    }

    @Override
    public void warn(@NotNull final String message) {
        logger.warn(message);
    }

    @Override
    public void error(@NotNull final String message) {
        logger.error(message);
    }
}
