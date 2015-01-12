package se.eris.test;

import java.util.Arrays;
import org.jetbrains.annotations.NotNull;

public class TestNotNull {

    public static void main(final String[] args) {
        System.out.println("args = " + Arrays.toString(args));
        notNullParameter("");
    }

    public static void notNullParameter(@NotNull final String s) {
    }

}
