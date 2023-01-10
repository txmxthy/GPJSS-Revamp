package simulation.rules.rule.workcenter.basic;

import simulation.definition.OperationOption;
import simulation.definition.WorkCenter;
import simulation.definition.logic.state.SystemState;
import simulation.rules.rule.AbstractRule;
import simulation.rules.rule.RuleType;

/**
 * Created by dyska on 6/06/17.
 * Shortest ready time.
 * Priority of this rule should be the ready time of the workCenter, which is the least ready time of
 * all machines in the workCenter.
 */
public class SRT extends AbstractRule {

    public SRT(RuleType t) {
        name = "\"SRT\"";
    }


    @Override
    public double priority(OperationOption op, WorkCenter workCenter, SystemState systemState) {
        return workCenter.getReadyTime();
    }
}
