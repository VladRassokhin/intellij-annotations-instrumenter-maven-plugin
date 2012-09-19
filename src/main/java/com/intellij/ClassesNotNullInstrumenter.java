/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

/**
 * @author Vladislav.Rassokhin
 */
@Mojo(
        name = "instrument",
        defaultPhase = LifecyclePhase.PROCESS_CLASSES,
        requiresOnline = false,
        requiresProject = true,
        requiresDependencyResolution = ResolutionScope.COMPILE)
public class ClassesNotNullInstrumenter extends com.intellij.AbstractNotNullInstrumenterTask {

    public void execute() throws org.apache.maven.plugin.MojoExecutionException {
        try {
            instrument(project.getBuild().getOutputDirectory(), project.getCompileClasspathElements());
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
}
