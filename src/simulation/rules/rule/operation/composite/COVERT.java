package simulation.rules.rule.operation.composite;

import simulation.definition.OperationOption;
import simulation.definition.WorkCenter;
import simulation.definition.logic.state.SystemState;
import simulation.rules.rule.AbstractRule;
import simulation.rules.rule.RuleType;

/**
 * Created by YiMei on 28/09/16.
 */
public class COVERT extends AbstractRule {
    protected double totalExpWaitingTime;
    private double k;
    private double b;

    public COVERT(RuleType type, double k, double b) {
        name = "COVERT";
        this.type = type;
        setK(k);
        setB(b);
    }

    public COVERT(RuleType type) {
        this(type, 2, 2);
    }

    @Override
    public String getName() {
        return "\"" + name + "(k=" + getK() + ",b=" + getB() + ")\"";
    }

    public double getK() {
        return k;
    }

    public void setK(double k) {
        this.k = k;
    }

    public double getB() {
        return b;
    }

    public void setB(double b) {
        this.b = b;
    }

    public void calcTotalExpWaitingTime(OperationOption op) {
        totalExpWaitingTime = b * op.getWorkRemaining();
    }

    @Override
    public double priority(OperationOption op, WorkCenter workCenter, SystemState systemState) {
        calcTotalExpWaitingTime(op);

        double slack = op.getJob().getDueDate() - systemState.getClockTime() - op.getWorkRemaining();

        if (slack < 0)
            slack = 0;

        double priority = 1 - (slack / (k * totalExpWaitingTime));

        if (priority < 0)
            priority = 0;

        return -priority / op.getProcTime();
    }
}
