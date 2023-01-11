package solvers.algorithm.featureweighted;

import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.coevolve.GroupedProblemForm;
import ec.coevolve.MultiPopCoevolutionaryEvaluator;
import ec.gp.GPNode;
import ec.util.Parameter;
import solvers.gp.GPRuleEvolutionState;
import simulation.jss.helper.Flag;
import simulation.jss.helper.Weights;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class multiPopCoevolutionaryEvaluator extends MultiPopCoevolutionaryEvaluator {
    public static final String P_PRE_GENERATIONS = "pre-generations";
    public static final String P_NUM_TOP_INDS = "num-topinds";
    public final static String P_GENERATIONS = "generations";
    final ArrayList<double[]> saveWeights = new ArrayList<>();
    final ArrayList<Integer> badInds = new ArrayList<>();
    protected long jobSeed;
    // individuals to evaluate together
    Individual[] inds = null;
    // which individual should have its fitness updated as a result
    boolean[] updates = null;
    private int preGenerations;

    public void evaluatePopulation(final EvolutionState state) {
        // determine who needs to be evaluated
        boolean[] preAssessFitness = new boolean[state.population.subpops.length];
        boolean[] postAssessFitness = new boolean[state.population.subpops.length];
        for (int i = 0; i < state.population.subpops.length; i++) {
            postAssessFitness[i] = shouldEvaluateSubpop(state, i, 0);
            //System.out.println(shouldEvaluateSubpop(state, i, 0));  //true  true
            preAssessFitness[i] = postAssessFitness[i] || (state.generation == 0);  // always prepare (set up trials) on generation 0
        }

        // do evaluation
        beforeCoevolutionaryEvaluation(state, state.population, (GroupedProblemForm) p_problem);

        ((GroupedProblemForm) p_problem).preprocessPopulation(state, state.population, preAssessFitness, false);
        performCoevolutionaryEvaluation(state, state.population, (GroupedProblemForm) p_problem);
        ((GroupedProblemForm) p_problem).postprocessPopulation(state, state.population, postAssessFitness, false); //set the objective and set the flags of evaluated individuals as true

        int topInds = state.parameters.getInt(new Parameter(P_NUM_TOP_INDS), null);
        preGenerations = state.parameters.getIntWithDefault(new Parameter(P_PRE_GENERATIONS), null, 0);
        //fzhang 2019.5.25 remove the bad individuals
        if (state.generation >= preGenerations) {
            //calculateWeights(state);//after this, the populaiton is sorted by increment
            double[][] weights = new double[state.population.subpops.length][];
            Weights.calculateWeights(state, topInds, weights, saveWeights);
            ((GPRuleEvolutionState) state).setWeights(weights);
            removeBadInds(state);
        }

        int generations = state.parameters.getIntWithDefault(new Parameter(P_GENERATIONS), null, -1);

        if (state.generation == generations - 1) {
            jobSeed = ((GPRuleEvolutionState) state).getJobSeed();
            writeBadIndsToFile(state, jobSeed);
            saveWeights(state, jobSeed);
        }

        //change the eliteIndividuals
        afterCoevolutionaryEvaluation(state, state.population, (GroupedProblemForm) p_problem);
    }

    //fzhang 2019.5.22 delect bad individuals in the population and replaced by generating new individuals with weighted features
    public void removeBadInds(final EvolutionState state) {
//		Population newpop = new Population();
        int i = 0;
        inds = new Individual[state.population.subpops.length];
        updates = new boolean[state.population.subpops.length];
        Population newpop = (Population) state.population.emptyClone();
        //Population ReplacedBadIndsPop = (Population) population.emptyClone();
        for (int pop = 0; pop < state.population.subpops.length; pop++) {
            for (int ind = 0; ind < state.population.subpops[pop].individuals.length; ind++) {
                if (state.population.subpops[pop].individuals[ind].fitness.fitness() != Double.MAX_VALUE) {
                    newpop.subpops[pop].individuals[ind] = state.population.subpops[pop].individuals[ind];
                } else {
                    i++;
                    Flag.value = true;
                    //newIndEvaluate = true; //if it is a generated new individual, do not increase badrun number
                    Individual newInd;
                    do {
                        newInd = state.population.subpops[pop].species.newIndividual(state, 0);

                        //read eliteIndividuals for coevolution. Must be here, because the new individual will be used inside.
                        for (int k = 0; k < eliteIndividuals[pop].length; k++) { //2
                            for (int ind1 = 0; ind1 < inds.length; ind1++) { //2
                                if (ind1 == pop) {   //j = 0, 1  (ind j) ---> (0 0) or (1 1) that is to say, this is the subpopulation1
                                    inds[ind1] = newInd; //inds[0] = individual = state.population.subpops[0].individuals[0];
                                    //the individuals to evaluate together
                                    updates[ind1] = true;   // updates[0] = true    updates[1] = true   evaluate
                                } else {  // this is subpopulation2
                                    inds[ind1] = eliteIndividuals[ind1][k];   // (ind j) ---> (0 1) or (1 0)
                                    updates[ind1] = false;  // do not evaluate
                                }
                            }
                        }

                        //evaluate new individuals
                        newInd.fitness.trials = new ArrayList();//this is always make trials.size == 1, actually useless
                        ((GroupedProblemForm) (this.p_problem)).evaluate(state, inds
                                , updates // Should the fitness of individuals be updated? Here it says yes and yes.
                                , false
                                , new int[]{0, 1} // Which subpopulation to use? Here we have two subpops and we want to use them both so it should be 0 and 1
                                , 0);// real evaluation

                    } while (newInd.fitness.fitness() == Double.MAX_VALUE);
                    Flag.value = false;
                    newInd.evaluated = true;
                    newpop.subpops[pop].individuals[ind] = newInd;
                }
            }
            badInds.add(i);
            //System.out.println(i);
            i = 0;
        }
        state.population = newpop; //the new individuals are located in the last position of the population,
        //the whole population is not sorted now(if there is bad individuals that are replaced).
    }

    //before here, the bad individuals are already replaced, the frequency is based on new population.
    //fzhang 2019.5.19 chenck the frequency of terminals in each generation and set them as weighting power
/*   public static void calculateWeights(final EvolutionState state){
           ArrayList<HashMap<String, Integer>> stats = PopulationUtils.Frequency(state.population, topInds); //stats contains two values, one is terminal name
           //and the other is its frequency
           //stats.toString();
           GPNode[][] terminals = ((FreBadGPRuleEvolutionState)state).getTerminals();

           weights = new double[state.population.subpops.length][];
           for (int subpop = 0; subpop < state.population.subpops.length; subpop++) {
               weights[subpop] = new double[terminals[subpop].length];
               for (int i = 0; i < terminals[0].length; i++) {
                   String name = (terminals[0][i]).name();//the terminals in each population is same.  need to modifiy later for different terminal set setting
                   for (int w = subpop; w < topInds * state.population.subpops.length; w += 2) {
                       if (stats.get(w).containsKey(name)) {
                           weights[subpop][i] += stats.get(w).get(name);
                       } else {
                           weights[subpop][i] += 0;
                       }
                   }
               }
               //save the weights values in each generation
               //saveWeights.add(weights[subpop]) this is a java style, the weights will be changed later
               saveWeights.add(weights[subpop].clone()); //need to use clone to copy the array
               RandomChoice.organizeDistribution(weights[subpop]);
           } // for(int subpop = 0; ...
       }*/

      /* public double[][] getWeights(){
        return weights;
       }*/

    public void saveWeights(final EvolutionState state, long jobSeed) {
        //fzhang 2019.5.21 save the weight values
        File weightFile = new File("job." + jobSeed + ".weight.csv"); // jobSeed = 0
        GPNode[][] terminals = ((FreBadGPRuleEvolutionState) state).getTerminals();
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(weightFile));
            writer.write("Gen, sNIQ, sWIQ, sMWT, sPT, sNPT, sOWT, sWKR, sNOR, sW, sTIS, "
                    + "rNIQ, rWIQ, rMWT, rPT, rNPT, rOWT, rWKR, rNOR, rW, rTIS");
            writer.newLine();
            double[] zero = new double[terminals[0].length];
//			zero

            for (int i = 0; i < preGenerations; i++) {
                //writer.newLine();
                writer.write(i + ", " + Arrays.toString(zero).replaceAll("\\[|\\]", ""));
                writer.write(", " + Arrays.toString(zero).replaceAll("\\[|\\]", "") + "\n");
                //Returns a string representation of the contents of the specified array.
            }
            for (int i = 0; i < saveWeights.size(); i += 2) { //every two into one generation
                //writer.newLine();
                writer.write(i / 2 + preGenerations + ", " + Arrays.toString(saveWeights.get(i)).replaceAll("\\[|\\]", ""));
                writer.write(", " + Arrays.toString(saveWeights.get(i + 1)).replaceAll("\\[|\\]", "") + "\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeBadIndsToFile(final EvolutionState state, long jobSeed) {
        //fzhang 2019.5.21 save the weight values
        File badindsFile = new File("job." + jobSeed + ".BadInd.csv"); // jobSeed = 0
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(badindsFile));
            writer.write("Gen, sBadInds, rBadInds, totalBadInds");
            writer.newLine();
            int[] zero = new int[state.population.subpops.length + 1];
//			zero

            for (int i = 0; i < preGenerations; i++) {
                //writer.newLine();
                writer.write(i + "," + Arrays.toString(zero).replaceAll("\\[|\\]", "") + "\n");
                //Returns a string representation of the contents of the specified array.
            }
            for (int i = 0; i < badInds.size(); i += 2) { //every two into one generation
                //writer.newLine();
                writer.write(i / 2 + preGenerations + "," + badInds.get(i) + "," + badInds.get(i + 1) + "," + (badInds.get(i) + badInds.get(i + 1)) + "\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}