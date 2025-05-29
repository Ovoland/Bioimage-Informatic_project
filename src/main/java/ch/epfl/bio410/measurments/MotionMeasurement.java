package ch.epfl.bio410.measurments;



import ch.epfl.bio410.graph.PartitionedGraph;
import ch.epfl.bio410.graph.Spot;
import ch.epfl.bio410.graph.Spots;
import ij.ImagePlus;
import ij.gui.Plot;
import ij.measure.Calibration;
import ij.process.ImageProcessor;

import java.awt.*;
import java.util.Arrays;

public class MotionMeasurement {
    public static void motionMeasurment(ImagePlus img, PartitionedGraph trajectories, double deltaT) {
        Calibration calImp = img.getCalibration();
        double pixelWidth = calImp.pixelWidth;
        double pixelHeight = calImp.pixelHeight;
        String unit = calImp.getUnit();

        double[] distances = computeTraveledDistance(trajectories, pixelWidth, pixelHeight);

        String[] labelsDistances = {"Total distance travelled by replisome", "Replisome Index", "Total displacement (" + unit + ")"};
        plotMotion(distances, labelsDistances);
        double[] velocities = computeVelocity(distances, trajectories, deltaT);
        String[] labelsMotion = {"Mean velocity per replisome", "Replisome Index", "Speed (" + unit + "/s)"};
        plotMotion(velocities, labelsMotion);

        //plotMotionAcrossTime(velocities, trajectories, new String[]{"Mean velocity across time", "Replisome Index", "Speed (" + unit + "/s)"});
        //plotUnitMotion(trajectories.get(0), pixelWidth, pixelHeight, new String[]{"Unit motion of replisome", "Time", "Displacement (" + unit + ")"});
    }

    private static double[] computeTraveledDistance(PartitionedGraph trajectories, double pixelWidth, double pixelHeight){
        double[] distances = new double[trajectories.size()];
        for(int i =0; i< trajectories.size(); ++i){
            Spots trajectory = trajectories.get(i);
            for(int j = 0; j < trajectory.size() - 1; ++j){
                Spot spot = trajectory.get(j);
                assert(j+1 <= trajectory.size());
                Spot nextSpot = trajectory.get(j+1);

                double distance = spot.distanceMicroMeter(nextSpot, pixelWidth, pixelHeight);
                //System.out.println("adding at index " + i  + "distance " + distance);
                distances[i] += distance;

            }
        }
        return distances;
    }

    private static void plotDistances(double[] distances){
        Plot plot = new Plot("Distance results", "Replisome Index", "Displacement");
        plot.setLimits(-1, distances.length,-5,Arrays.stream(distances).max().getAsDouble());
        double[] trajectoriesRange = new double[distances.length];
        for(int i = 0; i < distances.length; ++i){
            trajectoriesRange[i] = i;
        }
        plot.addPoints(trajectoriesRange, distances,3);
        plot.show();
    }

    private static double[] computeVelocity(double[] distances, PartitionedGraph trajectories, double deltaT){
        double[] velocities = new double[distances.length];
        for(int t = 0; t < distances.length; ++t){
            velocities[t] = distances[t]/(trajectories.get(t).size()*deltaT);
        }
        return velocities;
    }

    public static void plotMotion(double[] motion, String[] labels){
        Plot plot = new Plot(labels[0], labels[1], labels[2]);
        plot.setLimits(-1, motion.length,-0.01, Arrays.stream(motion).max().getAsDouble());
        double[] trajectoriesRange = new double[motion.length];
        for(int i = 0; i < motion.length; ++i){
            trajectoriesRange[i] = i;
        }
        plot.addPoints(trajectoriesRange, motion,3);
        plot.show();
    }

    public static void plotMotionAcrossTime(double [] motion, PartitionedGraph replisomes, String[] labels){
        Plot plot = new Plot(labels[0], labels[1], labels[2]);
        plot.setLimits(-1, replisomes.size(), -0.01, Arrays.stream(motion).max().getAsDouble());
        double[] timeRange = new double[motion.length];
        for(int i = 0; i < replisomes.size(); ++i){
            Spots spots = replisomes.get(i);
            Spot firstSpot = spots.get(0);
            timeRange[i] = firstSpot.t;


        }
        plot.addPoints(timeRange, motion,3);
        plot.show();
    }

    public static void plotUnitMotion(Spots replisome,double pixelWidth, double pixelHeight,String[] labels){
        double[] distances = computeUnitMotion(replisome, pixelWidth, pixelHeight);
        Plot plot = new Plot(labels[0], labels[1], labels[2]);
        plot.setLimits(-1, replisome.size(), -0.01, Arrays.stream(distances).max().getAsDouble());
        plot.addPoints(getReplisomeTimeRange(replisome), distances, 3);
        plot.show();
    }

    private static double[] getReplisomeTimeRange(Spots replisome){
        double[] timeRange = new double[replisome.size()];
        for(int i = 0; i < replisome.size(); ++i){
            Spot spot = replisome.get(i);
            timeRange[i] = spot.t;
        }
        return timeRange;
    }

    private static double[] computeUnitMotion(Spots replisome, double pixelWidth, double pixelHeight){
        double[] distances = new double[replisome.size()];

        for(int j = 0; j < replisome.size() - 1; ++j){
            Spot spot = replisome.get(j);
            assert(j+1 <= replisome.size());
            Spot nextSpot = replisome.get(j+1);

            double distance = spot.distanceMicroMeter(nextSpot, pixelWidth, pixelHeight);
            //System.out.println("adding at index " + i  + "distance " + distance);
            distances[j] = distance;
        }
        return distances;
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