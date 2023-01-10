package simulation.rules.rule.workcenter.basic;

import simulation.definition.OperationOption;
import simulation.definition.WorkCenter;
import simulation.rules.rule.AbstractRule;
import simulation.rules.rule.RuleType;
import simulation.definition.logic.state.SystemState;

/**
 * Created by dyska on 6/06/17.
 * Shortest ready time.
 * Priority of this rule should be the ready time of the workCenter, which is the least ready time of
 * all machines in the workCenter.
 */
public class SRT extends AbstractRule {
    private RuleType type;

    public SRT(RuleType t) {
        name = "\"SRT\"";
        this.type = t;
    }


    @Override
    public double priority(OperationOption op, WorkCenter workCenter, SystemState systemState) {
        return workCenter.getReadyTime();
    }
}
