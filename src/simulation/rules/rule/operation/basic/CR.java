package simulation.rules.rule.operation.basic;

import simulation.rules.rule.AbstractRule;
import simulation.jss.jobshop.OperationOption;
import simulation.jss.jobshop.WorkCenter;
import simulation.rules.rule.RuleType;
import simulation.jss.simulation.state.SystemState;

/**
 * Created by yimei on 5/12/16.
 */
public class CR extends AbstractRule {

    public CR(RuleType type) {
        name = "\"CR\"";
        this.type = type;
    }

    @Override
    public double priority(OperationOption op, WorkCenter workCenter, SystemState systemState) {
        return (op.getJob().getDueDate() - systemState.getClockTime()) / op.getWorkRemaining();
    }
}
