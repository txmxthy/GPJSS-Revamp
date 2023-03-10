package solvers.algorithm.onlyselectedfeatures;

import org.apache.commons.math3.ml.clustering.Clusterable;

public class Point implements Clusterable {
    final double[] position;

    public Point(double[] position) {
        this.position = position;
    }

    public double[] getPoint() {
        return this.position;
    }
}
