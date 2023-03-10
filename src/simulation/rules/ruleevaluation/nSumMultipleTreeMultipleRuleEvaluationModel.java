package simulation.rules.ruleevaluation;

import ec.EvolutionState;
import ec.Fitness;
import ec.multiobjective.MultiObjectiveFitness;
import ec.util.Parameter;
import simulation.definition.Objective;
import simulation.definition.WorkCenter;
import simulation.definition.logic.Simulation;
import simulation.rules.rule.AbstractRule;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class nSumMultipleTreeMultipleRuleEvaluationModel extends MultipleRuleEvaluationModel {

    public final static String P_OBJNAMES = "objNames";
    public final static String P_WEIGHTS = "weights";
    //fzhang 2019.1.11 save the training fitnesses
    final List<Double> genTrainFitnesses = new ArrayList<>();
    final List<Double> GenObjvalue = new ArrayList<>();
    final ArrayList<Double> trainfitnesses = new ArrayList<Double>() {{
        add(Double.MAX_VALUE);
        add(Double.MAX_VALUE);
    }};
    protected List<String> objNames;

    //fzhang 2018.12.7 fix the problem 0.29+0.57=0.899999999
//	  protected int[] weights;
    protected List<Double> weights;
    //fzhang 2019.1.11 to save the two fitness in training process
    double tempfitness = Double.MAX_VALUE;

    @Override
    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state, base);

        //this part adds more parameters need to use for weighted sum method
        //fzhang 2018.12.3 read objectives (for weighted sum) from parameter file
        // Get the name of objectives.
        //if we define the data type as objective, there is some error. May be "objective" is a special type.
        //so, here, we define it as string, as convert it to objective when use it.
        objNames = new ArrayList<>();
        weights = new ArrayList<>();

        Parameter p = base.push(P_OBJNAMES);
        int numObjNames = state.parameters.getIntWithDefault(p, null, 0);

        //fzhang 2018.12.7 fix the problem 0.29+0.57=0.899999999
//	        weights = new int[numObjNames];

        if (numObjNames == 0) {
            System.err.println("ERROR:");
            System.err.println("No objName is specified.");
            System.exit(1);
        }

        for (int i = 0; i < numObjNames; i++) {
            p = base.push(P_OBJNAMES).push("" + i);
            String objName = state.parameters.getStringWithDefault(p, null, "");
            objNames.add(objName);

            Parameter b = base.push(P_OBJNAMES).push("" + i);
            p = b.push(P_WEIGHTS);
            double weight = state.parameters.getDoubleWithDefault(p, null, 0.0);
            weights.add(weight);
//	            weights[i] = (int)(weight*100+0.5);
        }

        //fzhang 2018.12.7 fix the problem 0.29+0.57=0.899999999  no need to check, it does not matter
