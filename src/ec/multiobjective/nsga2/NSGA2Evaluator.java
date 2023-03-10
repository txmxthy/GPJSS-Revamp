/*
  Copyright 2010 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "license.md" for more information
*/

package ec.multiobjective.nsga2;

import ec.*;
import ec.multiobjective.MultiObjectiveFitness;
import ec.simple.SimpleEvaluator;
import ec.util.Parameter;
import ec.util.SortComparator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/*
 * NSGA2Evaluator.java
 *
 * Created: Sat Oct 16 00:19:57 EDT 2010
 * By: Faisal Abidi and Sean Luke
 */

/**
 * The NSGA2Evaluator is a simple generational evaluator which evaluates every
 * single member of the population (in a multithreaded fashion). Then it reduces
 * the population size to an <i>archive</i> consisting of the best front ranks.
 * When there isn't enough space to fit another front rank, individuals in that
 * final front rank vie for the remaining slots in the archive based on their
 * sparsity.
 *
 * <p>
 * The evaluator is also responsible for calculating the rank and sparsity
 * values stored in the NSGA2MultiObjectiveFitness class and used largely for
 * statistical information.
 *
 * <p>
 * NSGA-II has fixed archive size (the population size), and so ignores the
 * 'elites' declaration. However it will adhere to the 'reevaluate-elites'
 * parameter in SimpleBreeder to determine whether to force fitness
 * reevaluation.
 */

