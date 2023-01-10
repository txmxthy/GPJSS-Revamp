package simulation.jss.gp.terminal;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;

/**
 * Created by YiMei on 3/10/16.
 */
public class BuildingBlock extends GPNode {

    private final GPNode root;

    public BuildingBlock(GPNode root) {
        super();
        this.root = (GPNode) root.clone();
    }

    public GPNode getRoot() {
        return root;
    }

    public String toString() {
        return subTreeString(root);
    }

    private String subTreeString(GPNode node) {
        if (node.children.length == 0) {
            return node.toString();
        } else {
            StringBuilder string = new StringBuilder("(" + node);
            for (GPNode child : node.children) {
                string.append(" ").append(subTreeString(child));
            }
            string.append(")");

            return string.toString();
        }
    }

    @Override
    public void eval(EvolutionState state, int thread, GPData input,
                     ADFStack stack, GPIndividual individual, Problem problem) {
        root.eval(state, thread, input, stack, individual, problem);
    }
}
