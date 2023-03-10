package simulation.definition;

import simulation.definition.logic.event.ProcessFinishEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A job.
 * <p>
 * Created by yimei on 22/09/16.
 */
public class Job implements Comparable<Job> {

    private final int id;
    private final List<Operation> operations;
    private final List<ProcessFinishEvent> processFinishEvents;
    private final double arrivalTime;
    private final double releaseTime;
    private final double weight;
    private double dueDate;
    private double totalProcTime;
    private double avgProcTime;

    private double completionTime;

    public Job(int id,
               List<Operation> operations,
               double arrivalTime,
               double releaseTime,
               double dueDate,
               double weight) {
        this.id = id;
        this.operations = operations;
        this.arrivalTime = arrivalTime;
        this.releaseTime = releaseTime;
        this.dueDate = dueDate;
        this.weight = weight;
        this.processFinishEvents = new ArrayList<>();
    }

    public Job(int id, List<Operation> operations) {
        this(id, operations,
                0, 0, Double.POSITIVE_INFINITY, 1.0);
    }

    public int getId() {
        return id;
    }

    public List<Operation> getOperations() {
        return operations;
    }

    public List<ProcessFinishEvent> getProcessFinishEvents() {
        return processFinishEvents;
    }

    public void addProcessFinishEvent(ProcessFinishEvent processFinishEvent) {
//        for (ProcessFinishEvent p: processFinishEvents) {
//            if (p.getProcess().getOperationOption().getOperation().getId() ==
//                    processFinishEvent.getProcess().getOperationOption().getOperation().getId()) {
//                System.out.println("Shouldn't happen");
//            }
//        }
        processFinishEvents.add(processFinishEvent);
    }

    public Operation getOperation(int idx) {
        return operations.get(idx);
    }

    public double getArrivalTime() {
        return arrivalTime;
    }

    public double getReleaseTime() {
        return releaseTime;
    }

    public double getDueDate() {
        return dueDate;
    }

    public void setDueDate(double dueDate) {
        this.dueDate = dueDate;
    }

    public double getWeight() {
        return weight;
    }

    public double getTotalProcTime() {
        return totalProcTime; //the time that really eas used to process the jobs
    }

    public double getAvgProcTime() {
        return avgProcTime;
    }

    //fzhang 29.8.2018 get the unprocessingtime (waiting time)
    public double getWaitingTime() {
        return this.flowTime() - totalProcTime;
    }

    public double getCompletionTime() {
        return completionTime; //completionTime: the finish time(a time points)
    }

    public void setCompletionTime(double completionTime) {
        this.completionTime = completionTime;
    }

    public double flowTime() {
        return completionTime - arrivalTime; // the time period between the job arrives and the job is finished. Including the waiting time
    }

    public double weightedFlowTime() {
        return weight * flowTime();
    }

    public double tardiness() {
        double tardiness = completionTime - dueDate;
        if (tardiness < 0)
            tardiness = 0;

        return tardiness;
    }

    public double weightedTardiness() {
        return weight * tardiness();
    }

    public void addOperation(Operation op) {
        operations.add(op);
    }

    public void linkOperations() {
        Operation next = null;
        double nextProcTime = 0.0;

        //double fdd = releaseTime;

//        for (int i = 0; i < operations.size(); i++) {
//            Operation operation = operations.get(i);
//            for (OperationOption option: operation.getOperationOptions()) {
//                option.setFlowDueDate(fdd + option.getProcTime());
//            }
//            fdd += operation.getOperationOption().getProcTime();
//        }

        //play with this - just use average values?
        //or average among the options?

        double workRemaining = 0.0;
        int numOpsRemaining = 0;
        for (int i = operations.size() - 1; i > -1; i--) {
            Operation operation = operations.get(i);

            double medianProcTime;
            //for one operation, it has several options
            double[] procTimes = new double[operation.getOperationOptions().size()];
            //put different processing time in proceTimes[], but now here the value should be the same
            for (int j = 0; j < operation.getOperationOptions().size(); ++j) {
                procTimes[j] = operation.getOperationOptions().get(j).getProcTime();
            }
            Arrays.sort(procTimes);
            //get the median value
            if (procTimes.length % 2 == 0) {
                //halfway between two points, as even number of elements
                medianProcTime = (procTimes[procTimes.length / 2]
                        + procTimes[procTimes.length / 2 - 1]) / 2;
            } else {
                medianProcTime = procTimes[procTimes.length / 2];
            }

            //set every option to the same values
            for (OperationOption option : operation.getOperationOptions()) {

                option.setWorkRemaining(workRemaining + medianProcTime);

                option.setNumOpsRemaining(numOpsRemaining);

                option.setNextProcTime(nextProcTime);
            }

            numOpsRemaining++;
            //workRemaining is a variable for the whole machines, but for one specific machine
            workRemaining += medianProcTime;

            operation.setNext(next);

            next = operation;
            nextProcTime = medianProcTime; //average guess 
            //nextProcTime is the median value of processing time.
            //in flexible job shop scheduling, we have different processing times, but we do not know the next job will
            //be assigned to which machine, so guess a value (use median time as next processing time)
        }
        totalProcTime = workRemaining;
        avgProcTime = totalProcTime / operations.size();
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder(String.format("Job %d, arrives at %.1f, due at %.1f, weight is %.1f. It has %d operations:\n",
                id, arrivalTime, dueDate, weight, operations.size()));
        for (Operation operation : operations) {
            string.append(operation.toString());
        }

        return string.toString();
    }

    public boolean equals(Job other) {
        return id == other.id;
    }

    @Override
    public int compareTo(Job other) {
        return Double.compare(arrivalTime, other.arrivalTime);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Job job = (Job) o;

        if (id != job.id) return false;
        if (Double.compare(job.arrivalTime, arrivalTime) != 0) return false;
        if (Double.compare(job.releaseTime, releaseTime) != 0) return false;
        if (Double.compare(job.dueDate, dueDate) != 0) return false;
        if (Double.compare(job.weight, weight) != 0) return false;
        if (Double.compare(job.totalProcTime, totalProcTime) != 0) return false;
        if (Double.compare(job.avgProcTime, avgProcTime) != 0) return false;
        return Double.compare(job.completionTime, completionTime) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = id;
        result = 31 * result + (operations != null ? operations.hashCode() : 0);
        result = 31 * result + (processFinishEvents != null ? processFinishEvents.hashCode() : 0);
        temp = Double.doubleToLongBits(arrivalTime);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(releaseTime);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(dueDate);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(weight);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(totalProcTime);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(avgProcTime);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(completionTime);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
