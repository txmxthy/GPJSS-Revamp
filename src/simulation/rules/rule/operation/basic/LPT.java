package simulation.rules.rule.operation.basic;

import simulation.jss.jobshop.OperationOption;
import simulation.jss.jobshop.WorkCenter;
import simulation.rules.rule.AbstractRule;
import simulation.rules.rule.RuleType;
import simulation.jss.simulation.state.SystemState;

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
