/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "license.md" for more information
*/


package ec.app.lid.func;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;

/*
 * LidJ.java
 *
 * Created: Thursday 23 November 2011
 * By: James McDermott
 */

/**
 * @author James McDermott
 */

public class LidJ extends GPNode {
    public String toString() {
        return "J";
    }

    public int expectedChildren() {
        return 2;
    }

    public void eval(final EvolutionState state,
                     final int thread,
                     final GPData input,
                     final ADFStack stack,
                     final GPIndividual individual,
                     final Problem problem) {
        // No need to evaluate or look at children. Lid is only
        // about tree shape/size
    }
}
