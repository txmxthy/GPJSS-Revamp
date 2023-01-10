package simulation.rules.rule.workcenter.basic;

import simulation.definition.OperationOption;
import simulation.definition.WorkCenter;
import simulation.rules.rule.AbstractRule;
import simulation.rules.rule.RuleType;
import simulation.definition.logic.state.SystemState;

/**
 * Created by dyska on 6/06/17.
 * Work in queue.
 * The priority of this method should be the amount of work in the queue.
 */
public class WIQ extends AbstractRule {

    public WIQ(RuleType t) {
        name = "\"WIQ\"";
        this.type = t;
    }

    @Override
    public double priority(OperationOption op, WorkCenter workCenter, SystemState systemState) {
        return workCenter.getWorkInQueue();
    }
}
