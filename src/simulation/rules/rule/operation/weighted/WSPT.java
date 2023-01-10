package simulation.rules.rule.operation.weighted;

import simulation.definition.OperationOption;
import simulation.definition.WorkCenter;
import simulation.rules.rule.AbstractRule;
import simulation.rules.rule.RuleType;
import simulation.definition.logic.state.SystemState;

/**
 * Created by YiMei on 27/09/16.
 */
public class WSPT extends AbstractRule {

    public WSPT(RuleType t) {
        name = "\"WSPT\"";
        this.type = t;
    }

    @Override
    public double priority(OperationOption op, WorkCenter workCenter, SystemState systemState) {
        return op.getProcTime() / op.getJob().getWeight();
    }

}
