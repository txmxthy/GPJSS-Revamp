package simulation.definition.logic.event;

import simulation.definition.Process;
import simulation.definition.WorkCenter;
import simulation.definition.logic.RoutingDecisionSituation;
import simulation.definition.logic.SequencingDecisionSituation;
import simulation.definition.logic.Simulation;

import java.util.List;

/**
 * Created by YiMei on 25/09/16.
 */
public class ProcessStartEvent extends AbstractEvent {

    private final Process process;

    public ProcessStartEvent(double time, Process process) {
        super(time);
        this.process = process;
    }

    public ProcessStartEvent(Process process) {
        this(process.getStartTime(), process);
    }

    public Process getProcess() {
        return process;
    }

    @Override
    public void trigger(Simulation simulation) {
        WorkCenter workCenter = process.getWorkCenter();
        workCenter.setMachineReadyTime(
                process.getMachineId(), process.getFinishTime());
        workCenter.incrementBusyTime(process.getDuration());

        simulation.addEvent(
                new ProcessFinishEvent(process.getFinishTime(), process));
    }

    @Override
    public void addSequencingDecisionSituation(Simulation simulation,
                                               List<SequencingDecisionSituation> situations,
                                               int minQueueLength) {
        trigger(simulation);
    }

    @Override
    public void addRoutingDecisionSituation(Simulation simulation,
                                            List<RoutingDecisionSituation> situations,
                                            int minQueueLength) {
        trigger(simulation);
    }

    @Override
    public String toString() {
        return String.format("%.1f: job %d op %d started on work center %d.\n",
                time,
                process.getOperationOption().getJob().getId(),
                process.getOperationOption().getOperation().getId(),
                process.getWorkCenter().getId());
    }

    @Override
    public int compareTo(AbstractEvent other) {
        if (time < other.time)
            return -1;

        if (time > other.time)
            return 1;

        if (other instanceof ProcessStartEvent)
            return 0;

        if (other instanceof ProcessFinishEvent)
            return -1;

        return 1;
    }
}
