package simulation.rules.rule.operation.composite;

import simulation.definition.OperationOption;
import simulation.definition.WorkCenter;
import simulation.rules.rule.AbstractRule;
import simulation.rules.rule.RuleType;
import simulation.definition.logic.state.SystemState;

/**
 * Created by YiMei on 27/09/16.
 */
public class CRplusPT extends AbstractRule {

    public CRplusPT(RuleType t) {
        name = "\"CR+PT\"";
        this.type = t;
    }

    @Override
    public double priority(OperationOption op, WorkCenter workCenter, SystemState systemState) {
        return (op.getJob().getDueDate() - systemState.getClockTime())
                / op.getWorkRemaining() + op.getProcTime();
    }
}
