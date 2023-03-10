/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "license.md" for more information
*/


package ec.gp;

/*
 * GPAtomicType.java
 *
 * Created: Fri Aug 27 21:16:45 1999
 * By: Sean Luke
 */

/**
 * A GPAtomicType is a simple, atomic GPType.  For more information, see GPType.
 *
 * @author Sean Luke
 * @version 1.0
 * @see ec.gp.GPType
 */

public final class GPAtomicType extends GPType {
    /**
     * Use this constructor for GPAtomic Type unless you know what you're doing
     */
    public GPAtomicType(final String n) {
        name = n;
    }

    /**
     * Don't use this constructor unless you call setup(...) immediately after it.
     */
    public GPAtomicType() {
    }

    public boolean compatibleWith(final GPInitializer initializer, final GPType t) {
        // if the type is me, then I'm compatible with it
        if (t.type == type) return true;

            // if the type an atomic type, then return false
        else if (t.type < initializer.numAtomicTypes) return false;

            // if the type is < 0 (it's a set type), then I'm compatible
            // if I'm contained in it.  Use its sparse array.
        else return ((GPSetType) t).types_sparse[type];
    }
}
