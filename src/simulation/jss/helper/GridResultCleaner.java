package simulation.jss.helper;

import simulation.definition.FlexibleStaticInstance;
import simulation.definition.Objective;
import simulation.definition.SchedulingSet;
import simulation.definition.logic.Simulation;
import simulation.definition.logic.StaticSimulation;
import simulation.jss.FJSSMain;
import simulation.rules.rule.AbstractRule;
import simulation.rules.rule.RuleType;
import simulation.rules.rule.workcenter.basic.SBT;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Have results from the jobs run on the grid. There are 30 result files per
 * instance file, and we only need the best fitness from each result file. This
 * program should output a csv file containing the best fitness from each of the
 * 30 result files for each instance.
 * <p>
 * We have 30 output files for each instance, and 50 generations per output file,
 * plus a 'best individual of run' output.
 * <p>
 * For each generation/best, we have a fitness and the rule itself.
 * We SHOULD be able to derive makespan from the fitness, by calculating the benchmark makespan
 * that would have veeb used. We could also parse the rule(s) and re-calculate the makespan
 * from this. Once we have results back from the grid, can verify this, but calculating from
 * benchmark is definitely simpler.
 * <p>
 * When looking through grid results, two main scenarios.
 * Either we only care about the best makespan for each file, or we also care about the
 * best makespan for each generation.
 * <p>
 * May as well combine the two and store the best makespan from each generation,
 * plus the best makespan of any rule. Should store each of these on the same row,
 * and have 30 rows.
 * <p>
 * Created by dyska on 8/07/17.
 */
public class GridResultCleaner {
    private static final char DEFAULT_SEPARATOR = ',';
    private static final String GRID_PATH = "/Users/dyska/Desktop/Uni/COMP489/GPJSS/grid_results/";
    private final String dataPath;
    private final String outPath;
    private final boolean doIncludeGenerations;
    private final int numPops;
    private final boolean isStatic;
    private HashMap<String, Integer> benchmarkMakespans;
    private AbstractRule routingRule;

    public GridResultCleaner(String simulationType, String dirName, int numPops, boolean doIncludeGenerations) {
        this.dataPath = GRID_PATH + simulationType + "/raw/" + dirName;
        this.outPath = GRID_PATH + simulationType + "/cleaned/" + dirName;
        this.numPops = numPops;
        this.doIncludeGenerations = doIncludeGenerations;
        if (simulationType.equalsIgnoreCase("static")) {
            isStatic = true;
            benchmarkMakespans = InitBenchmarkMakespans();
        } else {
            isStatic = false;
        }
    }

    public GridResultCleaner(String simulationType, String dirName, AbstractRule routingRule, int numPops, boolean doIncludeGenerations) {
        this.dataPath = GRID_PATH + simulationType + "/raw/" + dirName;
        this.outPath = GRID_PATH + simulationType + "/cleaned/" + dirName + "/" + routingRule.getName();
        this.numPops = numPops;
        this.routingRule = routingRule;
        this.doIncludeGenerations = doIncludeGenerations;
        if (simulationType.equalsIgnoreCase("static")) {
            isStatic = true;
            benchmarkMakespans = InitBenchmarkMakespans();
        } else {
            isStatic = false;
        }
    }

    public static int roundMakespan(double makespan) {
        //makespans are being calculated by multiplying benchmark by fitness
        //should be extremely close to an integer value
        int makespanInt = (int) Math.round(makespan);
        if (Math.abs(makespanInt - makespan) > 0.0000001) {
            //arbitrary value, but should be very very close
            System.out.println("Why is the value not an integer?");
            return -1;
        }
        return makespanInt;
    }

