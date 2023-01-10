package simulation.rules.rule.operation.composite;

import simulation.definition.OperationOption;
import simulation.definition.WorkCenter;
import simulation.definition.logic.state.SystemState;
import simulation.rules.rule.AbstractRule;
import simulation.rules.rule.RuleType;

/**
 * Created by YiMei on 27/09/16.
 */
public class OPFSLKperPT extends AbstractRule {

    public OPFSLKperPT(RuleType t) {
        name = "\"OPFSLK/PT\"";
        this.type = t;
    }

    @Override
    public double priority(OperationOption op, WorkCenter workCenter, SystemState systemState) {
        double value = (systemState.getClockTime() + op.getProcTime()
                - op.getFlowDueDate()) / op.getProcTime();

        if (value < 0)
            value = 0;

        return value;
    }
}
