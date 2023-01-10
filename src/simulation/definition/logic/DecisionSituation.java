package simulation.definition.logic;

import simulation.definition.OperationOption;
import simulation.definition.logic.state.SystemState;

import java.util.List;

/**
 * Created by dyska on 22/09/17.
 */
public abstract class DecisionSituation {

    private List<OperationOption> queue = null;
    private SystemState systemState = null;

    public List<OperationOption> getQueue() {
        return queue;
    }

    public SystemState getSystemState() {
        return systemState;
    }
}
