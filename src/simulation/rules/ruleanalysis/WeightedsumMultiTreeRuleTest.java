package simulation.rules.ruleanalysis;

import ec.gp.GPNode;
import ec.multiobjective.MultiObjectiveFitness;
import simulation.definition.Objective;
import simulation.definition.SchedulingSet;
import simulation.rules.rule.AbstractRule;
import simulation.rules.rule.operation.evolved.GPRule;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WeightedsumMultiTreeRuleTest {

    public static final long simSeed = 968356;

    protected final String trainPath; //the directory of training things
    protected final RuleType ruleType;
    protected final int numRuns;
    protected final String testScenario;
    protected final String testSetName;
    protected final int numTrees;
    protected List<Objective> objectives; // The objectives to test.

    public WeightedsumMultiTreeRuleTest(String trainPath, RuleType ruleType, int numRuns,
                                        String testScenario, String testSetName,
                                        List<Objective> objectives, int numTrees) {
        this.trainPath = trainPath;
        this.ruleType = ruleType;
        this.numRuns = numRuns;
        this.testScenario = testScenario;
        this.testSetName = testSetName;
        this.objectives = objectives;
        this.numTrees = numTrees;
    }

    //fzhang 17.11.2018 test for multi-objectives
    public WeightedsumMultiTreeRuleTest(String trainPath, RuleType ruleType, int numRuns,
                                        String testScenario, String testSetName, int numTreess) {
        this(trainPath, ruleType, numRuns, testScenario, testSetName, new ArrayList<>(), numTreess);
    }

    /**
     * Call this main method with several parameters
     * <p>
     * /Users/dyska/Desktop/Uni/COMP489/GPJSS/grid_results/dynamic/raw/coevolution-fixed/0.85-max-flowtime/
     * simple-rule
     * 30
     * dynamic-job-shop
     * missing-0.85-4.0
     * 2
     * 1
     * max-flowtime
     */
    public static void main(String[] args) {
        int idx = 0;
        String trainPath = args[idx];
        idx++;
        RuleType ruleType = RuleType.get(args[idx]);
        idx++;
        int numRuns = Integer.parseInt(args[idx]); //30
        idx++;
        String testScenario = args[idx]; //dynamic
        idx++;
        String testSetName = args[idx]; //missing-0.85-4.0
        idx++;
        int numTrees = Integer.parseInt(args[idx]); //2
        idx++;
        int numObjectives = Integer.parseInt(args[idx]); //1
        idx++;

        //RuleTest ruleTest = new RuleTest(trainPath, ruleType, numRuns, testScenario, testSetName, numTrees);
        //modified by fzhang  24.5.2018  use multipleTreeRuleTest
        WeightedsumMultiTreeRuleTest multipletreeruleTest = new WeightedsumMultiTreeRuleTest(trainPath, ruleType, numRuns, testScenario, testSetName, numTrees);

        for (int i = 0; i < numObjectives; i++) {
            multipletreeruleTest.addObjective(args[idx]);
            idx++;
        }

        multipletreeruleTest.writeToCSV();
    }

    public String getTrainPath() {
        return trainPath;
    }

    public RuleType getRuleType() {
        return ruleType;
    }

    public int getNumRuns() {
        return numRuns;
    }

    public int getnumTrees() {
        return numTrees;
    }

    public String getTestScenario() {
        return testScenario;
    }

    public List<Objective> getObjectives() {
        return objectives;
    }

    public void setObjectives(List<Objective> objectives) {
        this.objectives = objectives;
    }

    public void addObjective(Objective objective) {
        this.objectives.add(objective);
    }

    public void addObjective(String objective) {
        addObjective(Objective.get(objective));
    }

    //generate testset using simseed, replications
    public SchedulingSet generateTestSet() {
        return SchedulingSet.generateSet(simSeed, testScenario,
                testSetName, objectives, 50);
    }

    public void writeToCSV() {
        SchedulingSet testSet = generateTestSet();

        File targetPath = new File(trainPath + "test"); //create a folder named "test" in trainPath
        if (!targetPath.exists()) {
            targetPath.mkdirs();
        }

        File csvFile = new File(targetPath + "/" + testSetName + ".csv"); //create a .csv to save the test result

        List<TestResult> testResults = new ArrayList<>();

        //for test: which machines are choosen by routing rule, CCGP. Scenario: run1, rule in generaiton 51 numRuns
        for (int i = 0; i < numRuns; i++) {
            System.out.println("Run " + i);
            //for (int i = 0; i < 1; i++) {
            File sourceFile = new File(trainPath + "job." + i + ".out.stat");  //this file keeps the rule
            TestResult result = WeightedsumMultipleTreeTestResult.readFromFile(sourceFile, ruleType, numTrees);

            //Didn't bother saving time files
            //for multi-objective, there are some error
            File timeFile = new File(trainPath + "job." + i + ".time.csv");
            result.setGenerationalTimeStat(WeightedsumMultipleTreeResultFileReader.readTimeFromFile(timeFile));

            //24.8.2018 fzhang read badrun in CSV
            File trainingfitnessFile = new File(trainPath + "job." + i + ".TrainingFitness.csv");
            result.setGenerationalTrainingFitnessStat0(WeightedsumMultipleTreeResultFileReader.readTrainingFitness0FromFile(trainingfitnessFile));
            result.setGenerationalTrainingFitnessStat1(WeightedsumMultipleTreeResultFileReader.readTrainingFitness1FromFile(trainingfitnessFile));

            long start = System.currentTimeMillis();

//	            result.validate(objectives);

            //for (int j = 42; j < result.getGenerationalRules().size(); j++) {
//	            System.out.println(result.getGenerationalRules().size());  // 1
            for (int j = 0; j < result.getGenerationalRules().size(); j++) {
                System.out.print("Generation " + j + ": ");

                AbstractRule[] generationalRules = result.getGenerationalRules(j);
                if (numTrees == 2) {
                    generationalRules[0].calcFitness(  //in calcFitness(), it will check which one is routing/sequencing rule
                            result.getGenerationalTestFitness(j), null,
                            testSet, generationalRules[1], objectives);
                    //MultiObjecitve and KozaFitness extend from fitness(only return the largest fitness)
                    //when we want to output more than one objective, first should be casted to multiobjective
//	                    System.out.println(((MultiObjectiveFitness)result.getGenerationalTestFitness(j)).objectives[0]);
//	                    System.out.println(((MultiObjectiveFitness)result.getGenerationalTestFitness(j)).objectives[1]);
                }
                //generationalRules[1] is routing rule

                //fzhang 2018.12.6 automatically print objective values
                for (int k = 0; k < objectives.size(); k++) {
                    System.out.print("obj " + k + " = " + ((MultiObjectiveFitness) result.getGenerationalTestFitness(j)).objectives[k]
                            + " ");
                }
                System.out.print("\n");

	               /* System.out.println("Rule " + j + ": objecitve 1 = " +
	                		((MultiObjectiveFitness)result.getGenerationalTestFitness(j)).objectives[0] +
	                        ", objecitve 2 = "+ ((MultiObjectiveFitness)result.getGenerationalTestFitness(j)).objectives[1] +
	                        ", objecitve 3 = "+ ((MultiObjectiveFitness)result.getGenerationalTestFitness(j)).objectives[2]);*/
                //fitness(): return the max value
            }

            long finish = System.currentTimeMillis();
            long duration = (finish - start) / 1000;
            System.out.println("Duration = " + duration + " s.");

            testResults.add(result);
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile.getAbsoluteFile()));
            if (objectives.size() == 1) {
                writer.write("Run,Generation,SeqRuleSize,SeqRuleUniqueTerminals,RoutRuleSize," +
                        "RoutRuleUniqueTerminals,Obj,TrainFitness,TestFitness,TrainTime");
                writer.newLine();
            } else if (objectives.size() == 2) {
                writer.write("Run,Generation,SeqRuleSize,SeqRuleUniqueTerminals,RoutRuleSize," +
                        "RoutRuleUniqueTerminals,Obj,TrainFitness,TrainFitness1,TrainFitness2,TestFitness1,TestFitness2,TrainTime");
                writer.newLine();
            } else {
                writer.write("Run,Generation,SeqRuleSize,SeqRuleUniqueTerminals,RoutRuleSize," +
                        "RoutRuleUniqueTerminals,Obj,TrainFitness,TestFitness1,TestFitness2,TestFitness3,TrainTime");
                writer.newLine();
            }

            //for (int i = 0; i < 1; i++) {
            for (int i = 0; i < numRuns; i++) {
                TestResult result = testResults.get(i);

                //for (int j = 42; j < result.getGenerationalRules().size(); j++) { //use rules in each generation for testing
                for (int j = 0; j < result.getGenerationalRules().size(); j++) { //use rules in each generation for testing

                    MultiObjectiveFitness trainFit =
                            (MultiObjectiveFitness) result.getGenerationalTrainFitness(j);
                    MultiObjectiveFitness testFit =
                            (MultiObjectiveFitness) result.getGenerationalTestFitness(j);
                    GPRule[] rules = (GPRule[]) result.getGenerationalRules(j);
                    GPRule seqRule;
                    GPRule routRule = null;
                    if (numTrees == 2) {
                        if (rules[0].getType() == simulation.rules.rule.RuleType.SEQUENCING) {
                            seqRule = rules[0];
                            routRule = rules[1];
                        } else {
                            seqRule = rules[1];
                            routRule = rules[0];
                        }
                    } else {
                        seqRule = rules[0];
                    }


                    UniqueTerminalsGatherer gatherer = new UniqueTerminalsGatherer();
                    int numUniqueTerminalsSeq = seqRule.getGPTree().child.numNodes(gatherer);
                    int seqRuleSize = seqRule.getGPTree().child.numNodes(GPNode.NODESEARCH_ALL);

                    int numUniqueTerminalsRout = 0;
                    int routRuleSize = 0;
                    if (numTrees == 2) {
                        gatherer = new UniqueTerminalsGatherer();
                        numUniqueTerminalsRout = routRule.getGPTree().child.numNodes(gatherer);
                        routRuleSize = routRule.getGPTree().child.numNodes(GPNode.NODESEARCH_ALL);
                    }

                    if (objectives.size() == 1) {

                        writer.write(i + "," + j + "," +
                                seqRuleSize + "," +
                                numUniqueTerminalsSeq + "," +
                                routRuleSize + "," +
                                numUniqueTerminalsRout + ",1," +
                                trainFit.fitness() + "," +
                                testFit.fitness() + "," +
                                result.getGenerationalTime(j));
                        writer.newLine();
                    } else if (objectives.size() == 2) {
                        writer.write(i + "," + j + "," +
                                seqRuleSize + "," +
                                numUniqueTerminalsSeq + "," +
                                routRuleSize + "," +
                                numUniqueTerminalsRout + ",2" + "," +
                                trainFit.getObjective(0));

                        writer.write("," + result.getGenerationalTrainingFitness0(j));
                        writer.write("," + result.getGenerationalTrainingFitness1(j));

                        for (int k = 0; k < objectives.size(); k++) {
                            writer.write("," + testFit.getObjective(k));
                        }
                        writer.write("," + result.getGenerationalTime(j));
                        writer.newLine();
                    } else {
                        writer.write(i + "," + j + "," +
                                seqRuleSize + "," +
                                numUniqueTerminalsSeq + "," +
                                routRuleSize + "," +
                                numUniqueTerminalsRout + ",3" + "," +
                                trainFit.getObjective(0));

                        for (int k = 0; k < objectives.size(); k++) {
                            writer.write("," + testFit.getObjective(k));
                        }
                        writer.write("," + result.getGenerationalTime(j));
                        writer.newLine();
                    }
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
