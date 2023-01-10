package simulation.rules.rule.operation.composite;

import simulation.jss.jobshop.OperationOption;
import simulation.jss.jobshop.WorkCenter;
import simulation.rules.rule.AbstractRule;
import simulation.rules.rule.RuleType;
import simulation.jss.simulation.state.SystemState;

import java.util.List;

/**
 * Created by YiMei on 28/09/16.
 */
public class ATC extends AbstractRule {
    private double k;
    private double b;

    public ATC(RuleType type,double k, double b) {
        name = "ATC";
        this.type = type;
        setK(k);
        setB(b);
    }

    public ATC(RuleType type) {
        this(type,3, 2);
    }

    @Override
    public String getName() {
        return "\"" + name + "(k=" + getK() + ",b=" + getB() + ")\"";
    }

    public void setK(double k) {
        this.k = k;
    }

    public void setB(double b) {
        this.b = b;
    }

    public double getK() {
        return k;
    }

    public double getB() {
        return b;
    }

    protected double slackNorm;
    protected double expWaitingTime;

    public void calcSlackNorm(OperationOption op, WorkCenter workCenter, SystemState systemState) {
        slackNorm = 0.0d;

        // Get the queue that the processing is in
        List<OperationOption> queue = workCenter.getQueue();
        for (OperationOption o : queue) {
            slackNorm += o.getProcTime();
        }

        slackNorm = (slackNorm / queue.size()) * k;
    }

    public void calcExpWaitingTime(OperationOption op) {
        expWaitingTime = b * (op.getWorkRemaining() - op.getProcTime());
    }

    @Override
    public double priority(OperationOption op, WorkCenter workCenter, SystemState systemState) {
        calcSlackNorm(op, workCenter, systemState);
        calcExpWaitingTime(op);

        double slack = systemState.slack(op);
        slack -= expWaitingTime;
        double prod1 = -Math.max(slack, 0.0d) / slackNorm;

        return -(1.0 / op.getProcTime()) * Math.exp(prod1);
    }
}
