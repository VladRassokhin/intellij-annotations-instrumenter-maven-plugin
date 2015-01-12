package se.eris.notnull;

import org.jetbrains.annotations.NotNull;

public class InstrumenterExecutionException extends RuntimeException {

    public InstrumenterExecutionException(@NotNull final String message, @NotNull final Throwable throwable) {
        super(message, throwable);
    }

}
