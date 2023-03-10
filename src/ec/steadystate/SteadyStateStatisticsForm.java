/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "license.md" for more information
*/


package ec.steadystate;

import ec.EvolutionState;
import ec.Individual;

/*
 * SteadyStateStatisticsForm.java
 *
 * Created: Fri Nov  9 20:45:26 EST 2001
 * By: Sean Luke
 */

/**
 * This interface defines the hooks for SteadyStateEvolutionState objects
 * to update themselves on.  Note that the the only methods in common
 * with the standard statistics are initialization and final. This is an
 * optional interface: SteadyStateEvolutionState will complain, but
 * will permit Statistics objects that don't adhere to it, though they will
 * only have their initialization and final statistics methods called!
 *
 * <p>See SteadyStateEvolutionState for how regular Statistics objects'
 * hook methods are called in steady state evolution.
 *
 * @author Sean Luke
 * @version 1.0
 */

public interface SteadyStateStatisticsForm {
    /**
     * Called when we created an empty initial Population.
     */
    void enteringInitialPopulationStatistics(SteadyStateEvolutionState state);

    /**
     * Called when we have filled the initial population and are entering the steady state.
     */
    void enteringSteadyStateStatistics(int subpop, SteadyStateEvolutionState state);

    /**
     * Called each time new individuals are bred during the steady-state
     * process.
     */
    void individualsBredStatistics(SteadyStateEvolutionState state, Individual[] individuals);

    /**
     * Called each time new individuals are evaluated during the steady-state
     * process, NOT including the initial generation's individuals.
     */
    void individualsEvaluatedStatistics(SteadyStateEvolutionState state, Individual[] newIndividuals,
                                        Individual[] oldIndividuals, int[] subpopulations, int[] indices);

    /**
     * Called when the generation count increments
     */
    void generationBoundaryStatistics(final EvolutionState state);

    /**
     * Called immediately before checkpointing occurs.
     */
    void preCheckpointStatistics(final EvolutionState state);

    /**
     * Called immediately after checkpointing occurs.
     */
    void postCheckpointStatistics(final EvolutionState state);

    /**
     * Called immediately before the pre-breeding exchange occurs.
     */
    void prePreBreedingExchangeStatistics(final EvolutionState state);

    /**
     * Called immediately after the pre-breeding exchange occurs.
     */
    void postPreBreedingExchangeStatistics(final EvolutionState state);

    /**
     * Called immediately before the post-breeding exchange occurs.
     */
    void prePostBreedingExchangeStatistics(final EvolutionState state);

    /**
     * Called immediately after the post-breeding exchange occurs.
     */
    void postPostBreedingExchangeStatistics(final EvolutionState state);

    /**
     * Called immediately after the run has completed.  <i>result</i>
     * is either <tt>state.R_FAILURE</tt>, indicating that an ideal individual
     * was not found, or <tt>state.R_SUCCESS</tt>, indicating that an ideal
     * individual <i>was</i> found.
     */
    void finalStatistics(final EvolutionState state, final int result);
}
