package simulation.rules.rule.operation.basic;

import simulation.definition.OperationOption;
import simulation.definition.WorkCenter;
import simulation.definition.logic.state.SystemState;
import simulation.rules.rule.AbstractRule;
import simulation.rules.rule.RuleType;

/**
 * The LPT (longest processing time) rule.
 * <p>
 * Created by YiMei on 4/10/16.
 */
public class LPT extends AbstractRule {

    public LPT(RuleType type) {
        name = "\"LPT\"";
        this.type = type;
    }

    @Override
    public double priority(OperationOption op, WorkCenter workCenter, SystemState systemState) {
        return -op.getProcTime();
    }

}
