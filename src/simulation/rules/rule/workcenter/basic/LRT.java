package simulation.rules.rule.workcenter.basic;

import simulation.definition.OperationOption;
import simulation.definition.WorkCenter;
import simulation.rules.rule.AbstractRule;
import simulation.rules.rule.RuleType;
import simulation.definition.logic.state.SystemState;

/**
 * Created by dyska on 6/06/17.
 * Longest ready time.
 * This rule should have a priority of the negative of the workCenter's ready time.
 */
public class LRT extends AbstractRule {
    private final RuleType type;

    public LRT(RuleType t) {
        name = "\"LRT\"";
        this.type = t;
    }


    @Override
    public double priority(OperationOption op, WorkCenter workCenter, SystemState systemState) {
        return - workCenter.getReadyTime();
    }
}
