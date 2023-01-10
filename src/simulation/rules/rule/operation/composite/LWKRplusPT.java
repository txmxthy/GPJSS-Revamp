package simulation.rules.rule.operation.composite;

import simulation.definition.OperationOption;
import simulation.definition.WorkCenter;
import simulation.rules.rule.AbstractRule;
import simulation.rules.rule.RuleType;
import simulation.definition.logic.state.SystemState;

/**
 * Created by YiMei on 27/09/16.
 */
public class LWKRplusPT extends AbstractRule {

    public LWKRplusPT(RuleType t) {
        name = "\"LWKR+SPT\"";
        this.type = t;
    }

    @Override
    public double priority(OperationOption op, WorkCenter workCenter, SystemState systemState) {
        return op.getWorkRemaining() + op.getProcTime();
    }
}
