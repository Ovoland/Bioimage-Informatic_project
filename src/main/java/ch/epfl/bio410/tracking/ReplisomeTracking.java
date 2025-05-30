package ch.epfl.bio410.tracking;

import ch.epfl.bio410.cost.AbstractCost;
import ch.epfl.bio410.cost.DistanceAndIntensityCost;
import ch.epfl.bio410.graph.PartitionedGraph;
import ch.epfl.bio410.graph.Spot;
import ch.epfl.bio410.graph.Spots;
import ch.epfl.bio410.graph.Trajectories;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.ChannelSplitter;
import ij.plugin.filter.MaximumFinder;
import ij.process.ImageProcessor;
import io.scif.jj2000.j2k.image.ImgDataAdapter;

import java.awt.*;

public class ReplisomeTracking {

    private static final double threshold = 100;  // Detection parameters, threshold of the localmax TODO : adapt it
    private static double costmax = 0.008;	// Cost parameters, maximum cost allowed to link spots togethers TODO : adapt it
    private static final double lambda = 0.8; 	// Cost parameters, hyperparameter to balance cost function terms TODO : adapt it


    public static PartitionedGraph replisomeTracking(ImagePlus inputImg){
        ImagePlus imp = inputImg.duplicate();
        ImagePlus[] channels = ChannelSplitter.split(imp);
        ImagePlus replisome = channels[1];



        // Detection
        PartitionedGraph frames = detectReplisome(replisome, threshold, true);
        //frames.drawSpots(replisome, 5,1);

        // Create cost function
        AbstractCost cost = new DistanceAndIntensityCost(imp, costmax, lambda);

        // Linking trajectories
        PartitionedGraph trajectories = trackToNearestTrajectory(frames, cost);

        trajectories.drawLines(replisome);

        return trajectories;
    }

    private static PartitionedGraph detectReplisome(ImagePlus imp, double threshold, boolean excludeOnEdges){
        //Denoising step


        //IJ.run("Omnipose ...", "env_path=C:\\Users\\ovola\\anaconda3\\envs\\omnipose env_type=conda model=bact_phase_omni model_path=C:\\Fiji.app diameter=30 ch1=0 ch2=-1 additional_flags=[--omni, --cluster]");
        //IJ.run(imp, "Non-local Means Denoising", "sigma=15 smoothing_factor=1 auto stack");

        //get the number of images in a stack
        int stackSize = imp.getNFrames();

        PartitionedGraph graph = new PartitionedGraph();

        for(int t = 0; t < stackSize ; t++) {
            imp.setSlice(t);
            ImageProcessor ip = imp.getProcessor();

            MaximumFinder maxFinder = new MaximumFinder();
            Polygon poly = maxFinder.getMaxima(ip,threshold,excludeOnEdges);
            Spots spots = new Spots();
            for(int n = 0; n < poly.npoints; n++){
                int x = poly.xpoints[n];
                int y = poly.ypoints[n];
                double valueImage = ip.getPixelValue(x, y);
                spots.add(new Spot(x, y, t, valueImage));
            }
            graph.add(spots);
        }
        graph.drawSpots(imp, 5,1, "Replisome detection");

        return graph;
    }

    /**
     * This method allows to track single spots across frames.
     * The algorithm is working by extending the current trajectories by
     * appending the nearest valid spot of the next frame.
     *
     * @param frames Graph organized by partition of spots belonging to the same frame
     * @param cost Cost function for the connection of spots
     * @return Graph organized by partition of spots belonging to the same trajectory
     */
    private static PartitionedGraph trackToNearestTrajectory(PartitionedGraph frames, AbstractCost cost) {
        PartitionedGraph trajectories = new PartitionedGraph();
        for (Spots frame : frames) {
            for (Spot spot : frame) {
                Spots trajectory = trajectories.getPartitionOf(spot);
                if (trajectory == null) trajectory = trajectories.createPartition(spot);
                if (spot.equals(trajectory.last())) {
                    int t0 = spot.t;
                    for (int t=t0; t < frames.size() - 1; t++) {
                        //Let's keep track of the nearest Value and spot
                        //Take the reference value of the first spot of the next frame
                        double nearestValue = cost.evaluate(spot,frames.get(t+1).first());
                        Spot nearestSpot = frames.get(t+1).first();
                        //Determine if the next spot is probably missing
                        boolean missingDot = true;

                        for(Spot next : frames.get(t+1)) {
                            double dist = cost.evaluate(spot, next);
                            if(dist <= nearestValue && cost.validate(spot,next)){
                                IJ.log("#" + trajectories.size() + " spot " + next + " with a cost:" + cost.evaluate(next, spot));
                                nearestValue = dist;
                                nearestSpot = next;
                                //Indicates that at least one point has been found for the trajectory
                                missingDot = false;
                            }
                        }
                        //If no valid spot for the trajectory have been found, we don't add anything to the trajectory
                        //If at least one valid spot have been found we add it to the trajectory
                        if(!missingDot){
                            trajectory.add(nearestSpot);
                            spot = nearestSpot;
                        }
                    }
                }
            }
        }
        return trajectories;
    }
}