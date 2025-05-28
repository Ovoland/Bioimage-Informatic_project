package ch.epfl.bio410.segmentation;

import ch.epfl.bio410.graph.PartitionedGraph;
import ch.epfl.bio410.graph.Spot;
import ch.epfl.bio410.graph.Spots;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.measure.ResultsTable;
import ij.plugin.ChannelSplitter;
import ij.plugin.frame.RoiManager;
import ij.process.ImageConverter;
import ij.text.TextWindow;

public class GetCentroid {
    public static PartitionedGraph getCentroid(ImagePlus InputImg) {
        ImagePlus img = InputImg.duplicate();

        //to prevent the log: ParticleAnalyzer: threshold not set; assumed to be 0-0 ("Black background" not set)
        IJ.setAutoThreshold(img, "Default");
        img.setCalibration(null);

        IJ.run("Set Measurements...", "area mean min centroid center perimeter bounding stack display redirect=None decimal=5");
        IJ.run(img, "Analyze Particles...", "pixel display clear exclude overlay add composite stack");


        // get the open ResultsTable
        ResultsTable rt = ResultsTable.getResultsTable();

        //get the coordinate of the centroid
        double[] xCentroids = rt.getColumn("XM");
        double[] yCentroids = rt.getColumn("YM");

        //Get all the information of the bounding boxes
        double[] bx = rt.getColumn("BX");
        double[] by = rt.getColumn("BY");
        double[] width = rt.getColumn("Width");
        double[] height = rt.getColumn("Height");

        double[] frames = rt.getColumn("Slice");
        //Get the index of the frames wih values 3

        PartitionedGraph graph = new PartitionedGraph();

        double currentSlice = 1;
        Spots spots = new Spots();

        for (int idx = 0; idx < frames.length; idx++) {
            if (frames[idx] == currentSlice) {
                //Add the centroid of the bacteria
                double xCentroid = xCentroids[idx];
                double yCentroid = yCentroids[idx];
                double value = img.getProcessor().getPixelValue((int) xCentroid, (int) yCentroid);
                Spot centroid = new Spot((int) xCentroid, (int) yCentroid, (int) currentSlice - 1 , value);
                spots.add(centroid);
            } else if (frames[idx] == currentSlice + 1) {
                //Add the spots to the graph
                graph.add(spots);
                //Reset the spots for the next frame
                spots = new Spots();
                currentSlice = frames[idx];
            } else {
                //No bacteria found in this frame
                IJ.log("No bacteria found in frame " + frames[idx]);
            }
        }

        //Add the last spots to the graph
        RoiManager roiManager = RoiManager.getInstance();
        roiManager.close();

        graph.add(spots);
        return graph;


    }
}
