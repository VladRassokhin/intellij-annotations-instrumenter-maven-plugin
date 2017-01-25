package se.eris.test;

import org.jetbrains.annotations.Nullable;
import se.eris.notnull.ImplicitNotNull;

@ImplicitNotNull
public class TestClassImplicit {

    public static String implicitReturn(@Nullable final String s) {
        return s;
    }

}
