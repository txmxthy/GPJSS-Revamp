package solvers.gp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dyska on 21/05/17.
 */
public class GPMain {

    public static void main(String[] args) {
        List<String> gpRunArgs = new ArrayList<>();
        boolean isTest = true;
        int maxTests = 1;
        boolean isDynamic = true;

        //include path to params file
        gpRunArgs.add("-file");

        double utilLevel = 0.85;
        String objective = "max-flowtime";

        gpRunArgs.add("/Users/dyska/Desktop/Uni/COMP489/GPJSS/src/yimei/jss/algorithm/featureconstruction/fcgp-simplegp-dynamic.params");
        //gpRunArgs.add("/Users/dyska/Desktop/Uni/COMP489/GPJSS/src/yimei/jss/algorithm/coevolutiongp/baseline-coevolutiongp-dynamic.params");
        //gpRunArgs.add("/Users/dyska/Desktop/Uni/COMP489/GPJSS/src/yimei/jss/algorithm/simplegp/simplegp-dynamic.params");
        gpRunArgs.add("-p");
        gpRunArgs.add("eval.problem.eval-model.sim-models.0.util-level=" + utilLevel);
        gpRunArgs.add("-p");
        gpRunArgs.add("eval.problem.eval-model.objectives.0=" + objective);
        gpRunArgs.add("-p");
        for (int i = 1; i <= 30 && i <= maxTests; ++i) {
            gpRunArgs.add("seed.0=" + i);
            //convert list to array
            GPRun.main(gpRunArgs.toArray(new String[0]));
            //now remove the seed, we will add new value in next loop
            gpRunArgs.remove(gpRunArgs.size() - 1);
        }
    }
}
