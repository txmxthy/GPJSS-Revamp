package simulation.rules.rule.operation.basic;

import simulation.definition.OperationOption;
import simulation.definition.WorkCenter;
import simulation.definition.logic.state.SystemState;
import simulation.rules.rule.AbstractRule;
import simulation.rules.rule.RuleType;

/**
 * Created by yimei on 5/12/16.
 */
public class Slack extends AbstractRule {

    public Slack(RuleType type) {
        name = "\"Slack\"";
        this.type = type;
    }

    @Override
    public double priority(OperationOption op, WorkCenter workCenter, SystemState systemState) {
        return op.getJob().getDueDate() - systemState.getClockTime() - op.getWorkRemaining();
    }
}
