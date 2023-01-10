package simulation.jss.gp;

import ec.gp.GPNode;

/**
 * An EvolutionState class with this interface
 * means that the terminal set is changable
 * during the evolutionary process.
 * <p>
 * Created by YiMei on 6/10/16.
 */
public interface TerminalsChangable {

    GPNode[][] getTerminals();
   /* //fzhang 16.7.2018 in order to initialize population with multiple trees  F
    GPNode[] getMultiTerminals(int numTrees);*/

    void setTerminals(GPNode[][] terminals);

    GPNode[] getTerminals(int subPopNum);

    void adaptPopulation(int subPopNum);
}
