package simulation.rules.rule.operation.basic;

import simulation.definition.OperationOption;
import simulation.definition.WorkCenter;
import simulation.definition.logic.state.SystemState;
import simulation.rules.rule.AbstractRule;
import simulation.rules.rule.RuleType;

/**
 * Created by YiMei on 27/09/16.
 */
public class FCFS extends AbstractRule {

    public FCFS(RuleType type) {
        name = "\"FCFS\"";
        this.type = type;
    }

    @Override
    public double priority(OperationOption op, WorkCenter workCenter, SystemState systemState) {
        return op.getReadyTime();
    }
}
