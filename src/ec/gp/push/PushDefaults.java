
package ec.gp.push;
import ec.util.Parameter;
import ec.*;

public final class PushDefaults implements DefaultsForm
    {
    public static final String P_PUSH = "push";

    /** Returns the default base. */
    public static Parameter base()
        {
        return new Parameter(P_PUSH);
        }
    }
