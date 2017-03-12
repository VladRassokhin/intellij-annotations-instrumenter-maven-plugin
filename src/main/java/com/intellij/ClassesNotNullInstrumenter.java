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

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;
import java.nio.file.Path;

/**
 * @author Vladislav.Rassokhin
 * @author Olle Sundblad
 */
@SuppressWarnings({"UnusedDeclaration", "DefaultAnnotationParam"})
@Mojo(
        name = "instrument",
        defaultPhase = LifecyclePhase.PROCESS_CLASSES,
        requiresOnline = false,
        requiresProject = true,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        threadSafe = true)
public class ClassesNotNullInstrumenter extends AbstractNotNullInstrumenterTask {

    @Override
    public void execute() throws MojoExecutionException {
        try {
            final Path classesDirectory = new File(project.getBuild().getOutputDirectory()).toPath();
            instrument(classesDirectory, project.getCompileClasspathElements());
        }
        catch (final DependencyResolutionRequiredException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
}
