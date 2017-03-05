package se.eris.implicit;

import org.jetbrains.annotations.Nullable;
import se.eris.notnull.ImplicitNotNull;

@ImplicitNotNull
public abstract class Abstract {

    public Abstract(@Nullable String s) {}

    public Abstract(Integer i) {}

}
