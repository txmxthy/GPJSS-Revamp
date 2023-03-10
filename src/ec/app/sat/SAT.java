package ec.app.sat;

import ec.EvolutionState;
import ec.Individual;
import ec.Problem;
import ec.simple.SimpleFitness;
import ec.simple.SimpleProblemForm;
import ec.util.Parameter;
import ec.vector.BitVectorIndividual;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * SAT implements the boolean satisfiability problem.
 *
 * <p><b>Parameters</b><br>
 * <table>
 * <tr><td valign=top><i>base</i>.<tt>sat-filename</tt><br>
 * <font size=-1>String</td>
 * <td valign=top>(Filename containing boolean satisfiability formula in Dimacs CNF format)</td></tr>
 * </table>
 *
 * @author Keith Sullivan
 * @version 1.0
 */

public class SAT extends Problem implements SimpleProblemForm {
    public static final String P_FILENAME = "sat-filename";
    private static final long serialVersionUID = 1;
    Clause[] formula;

    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        File filename = state.parameters.getFile(base.push(P_FILENAME), null);
        if (filename == null)  // uh oh
            state.output.fatal("Filename must be provided", base.push(P_FILENAME));

        try {
            BufferedReader inFile = new BufferedReader(new FileReader(filename));
            String line = "";
            int cnt = 0;
            boolean start = false;
            while ((line = inFile.readLine()) != null) {
                if (start) {
                    formula[cnt++] = new Clause(line);
                    continue;
                }

                if (line.startsWith("p")) {
                    start = true;
                    line = line.trim();
                    int index = line.lastIndexOf(" ");
                    formula = new Clause[Integer.parseInt(line.substring(index + 1))];
                }
            }
            inFile.close();
        } catch (IOException e) {
            state.output.fatal("Error in SAT setup, while loading from file " + filename +
                    "\nFrom parameter " + base.push(P_FILENAME) + "\nError:\n" + e);
        }
    }

    /**
     * Evalutes the individual using the MAXSAT fitness function.
     */
    public void evaluate(final EvolutionState state, final Individual ind, final int subpopulation, final int threadnum) {
        BitVectorIndividual ind2 = (BitVectorIndividual) ind;
        double fitness = 0;

        for (int i = 0; i < formula.length; i++)
            fitness += formula[i].eval(ind2);

        ((SimpleFitness) (ind2.fitness)).setFitness(state, fitness, false);
        ind2.evaluated = true;
    }

    @Override
    public void normObjective(EvolutionState state, Individual ind, int subpopulation, int threadnum) {
        // TODO Auto-generated method stub

    }

    /**
     * Private helper class holding a single clause in the boolean formula. Each clause
     * is a disjunction of boolean variables (or their negation).
     */
    public static class Clause implements java.io.Serializable {
        private static final long serialVersionUID = 1;

        final int[] variables;

        public Clause(String c) {
            StringTokenizer st = new StringTokenizer(c);
            variables = new int[st.countTokens() - 1];
            for (int i = 0; i < variables.length; i++) {
                variables[i] = Integer.parseInt(st.nextToken());
            }
        }

        /**
         * Evaluates the individual with the clause.  Returns 1 is clase is satisfiabile, 0 otherwise.
         */
        public int eval(BitVectorIndividual ind) {
            boolean tmp;
            int x;
            for (int i = 0; i < variables.length; i++) {
                x = variables[i];
                if (x < 0)
                    tmp = !ind.genome[-x - 1];
                else
                    tmp = ind.genome[x - 1];

                if (tmp) return 1;
            }
            return 0;
        }
    }
}
