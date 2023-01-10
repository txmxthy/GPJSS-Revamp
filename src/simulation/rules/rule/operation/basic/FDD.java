package simulation.rules.rule.operation.basic;

import simulation.definition.OperationOption;
import simulation.definition.WorkCenter;
import simulation.definition.logic.state.SystemState;
import simulation.rules.rule.AbstractRule;
import simulation.rules.rule.RuleType;

/**
 * Created by YiMei on 28/09/16.
 */
public class FDD extends AbstractRule {

    public FDD(RuleType type) {
        name = "\"FDD\"";
        this.type = type;
    }

    @Override
    public double priority(OperationOption op, WorkCenter workCenter, SystemState systemState) {
        return op.getFlowDueDate();
    }

}
