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
