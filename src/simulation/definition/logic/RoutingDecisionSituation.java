package simulation.definition.logic;

import simulation.definition.OperationOption;
import simulation.definition.logic.state.SystemState;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dyska on 7/09/17.
 */
public class RoutingDecisionSituation extends DecisionSituation {

    private final List<OperationOption> queue;
    private final SystemState systemState;

    public RoutingDecisionSituation(List<OperationOption> operationOptions, SystemState systemState) {
        this.queue = operationOptions;
        this.systemState = systemState;
    }

    public List<OperationOption> getQueue() {
        return queue;
    }

    public SystemState getSystemState() {
        return systemState;
    }

    public RoutingDecisionSituation clone() {
        RoutingDecisionSituation routingDecisionSituation = (RoutingDecisionSituation) clone();
        List<OperationOption> clonedQ = new ArrayList<>(queue);
        SystemState clonedState = systemState.clone();

        return new RoutingDecisionSituation(clonedQ, clonedState);
    }
}
