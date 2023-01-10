package simulation.rules.rule.operation.composite;

import simulation.definition.OperationOption;
import simulation.definition.WorkCenter;
import simulation.definition.logic.state.SystemState;
import simulation.rules.rule.AbstractRule;
import simulation.rules.rule.RuleType;

/**
 * Created by yimei on 5/12/16.
 */
public class SlackperOPN extends AbstractRule {

    public SlackperOPN(RuleType t) {
        name = "\"Slack/OPN\"";
        this.type = t;
    }

    @Override
    public double priority(OperationOption op, WorkCenter workCenter, SystemState systemState) {
        double slack = op.getJob().getDueDate() - systemState.getClockTime() - op.getWorkRemaining();

        if (slack > 0) {
            return slack / op.getNumOpsRemaining();
        } else {
            return slack * op.getNumOpsRemaining();
        }
    }
}
