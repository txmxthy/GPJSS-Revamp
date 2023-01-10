package simulation.jss.gp;

import ec.EvolutionState;
import ec.Individual;
import ec.Problem;
import ec.simple.SimpleProblemForm;
import simulation.jss.jobshop.OperationOption;
import simulation.jss.jobshop.WorkCenter;
import simulation.jss.simulation.state.SystemState;

/**
 * Created by YiMei on 27/09/16.
 */
public class CalcPriorityProblem extends Problem implements SimpleProblemForm {

    private OperationOption operation;
    private WorkCenter workCenter;
    private SystemState systemState;

    public CalcPriorityProblem(OperationOption operation,
                               WorkCenter workCenter,
                               SystemState systemState) {
        this.operation = operation;
        this.workCenter = workCenter;
        this.systemState = systemState;
    }
    public OperationOption getOperation() {
        return operation;
    }

    public WorkCenter getWorkCenter() {
        return workCenter;
    }

    public SystemState getSystemState() {
        return systemState;
    }
    @Override
    public void evaluate(EvolutionState state, Individual ind,
                         int subpopulation, int threadnum) {
    }
	@Override
	public void normObjective(EvolutionState state, Individual ind,
			                  int subpopulation, int threadnum) {
		// TODO Auto-generated method stub

	}
}