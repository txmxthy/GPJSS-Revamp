package simulation.rules.rule.workcenter.basic;

import simulation.definition.OperationOption;
import simulation.definition.WorkCenter;
import simulation.definition.logic.state.SystemState;
import simulation.rules.rule.AbstractRule;
import simulation.rules.rule.RuleType;

/**
 * Created by dyska on 6/06/17.
 * Shortest busy time.
 * This rule should have a priority of the busy time of the workCenter.
 */
public class SBT extends AbstractRule {

    public SBT(RuleType t) {
        name = "\"SBT\"";
    }

    @Override
    public double priority(OperationOption op, WorkCenter workCenter, SystemState systemState) {
        return workCenter.getBusyTime();
    }
}
