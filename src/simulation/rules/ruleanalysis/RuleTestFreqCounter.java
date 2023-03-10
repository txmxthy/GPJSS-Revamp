package simulation.rules.ruleanalysis;

import ec.gp.GPNode;
import org.apache.commons.lang3.math.NumberUtils;
import simulation.definition.Objective;
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
 * Created by yimei on 17/10/16.
 */
public class RuleTestFreqCounter extends RuleTest {

    private final String featureSetName;
    private List<GPNode> features;

    public RuleTestFreqCounter(String trainPath,
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

    public RuleTestFreqCounter(String trainPath,
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
        String dir = "/local/scratch/Dropbox/Research/JobShopScheduling/ExpResults/";
        String[] algos = new String[]{"simple-gp-basic-terminals", "simple-gp-relative-terminals"};
        String[] fsNames = new String[]{"basic-terminals", "relative-terminals"};
        String[] scenarios = new String[]{"max-tardiness-0.85-4", "max-tardiness-0.95-4",
                "mean-tardiness-0.85-4", "mean-tardiness-0.95-4",
                "mean-weighted-tardiness-0.85-4", "mean-weighted-tardiness-0.95-4"};

        String trainPath;
        RuleType ruleType = RuleType.get("simple-rule");
        int numRuns = 30;
        String testScenario = "";
        String testSetName = "";
        int numObjectives = 0;
        int numPopulations = 1;
        List<Objective> objectives = new ArrayList<>();
        String featureSetName;
        for (int i = 0; i < algos.length; i++) {
            for (String scenario : scenarios) {
                trainPath = dir + algos[i] + "/" + scenario + "/";
                featureSetName = fsNames[i];

                RuleTestFreqCounter ruleTest = new RuleTestFreqCounter(trainPath,
                        ruleType, numRuns, testScenario, testSetName, objectives, featureSetName, numPopulations);

                ruleTest.writeToCSV();
            }
        }


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

    public void countFeatureFreq(double[] featureFreq, GPNode tree) {
        if (tree.depth() == 1) {
            if (NumberUtils.isNumber(tree.toString()))
                return;

            int idx = -1;
            for (int i = 0; i < features.size(); i++) {
                if (features.get(i).toString().equals(tree.toString())) {
                    idx = i;
                    break;
                }
            }

            featureFreq[idx]++;
        } else {
            for (GPNode child : tree.children)
                countFeatureFreq(featureFreq, child);
        }
    }

    @Override
    public void writeToCSV() {
        features = featuresFromSetName();

        File targetPath = new File(trainPath + "test");
        if (!targetPath.exists()) {
            targetPath.mkdirs();
        }

        File csvFile = new File(targetPath + "/feature-freq.csv");

        double[][] featureFreqMtx = new double[numRuns][features.size()];
        double[] numTerminals = new double[numRuns];

        for (int i = 0; i < numRuns; i++) {
            System.out.println("run: " + i);
            File sourceFile = new File(trainPath + "job." + i + ".out.stat");

            TestResult result = TestResult.readFromFile(sourceFile, ruleType, numPopulations);

            GPRule[] bestRules = (GPRule[]) (result.getBestRules());
            GPRule seqRule;
            if (bestRules[0].getType() == simulation.rules.rule.RuleType.SEQUENCING) {
                seqRule = bestRules[0];
            } else {
                seqRule = bestRules[1];
            }

            numTerminals[i] = seqRule.getGPTree().child.numNodes(GPNode.NODESEARCH_TERMINALS);
            countFeatureFreq(featureFreqMtx[i], seqRule.getGPTree().child);
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile.getAbsoluteFile()));
            writer.write("Run,Feature,Freq");
            writer.newLine();
            for (int i = 0; i < numRuns; i++) {
                for (int j = 0; j < features.size(); j++) {
                    writer.write(i + "," + features.get(j).toString() + "," +
                            featureFreqMtx[i][j] / numTerminals[i]);
                    writer.newLine();
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
