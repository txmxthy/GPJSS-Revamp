package simulation.rules.rule.workcenter.basic;

import simulation.definition.OperationOption;
import simulation.definition.WorkCenter;
import simulation.rules.rule.AbstractRule;
import simulation.rules.rule.RuleType;
import simulation.definition.logic.state.SystemState;

/**
 * Created by dyska on 6/06/17.
 * Longest busy time.
 * This rule should have as its priority the negative of the busy time of the workCenter.
 * Should always be a non-negative quantity before taking its negative.
 */
public class LBT extends AbstractRule {
    private final RuleType type;

    public LBT(RuleType type) {
        name = "\"LBT\"";
        this.type = type;
    }

    @Override
    public double priority(OperationOption op, WorkCenter workCenter, SystemState systemState) {
        return -workCenter.getBusyTime();
    }


}
