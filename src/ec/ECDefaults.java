/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "license.md" for more information
*/


package ec;

import ec.util.Parameter;

/*
 * ECDefaults.java
 *
 * Created: Thu Jan 20 16:49:57 2000
 * By: Sean Luke
 */

/**
 * @author Sean Luke
 * @version 1.0
 */

public final class ECDefaults implements DefaultsForm {
    public static final String P_EC = "ec";

    /**
     * Returns the default base.
     */
    public static Parameter base() {
        return new Parameter(P_EC);
    }
}
