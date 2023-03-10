package simulation.rules.ruleanalysis;

import ec.Fitness;
import ec.multiobjective.MultiObjectiveFitness;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import simulation.rules.rule.operation.evolved.GPRule;
import simulation.util.lisp.LispSimplifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class NSGAIIMultipleTreeResultFileReader extends ResultFileReader {

    public static TestResult readTestResultFromFile(File file, RuleType ruleType, boolean isMultiObjective,
                                                    int numTrees) {
        TestResult result = new TestResult();

        String line;
        Fitness fitnesses;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            //read the file until arriveing the position 'Pareto Front of Subpopulation 0'
            line = br.readLine();
            while (!(line.equals("Pareto Front of Subpopulation 0"))) {
                line = br.readLine();
            }
            //the output here is line = Pareto Front of Subpopulation 0
            line = br.readLine();

            while (line != null) {
                if (line.startsWith("Evaluated: true")) {
                    GPRule sequencingRule;
                    GPRule routingRule;

                    line = br.readLine(); // read in fitness on following line
                    fitnesses = readFitnessFromLine(line, isMultiObjective);

                    //fzhang  2018.11.4   for NSGA-II
                    br.readLine(); //Rank: 0
                    br.readLine(); //Sparsity: Infinity

                    br.readLine(); // tree 0
                    line = br.readLine(); // this is a sequencing rule

                    // sequencing rule
                    line = LispSimplifier.simplifyExpression(line);
                    sequencingRule = GPRule.readFromLispExpression(simulation.rules.rule.RuleType.SEQUENCING, line);

                    // routing rule
                    br.readLine();
                    line = br.readLine();
                    routingRule = GPRule.readFromLispExpression(simulation.rules.rule.RuleType.ROUTING, line);

                    Fitness fitness = fitnesses;
                    GPRule[] bestRules = new GPRule[numTrees];

                    bestRules[0] = sequencingRule; // sequencing rule
                    bestRules[1] = routingRule; // routing rule

                    result.setBestRules(bestRules);
                    result.setBestTrainingFitness(fitness);

                    result.addGenerationalRules(bestRules);
                    result.addGenerationalTrainFitness(fitness);
                    result.addGenerationalValidationFitnesses((Fitness) fitness.clone());
                    result.addGenerationalTestFitnesses((Fitness) fitness.clone());
                    line = br.readLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    private static MultiObjectiveFitness parseFitness(String line) {
        String[] spaceSegments = line.split("\\s+");//\\s????????   ????????,????????,????????????????????, +????????????????????????????????????????????
        MultiObjectiveFitness f = new MultiObjectiveFitness();
        f.objectives = new double[spaceSegments.length - 1];
        for (int i = 1; i < spaceSegments.length; i++) {
            String[] equation = spaceSegments[i].split("\\[|\\]");
            double fitness = Double.parseDouble(equation[i == 1 ? 1 : 0]);
            f.objectives[i - 1] = fitness;
        }

        return f;
//		String[] equation1 = spaceSegments[1].split("\\[|\\]");
//		String[] equation2 = spaceSegments[2].split("\\[|\\]");
//		double fitness1 = Double.valueOf(equation1[1]);
//		double fitness2 = Double.valueOf(equation2[0]);
////		MultiObjectiveFitness f = new MultiObjectiveFitness();
////		f.objectives = new double[2];
//		f.objectives[0] = fitness1;
//		f.objectives[1] = fitness2;
    }

    private static Fitness readFitnessFromLine(String line, boolean isMultiobjective) {
        if (isMultiobjective) {
            // TODO read multi-objective fitness line
			/*String[] spaceSegments = line.split("\\s+");//\\s????????   ????????,????????,????????????????????, +????????????????????????????????????????????
			String[] equation = spaceSegments[1].split("=");
			double fitness = Double.valueOf(equation[1]);
			KozaFitness f = new KozaFitness();
			f.setStandardizedFitness(null, fitness);*/

            //save objective from training fzhang 18.11.2018
			/*String[] spaceSegments = line.split("\\s+");//\\s????????   ????????,????????,????????????????????, +????????????????????????????????????????????
			String[] equation1 = spaceSegments[1].split("\\[|\\]");
			String[] equation2 = spaceSegments[2].split("\\[|\\]");
			double fitness1 = Double.valueOf(equation1[1]);
			double fitness2 = Double.valueOf(equation2[0]);
			MultiObjectiveFitness f = new MultiObjectiveFitness();
			f.objectives = new double[2];
			f.objectives[0] = fitness1;
			f.objectives[1] = fitness2;

			return f;*/
            return parseFitness(line);

        } else {
            String[] spaceSegments = line.split("\\s+"); // . ?????? | ?????? * ????????????????????????????????????????????? \\??????
            String[] fitVec = spaceSegments[1].split("\\[|\\]");//??????????????????????????????????????????????? | ????????????????????????????
            double fitness = Double.parseDouble(fitVec[1]);
            MultiObjectiveFitness f = new MultiObjectiveFitness();
            f.objectives = new double[1];
            f.objectives[0] = fitness;

            return f;
        }
    }

    //24.8.2018  fzhang read badrun into CSV
    public static DescriptiveStatistics readBadRunFromFile(File file) {
        DescriptiveStatistics generationalBadRunStat = new DescriptiveStatistics();

        String line;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine();
            while (true) {
                line = br.readLine();

                if (line == null)
                    break;

                String[] commaSegments = line.split(",");
                generationalBadRunStat.addValue(Double.parseDouble(commaSegments[1])); //read from excel, the first column is 0
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return generationalBadRunStat;
    }

}
