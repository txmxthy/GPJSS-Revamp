package simulation.rules.rule.operation.composite;

import simulation.definition.OperationOption;
import simulation.definition.WorkCenter;
import simulation.definition.logic.state.SystemState;
import simulation.rules.rule.AbstractRule;
import simulation.rules.rule.RuleType;

/**
 * Created by YiMei on 27/09/16.
 */
public class PTplusPW extends AbstractRule {

    public PTplusPW(RuleType t) {
        name = "\"SPT+PW\"";
        this.type = t;
    }

    @Override
    public double priority(OperationOption op, WorkCenter workCenter, SystemState systemState) {
        return op.getProcTime() - op.getReadyTime();
    }
}
