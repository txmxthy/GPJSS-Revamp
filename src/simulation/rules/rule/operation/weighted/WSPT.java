package simulation.rules.rule.operation.weighted;

import simulation.jss.jobshop.OperationOption;
import simulation.jss.jobshop.WorkCenter;
import simulation.rules.rule.AbstractRule;
import simulation.rules.rule.RuleType;
import simulation.jss.simulation.state.SystemState;

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
