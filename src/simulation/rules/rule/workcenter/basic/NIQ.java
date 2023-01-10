package simulation.rules.rule.workcenter.basic;

import simulation.jss.jobshop.OperationOption;
import simulation.jss.jobshop.WorkCenter;
import simulation.rules.rule.AbstractRule;
import simulation.rules.rule.RuleType;
import simulation.jss.simulation.state.SystemState;

/**
 * Created by dyska on 6/06/17.
 * Number in queue.
 * This (routing) rule should return as the priority the number of operations in the queue of the workCenter
 */
public class NIQ extends AbstractRule {
    private RuleType type;

    public NIQ(RuleType t) {
        name = "\"NIQ\"";
        this.type = t;
    }


    @Override
    public double priority(OperationOption op, WorkCenter workCenter, SystemState systemState) {
        return workCenter.getNumMachines();
    }
}
