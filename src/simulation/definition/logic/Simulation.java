package simulation.definition.logic;

import ec.gp.GPNode;
import simulation.definition.Job;
import simulation.definition.Objective;
import simulation.definition.Process;
import simulation.definition.WorkCenter;
import simulation.definition.logic.event.AbstractEvent;
import simulation.definition.logic.event.ProcessStartEvent;
import simulation.definition.logic.state.SystemState;
import simulation.rules.rule.AbstractRule;
import simulation.rules.rule.operation.evolved.GPRule;

import java.util.Iterator;
import java.util.Objects;
import java.util.PriorityQueue;

/**
 * The abstract simulation class for evaluating rules.
 * <p>
 * Created by yimei on 21/11/16.
 */
public abstract class Simulation {
    protected final SystemState systemState;
    protected final PriorityQueue<AbstractEvent> eventQueue;
    protected final int numWorkCenters;
    protected final int numJobsRecorded;
    protected final int warmupJobs;
    protected AbstractRule sequencingRule;
    protected AbstractRule routingRule;
    protected int numJobsArrived;
    protected int throughput;
    //fzhang 3.6.2018  discard the individual(rule) can not complete the whole jobs well, take a long time (prefer to do part of each job)
    int beforeThroughput; //save the throughput value before updated (a job finished)
    //protected int[] jobStates;
    int afterThroughput; //save the throughput value after updated (a job finished)
    int count = 0;
    public Simulation(AbstractRule sequencingRule,
                      AbstractRule routingRule,
                      int numWorkCenters,
                      int numJobsRecorded,
                      int warmupJobs) {
        this.sequencingRule = sequencingRule;
        this.routingRule = routingRule;
        this.numWorkCenters = numWorkCenters;
        this.numJobsRecorded = numJobsRecorded;
        this.warmupJobs = warmupJobs;

        systemState = new SystemState();
        eventQueue = new PriorityQueue<>();
//        int[] jobStates = new int[numJobsRecorded];
//        fill(jobStates, -1);
//        this.jobStates = jobStates;
    }

    @Override
    public String toString() {
        return "Simulation{" +
                "sequencingRule=" + sequencingRule +
                ", routingRule=" + routingRule +
                ", systemState=" + systemState +
                ", eventQueue=" + eventQueue +
                ", numWorkCenters=" + numWorkCenters +
                ", numJobsRecorded=" + numJobsRecorded +
                ", warmupJobs=" + warmupJobs +
                ", numJobsArrived=" + numJobsArrived +
                ", throughput=" + throughput +
                '}';
    }

    public AbstractRule getSequencingRule() {
        return sequencingRule;
    }

//    public int[] getJobStates() { return jobStates; }

    public void setSequencingRule(AbstractRule sequencingRule) {
        this.sequencingRule = sequencingRule;
    }

    public AbstractRule getRoutingRule() {
        return routingRule;
    }

    public void setRoutingRule(AbstractRule routingRule) {
        this.routingRule = routingRule;
        //need to reset state as well, as the operationoptions associated
        //with workcenters are chosen using this routing rule, so current
        //values are outdated
        resetState();
    }

    public SystemState getSystemState() {
        return systemState;
    }

//    public void setJobStates(int[] jobStates) { this.jobStates = jobStates; }

    public PriorityQueue<AbstractEvent> getEventQueue() {
        return eventQueue;
    }

    public double getClockTime() {
        return systemState.getClockTime();
    }

    public void addEvent(AbstractEvent event) {
        eventQueue.add(event);
    }

    public boolean canAddToQueue(Process process) {
        Iterator<AbstractEvent> e = eventQueue.iterator();
        if (e.hasNext()) {
            AbstractEvent a = e.next();
            if (a instanceof ProcessStartEvent) {
                return ((ProcessStartEvent) a).getProcess().getWorkCenter().getId() != process.getWorkCenter().getId();
            }
        }
        return true;
    }

