package ch.epfl.bio410.cost;

import ch.epfl.bio410.graph.Spot;
import ij.ImagePlus;
import ij.plugin.ZProjector;

import static java.lang.Math.abs;

/**
 * This class implements the "DistanceAndIntensityCost" algorithm for tracking particles.
 * It implements the "AbstractCost" interface to benefit from the generic methods "evaluate" and "validate"
 */
public class DistanceAndIntensityCost implements AbstractCost {

    private double lambda = 0;
    private double costMax = 0;

    /** normalization distance */
    private double normDist = 1;

    /** normalization intensity */
    private double normInt = 1;

    public DistanceAndIntensityCost(ImagePlus imp, double costMax, double lambda) {
        this.lambda = lambda;
        this.costMax = costMax;
        int height = imp.getHeight();
        int width = imp.getWidth();
        this.normDist = Math.sqrt(height * height + width * width);
        this.normInt = ZProjector.run(imp,"max").getStatistics().max - ZProjector.run(imp,"min").getStatistics().min;
    }

    @Override
    public double evaluate(Spot a, Spot b) {
        // TODO question 3 - Add your code here
        double dist = a.distance(b);
        return lambda * dist/normDist + (1 - lambda)*abs(a.value - b.value)/normInt;
    }

    @Override
    public boolean validate(Spot a, Spot b) {
        if (a == null) return false;
        if (b == null) return false;
        return evaluate(a, b) < costMax;
    }
}
