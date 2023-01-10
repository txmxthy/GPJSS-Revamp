package simulation.rules.rule.workcenter.basic;

import simulation.jss.jobshop.OperationOption;
import simulation.jss.jobshop.WorkCenter;
import simulation.rules.rule.AbstractRule;
import simulation.rules.rule.RuleType;
import simulation.jss.simulation.state.SystemState;

/**
 * Created by dyska on 6/06/17.
 * Shortest busy time.
 * This rule should have a priority of the busy time of the workCenter.
 */
public class SBT extends AbstractRule {
    private RuleType type;

    public SBT(RuleType t) {
        name = "\"SBT\"";
        this.type = t;
    }

    @Override
    public double priority(OperationOption op, WorkCenter workCenter, SystemState systemState) {
        return workCenter.getBusyTime();
    }
}
