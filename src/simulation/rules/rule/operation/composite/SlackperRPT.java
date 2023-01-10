package simulation.rules.rule.operation.composite;

import simulation.definition.OperationOption;
import simulation.definition.WorkCenter;
import simulation.rules.rule.AbstractRule;
import simulation.rules.rule.RuleType;
import simulation.definition.logic.state.SystemState;

/**
 * Created by yimei on 5/12/16.
 */
public class SlackperRPT extends AbstractRule {

    public SlackperRPT(RuleType t) {
        name = "\"Slack/RPT\"";
        this.type = t;
    }

    @Override
    public double priority(OperationOption op, WorkCenter workCenter, SystemState systemState) {
        double slack = op.getJob().getDueDate() - systemState.getClockTime() - op.getWorkRemaining();

        return slack / op.getWorkRemaining();
    }
}
