/*
 * Copyright 2013-2015 Eris IT AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.eris.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * To make testing easier. There are better ReflectionUtil out there but this minimizes dependencies.
 */
public class ReflectionUtil {

    public static Object simulateMethodCall(@NotNull final Method method, @NotNull final Object... params) throws IllegalAccessException, InvocationTargetException {
        return simulateMethodCall(null, method, params);
    }

    public static Object simulateMethodCall(@Nullable final Object o, @NotNull final Method method, @NotNull final Object... params) throws IllegalAccessException, InvocationTargetException {
        try {
            return method.invoke(o, params);
        } catch (final InvocationTargetException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw RuntimeException.class.cast(cause);
            } else {
                throw e;
            }
        }
    }

    public static Object simulateConstructorCall(@NotNull final Constructor constructor, @NotNull final Object... params) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        try {
            return constructor.newInstance(params);
        } catch (final InstantiationException | InvocationTargetException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw RuntimeException.class.cast(cause);
            } else {
                throw e;
            }
        }
    }
}
