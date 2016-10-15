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
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
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
