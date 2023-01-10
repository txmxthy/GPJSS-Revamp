package simulation.definition.logic.event;

import simulation.definition.logic.RoutingDecisionSituation;
import simulation.definition.logic.SequencingDecisionSituation;
import simulation.definition.logic.Simulation;

import java.util.List;

/**
 * Created by yimei on 22/09/16.
 */
public abstract class AbstractEvent implements Comparable<AbstractEvent> {

    protected final double time;

    public AbstractEvent(double time) {
        this.time = time;
    }

    public double getTime() {
        return time;
    }

    public abstract void trigger(Simulation simulation);

    public abstract void addSequencingDecisionSituation(Simulation simulation,
                                                        List<SequencingDecisionSituation> situations,
                                                        int minQueueLength);

    public abstract void addRoutingDecisionSituation(Simulation simulation,
                                                     List<RoutingDecisionSituation> situations,
                                                     int minOptions);

    @Override
    public int compareTo(AbstractEvent other) {
        return Double.compare(time, other.time);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractEvent that = (AbstractEvent) o;

        return Double.compare(that.time, time) == 0;
    }

    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(time);
        return (int) (temp ^ (temp >>> 32));
    }
}
