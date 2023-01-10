package simulation.jss.analysis;

import simulation.jss.jobshop.Job;
import simulation.rules.rule.RuleType;
import simulation.rules.rule.operation.basic.FCFS;
import simulation.rules.rule.operation.evolved.GPRule;
import simulation.rules.rule.operation.weighted.WATC;
import simulation.rules.rule.workcenter.basic.WIQ;
import simulation.jss.simulation.DynamicSimulation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by yimei on 8/11/16.
 */
public class SimulationJobAnalysis {

    public static void writeJobsToCSV(DynamicSimulation simulation, File csvFile) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile.getAbsoluteFile()));
            writer.write("Job,NumOps,TotalWork,Weight,ArrivalTime,CompletionTime,DueDate," +
                    "FlowTime,Tardiness,WTardiness");
            writer.newLine();
            for (Job job : simulation.getSystemState().getJobsCompleted()) {
                double flowTime = job.getCompletionTime() - job.getArrivalTime();
                double tardiness = job.getCompletionTime() - job.getDueDate();
                if (tardiness < 0)
                    tardiness = 0;
                double wTardiness = job.getWeight() * tardiness;
                writer.write(job.getId() + "," + job.getOperations().size() + "," +
                        job.getTotalProcTime() + "," +
                        job.getWeight() + "," + job.getArrivalTime() + "," +
                        job.getCompletionTime() + "," + job.getDueDate() + "," +
                        flowTime + "," + tardiness + "," + wTardiness
                );
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        DynamicSimulation simulation1 = DynamicSimulation.standardMissing(72334,
                new WATC(RuleType.SEQUENCING), new WIQ(RuleType.ROUTING), 10, 5000, 0, 0.9, 4.0);
        simulation1.run();
        File csvFile1 = new File("jobs-missing-0.9-4-WATC.csv");
        writeJobsToCSV(simulation1, csvFile1);

        DynamicSimulation simulation2 = DynamicSimulation.standardMissing(72334,
                new FCFS(RuleType.SEQUENCING), new WIQ(RuleType.ROUTING),10, 5000, 0, 0.9, 4.0);
        simulation2.run();
        File csvFile2 = new File("jobs-missing-0.9-4-FCFS.csv");
        writeJobsToCSV(simulation2, csvFile2);

        GPRule rule = GPRule.readFromLispExpression(RuleType.SEQUENCING, "(* (max (- (+ PT (min (+ (+ NOR PT) WINQ) (max NOR SL))) (/ WKR W)) (/ (+ PT (min (/ WINQ NOR) (min (max NPT W) (max NPT SL)))) W)) (/ (+ PT (min (/ WINQ (/ WIQ PT)) (max NPT W))) W))");
        DynamicSimulation simulation3 = DynamicSimulation.standardMissing(72334,
                rule, new WIQ(RuleType.ROUTING),10, 5000, 0, 0.9, 4.0);
        simulation3.run();
        File csvFile3 = new File("jobs-missing-0.9-4-GPRule.csv");
        writeJobsToCSV(simulation3, csvFile3);
    }
}
