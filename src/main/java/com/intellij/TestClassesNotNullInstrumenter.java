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
        name = "tests-instrument",
        defaultPhase = LifecyclePhase.PROCESS_TEST_CLASSES,
        requiresOnline = false,
        requiresProject = true,
        requiresDependencyResolution = ResolutionScope.TEST)
public class TestClassesNotNullInstrumenter extends com.intellij.AbstractNotNullInstrumenterTask {

    public void execute() throws org.apache.maven.plugin.MojoExecutionException {
        try {
            instrument(project.getBuild().getTestOutputDirectory(), project.getTestClasspathElements());
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
}