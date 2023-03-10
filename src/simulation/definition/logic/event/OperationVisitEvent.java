package simulation.definition.logic.event;

import simulation.definition.Machine;
import simulation.definition.OperationOption;
import simulation.definition.Process;
import simulation.definition.WorkCenter;
import simulation.definition.logic.RoutingDecisionSituation;
import simulation.definition.logic.SequencingDecisionSituation;
import simulation.definition.logic.Simulation;

import java.util.List;

/**
 * Created by YiMei on 25/09/16.
 */
public class OperationVisitEvent extends AbstractEvent {

    private final OperationOption operationOption;

    public OperationVisitEvent(double time, OperationOption operationOption) {
        super(time);
        this.operationOption = operationOption;
    }

    public OperationVisitEvent(OperationOption operation) {
        this(operation.getReadyTime(), operation);
    }

    @Override
    public void trigger(Simulation simulation) {
        operationOption.setReadyTime(time);

        WorkCenter workCenter = operationOption.getWorkCenter();
        Machine earliestMachine = workCenter.earliestReadyMachine();
        Process p = new Process(workCenter, earliestMachine.getId(), operationOption, time);

        if (earliestMachine.getReadyTime() > time || !simulation.canAddToQueue(p)) {
            workCenter.addToQueue(operationOption);
        } else {
            simulation.addEvent(new ProcessStartEvent(p));
        }
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
        return String.format("%.1f: job %d op %d visits.\n",
                time, operationOption.getJob().getId(), operationOption.getOperation().getId());
    }

    @Override
    public int compareTo(AbstractEvent other) {
        if (time < other.time)
            return -1;

        if (time > other.time)
            return 1;

        if (other instanceof JobArrivalEvent)
            return 1;

        if (other instanceof OperationVisitEvent)
            return 0;

        return -1;
    }

    public OperationOption getOperationOption() {
        return operationOption;
    }
}
