package ch.epfl.bio410.utils;



import ch.epfl.bio410.graph.PartitionedGraph;
import ch.epfl.bio410.graph.Spot;
import ch.epfl.bio410.graph.Spots;
import gnu.trove.TCollections;
import ij.ImagePlus;
import ij.gui.Plot;
import ij.process.ImageProcessor;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.IntStream;

public class motionMeasurement {
    public static void measureMotion(ImagePlus img, PartitionedGraph trajectories){
        double[] distances = computeTraveledDistance(trajectories);
        plotDistances(distances);
        double[] velocities = computeVelocity(distances, trajectories);
        plotMotion(velocities);
        //annotateImage(img, distances, trajectories);
    }

    private static double[] computeTraveledDistance(PartitionedGraph trajectories){
        double[] distances = new double[trajectories.size()];
        for(int i =0; i< trajectories.size(); ++i){
            Spots trajectory = trajectories.get(i);
            for(int j = 0; j < trajectory.size() - 1; ++j){
                Spot spot = trajectory.get(j);
                assert(j+1 <= trajectory.size());
                Spot nextSpot = trajectory.get(j+1);

                double distance = spot.distance(nextSpot);
                //System.out.println("adding at index " + i  + "distance " + distance);
                distances[i] += distance;

            }
        }
        for(Spots trajectory: trajectories){
            for(int i = 0; i < trajectory.size() - 1; i++){
                Spot spot = trajectory.get(i);
                assert(i+1 <= trajectory.size());
                Spot nextSpot = trajectory.get(i+1);

                double distance = spot.distance(nextSpot);
                distances[i] += distance;
            }
        }
        return distances;
    }

    private static void plotDistances(double[] distances){
        Plot plot = new Plot("Distance results", "Replisome Index", "Displacement");
        plot.setLimits(-1, distances.length,0,600);
        double[] trajectoriesRange = new double[distances.length];
        for(int i = 0; i < distances.length; ++i){
            trajectoriesRange[i] = i;
        }
        plot.addPoints(trajectoriesRange, distances,3);
        plot.show();
    }

    private static double[] computeVelocity(double[] distances, PartitionedGraph trajectories){
        double[] velocities = new double[distances.length];
        for(int t = 0; t < distances.length; ++t){
            velocities[t] = distances[t]/trajectories.get(t).size();
        }
        return velocities;
    }

    private static void plotMotion(double[] motion){
        Plot plot2 = new Plot("Speed results", "Replisome Index", "Speed");
        plot2.setLimits(-1, motion.length,-5, Arrays.stream(motion).max().getAsDouble() + 5);
        double[] trajectoriesRange = new double[motion.length];
        for(int i = 0; i < motion.length; ++i){
            trajectoriesRange[i] = i;
        }
        plot2.addPoints(trajectoriesRange, motion,3);
        plot2.show();
    }

    private static void annotateImage(ImagePlus img, double[] distances,PartitionedGraph trajectories ){
        ImageProcessor ip = img.getProcessor();
        Font font = new Font("Arial", Font.BOLD, 18);
        ip.setFont(font);
        ip.setColor(Color.RED);  // Optional: set text color

        for(int i = 0; i < distances.length; ++i){
            Spot spot = trajectories.get(i).get(0);
            ip.drawString(String.valueOf(distances[i]), spot.x + 20, spot.y + 20);

        }

        img.updateAndDraw();  // Update the display
        img.show();
    }

}