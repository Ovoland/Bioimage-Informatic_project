package ch.epfl.bio410.measurments;

import ch.epfl.bio410.graph.PartitionedGraph;
import ch.epfl.bio410.graph.Spot;
import ch.epfl.bio410.graph.Spots;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.ChannelSplitter;

import static ch.epfl.bio410.measurments.MotionMeasurement.plotMotion;


public class LocalMotionMeasurement {

    public static void localMotionMeasurement(PartitionedGraph replisomes, PartitionedGraph bacterias, ImagePlus img, int[] replisomeToShow) {
        Calibration calImp = img.getCalibration();
        double pixelWidth = calImp.pixelWidth;
        double pixelHeight = calImp.pixelHeight;
        String unit = calImp.getUnit();

        PartitionedGraph closestBacterias = findNearestBacteria(replisomes, bacterias,pixelWidth, pixelHeight);

        replisomes.addAll(closestBacterias);
        //replisomes.drawSpots2C(img,2,1, "Closest Bacteria ");

        int[] idxReplisomeToShow = replisomeToShow;
        for(int idx : idxReplisomeToShow) {
            double[] localMotionUnit = computeLocalMotionUnit(replisomes.get(idx), closestBacterias.get(idx),pixelWidth, pixelHeight);
            String[] labelsMotion = {"Temporal evolation of local distance for replisome " + idx, "Time", "Local displacement(" + unit + "/s)"};

            plotMotion(localMotionUnit,labelsMotion);

        }
    }


    private static PartitionedGraph findNearestBacteria(PartitionedGraph replisomes, PartitionedGraph bacterias, double pixelWidth, double pixelHeight) {
        int Nreplisome = replisomes.size();

        //Vector that will store for each replisome the coordinates of the closest bacteria at each frame
        PartitionedGraph closestBacteria = new PartitionedGraph();

        for (int replisomeIdx = 0; replisomeIdx < Nreplisome; ++replisomeIdx) {
            Spots replisome = replisomes.get(replisomeIdx);
            int Nspots = replisome.size();

            //Store the coordinates of the closest bacteria for each frame in the replisome
            Spots closestBacteriaSpots = new Spots();

            for (int spotIdx = 0; spotIdx < Nspots; ++spotIdx) {
                Spot replisomeSpot = replisome.get(spotIdx);
                int t = replisomeSpot.t;

                //Get all the bacteria detected for the same time point
                Spots bacteriaCandidates = bacterias.get(t);
                //Store the minimum distance from the replisome spot to the bacteria candidates
                double minDistance = Double.MAX_VALUE;
                int idxClosestBacteria = -1;
                //Among these bacteria determine which one is the closest to the replisome spot
                for (int bactIdx = 0; bactIdx < bacteriaCandidates.size(); ++bactIdx) {
                    Spot bacteriaSpot = bacteriaCandidates.get(bactIdx);
                    double distance = replisomeSpot.distanceMicroMeter(bacteriaSpot,pixelWidth, pixelHeight);
                    //Determine if this is the closest bacteria
                    //Also verify if the bacteria is at a valide distance
                    if (distance < minDistance) {
                        minDistance = distance;
                        idxClosestBacteria = bactIdx;
                    }
                }
                Spot closestBacteriaSpot = bacteriaCandidates.get(idxClosestBacteria);

                closestBacteriaSpots.add(closestBacteriaSpot);
            }

            closestBacteria.add(closestBacteriaSpots);
        }
        return closestBacteria;

    }

    private static double[] computeLocalMotionUnit(Spots replisome, Spots closestBacteria,double pixelWidth, double pixelHeight) {
        int Nspots = replisome.size();
        double[] localMotionUnit = new double[Nspots];

        for (int spotIdx = 0; spotIdx < Nspots; ++spotIdx) {
            Spot replisomeSpot = replisome.get(spotIdx);
            Spot closestBacteriaSpot = closestBacteria.get(spotIdx);

            //Compute the local motion unit as the distance between the replisome and the closest bacteria
            double distance = replisomeSpot.distanceMicroMeter(closestBacteriaSpot, pixelWidth, pixelHeight);
            //If the centrosome found is considered too far, we keep the previous value
            if(distance > 400){
                if(spotIdx == 0) {
                    //If this is the first spot, we cannot use the previous value
                    localMotionUnit[spotIdx] = 0;
                } else {
                    //Otherwise we keep the previous value
                    localMotionUnit[spotIdx] = localMotionUnit[spotIdx - 1];
                }
            }else {
                localMotionUnit[spotIdx] = distance;
            }
        }

        return localMotionUnit;
    }
}