    public static void writeLine(Writer w, List<String> values, char separators, char customQuote) throws IOException {

        boolean first = true;

        //default customQuote is empty

        if (separators == ' ') {
            separators = DEFAULT_SEPARATOR;
        }

        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            if (!first) {
                sb.append(separators);
            }
            if (customQuote == ' ') {
                sb.append(followCSVformat(value));
            } else {
                sb.append(customQuote).append(followCSVformat(value)).append(customQuote);
            }

            first = false;
        }
        sb.append("\n");
        w.append(sb.toString());
    }

    private static String followCSVformat(String value) {

        String result = value;
        if (result.contains("\"")) {
            result = result.replace("\"", "\"\"");
        }
        return result;
    }

    public static void writeLine(Writer w, List<String> values) throws IOException {
        writeLine(w, values, DEFAULT_SEPARATOR, ' ');
    }

    public static void main(String[] args) {
        AbstractRule routingRule = new SBT(RuleType.ROUTING);
        GridResultCleaner grc = new GridResultCleaner("static", "simple_routing_rule_tests", routingRule,
                1, true);
        grc.cleanResults();
    }

    private HashMap<String, Integer> InitBenchmarkMakespans() {
        String homePath = "/Users/dyska/Desktop/Uni/COMP489/GPJSS/";
        String dataPath = homePath + "data/FJSS/";
        List<Objective> objectives = new ArrayList<>();
        objectives.add(Objective.MAKESPAN);
        List<Integer> replications = new ArrayList<>();
        replications.add(1);

        List<String> fileNames = FJSSMain.getFileNames(new ArrayList<>(), Paths.get(dataPath), ".fjs");
        HashMap<String, Integer> makeSpans = new HashMap<>();

        for (String fileName : fileNames) {

            List<Simulation> simulations = new ArrayList<>();
            FlexibleStaticInstance instance = FlexibleStaticInstance.readFromAbsPath(fileName);
            Simulation simulation = new StaticSimulation(null, routingRule, instance);
            simulations.add(simulation);
            SchedulingSet schedulingSet = new SchedulingSet(simulations, replications, objectives);

            int benchmarkMakespan = roundMakespan(schedulingSet.getObjectiveLowerBoundMtx().getData()[0][0]);
            fileName = fileName.substring(dataPath.length());
            makeSpans.put(fileName, benchmarkMakespan);
        }
        return makeSpans;
    }

    public void cleanResults() {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dataPath))) {
            for (Path path : stream) {
                if (path.toFile().isDirectory()) {
                    //don't want .DS_Store files...
                    if (!path.toString().startsWith(dataPath + "/.")) {
                        if (routingRule != null) {
                            if (!path.toString().endsWith(routingRule.getName())) {
                                continue;
                            }
                        }
                        HashMap<Integer, Double[]> makespans = parseMakespans(path.toString());
                        if (makespans != null) {
                            System.out.println("Creating results file for: " +
                                    path.toString().substring(dataPath.length() + 1));
                            createResultFile(path.toString(), makespans);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
     * All the code below this line is not mine, taken from:
     * https://www.mkyong.com/java/how-to-export-data-to-csv-file-java/
     */

    public HashMap<Integer, Double[]> parseMakespans(String directoryPath) {
        List<String> fileNames = FJSSMain.getFileNames(new ArrayList<>(), Paths.get(directoryPath), ".stat");
        if (fileNames.isEmpty()) {
            //must not be a directory for this file
            return null;
        }
        HashMap<Integer, Double[]> makespans = new HashMap<>();
        //we have a file, and the fitness of all rules evolved from this file is the makespan of that
        //rule divided by the benchmark makespan (which is constant)
        int benchmarkMakespan = 0;
        if (isStatic) {
            benchmarkMakespan = roundMakespan(getBenchmarkMakeSpan(directoryPath));
        }

        //iterating through the output from each different seed value
        for (String fileName : fileNames) {
            Double[] fitnesses = GetFitnesses(fileName);

            String fileNumber = fileName.substring(fileName.indexOf("job") + "job.".length());
            int fileNum = Integer.parseInt(fileNumber.substring(0, fileNumber.indexOf(".out.stat")));

            if (isStatic) {
                Double[] fileMakespans = new Double[fitnesses.length];
                for (int i = 0; i < fitnesses.length; ++i) {
                    fileMakespans[i] = (double) roundMakespan(benchmarkMakespan * fitnesses[i]);
                }
                makespans.put(fileNum, fileMakespans);
            } else {
                //just going to record fitnesses
                makespans.put(fileNum, fitnesses);
            }
        }
        return makespans;
    }

    public double getBenchmarkMakeSpan(String directoryPath) {
        String fileName = directoryPath.substring(directoryPath.indexOf("data-FJSS-") + "data-FJSS-".length());
        fileName = fileName.replace('-', '/');
        if (routingRule != null) {
            fileName = fileName.substring(0, fileName.length() - routingRule.getName().length() - 1);
        }
        fileName = fileName + ".fjs";

        return benchmarkMakespans.getOrDefault(fileName, -1);
    }

    public Double[] GetFitnesses(String fileName) {
        BufferedReader br;
        List<Double> bestFitnesses = new ArrayList<>();
        try {
            br = new BufferedReader(new FileReader(fileName));
            String sCurrentLine;
            //may be multiple fitnesses per generation if numpops > 1
            Double[] fitnesses = new Double[numPops]; //should be reset every generation
            int numFound = 0;
            while ((sCurrentLine = br.readLine()) != null) {
                if (sCurrentLine.startsWith("Fitness")) {
                    //line should be in format "Fitness: [0.8386540120793787]"
                    sCurrentLine = sCurrentLine.substring(sCurrentLine.indexOf("[") + 1, sCurrentLine.length() - 1);
                    fitnesses[numFound] = Double.parseDouble(sCurrentLine);
                    numFound++;
                }
                if (numFound == numPops) {
                    //quickly sort the fitnesses - only want lower one (best)
                    Double best = fitnesses[0];
                    if (fitnesses.length == 2) {
                        if (fitnesses[1] < best) {
                            best = fitnesses[1];
                        }
                    }
                    bestFitnesses.add(best);
                    //reset
                    fitnesses = new Double[numPops];
                    numFound = 0;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bestFitnesses.toArray(new Double[0]);
    }

    public void createResultFile(String directoryPath, HashMap<Integer, Double[]> makespanMap) {
        String outputFileName;
        if (isStatic) {
            outputFileName = directoryPath.substring(dataPath.length() + 1 + "data-".length()) + ".csv";
        } else {
            outputFileName = directoryPath.substring(directoryPath.lastIndexOf("/") + 1) + ".csv";
        }
        String CSVFile = outPath + "/" + outputFileName;

        try (FileWriter writer = new FileWriter(CSVFile)) {
            //add header first
            List<String> headers = new ArrayList<>();
            //expecting the same number of generations for all seeds, so just get any value
            Double[] entry = makespanMap.get(makespanMap.keySet().iterator().next());
            for (int i = 0; i < entry.length - 1; ++i) {
                headers.add("Gen" + i);
            }
            headers.add("Best");
            writeLine(writer, headers);

            for (Integer i : makespanMap.keySet()) {
                List<String> makespanCSV = new ArrayList<>();
                StringBuilder makeSpansString = new StringBuilder();
                Double[] makespans = makespanMap.get(i);
                for (Double makespan : makespans) {
                    makeSpansString.append(makespan.toString()).append(",");
                }
                makespanCSV.add(makeSpansString.substring(0, makeSpansString.length() - 1));
                writeLine(writer, makespanCSV);
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
