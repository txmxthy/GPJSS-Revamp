package simulation.rules.rule.operation.weighted;

import simulation.definition.OperationOption;
import simulation.definition.WorkCenter;
import simulation.definition.logic.state.SystemState;
import simulation.rules.rule.RuleType;
import simulation.rules.rule.operation.composite.ATC;

/**
 * Created by YiMei on 27/09/16.
 */
public class WATC extends ATC {

    private double k;
    private double b;

    public WATC(RuleType t, double k, double b) {
        super(t, k, b);
        name = "WATC";
        setK(k);
        setB(b);
    }

    public WATC(RuleType t) {
        this(t, 3, 2);
    }

    @Override
    public double priority(OperationOption op, WorkCenter workCenter, SystemState systemState) {
        calcSlackNorm(op, workCenter, systemState);
        calcExpWaitingTime(op);

        double slack = systemState.slack(op);
        slack -= expWaitingTime;
        double prod1 = -Math.max(slack, 0.0d) / slackNorm;

        return -(op.getJob().getWeight() / op.getProcTime()) * Math.exp(prod1);
    }
}
