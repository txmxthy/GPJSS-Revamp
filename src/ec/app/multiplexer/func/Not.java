/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "license.md" for more information
*/


package ec.app.multiplexer.func;

import ec.EvolutionState;
import ec.Problem;
import ec.app.multiplexer.MultiplexerData;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;

/*
 * Not.java
 *
 * Created: Wed Nov  3 18:26:37 1999
 * By: Sean Luke
 */

/**
 * @author Sean Luke
 * @version 1.0
 */

public class Not extends GPNode {
    public String toString() {
        return "not";
    }

    /*
      public void checkConstraints(final EvolutionState state,
      final int tree,
      final GPIndividual typicalIndividual,
      final Parameter individualBase)
      {
      super.checkConstraints(state,tree,typicalIndividual,individualBase);
      if (children.length!=1)
      state.output.error("Incorrect number of children for node " +
      toStringForError() + " at " +
      individualBase);
      }
    */
    public int expectedChildren() {
        return 1;
    }

    public void eval(final EvolutionState state,
                     final int thread,
                     final GPData input,
                     final ADFStack stack,
                     final GPIndividual individual,
                     final Problem problem) {
        MultiplexerData md = (MultiplexerData) input;
        children[0].eval(state, thread, input, stack, individual, problem);

        if (md.status == MultiplexerData.STATUS_3)
            md.dat_3 ^= -1;
        else if (md.status == MultiplexerData.STATUS_6)
            md.dat_6 ^= -1L;
        else // md.status == MultiplexerData.STATUS_11
            for (int x = 0; x < MultiplexerData.MULTI_11_NUM_BITSTRINGS; x++)
                md.dat_11[x] ^= -1L;
    }
}



