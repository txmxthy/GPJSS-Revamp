package simulation.rules.ruleanalysis;

import ec.gp.GPNode;
import ec.gp.GPTree;
import ec.multiobjective.MultiObjectiveFitness;
import simulation.definition.Objective;
import simulation.definition.SchedulingSet;
import simulation.jss.feature.ignore.Ignorer;
import simulation.jss.feature.ignore.SimpleIgnorer;
import solvers.gp.terminal.AttributeGPNode;
import solvers.gp.terminal.JobShopAttribute;
import simulation.rules.rule.operation.evolved.GPRule;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yimei on 12/10/16.
 */
public class RuleTestFeatureContribution extends RuleTest {

    private final String featureSetName;
    private final Ignorer ignorer = new SimpleIgnorer();

    public RuleTestFeatureContribution(String trainPath,
                                       RuleType ruleType,
                                       int numRuns,
                                       String testScenario,
                                       String testSetName,
                                       List<Objective> objectives,
                                       String featureSetName,
                                       int numPopulations) {
        super(trainPath, ruleType, numRuns, testScenario, testSetName, objectives, numPopulations);
        this.featureSetName = featureSetName;
    }

    public RuleTestFeatureContribution(String trainPath,
                                       RuleType ruleType,
                                       int numRuns,
                                       String testScenario,
                                       String testSetName,
                                       String featureSetName,
                                       int numPopulations) {
        this(trainPath, ruleType, numRuns, testScenario, testSetName,
                new ArrayList<>(), featureSetName, numPopulations);
    }

    public static void main(String[] args) {
        //should follow this sorting to set parameters for testing
        int idx = 0;
        String trainPath = args[idx];
        idx++;
        RuleType ruleType = RuleType.get(args[idx]);
        idx++;
        int numRuns = Integer.parseInt(args[idx]);
        idx++;
        String testScenario = args[idx];
        idx++;
        String testSetName = args[idx];
        idx++;
        int numPopulations = Integer.parseInt(args[idx]);
        idx++;
        int numObjectives = Integer.parseInt(args[idx]);
        idx++;
        List<Objective> objectives = new ArrayList<>();
        for (int i = 0; i < numObjectives; i++) {
            objectives.add(Objective.get(args[idx]));
            idx++;
        }
        String featureSetName = String.valueOf(args[idx]);
        idx++;

        RuleTestFeatureContribution ruleTest = new RuleTestFeatureContribution(trainPath,
                ruleType, numRuns, testScenario, testSetName, objectives, featureSetName, numPopulations);

        ruleTest.writeToCSV();
    }

    public List<GPNode> featuresFromSetName() {
        List<GPNode> features = new ArrayList<>();

        switch (featureSetName) {
            case "basic-terminals":
                for (JobShopAttribute a : JobShopAttribute.basicAttributes()) {
                    features.add(new AttributeGPNode(a));
                }
                break;
            case "relative-terminals":
                for (JobShopAttribute a : JobShopAttribute.relativeAttributes()) {
                    features.add(new AttributeGPNode(a));
                }
                break;
            default:
                break;
        }

        return features;
    }

    @Override
    public void writeToCSV() {
        SchedulingSet testSet = generateTestSet();
        List<GPNode> features = featuresFromSetName();

        File targetPath = new File(trainPath + "test");
        if (!targetPath.exists()) {
            targetPath.mkdirs();
        }

        File csvFile = new File(targetPath + "/" + testSetName + "-feature-contribution.csv");

        double[][] featureContributionMtx = new double[numRuns][features.size()];

        for (int i = 0; i < numRuns; i++) {
            File sourceFile = new File(trainPath + "job." + i + ".out.stat");

            TestResult result = TestResult.readFromFile(sourceFile, ruleType, numPopulations);

            long start = System.currentTimeMillis();

            GPRule[] bestRules = (GPRule[]) result.getBestRules();

            MultiObjectiveFitness allFeaturesFit = new MultiObjectiveFitness();
            allFeaturesFit.objectives = new double[1];
            allFeaturesFit.maxObjective = new double[1];
            allFeaturesFit.minObjective = new double[1];
            allFeaturesFit.maximize = new boolean[1];
            bestRules[0].calcFitness(allFeaturesFit, null, testSet, bestRules[1], objectives);

            for (int j = 0; j < features.size(); j++) {
                GPNode feature = features.get(j);
                MultiObjectiveFitness fit = new MultiObjectiveFitness();
                fit.objectives = new double[1];
                fit.maxObjective = new double[1];
                fit.minObjective = new double[1];
                fit.maximize = new boolean[1];

                GPRule tempSeqRule;
                GPRule tempRoutingRule;
                if (bestRules[0].getType() == simulation.rules.rule.RuleType.SEQUENCING) {
                    tempSeqRule = new GPRule(simulation.rules.rule.RuleType.SEQUENCING, (GPTree) (bestRules[0].getGPTree().clone()));
                    tempRoutingRule = new GPRule(simulation.rules.rule.RuleType.ROUTING, (GPTree) (bestRules[1].getGPTree().clone()));
                } else {
                    tempSeqRule = new GPRule(simulation.rules.rule.RuleType.SEQUENCING, (GPTree) (bestRules[1].getGPTree().clone()));
                    tempRoutingRule = new GPRule(simulation.rules.rule.RuleType.ROUTING, (GPTree) (bestRules[0].getGPTree().clone()));
                }

                //TODO: What do we do here? Which rule do we ignore features from? One at a time?

                tempSeqRule.ignore(feature, ignorer);
                tempSeqRule.calcFitness(fit, null, testSet, tempRoutingRule, objectives);

                System.out.format("Run %d, %s: %.2f\n", i, feature.toString(),
                        fit.fitness() - allFeaturesFit.fitness());

                featureContributionMtx[i][j] = fit.fitness() - allFeaturesFit.fitness();
            }

            long finish = System.currentTimeMillis();
            long duration = finish - start;
            System.out.println("Run " + i + ": Duration = " + duration + " ms.");
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile.getAbsoluteFile()));
            writer.write("Run,Feature,Contribution");
            writer.newLine();
            for (int i = 0; i < numRuns; i++) {
                for (int j = 0; j < features.size(); j++) {
                    writer.write(i + "," + features.get(j).toString() + "," +
                            featureContributionMtx[i][j]);
                    writer.newLine();
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
