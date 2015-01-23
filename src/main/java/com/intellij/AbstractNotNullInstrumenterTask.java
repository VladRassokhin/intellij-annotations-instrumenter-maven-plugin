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

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * @author Vladislav.Rassokhin
 * @author Olle Sundblad
 */
public abstract class AbstractNotNullInstrumenterTask extends AbstractMojo {

    @SuppressWarnings("UnusedDeclaration")
    @Component
    protected MavenProject project;

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Parameter
    private List<String> annotations;

    private NotNullInstrumenter instrumenter = new NotNullInstrumenter(new MavenLogWrapper(getLog()));

    protected void instrument(@NotNull final String classesDirectory, @NotNull final List<String> classpathElements) throws MojoExecutionException {
        final Set<String> notNullAnnotations = getNotNullAnnotations();
        final List<URL> urls = new ArrayList<URL>();
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
            final int instrumented = instrumenter.addNotNullAnnotations(classesDirectory, notNullAnnotations, urls);
        getLog().info("Added @NotNull assertions to " + instrumented + " files");
    }

    @NotNull
    private Set<String> getNotNullAnnotations() {
        final Set<String> notNullAnnotations = new HashSet<String>();
        if (isConfigurationOverrideAnnotations()) {
            notNullAnnotations.addAll(annotations);
            logAnnotations();
        } else {
            notNullAnnotations.add(NotNull.class.getName());
        }
        return notNullAnnotations;
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