    //int countBadrun =0;
    public void run() {
        while (!eventQueue.isEmpty() && throughput < numJobsRecorded) { //numJobsRecorded == 5000
            AbstractEvent nextEvent = eventQueue.poll(); // the head of this queue, or null if this queue is empty

//            System.out.println("EventQueue's size: " + eventQueue.size());
            //fzhang 3.6.2018  fix the stuck problem
            beforeThroughput = throughput; //save the throughput value before updated (a job finished)

            systemState.setClockTime(nextEvent.getTime());
            nextEvent.trigger(this); //nextEvent includes many different types of events

            afterThroughput = throughput; //save the throughput value after updated (a job finished)

            if (throughput > warmupJobs & afterThroughput - beforeThroughput == 0) { //if the value was not updated
                count++;
            }

            //System.out.println("count "+count);
            if (count > 100000) {
                count = 0;
                systemState.setClockTime(Double.MAX_VALUE);
                eventQueue.clear();
            }

            //===================ignore busy machine here==============================
            //when nextEvent was done, check the numOpsInQueue
            for (WorkCenter w : systemState.getWorkCenters()) {
                if (w.numOpsInQueue() > 100) {
                    systemState.setClockTime(Double.MAX_VALUE);
                    eventQueue.clear();
                    //countBadrun++;
                }
            }
        }
        //modified by fzhang 18.04.2018
        /*if(countBadrun>0) {
        	 System.out.println("The number of badrun grasped in simulation: "+ countBadrun);
         }*/

        if (!systemState.getJobsInSystem().isEmpty() && !(this instanceof DynamicSimulation)) {
            System.out.println("Event queue is empty but simulation is not complete.");
            System.out.println("Makespan is garbage - cannot continue.");
            System.exit(0);
        }
    }

//    private boolean eventIsDuplicate(AbstractEvent event) {
//        if (event instanceof ProcessFinishEvent) {
//            Process p = ((ProcessFinishEvent) event).getProcess();
//            //want to check whether this operation has already been performed
//            int jobId = p.getOperationOption().getJob().getId();
//            if (jobId >= 0) {
//                int jobState = jobStates[jobId];
//                int opNum = p.getOperationOption().getOperation().getId();
//                if ((jobState+1) != opNum) {
//                    //upcoming event should only be the next job in the sequence,
//                    //not a job we've already done, or one ahead of the next one
//                    return true;
//                }
//            }
//        }
//        return false;
//    }

    public void rerun() {
        //original
        //fzhang 2018.11.5 this is used for generate different instances in a generation.
        //if the replications is 1, does not have influence
        resetState();

        //reset(): reset seed value, will get the same instance
        //reset();
        run();
    }

    public void completeJob(Job job) {
        if (numJobsArrived > warmupJobs && job.getId() >= 0
                && job.getId() < numJobsRecorded + warmupJobs) {
            throughput++;  //before only have this line

            count = 0;


            systemState.addCompletedJob(job);

//            int a = systemState.getJobsCompleted().size();
//            System.out.println("The number of completed jobs: "+systemState.getJobsCompleted().size());
        }
        systemState.removeJobFromSystem(job);
    }

    public double makespan() {
        double value = 0.0;
        for (Job job : systemState.getJobsCompleted()) {
            double tmp = job.getCompletionTime();
            if (value < tmp)
                value = tmp;
        }

        return value;
    }

    public double meanFlowtime() {
        double value = 0.0;
        for (Job job : systemState.getJobsCompleted()) {
            value += job.flowTime();
        }

        return value / numJobsRecorded;
    }

    public double maxFlowtime() {
        double value = 0.0;
        for (Job job : systemState.getJobsCompleted()) {
            double tmp = job.flowTime();
            if (value < tmp)
                value = tmp;
        }

        return value;
    }

    public double meanWeightedFlowtime() {
        double value = 0.0;
        for (Job job : systemState.getJobsCompleted()) {
            value += job.weightedFlowTime();
        }

        return value / numJobsRecorded;
    }

    public double maxWeightedFlowtime() {
        double value = 0.0;
        for (Job job : systemState.getJobsCompleted()) {
            double tmp = job.weightedFlowTime();
            if (value < tmp)
                value = tmp;
        }

        return value;
    }

    public double meanTardiness() {
        double value = 0.0;
        for (Job job : systemState.getJobsCompleted()) {
            value += job.tardiness();
        }

        return value / numJobsRecorded;
    }

    public double maxTardiness() {
        double value = 0.0;
        for (Job job : systemState.getJobsCompleted()) {
            double tmp = job.tardiness();

            if (value < tmp)
                value = tmp;
        }

        return value;
    }

    public double meanWeightedTardiness() {
        double value = 0.0;
        for (Job job : systemState.getJobsCompleted()) {
            value += job.weightedTardiness();
        }

        return value / numJobsRecorded;
    }

    public double maxWeightedTardiness() {
        double value = 0.0;
        for (Job job : systemState.getJobsCompleted()) {
            double tmp = job.weightedTardiness();

            if (value < tmp)
                value = tmp;
        }

        return value;
    }

