package se.eris.util.compiler;

import javax.tools.ToolProvider;

public class JavaSystemCompilerUtil {

    public static boolean supportParametersOption() {
        return ToolProvider.getSystemJavaCompiler().isSupportedOption("-parameters") != -1;
    }

}
