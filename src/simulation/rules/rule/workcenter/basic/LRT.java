package simulation.rules.rule.workcenter.basic;

import simulation.definition.OperationOption;
import simulation.definition.WorkCenter;
import simulation.definition.logic.state.SystemState;
import simulation.rules.rule.AbstractRule;
import simulation.rules.rule.RuleType;

/**
 * Created by dyska on 6/06/17.
 * Longest ready time.
 * This rule should have a priority of the negative of the workCenter's ready time.
 */
public class LRT extends AbstractRule {

    public LRT(RuleType t) {
        name = "\"LRT\"";
    }


    @Override
    public double priority(OperationOption op, WorkCenter workCenter, SystemState systemState) {
        return -workCenter.getReadyTime();
    }
}
