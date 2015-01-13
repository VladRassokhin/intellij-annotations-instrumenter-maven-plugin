package se.eris.util;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * To make testing easier. There are better ReflectionUtil out there but this minimizes dependencies.
 */
public class ReflectionUtil {

    public static void simulateMethodCall(@NotNull final Method notNullReturnMethod, @NotNull final Object... params) throws IllegalAccessException, InvocationTargetException {
        try {
            notNullReturnMethod.invoke(null, params);
        } catch (final InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException) {
                throw RuntimeException.class.cast(e.getCause());
            } else {
                throw e;
            }
        }
    }
}
