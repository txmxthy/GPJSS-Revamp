package simulation.rules.rule.operation.composite;

import simulation.definition.OperationOption;
import simulation.definition.WorkCenter;
import simulation.definition.logic.state.SystemState;
import simulation.rules.rule.AbstractRule;
import simulation.rules.rule.RuleType;

/**
 * Created by yimei on 5/12/16.
 */
public class SlackperRPTplusPT extends AbstractRule {

    public SlackperRPTplusPT(RuleType t) {
        name = "\"Slack/RPT+SPT\"";
        this.type = t;
    }

    @Override
    public double priority(OperationOption op, WorkCenter workCenter, SystemState systemState) {
        double slack = op.getJob().getDueDate() - systemState.getClockTime() - op.getWorkRemaining();

        return slack / op.getWorkRemaining() + op.getProcTime();
    }
}
