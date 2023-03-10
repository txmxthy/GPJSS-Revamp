package simulation.rules.rule.operation.evolved;

import ec.gp.GPNode;
import ec.gp.GPTree;
import simulation.definition.OperationOption;
import simulation.definition.WorkCenter;
import simulation.definition.logic.state.SystemState;
import simulation.jss.feature.ignore.Ignorer;
import solvers.gp.CalcPriorityProblem;
import solvers.gp.GPNodeComparator;
import solvers.gp.data.DoubleData;
import simulation.rules.rule.AbstractRule;
import simulation.rules.rule.RuleType;
import simulation.util.lisp.LispParser;

/**
 * The GP-evolved rule.
 * <p>
 * Created by YiMei on 27/09/16.
 */
public class GPRule extends AbstractRule {

    private GPTree gpTree;
    private String lispString;

    public GPRule(RuleType t, GPTree gpTree) {
        name = "\"GPRule\"";
        this.gpTree = gpTree;
        type = t;
    }

    public GPRule(RuleType t, GPTree gpTree, String expression) {
        name = "\"GPRule\"";
        this.lispString = expression;
        this.gpTree = gpTree;
        this.type = t;
    }

    public static GPRule readFromLispExpression(RuleType type, String expression) {
        GPTree tree = LispParser.parseJobShopRule(expression);

        return new GPRule(type, tree, expression);
    }

    public GPTree getGPTree() {
        return gpTree;
    }

    public void setGPTree(GPTree gpTree) {
        this.gpTree = gpTree;
    }

    public String getLispString() {
        return lispString;
    }

    public void ignore(GPNode tree, GPNode feature, Ignorer ignorer) {

        //System.out.println(tree.depth());
        //System.out.println(feature.depth());

        if (tree.depth() < feature.depth())
            return;

        if (GPNodeComparator.equals(tree, feature)) {
            ignorer.ignore(tree);

            return;
        }

        if (tree.depth() == feature.depth())
            return;  //after ignoring, check again

        for (GPNode child : tree.children) {
            ignore(child, feature, ignorer);
        }
    }

    public void ignore(GPNode feature, Ignorer ignorer) {
        ignore(gpTree.child, feature, ignorer);
    }

    public double priority(OperationOption op, WorkCenter workCenter,
                           SystemState systemState) {
        CalcPriorityProblem calcPrioProb =
                new CalcPriorityProblem(op, workCenter, systemState);

        DoubleData tmp = new DoubleData();
        gpTree.child.eval(null, 0, tmp, null, null, calcPrioProb);

        return tmp.value;
    }
}
