package simulation.rules.ruleoptimisation;

import ec.EvolutionState;
import ec.Fitness;
import ec.Individual;
import ec.Population;
import ec.coevolve.GroupedProblemForm;
import ec.gp.GPIndividual;
import ec.multiobjective.MultiObjectiveFitness;
import ec.util.Parameter;
import simulation.definition.Job;
import simulation.definition.Objective;
import simulation.rules.rule.AbstractRule;
import simulation.rules.rule.RuleType;
import simulation.rules.rule.operation.evolved.GPRule;
import simulation.rules.ruleevaluation.AbstractEvaluationModel;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dyska on 28/06/17.
 */
public class RuleCoevolutionProblem extends RuleOptimizationProblem implements GroupedProblemForm {

    public static final String P_EVAL_MODEL = "eval-model";
    public static final String P_SHOULD_SET_CONTEXT = "set-context";
    boolean shouldSetContext;
    public double best_schedule_makespan = Double.MAX_VALUE;
    public List<Job> best_schedule = new ArrayList<>();
    private AbstractEvaluationModel evaluationModel;

    public AbstractEvaluationModel getEvaluationModel() {
        return evaluationModel;
    }

    public List<Objective> getObjectives() {
        return evaluationModel.getObjectives();
    }

    public void rotateEvaluationModel() {
        evaluationModel.rotate();
    }

    //=====================================================================
    @Override
    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state, base);

        Parameter p = base.push(P_EVAL_MODEL);
        evaluationModel = (AbstractEvaluationModel)
                state.parameters.getInstanceForParameter(p, null,
                        AbstractEvaluationModel.class);

        evaluationModel.setup(state, p);
        shouldSetContext = state.parameters.getBoolean(base.push(P_SHOULD_SET_CONTEXT),
                null, true);

    }

    @Override
    public void preprocessPopulation(EvolutionState state,
                                     Population pop,
                                     boolean[] prepareForFitnessAssessment,
                                     boolean countVictoriesOnly) {
        for (int i = 0; i < pop.subpops.length; i++) {
            if (prepareForFitnessAssessment[i]) {
                for (int j = 0; j < pop.subpops[i].individuals.length; j++) {
                    //System.out.println("i = " + i + ", j = " + j);
                    pop.subpops[i].individuals[j].fitness.trials = new ArrayList();
                }
            }
        }
    }

    @Override
    public void postprocessPopulation(EvolutionState state,
                                      Population pop,
                                      boolean[] assessFitness,
                                      boolean countVictoriesOnly) {
        for (int i = 0; i < pop.subpops.length; i++) {
            if (assessFitness[i]) {
                for (int j = 0; j < pop.subpops[i].individuals.length; j++) {
                    MultiObjectiveFitness fit = ((MultiObjectiveFitness) (pop.subpops[i].individuals[j].fitness));

                    double[] objectiveFitness = new double[1];
                    //can use fitness from first trial, as this will always be least
                    objectiveFitness[0] = (Double) (fit.trials.get(0));
                    fit.setObjectives(state, objectiveFitness);
                    pop.subpops[i].individuals[j].evaluated = true;
                }
            }
        }
    }

    //=============================!!!!!! problem.evaluate()======================================
    @Override
    public void evaluate(EvolutionState state,
                         Individual[] ind,
                         boolean[] updateFitness,
                         boolean countVictoriesOnly,
                         int[] subpops,
                         int threadnum) {
        if (ind.length == 0) {
            state.output.fatal("Number of individuals provided to RuleCoevolutionProblem is 0!");
        }
        if (ind.length == 1) {
            state.output.warnOnce("Coevolution used," +
                    " but number of individuals provided to RuleCoevolutionProblem is 1.");
        }

        boolean writeSchedule = false;
        List<AbstractRule> rules = new ArrayList<>();
        List<Fitness> fitnesses = new ArrayList<>();

        //step 1: setup sequencing rule for the first GPtree
        rules.add(new GPRule(RuleType.SEQUENCING, ((GPIndividual) ind[0]).trees[0])); //the first one always the best one
        fitnesses.add(ind[0].fitness);

        //setup routing rule for the second GPTree
        rules.add(new GPRule(RuleType.ROUTING, ((GPIndividual) ind[1]).trees[0])); //the first one always the best one
        fitnesses.add(ind[1].fitness);

        evaluationModel.evaluate(fitnesses, rules, state);  // yimei.jss.ruleevaluation.MultipleRuleEvaluationModel
        double found = evaluationModel.getBest_schedule_makespan();
        if (found < best_schedule_makespan) {
//            @TODO
            best_schedule_makespan = found;
            best_schedule = evaluationModel.getBest_schedule();
            writeSchedule = true;

        }

        if (writeSchedule) {
            // Create an empty file in ./schedules/ dir
            // Name the file bestFitness.txt
            // Overwrite any existing file with that name

            // Make the dir
            try {
                Files.createDirectories(Paths.get("./schedules/"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            String path = "schedules/" + best_schedule_makespan + ".txt";

            try (BufferedWriter bf = Files.newBufferedWriter(Paths.get(path), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                for (Job job : best_schedule) {
                    bf.write(job.toString());
                    bf.newLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //okay, we have run a trial for the above individuals/rules - now we add a trial for each
        // update individuals to reflect the trial
        //System.out.println(ind.length);  // 2  repeat
        for (int i = 0; i < ind.length; i++) {
            GPIndividual coind = (GPIndividual) (ind[i]);
            Double trialValue = fitnesses.get(i).fitness(); //will actually be the same for both individuals

            if (updateFitness[i]) {
                // Update the context if this is the best trial.  We're going to assume that the best
                // trial is trial #0 so we don't have to search through them.
                int len = coind.fitness.trials.size();

                if (len == 0)  // easy
                {
                    if (shouldSetContext) {
                        coind.fitness.setContext(ind, i);
                    }
                    coind.fitness.trials.add(trialValue);
                } else if ((Double) (coind.fitness.trials.get(0)) > trialValue)  // best trial is presently #0
                {
                    if (shouldSetContext) {
                        //this is the new best trial, update context
                        coind.fitness.setContext(ind, i);
                    }
                    // put me at position 0
                    Double t = (Double) (coind.fitness.trials.get(0));
                    coind.fitness.trials.set(0, trialValue);  // put me at 0
                    coind.fitness.trials.add(t);  // move him to the end
                }

                // finally set the fitness for good measure
                double[] objectiveFitness = new double[1];
                objectiveFitness[0] = trialValue;
                //System.out.println(trialValue); //fitness value:  2.3109340359924153  0.9812294913889953     1.5828466799227603 etc
                ((MultiObjectiveFitness) coind.fitness).setObjectives(state, objectiveFitness);
            }
        }
    }
}
