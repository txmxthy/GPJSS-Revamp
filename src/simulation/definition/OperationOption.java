package simulation.definition;

import simulation.definition.logic.state.SystemState;
import simulation.rules.rule.AbstractRule;

import java.util.Objects;

/**
 * Created by dyska on 7/05/17.
 * <p>
 * An operation contains one or more operation options.
 */
public class OperationOption implements Comparable<OperationOption> {

    private final Operation operation;
    private final int optionId;
    private final double procTime;
    private final WorkCenter workCenter;

    // Attributes for simulation.
    private double readyTime;
    private double workRemaining;
    private int numOpsRemaining;
    private double flowDueDate;
    private double nextProcTime;
    private double priority;

    public OperationOption(Operation operation, int optionId, double procTime, WorkCenter workCenter) {
        this.operation = operation;
        this.optionId = optionId;
        this.procTime = procTime;
        this.workCenter = workCenter;
    }

    public Operation getOperation() {
        return operation;
    }

    public int getOptionId() {
        return optionId;
    }

    public double getProcTime() {
        return procTime;
    }

    public WorkCenter getWorkCenter() {
        return workCenter;
    }

    public Operation getNext() {
        return operation.getNext();
    }

    public OperationOption getNext(SystemState systemState, AbstractRule routingRule) {
        Operation nextOperation = operation.getNext();
        if (nextOperation != null) {
            return nextOperation.chooseOperationOption(systemState, routingRule);
        }
        return null;
    }

    public double getReadyTime() {
        return readyTime;
    }

    public void setReadyTime(double readyTime) {
        this.readyTime = readyTime;
    }

    public Job getJob() {
        return operation.getJob();
    }

    public double getWorkRemaining() {
        return workRemaining;
    }

    public void setWorkRemaining(double workRemaining) {
        this.workRemaining = workRemaining;
    }

    public int getNumOpsRemaining() {
        return numOpsRemaining;
    }

    public void setNumOpsRemaining(int numOpsRemaining) {
        this.numOpsRemaining = numOpsRemaining;
    }

    public double getFlowDueDate() {
        return flowDueDate;
    }

    public void setFlowDueDate(double flowDueDate) {
        this.flowDueDate = flowDueDate;
    }

    public double getNextProcTime() {
        return nextProcTime;
    }

    public void setNextProcTime(double nextProcTime) {
        this.nextProcTime = nextProcTime;
    }

    public double getPriority() {
        return priority;
    }

    public void setPriority(double priority) {
        this.priority = priority;
    }

    /**
     * Compare with another process based on priority.
     *
     * @param other the other process.
     * @return true if prior to other, and false otherwise.
     */
    //that is to say, the larger the better
    //fzhang 2018.11.7  prefer the highest priority value is easy to handle.
/*    public boolean priorTo(OperationOption other) {
        if (Double.compare(priority, other.priority) < 0)
            return false;

        if (Double.compare(priority, other.priority) > 0)
            return true;

        //the default setting, when the priority is the same, choose the operation that comes first
        return operation.getJob().getId() < other.operation.getJob().getId();
    }*/

    //that is to say, the smaller the better
    public boolean priorTo(OperationOption other) {
        if (Double.compare(priority, other.priority) < 0)
            return true;

        if (Double.compare(priority, other.priority) > 0)
            return false;

        return operation.getJob().getId() < other.operation.getJob().getId();
    }

    @Override
    public String toString() {
        return String.format("[J%d O%d-%d, W%d, T%.1f]",
                getJob().getId(), operation.getId(), optionId, workCenter.getId(), procTime);
    }

    public boolean equals(OperationOption other) {
        return optionId == other.optionId && operation.getId() == other.operation.getId();
    }

    @Override
    public int compareTo(OperationOption other) {
        return Double.compare(readyTime, other.readyTime);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OperationOption option = (OperationOption) o;
        if (operation != option.operation) return false;
        if (optionId != option.optionId) return false;
        if (Double.compare(option.procTime, procTime) != 0) return false;
        if (Double.compare(option.readyTime, readyTime) != 0) return false;
        if (Double.compare(option.workRemaining, workRemaining) != 0) return false;
        if (numOpsRemaining != option.numOpsRemaining) return false;
        if (Double.compare(option.flowDueDate, flowDueDate) != 0) return false;
        if (Double.compare(option.nextProcTime, nextProcTime) != 0) return false;
        if (Double.compare(option.priority, priority) != 0) return false;
        return Objects.equals(workCenter, option.workCenter);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = operation != null ? operation.hashCode() : 0;
        result = 31 * result + optionId;
        temp = Double.doubleToLongBits(procTime);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (workCenter != null ? workCenter.hashCode() : 0);
        temp = Double.doubleToLongBits(readyTime);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(workRemaining);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + numOpsRemaining;
        temp = Double.doubleToLongBits(flowDueDate);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(nextProcTime);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(priority);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