//	        if (sum != 100) {
//	            System.err.println("ERROR:");
//		        System.err.println("The sum of weights should be equal 1.");
//		        System.exit(1);
//	        }
    }

    //=======================================normObjective for weighted sum method======================================
    public void normObjective(List<Fitness> currentFitnesses,
                              List<AbstractRule> rules,
                              EvolutionState state) {
        // expecting 2 rules here - one routing rule and one sequencing rule
        if (rules.size() != 2) {
            System.out.println("Rule evaluation failed!");
            System.out.println("Expecting 2 rules, only 1 found.");
            return;
        }

        countInd++;

        AbstractRule sequencingRule = rules.get(0); // for each arraylist in list, they have two elements, the first one												// is sequencing rule and the second one is routing rule
        AbstractRule routingRule = rules.get(1);
        double[] fitnesses = new double[objectives.size()];

        List<Simulation> simulations = schedulingSet.getSimulations();
        int col = 0;
        ArrayList<Double> ObjValue = new ArrayList<>();

        for (Simulation simulation : simulations) {
            // ========================change here======================================
            simulation.setSequencingRule(sequencingRule); // indicate different individuals
            simulation.setRoutingRule(routingRule);

            simulation.run();
            for (int i = 0; i < objectives.size(); i++) {
                for (String objName : objNames) {
                    Objective newObj0 = Objective.get(objName);
                    objectives.set(0, newObj0);

                    ObjValue.add(simulation.objectiveValue(objectives.get(0))); // this line: the value of makespan
                    // have two values
                }

                // in essence, here is useless. because if w.numOpsInQueue() > 100, the
                // simulation has been canceled in run(). here is a double check
                for (WorkCenter w : simulation.getSystemState().getWorkCenters()) {
                    if (w.numOpsInQueue() > 100) {
                        // this was a bad run
                        Collections.fill(ObjValue, Double.MAX_VALUE);
                        // countBadrun++;
                        break;
                    }
                }

                //ObjValue should be normalized, like [0,1], but we do not know the bounds,
                //so keep the best on previous generation
                for (int m = 0; m < ObjValue.size(); m++) {
                    fitnesses[i] += weights.get(m) * ObjValue.get(m);
                    //fzhang 2018.12.7 fix the problem 0.29+0.57=0.899999999
//                	fitnesses[i] +=  weights[m]/100.0* ObjValue.get(m); 
                }

                //fzhang 2019.1.11 save the two fitnesses of the best individuals
                if (fitnesses[i] < tempfitness) {
                    tempfitness = fitnesses[i];
                    for (int m = 0; m < ObjValue.size(); m++) {
                        //trainfitnesses.add(ObjValue.get(m));
                        trainfitnesses.set(m, ObjValue.get(m));
                    }
                }
            }
            col++;
            simulation.reset();

            GenObjvalue.addAll(ObjValue);

            if (countInd % state.population.subpops[0].individuals.length == 0) {
                double fit1Max = 0.0;
                double fit1Min = Double.MAX_VALUE;
                double fit2Max = 0.0;
                double fit2Min = Double.MAX_VALUE;

                for (int fit = 0; fit < GenObjvalue.size(); fit = fit + 2) {
                    if (GenObjvalue.get(fit) > fit1Max && GenObjvalue.get(fit) != Double.MAX_VALUE)
                        fit1Max = GenObjvalue.get(fit);
                    if (GenObjvalue.get(fit) < fit1Min)
                        fit1Min = GenObjvalue.get(fit);
                }

				/*System.out.println("fit1Max "+ fit1Max);
				System.out.println("fit1Min "+ fit1Min);*/

                for (int fit = 1; fit < GenObjvalue.size(); fit = fit + 2) {
                    if (GenObjvalue.get(fit) > fit2Max && GenObjvalue.get(fit) != Double.MAX_VALUE)
                        fit2Max = GenObjvalue.get(fit);
                    if (GenObjvalue.get(fit) < fit2Min)
                        fit2Min = GenObjvalue.get(fit);
                }

                for (int fit = 0; fit < GenObjvalue.size(); fit = fit + 2) {
                    if (GenObjvalue.get(fit) != Double.MAX_VALUE) //normalisation applied on non-Double.max values
                        GenObjvalue.set(fit, (GenObjvalue.get(fit) - fit1Min) / (fit1Max - fit1Min));
                }

                for (int fit = 1; fit < GenObjvalue.size(); fit = fit + 2) {
                    if (GenObjvalue.get(fit) != Double.MAX_VALUE)
                        GenObjvalue.set(fit, (GenObjvalue.get(fit) - fit2Min) / (fit2Max - fit2Min));
                }

				/*for(int fit = 0; fit < GenObjvalue.size(); fit++) {
					System.out.println(GenObjvalue.get(fit));
				}*/
                /*
                 * for(int ind = 0; ind < state.population.subpops[0].individuals.length; ind++)
                 * { for(int m = 0; m < ObjValue.size(); m++) {
                 * state.population.subpops[0].individuals[ind]. =
                 * weights.get(m)*ObjValue.get(m); } }
                 */
            }

            if (countInd == state.population.subpops[0].individuals.length * state.numGenerations)
                WriteTrainingFitnesses(state, null);
            // ObjValue should be normalized, like [0,1], but we do not know the bounds,
            // so keep the best on previous generation
            /*
             * for(int m = 0; m < ObjValue.size(); m++) { fitnesses[i] += weights.get(m)*
             * ObjValue.get(m); //fzhang 2018.12.7 fix the problem 0.29+0.57=0.899999999 //
             * fitnesses[i] += weights[m]/100.0* ObjValue.get(m); }
             */
        }
    }

    //========================rule evaluatation============================
    @Override
    public void evaluate(List<Fitness> currentFitnesses, List<AbstractRule> rules, EvolutionState state) {
        countInd++;
        // code taken from Abstract Rule
        double[] fitnesses = new double[objectives.size()];
        int col = 0;
        ArrayList<Double> ObjValue = new ArrayList<>();

        for (int i = 0; i < objectives.size(); i++) {
            GenObjvalue.remove(0);
            GenObjvalue.remove(1);
            col++;
        }

        for (int i = 0; i < fitnesses.length; i++) {
            fitnesses[i] /= col;
        }

        for (Fitness fitness : currentFitnesses) {
            MultiObjectiveFitness f = (MultiObjectiveFitness) fitness;
            f.setObjectives(state, fitnesses);
        }
    }


    //modified by fzhang 2019.1.11   write train fitness to *.csv
    public void WriteTrainingFitnesses(EvolutionState state, final Parameter base) {
        Parameter p;
        // Get the job seed.
        p = new Parameter("seed").push("" + 0);
        jobSeed = state.parameters.getLongWithDefault(p, null, 0);
        File trainingFitnessFile = new File("job." + jobSeed + ".TrainingFitness.csv");

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(trainingFitnessFile));
            writer.write("generation,trainfitness1,trainfitness2");
            writer.newLine();
            for (int cutPoint = 0; cutPoint < genTrainFitnesses.size() / 2; cutPoint++) {
                writer.write(cutPoint + "," + genTrainFitnesses.get(2 * cutPoint) + "," + genTrainFitnesses.get(2 * cutPoint + 1));
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
