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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * To make testing easier. There are better ReflectionUtil out there but this minimizes dependencies.
 */
public class ReflectionUtil {

    public static Object simulateMethodCall(@NotNull final Method method, @NotNull final Object... params) throws IllegalAccessException, InvocationTargetException {
        try {
            return method.invoke(null, params);
        } catch (final InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException) {
                throw RuntimeException.class.cast(e.getCause());
            } else {
                throw e;
            }
        }
    }
}
