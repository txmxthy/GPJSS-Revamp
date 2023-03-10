package simulation.rules.ruleevaluation;

import ec.EvolutionState;
import ec.Fitness;
import ec.util.Parameter;
import simulation.definition.FlexibleStaticInstance;
import simulation.definition.Job;
import simulation.definition.SchedulingSet;
import simulation.definition.logic.DynamicSimulation;
import simulation.definition.logic.Simulation;
import simulation.definition.logic.StaticSimulation;
import simulation.rules.rule.AbstractRule;

import java.util.ArrayList;
import java.util.List;

/**
 * The simple evaluation model: standard simulation.
 * <p>
 * Created by yimei on 10/11/16.
 */
public class SimpleEvaluationModel extends AbstractEvaluationModel {

    /**
     * The starting seed of the simulation models.
     */
    public final static String P_SIM_SEED = "sim-seed";

    /**
     * Whether to rotate the simulation seed or not.
     */
    public final static String P_ROTATE_SIM_SEED = "rotate-sim-seed";
    public final static String P_SIM_MODELS = "sim-models";
    public final static String P_SIM_NUM_MACHINES = "num-machines";
    public final static String P_SIM_NUM_JOBS = "num-jobs";
    public final static String P_SIM_WARMUP_JOBS = "warmup-jobs";
    public final static String P_SIM_MIN_NUM_OPERATIONS = "min-num-operations";
    public final static String P_SIM_MAX_NUM_OPERATIONS = "max-num-operations";
    public final static String P_SIM_MIN_NUM_OPTIONS = "min-num-options";
    public final static String P_SIM_MAX_NUM_OPTIONS = "max-num-options";
    public final static String P_SIM_UTIL_LEVEL = "util-level";
    public final static String P_SIM_DUE_DATE_FACTOR = "due-date-factor";
    public final static String P_SIM_REPLICATIONS = "replications";

    protected SchedulingSet schedulingSet;
    protected long simSeed;
    protected boolean rotateSimSeed;

    public SchedulingSet getSchedulingSet() {
        return schedulingSet;
    }

    public long getSimSeed() {
        return simSeed;
    }

    public boolean isRotateSimSeed() {
        return rotateSimSeed;
    }

    @Override
    public List<Job> getBest_schedule() {
        return null;
    }

    @Override
    public double getBest_schedule_makespan() {
        return 0;
    }

    @Override
    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state, base);

        // Get the seed for the simulation.
        Parameter p = base.push(P_SIM_SEED);
        simSeed = state.parameters.getLongWithDefault(p, null, 0);

        // Get the simulation models.
        p = base.push(P_SIM_MODELS);
        int numSimModels = state.parameters.getIntWithDefault(p, null, 0);

        if (numSimModels == 0) {
            System.err.println("ERROR:");
            System.err.println("No simulation model is specified.");
            System.exit(1);
        }

        List<Simulation> trainSimulations = new ArrayList<>();
        List<Integer> replications = new ArrayList<>();
        for (int x = 0; x < numSimModels; x++) {
            // Read this simulation model
            Parameter b = base.push(P_SIM_MODELS).push("" + x);
            // Number of machines
            p = b.push(P_SIM_NUM_MACHINES);
            int numMachines = state.parameters.getIntWithDefault(p, null, 10);
            // Number of jobs
            p = b.push(P_SIM_NUM_JOBS);
            int numJobs = state.parameters.getIntWithDefault(p, null, 5000);
            // Number of warmup jobs
            p = b.push(P_SIM_WARMUP_JOBS);
            int warmupJobs = state.parameters.getIntWithDefault(p, null, 1000);
            // Min number of operations
            p = b.push(P_SIM_MIN_NUM_OPERATIONS);
            int minNumOperations = state.parameters.getIntWithDefault(p, null, 1);
            // Max number of operations
            p = b.push(P_SIM_MAX_NUM_OPERATIONS);
            int maxNumOperations = state.parameters.getIntWithDefault(p, null, numMachines);
            // Min number of options a single operation will have
            p = b.push(P_SIM_MIN_NUM_OPTIONS);
            int minNumOptions = state.parameters.getIntWithDefault(p, null, 1);
            // Max number of options a single operation will have
            p = b.push(P_SIM_MAX_NUM_OPTIONS);
            int maxNumOptions = state.parameters.getIntWithDefault(p, null, numMachines);
            // Utilization level
            p = b.push(P_SIM_UTIL_LEVEL);
            double utilLevel = state.parameters.getDoubleWithDefault(p, null, 0.85);
            // Due date factor
            p = b.push(P_SIM_DUE_DATE_FACTOR);
            double dueDateFactor = state.parameters.getDoubleWithDefault(p, null, 4.0);
            // Number of replications
            p = b.push(P_SIM_REPLICATIONS);
            int rep = state.parameters.getIntWithDefault(p, null, 1);
            Simulation simulation;
            //only expecting filePath parameter for Static FJSS, so can use this
            String filePath = state.parameters.getString(new Parameter("filePath"), null);
            if (filePath == null) {
                //Dynamic Simulation
                simulation = new DynamicSimulation(simSeed,
                        null, null, numMachines, numJobs, warmupJobs,
                        minNumOperations, maxNumOperations,
                        utilLevel, dueDateFactor, false);
            } else {
                FlexibleStaticInstance instance = FlexibleStaticInstance.readFromAbsPath(filePath);
                simulation = new StaticSimulation(null, null, instance);
            }
            trainSimulations.add(simulation);
            replications.add(rep);
        }

        schedulingSet = new SchedulingSet(trainSimulations, replications, objectives);

        p = base.push(P_ROTATE_SIM_SEED);
        rotateSimSeed = state.parameters.getBoolean(p, null, false);
    }

    @Override
    public void evaluate(List<Fitness> fitnesses,
                         List<AbstractRule> rules,
                         EvolutionState state) {
        //only expecting one rule here
        if (rules.size() != 1) {
            try {
                throw new Exception(rules.size() + " - unexpected number of rules.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        AbstractRule rule = rules.get(0); //sequencing rule
        Fitness fitness = fitnesses.get(0);
        //can get the other rule from the simulation
        AbstractRule routingRule = schedulingSet.getSimulations().get(0).getRoutingRule();

        rule.calcFitness(fitness, state, schedulingSet, routingRule, objectives);
    }

    @Override
    public boolean isRotatable() {
        return rotateSimSeed;
    }

    @Override
    public void rotate() {
        schedulingSet.rotateSeed(objectives);
    }

    @Override
    public void normObjective(List<Fitness> fitnesses, List<AbstractRule> rule, EvolutionState state) {
        // TODO Auto-generated method stub

    }
}
