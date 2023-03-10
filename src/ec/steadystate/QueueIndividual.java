/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "license.md" for more information
*/

/*
  A simple class which contains both an Individual and the Queue it's located in.
  Used by SteadyState and by various assistant function in the distributed evaluator
  to provide individuals to SteadyState
*/

package ec.steadystate;

import ec.Individual;

public class QueueIndividual implements java.io.Serializable {
    public final Individual ind;
    public final int subpop;

    public QueueIndividual(Individual i, int s) {
        ind = i;
        subpop = s;
    }
}

