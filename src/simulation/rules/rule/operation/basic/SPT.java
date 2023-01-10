package simulation.rules.rule.operation.basic;

import simulation.definition.OperationOption;
import simulation.definition.WorkCenter;
import simulation.rules.rule.AbstractRule;
import simulation.rules.rule.RuleType;
import simulation.definition.logic.state.SystemState;

/**
 * The SPT (shortest processing time) rule.
 * <p>
 * Created by YiMei on 27/09/16.
 */
public class SPT extends AbstractRule {

    public SPT(RuleType type) {
        name = "\"SPT\"";
        this.type = type;
    }

    @Override
    public double priority(OperationOption op, WorkCenter workCenter, SystemState systemState) {
        return op.getProcTime();
    }

}
