/*
  Copyright 2010 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "license.md" for more information
*/

package solvers.algorithm.multiobjective;

import ec.EvolutionState;
import ec.Individual;
import ec.multiobjective.nsga2.NSGA2Evaluator;

import java.util.ArrayList;

/*
 * NSGA2Evaluator.java
 *
 * Created: Sat Oct 16 00:19:57 EDT 2010
 * By: Faisal Abidi and Sean Luke
 */


/**
 * The NSGA2Evaluator for gp. Use it to calculate the rank and distance only.
 */

public class NSGA2Evaluatorgp extends NSGA2Evaluator {

    /**
     *
     */

    public void evaluatePopulation(final EvolutionState state) {
        super.evaluatePopulationgp(state);
        for (int x = 0; x < state.population.subpops.length; x++)
            //state.population.subpops[x].individuals =
            extrafitness(state, x);//�������Ӵ�ͬ����
    }


    /**
     * Build the auxiliary fitness data
     */
    public void extrafitness(EvolutionState state, int subpop) {
        Individual[] dummy = new Individual[0];//α��Ⱥ
        ArrayList ranks = assignFrontRanks(state.population.subpops[subpop]);//����ranks

        int size = ranks.size();
        for (Object o : ranks) {
            Individual[] rank = (Individual[]) ((ArrayList) o).toArray(dummy);//ȡ��i��rank
            assignSparsity(rank);//���ŷ�ɢ��  [0,2]
        }
    }
}