public class NSGA2Evaluator extends SimpleEvaluator {
    /**
     * The original population size is stored here so NSGA2 knows how large to
     * create the archive (it's the size of the original population -- keep in mind
     * that NSGA2Breeder had made the population larger to include the children.
     */
    public int[] originalPopSize;

    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state, base);

        Parameter p = new Parameter(Initializer.P_POP);
        int subpopsLength = state.parameters.getInt(p.push(Population.P_SIZE), null, 1);
        Parameter p_subpop;
        originalPopSize = new int[subpopsLength];
        for (int i = 0; i < subpopsLength; i++) {
            p_subpop = p.push(Population.P_SUBPOP).push("" + i).push(Subpopulation.P_SUBPOPSIZE);
            originalPopSize[i] = state.parameters.getInt(p_subpop, null, 1);
        }
    }

    /**
     * Evaluates the population, then builds the archive and reduces the population
     * to just the archive.
     */
    public void evaluatePopulation(final EvolutionState state) {
        super.evaluatePopulation(state);  //the same with the simpleEvalutor, during the first generation, evaluate N individuals; after that, evaluate 2N individuals
        for (int x = 0; x < state.population.subpops.length; x++)
            state.population.subpops[x].individuals = buildArchive(state, x); // trade the individuals in archive as the population
    }

    /**
     * Build the auxiliary fitness data and reduce the subpopulation to just the
     * archive, which is returned.
     */
    //achieve a archive has the same size of original population
    public Individual[] buildArchive(EvolutionState state, int subpop) {
        Individual[] dummy = new Individual[0]; //allocates an array which has 0 elements.
        ArrayList ranks = assignFrontRanks(state.population.subpops[subpop]); //after this, get different several ranks
        //each rank cosists of the corresponding individuals

        ArrayList newSubpopulation = new ArrayList(); //a new one, size = 0
        int size = ranks.size();
        for (int i = 0; i < size; i++) { //do for each rank separately
            Individual[] rank = (Individual[]) ((ArrayList) (ranks.get(i))).toArray(dummy);
            assignSparsity(rank); //assign sparity value for each individual in this rank
            if (rank.length + newSubpopulation.size() >= originalPopSize[subpop]) {
                // first sort the rank by sparsity---from the small one to large one
                ec.util.QuickSort.qsort(rank, new SortComparator() {
                    public boolean lt(Object a, Object b) { //Returns true if a < b, else false
                        Individual i1 = (Individual) a;
                        Individual i2 = (Individual) b;
                        return (((NSGA2MultiObjectiveFitness) i1.fitness).sparsity > ((NSGA2MultiObjectiveFitness) i2.fitness).sparsity);
                    }

                    public boolean gt(Object a, Object b) { //Returns true if a > b, else false
                        Individual i1 = (Individual) a;
                        Individual i2 = (Individual) b;
                        return (((NSGA2MultiObjectiveFitness) i1.fitness).sparsity < ((NSGA2MultiObjectiveFitness) i2.fitness).sparsity);
                    }
                });

                // then put the m sparsest individuals in the new population
                int m = originalPopSize[subpop] - newSubpopulation.size(); //how many positions left for new individuals
                newSubpopulation.addAll(Arrays.asList(rank).subList(0, m));

                // and bail
                break;
            } else {
                // dump in everyone
                Collections.addAll(newSubpopulation, rank);
            }
        }

        Individual[] archive = (Individual[]) (newSubpopulation.toArray(dummy));

        // maybe force reevaluation
        NSGA2Breeder breeder = (NSGA2Breeder) (state.breeder);
        if (breeder.reevaluateElites[subpop])
            for (int i = 0; i < archive.length; i++)
                archive[i].evaluated = false;

        return archive;
    }

    /**
     * Divides inds into ranks and assigns each individual's rank to be the rank it
     * was placed into. Each front is an ArrayList.
     */
    public ArrayList assignFrontRanks(Subpopulation subpop) {
        Individual[] inds = subpop.individuals; //inds includes all individuals
        ArrayList frontsByRank = MultiObjectiveFitness.partitionIntoRanks(inds);

        int numRanks = frontsByRank.size();
        for (int rank = 0; rank < numRanks; rank++) {
            ArrayList front = (ArrayList) (frontsByRank.get(rank));
            int numInds = front.size();
            for (int ind = 0; ind < numInds; ind++)
                ((NSGA2MultiObjectiveFitness) (((Individual) (front.get(ind))).fitness)).rank = rank;
        }
        return frontsByRank;
    }

    /**
     * Computes and assigns the sparsity values of a given front.
     */
    public void assignSparsity(Individual[] front) {
        int numObjectives = ((NSGA2MultiObjectiveFitness) front[0].fitness).getObjectives().length;

        //front here means different fronts
        for (int i = 0; i < front.length; i++) //front.length means how many individuals in this ranking(front)
            ((NSGA2MultiObjectiveFitness) front[i].fitness).sparsity = 0; //the first individual in this front, the sparsity
        //is assigned to 0

        for (int i = 0; i < numObjectives; i++) {
            final int o = i;
            // 1. Sort front by each objective.
            // 2. Sum the manhattan distance of an individual's neighbours over
            // each objective.
            // NOTE: No matter which objectives objective you sort by, the
            // first and last individuals will always be the same (they maybe
            // interchanged though). This is because a Pareto front's
            // objective values are strictly increasing/decreasing.
            ec.util.QuickSort.qsort(front, new SortComparator() {
                public boolean lt(Object a, Object b) { //less than
                    Individual i1 = (Individual) a;
                    Individual i2 = (Individual) b;
                    return (((NSGA2MultiObjectiveFitness) i1.fitness)
                            .getObjective(o) < ((NSGA2MultiObjectiveFitness) i2.fitness).getObjective(o));
                }

                public boolean gt(Object a, Object b) { //great than
                    Individual i1 = (Individual) a;
                    Individual i2 = (Individual) b;
                    return (((NSGA2MultiObjectiveFitness) i1.fitness)
                            .getObjective(o) > ((NSGA2MultiObjectiveFitness) i2.fitness).getObjective(o));
                }
            });

            // Compute and assign sparsity.
            // the first and last individuals are the sparsest.
            ((NSGA2MultiObjectiveFitness) front[0].fitness).sparsity = Double.POSITIVE_INFINITY;
            ((NSGA2MultiObjectiveFitness) front[front.length - 1].fitness).sparsity = Double.POSITIVE_INFINITY;
            for (int j = 1; j < front.length - 1; j++) {
                NSGA2MultiObjectiveFitness f_j = (NSGA2MultiObjectiveFitness) (front[j].fitness);
                NSGA2MultiObjectiveFitness f_jplus1 = (NSGA2MultiObjectiveFitness) (front[j + 1].fitness);
                NSGA2MultiObjectiveFitness f_jminus1 = (NSGA2MultiObjectiveFitness) (front[j - 1].fitness);

//				System.out.println(f_j.maxObjective[o] - f_j.minObjective[o]);  //1
                // store the NSGA2Sparsity in sparsity
                f_j.sparsity += (f_jplus1.getObjective(o) - f_jminus1.getObjective(o))
                        / (f_j.maxObjective[o] - f_j.minObjective[o]);
            }
        }
    }

    //fzhang 2018.11.6 NSGA-II
    public void evaluatePopulationgp(final EvolutionState state) {
        super.evaluatePopulation(state);// evaluate population for GP with NSGA2
    }
}