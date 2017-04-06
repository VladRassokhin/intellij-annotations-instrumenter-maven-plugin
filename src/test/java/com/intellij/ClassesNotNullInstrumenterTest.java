package com.intellij;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import se.eris.util.ReflectionUtil;

public class ClassesNotNullInstrumenterTest {

    @Test
    public void execute_nullParameters_shouldWork() throws MojoExecutionException {
        final ClassesNotNullInstrumenter instrumenter = createInstrumenter();

        instrumenter.execute();
    }

    @NotNull
    private ClassesNotNullInstrumenter createInstrumenter() {
        final ClassesNotNullInstrumenter instrumenter = new ClassesNotNullInstrumenter();
        final Model model = new Model();
        model.setBuild(new Build());
        model.getBuild().setOutputDirectory("nothing here");
        instrumenter.project = new MavenProject(model);
        ReflectionUtil.setField(instrumenter, "instrument", true);
        return instrumenter;
    }

}