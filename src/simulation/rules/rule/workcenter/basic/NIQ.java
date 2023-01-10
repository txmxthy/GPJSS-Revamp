package simulation.rules.rule.workcenter.basic;

import simulation.definition.OperationOption;
import simulation.definition.WorkCenter;
import simulation.definition.logic.state.SystemState;
import simulation.rules.rule.AbstractRule;
import simulation.rules.rule.RuleType;

/**
 * Created by dyska on 6/06/17.
 * Number in queue.
 * This (routing) rule should return as the priority the number of operations in the queue of the workCenter
 */
public class NIQ extends AbstractRule {

    public NIQ(RuleType t) {
        name = "\"NIQ\"";
    }


    @Override
    public double priority(OperationOption op, WorkCenter workCenter, SystemState systemState) {
        return workCenter.getNumMachines();
    }
}