    public double propTardyJobs() {
        double value = 0.0;
        for (Job job : systemState.getJobsCompleted()) {
            if (job.getCompletionTime() > job.getDueDate())
                value++;
        }

        return value / numJobsRecorded;
    }

    //2018.12.20 define rule size as an objective
    public int rulesize() {
        int value;
        GPRule seqRule;
        GPRule routRule;

        seqRule = (GPRule) this.getSequencingRule();
        routRule = (GPRule) this.getRoutingRule();
        int seqRuleSize = seqRule.getGPTree().child.numNodes(GPNode.NODESEARCH_ALL);
        int routRuleSize = routRule.getGPTree().child.numNodes(GPNode.NODESEARCH_ALL);

        value = seqRuleSize + routRuleSize;
    	/*System.out.println("==========================");
    	System.out.println("RuleSize "+value);*/
        return value;
    }

    //2019.2.26 define routing rule size as an objective
    public int rulesizeR() {
        int value;
        GPRule routRule;
        routRule = (GPRule) this.getRoutingRule();
        //    	System.out.println("routRuleSize "+routRuleSize);
        value = routRule.getGPTree().child.numNodes(GPNode.NODESEARCH_ALL);
        return value;
    }

    public int rulesizeS() {
        int value;
        GPRule seqRule;
        seqRule = (GPRule) this.getSequencingRule();
        //    	System.out.println("seqRuleSize "+seqRuleSize);
        value = seqRule.getGPTree().child.numNodes(GPNode.NODESEARCH_ALL);
        return value;
    }

    public double objectiveValue(Objective objective) {
        switch (objective) {
            case MAKESPAN:
                return makespan();
            case MEAN_FLOWTIME:
                return meanFlowtime();
            case MAX_FLOWTIME:
                return maxFlowtime();
            case MEAN_WEIGHTED_FLOWTIME:
                return meanWeightedFlowtime();
            case MAX_WEIGHTED_FLOWTIME:
                return maxWeightedFlowtime();
            case MEAN_TARDINESS:
                return meanTardiness();
            case MAX_TARDINESS:
                return maxTardiness();
            case MEAN_WEIGHTED_TARDINESS:
                return meanWeightedTardiness();
            case MAX_WEIGHTED_TARDINESS:
                return maxWeightedTardiness();
            case PROP_TARDY_JOBS:
                return propTardyJobs();
            case RULESIZE:
                return rulesize();
            case RULESIZER:
                return rulesizeR();
            case RULESIZES:
                return rulesizeS();
        }

        return -1.0;
    }

    public double workCenterUtilLevel(int idx) {
        return systemState.getWorkCenter(idx).getBusyTime() / getClockTime();
    }

    public String workCenterUtilLevelsToString() {
        StringBuilder string = new StringBuilder("[");
        for (int i = 0; i < systemState.getWorkCenters().size(); i++) {
            string.append(String.format("%.3f ", workCenterUtilLevel(i)));
        }
        string.append("]");

        return string.toString();
    }

    public abstract void setup();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Simulation that = (Simulation) o;

        if (numWorkCenters != that.numWorkCenters) return false;
        if (numJobsRecorded != that.numJobsRecorded) return false;
        if (warmupJobs != that.warmupJobs) return false;
        if (numJobsArrived != that.numJobsArrived) return false;
        if (throughput != that.throughput) return false;
        if (!Objects.equals(sequencingRule, that.sequencingRule))
            return false;
        if (!Objects.equals(routingRule, that.routingRule)) return false;
        if (!Objects.equals(systemState, that.systemState)) return false;
        return Objects.equals(eventQueue, that.eventQueue);
    }

    @Override
    public int hashCode() {
        int result = sequencingRule != null ? sequencingRule.hashCode() : 0;
        result = 31 * result + (routingRule != null ? routingRule.hashCode() : 0);
        result = 31 * result + systemState.hashCode();
        result = 31 * result + eventQueue.hashCode();
        result = 31 * result + numWorkCenters;
        result = 31 * result + numJobsRecorded;
        result = 31 * result + warmupJobs;
        result = 31 * result + numJobsArrived;
        result = 31 * result + throughput;
        return result;
    }

    public abstract void resetState();

    public abstract void reset();

    public abstract void rotateSeed();

    public abstract void generateJob();

    public abstract Simulation surrogate(int numWorkCenters, int numJobsRecorded,
                                         int warmupJobs);

    public abstract Simulation surrogateBusy(int numWorkCenters, int numJobsRecorded,
                                             int warmupJobs);
}