/*
  Copyright 2014 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "license.md" for more information
*/


package ec.breed;

import ec.BreedingPipeline;
import ec.EvolutionState;
import ec.Individual;
import ec.SelectionMethod;
import ec.util.Parameter;

/*
 * CheckingPipeline.java
 *
 * Created: Tue Feb 25 08:33:02 EST 2014
 * By: Sean Luke
 */

/**
 * CheckingPipeline is a BreedingPipeline which just passes through the
 * individuals it receives from its source 0, but only if those individuals
 * ALL pass a validation check (the method allValid(), which you must override).
 * It tries to find valid individuals some num-times times, and if it cannot, it
 * instead reproduces individuals from its source 1 and returns them instead.
 *
 * <p>In some cases you may wish instead to produce individuals which
 * are individually checked for validity, rather than together.  The easiest way
 * to do this is to add the CheckingPipeline as a child to a ForceBreedingPipeline
 * which has been set with num-inds=1.
 *
 * <p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
 * ...as many as either child produces
 *
 * <p><b>Number of Sources</b><br>
 * 2
 *
 * <p><b>Parameters</b><br>
 * <table>
 * <tr><td valign=top><i>base.</i><tt>num-times</tt><br>
 * <font size=-1>int >= 1</font></td>
 * <td valign=top>(number of times we try to get children from source 0 before giving up and using source 1)</td></tr>
 *
 * </table>
 * <p><b>Default Base</b><br>
 * breed.check
 *
 * @author Sean Luke
 * @version 1.0
 */

public class CheckingPipeline extends BreedingPipeline {
    public static final String P_CHECK = "check";
    public static final String P_NUMTIMES = "num-times";
    public static final int NUM_SOURCES = 2;
    int numTimes = 0;

    public Parameter defaultBase() {
        return BreedDefaults.base().push(P_CHECK);
    }

    public int numSources() {
        return NUM_SOURCES;
    }

    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state, base);
        Parameter def = defaultBase();
        numTimes = state.parameters.getInt(base.push(P_NUMTIMES), def.push(P_NUMTIMES), 1);
        if (numTimes < 1)
            state.output.fatal("CheckingPipeline must have a num-times value >= 1.",
                    base.push(P_NUMTIMES),
                    def.push(P_NUMTIMES));
        if (likelihood != 1.0)
            state.output.warning("CheckingPipeline given a likelihood other than 1.0.  This is nonsensical and will be ignored.",
                    base.push(P_LIKELIHOOD),
                    def.push(P_LIKELIHOOD));
    }

    public boolean allValid(Individual[] inds, int numInds, int subpopulation, EvolutionState state, int thread) {
        return true;
    }

    public int produce(
            final int min,
            final int max,
            final int start,
            final int subpopulation,
            final Individual[] inds,
            final EvolutionState state,
            final int thread) {
        Individual[] inds2 = new Individual[max];

        for (int i = 0; i < numTimes; i++) {
            // grab individuals from our source and stick 'em into inds2 at position 0
            int n = sources[0].produce(min, max, 0, subpopulation, inds2, state, thread);

            // check for validity
            if (!allValid(inds2, n, subpopulation, state, thread))
                continue;  // failure, try again

            // success!  Copy to inds and possibly clone
            System.arraycopy(inds2, 0, inds, start, n);
            if (sources[0] instanceof SelectionMethod)
                for (int q = start; q < n + start; q++)
                    inds[q] = (Individual) (inds[q].clone());
            return n;
        }

        // big-time failure!  Grab from the other source
        int n = sources[1].produce(min, max, start, subpopulation, inds, state, thread);
        if (sources[0] instanceof SelectionMethod)
            for (int q = start; q < n + start; q++)
                inds[q] = (Individual) (inds[q].clone());
        return n;
    }

    //fzhang 2019.6.15
    @Override
    public int produceFrac(int min, int max, int start, int subpopulation, Individual[] inds, EvolutionState state, int thread) {
        return 0;
    }
}










