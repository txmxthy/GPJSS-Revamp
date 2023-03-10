/*
  Copyright 2013 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "license.md" for more information
*/


package ec.breed;

import ec.BreedingPipeline;
import ec.EvolutionState;
import ec.Individual;
import ec.Species;
import ec.util.Parameter;

/*
 * InitializationPipeline.java
 *
 * Created: Thu Dec 12 11:49:22 EST 2013
 * By: Sean Luke
 */

/**
 * InitializationPipeline is a BreedingPipeline which simply generates a new
 * random inidividual.  It has no sources at all.
 *
 * <p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
 * ...the maximum requested by the parent.
 *
 * <p><b>Number of Sources</b><br>
 * 0
 *
 * </table>
 * <p><b>Default Base</b><br>
 * breed.init
 *
 * @author Sean Luke
 * @version 1.0
 */

public class InitializationPipeline extends BreedingPipeline {
    public static final String P_INIT = "init";
    public static final int NUM_SOURCES = 0;

    public Parameter defaultBase() {
        return BreedDefaults.base().push(P_INIT);
    }

    public int numSources() {
        return NUM_SOURCES;
    }

    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state, base);
        Parameter def = defaultBase();

        if (likelihood != 1.0)
            state.output.warning("InitializationPipeline given a likelihood other than 1.0.  This is nonsensical and will be ignored.",
                    base.push(P_LIKELIHOOD),
                    def.push(P_LIKELIHOOD));
    }

    public int produce(final int min,
                       final int max,
                       final int start,
                       final int subpopulation,
                       final Individual[] inds,
                       final EvolutionState state,
                       final int thread) {
        Species s = state.population.subpops[subpopulation].species;
        for (int q = start; q < start + max; q++) {
            inds[q] = s.newIndividual(state, thread);
        }
        return max;
    }

    //fzhang 2019.6.15
    @Override
    public int produceFrac(int min, int max, int start, int subpopulation, Individual[] inds, EvolutionState state, int thread) {
        return 0;
    }
}
