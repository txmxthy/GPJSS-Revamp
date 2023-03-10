package simulation.definition;

import java.util.Objects;

/**
 * Created by yimei on 22/09/16.
 */
public class Process implements Comparable<Process> {

    private final WorkCenter workCenter;
    private final int machineId;
    private final OperationOption operationOption;
    private final double startTime;
    private final double finishTime;

    public Process(WorkCenter workCenter, int machineId, OperationOption operationOption, double startTime) {
        this.workCenter = workCenter;
        this.machineId = machineId;
        this.operationOption = operationOption;
        this.startTime = startTime;
        this.finishTime = startTime + operationOption.getProcTime();
    }

    public WorkCenter getWorkCenter() {
        return workCenter;
    }

    public int getMachineId() {
        return machineId;
    }

    public OperationOption getOperationOption() {
        return operationOption;
    }

    public double getStartTime() {
        return startTime;
    }

    public double getFinishTime() {
        return finishTime;
    }

    public double getDuration() {
        return finishTime - startTime;
    }

    @Override
    public String toString() {
        return String.format("([W%d,M%d], [J%d,O%d,O%d]: %.1f --> %.1f.\n",
                workCenter.getId(), machineId, operationOption.getJob().getId(),
                operationOption.getOperation().getId(), operationOption.getOptionId(), startTime, finishTime);
    }

    @Override
    public int compareTo(Process other) {
        return Double.compare(startTime, other.startTime);

    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Process process = (Process) o;

        if (machineId != process.machineId) return false;
        if (Double.compare(process.startTime, startTime) != 0) return false;
        if (Double.compare(process.finishTime, finishTime) != 0) return false;
        if (!Objects.equals(workCenter, process.workCenter)) return false;
        return Objects.equals(operationOption, process.operationOption);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = workCenter != null ? workCenter.hashCode() : 0;
        result = 31 * result + machineId;
        result = 31 * result + (operationOption != null ? operationOption.hashCode() : 0);
        temp = Double.doubleToLongBits(startTime);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(finishTime);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
