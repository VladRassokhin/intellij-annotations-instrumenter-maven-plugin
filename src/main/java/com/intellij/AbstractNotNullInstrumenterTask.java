/*
 * Copyright 2000-2013 JetBrains s.r.o.
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
package com.intellij;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.jetbrains.annotations.NotNull;
import se.eris.maven.MavenLogWrapper;
import se.eris.notnull.NotNullConfiguration;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Vladislav.Rassokhin
 * @author Olle Sundblad
 */
abstract class AbstractNotNullInstrumenterTask extends AbstractMojo {

    @Component
    MavenProject project;

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Parameter
    private List<String> annotations;
    @Parameter
    private boolean implicit;

    private final NotNullInstrumenter instrumenter = new NotNullInstrumenter(new MavenLogWrapper(getLog()));

    void instrument(@NotNull final String classesDirectory, @NotNull final List<String> classpathElements) throws MojoExecutionException {
        final NotNullConfiguration configuration = getConfiguration();
        final List<URL> urls = new ArrayList<>();
        try {
            for (final String cp : classpathElements) {
                urls.add(new File(cp).toURI().toURL());
            }
        }
        catch (final MalformedURLException e) {
            throw new MojoExecutionException("Cannot convert classpath element into URL", e);
        }
        catch (final RuntimeException e) {
            throw new MojoExecutionException(e.getMessage(), e.getCause());
        }
        final int instrumented = instrumenter.addNotNullAnnotations(classesDirectory, configuration, urls);
        getLog().info("Added @NotNull assertions to " + instrumented + " files");
    }

    private NotNullConfiguration getConfiguration() {
        return new NotNullConfiguration(implicit, getAnnotations());
    }

    @NotNull
    private Set<String> getAnnotations() {
        final Set<String> annotations = new HashSet<>();
        if (isConfigurationOverrideAnnotations()) {
            annotations.addAll(this.annotations);
            logAnnotations();
        } else {
            annotations.add(NotNull.class.getName());
        }
        return annotations;
    }

    private void logAnnotations() {
        getLog().info("Using the following NotNull annotations:");
        for (final String notNullAnnotation : annotations) {
            getLog().info("  " + notNullAnnotation);
        }
    }

    private boolean isConfigurationOverrideAnnotations() {
        return annotations != null && !annotations.isEmpty();
    }

}
