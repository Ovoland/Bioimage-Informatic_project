package ch.epfl.bio410.detectBacteria;

import ch.epfl.bio410.graph.PartitionedGraph;
import ch.epfl.bio410.graph.Spot;
import ch.epfl.bio410.graph.Spots;
import com.sun.jdi.IntegerValue;
import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.gui.Plot;
import ij.measure.ResultsTable;
import ij.plugin.Concatenator;
import ij.plugin.Duplicator;
import ij.plugin.ImageCalculator;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;
import net.imagej.ImageJ;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import java.util.Arrays;
import java.util.Scanner;

/**
 * Template plugin file with useful command to succeed in doing the homework
 *
 * It implements a standard pipeline : open an image, analyze the image,
 * do some measurements and plot results. The image was intentionally not the one of the homework
 * and the implemented pipeline not the one that should be implemented.
 *
 * Your task
 * 		- Pre-process the nucleus channel to be segmented by an automatic threshold
 * 		- Computes the gene expression of the cytoplasm around the periphery of the nucleus, on a band of 5 pixels.
 * 		- Output an overlay of the detected ROIs (i.e. bands) on the composite image, with different colors for each channel
 * 		- Output a scatter plot, over time, of the mean intensities of gene expression
 */
@Plugin(type = Command.class, menuPath = "Plugins>BII>detectBacteria")
public class DetectBacteria implements Command {
    private static String inputFolder = "";

    @Override
    public void run(){

        GenericDialog gd = new GenericDialog("Bright Spot tracking");

        gd.addDirectoryField("Image selection", inputFolder);
        inputFolder = gd.getNextString();

        gd.showDialog();

        if (gd.wasCanceled()) {
            return;
        }
        ImagePlus imp = IJ.openImage(inputFolder);

        // show the image
        imp.show();
        //IJ.run(impCyto, "Enhance Contrast...", "saturated=0.35 normalize process_all");
        imp.setTitle("SPB");

        /*
        ***********************************************************
        */
        String channel1 = "ChannelBacteria";
        IJ.run("Duplicate...", "duplicate channels=1 title="+ channel1);
        IJ.selectWindow(channel1);
        ImagePlus impDup = IJ.getImage();

        //Preparing for thresholding, thresholding & Watershed
        ImagePlus impPreThresholded = impDup.duplicate();
        impPreThresholded.show();
        IJ.run(impPreThresholded, "Enhance Contrast...", "saturated=0.35 process_all");
        //IJ.run(impPreThresholded, "Gaussian Blur...", "sigma=2 stack");
        ImagePlus impThresholded = impPreThresholded.duplicate();
        IJ.run(impThresholded, "Make Binary", "method=Default calculate black");
        IJ.run(impThresholded, "Skeletonize", "stack");
        //IJ.run(impThresholded, "Watershed", "stack");
        impThresholded.show();
        ImagePlus impSkeleton = impThresholded.duplicate();
        impSkeleton.setTitle("Segmenting");
        impSkeleton.show();


        int nFrames = impSkeleton.getNFrames();
        int sizeX = impSkeleton.getWidth();
        int sizeY = impSkeleton.getHeight();
        int maxLength = 13;
        PartitionedGraph extremitiesGraph = new PartitionedGraph();
        //PartitionedGraph bacterias = new PartitionedGraph();
        int counter = 0;
        ImagePlus impSegmentation = null;
        for (int t=0; t<nFrames; t++){
            impSkeleton.setPosition(1, 1, 1 + t);
            ImageProcessor ip = impSkeleton.getProcessor();
            Spots extremities = new Spots();
            Boolean remainingExtremities = Boolean.TRUE;
            PartitionedGraph bacterias = new PartitionedGraph();
            while (remainingExtremities) {
                for (int x = 0; x < sizeX; x++) {
                    for (int y = 0; y < sizeY; y++) {
                        float pixelValue = ip.getPixelValue(x, y);
                        float neighborsValue = ip.getPixelValue(x - 1, y + 1) + ip.getPixelValue(x, y + 1) + ip.getPixelValue(x + 1, y + 1) + ip.getPixelValue(x - 1, y) + ip.getPixelValue(x + 1, y) + ip.getPixelValue(x - 1, y - 1) + ip.getPixelValue(x, y - 1) + ip.getPixelValue(x + 1, y - 1);
                        //System.out.println(pixelValue);
                        //System.out.println(neighborsValue);
                        if (pixelValue != 0 && neighborsValue == 255) {
                            Spot extremity = new Spot(x, y, t, pixelValue);
                            extremities.add(extremity);
                            counter++;
                            Spots bacteriaUnit = new Spots();
                            bacteriaUnit.add(extremity);
                            int x2 = x;
                            int y2 = y;
                            for (int i = 0; i < maxLength - 1; i++) {
                                float[][] nextPixels = {{x2 - 1, x2, x2 + 1, x2 - 1, x2 + 1, x2 - 1, x2, x2 + 1},
                                        {y2 + 1, y2 + 1, y2 + 1, y2, y2, y2 - 1, y2 - 1, y2 - 1},
                                        {ip.getPixelValue(x2 - 1, y2 + 1), ip.getPixelValue(x2, y2 + 1), ip.getPixelValue(x2 + 1, y2 + 1), ip.getPixelValue(x2 - 1, y2), ip.getPixelValue(x2 + 1, y2), ip.getPixelValue(x2 - 1, y2 - 1), ip.getPixelValue(x2, y2 - 1), ip.getPixelValue(x2 + 1, y2 - 1)}};
                                for (int j = 0; j < nextPixels[2].length; j++) {
                                    if (nextPixels[2][j] != 0) {
                                        ip.putPixelValue(x2, y2, 0);
                                        x2 = Math.round(nextPixels[0][j]);
                                        y2 = Math.round(nextPixels[1][j]);
                                        Spot nextPixel = new Spot(x2, y2, t, nextPixels[2][j]);
                                        bacteriaUnit.add(nextPixel);
                                        break;
                                    }
                                }
                            }
                            bacterias.add(bacteriaUnit);
                            x = -1;
                            y = -1;
                        }
                    }
                }
                extremitiesGraph.add(extremities);
                remainingExtremities = Boolean.FALSE;
                bacterias.drawLines(impDup);
                IJ.run("Duplicate...", "duplicate frames="+t+1+" title=impT");
                //IJ.selectWindow("impT");
                ImagePlus impS = IJ.getImage();
                if (t==0){impSegmentation = impS;}
                impSegmentation = Concatenator.run(impSegmentation,impS);
                impS.close();
                IJ.selectWindow("Segmenting");
            }
        }
        //extremitiesGraph.drawSpots(impThresholded);

        System.out.println(nFrames);
/*
        for (int i=0; i<nFrames; i++) {
            impThresholded.setPosition(1,1, i+1);
            //int n = impThresholded.getSlice();C:\Users\chaye\Autres Documents\BIO410 - Projet SPB

            //IJ.run("Set Measurements...", "area mean min centroid center perimeter display redirect=None decimal=3");
            IJ.run("Set Measurements...", "area display redirect=None decimal=3");
            IJ.run(impThresholded, "Analyze Particles...", "display overlay add");
        }
        //impOverlay.changes = false;
        //impOverlay.close();
        /*
        ***********************************************************
        */

/*
        // get the RoiManager
        RoiManager rm = RoiManager.getRoiManager();
        Prefs.showAllSliceOnly = true;
        RoiManager.restoreCentered(false);
        Prefs.useNamesAsLabels = false;
        int nROI = rm.getCount(); // get the number of ROIs within the ROI Manager
        rm.runCommand(impThresholded,"Measure");
        ResultsTable rt = ResultsTable.getResultsTable();
        Double maxArea = 3.0 ;
        Double minArea = 0.5 ;


        IJ.selectWindow(channel1);
        for (int j = 0; j < nROI; j++) {
            rm.select(j); // select a specific roi
            Float area = rt.getColumn(0)[j];
            if (area > maxArea | area < minArea) {
                rm.runCommand(impDup,"Delete");
                rt.deleteRow(j);
                j -= 1;
                nROI = rm.getCount();
            }
            rm.runCommand("Update");
            //IJ.run("Make Band...", "band=0.5");// compute a band around the selected ROI
            //rm.runCommand("Update"); // update the selected ROI with the new shape (i.e. band)
            }

        // deselect all the ROIs to be able to make a measurement on all ROIs
        rm.deselect();
*/
        // clear results  in the ResultsTable
        //IJ.run("Clear Results", "");

        // do some measurements on the ROIs
        //rm.runCommand(impComposite,"Measure");

        // get the open ResultsTable
        //ResultsTable rt = ResultsTable.getResultsTable();

        // get a column of values
        //TODO: get the correct column(s)
        //double[] means = rt.getColumn("Mean");
        //int yMax = (int)Math.round(Arrays.stream(means).max().getAsDouble());
        //int yMin = (int)Math.round(Arrays.stream(means).min().getAsDouble());

        //TODO: replace the values for x-axis by suitable values
        //double[] timeSlices = new double[nROI];

        // create a plot
        //Plot plot = new Plot("Results", "Time" , "Intensity");

        //for (int i = 0 ; i < means.length; i++){
          //  String label = rt.getLabel(i);
            //String prefix = finalLabel + ":";
            //String sliceS = label.substring(label.indexOf(prefix) + prefix.length(),label.indexOf(prefix) + prefix.length()+4);
            //timeSlices[i] = Integer.valueOf(sliceS);
        //}
        //int xMax = (int)Math.round(Arrays.stream(timeSlices).max().getAsDouble());
        //int xMin = (int)Math.round(Arrays.stream(timeSlices).min().getAsDouble());

        //plot.addPoints(timeSlices, means, 0);
        //plot.setLimits(xMin-1,xMax+1,yMin-1,yMax+1);
        //plot.show();
    }



    /**
     * This main function serves for development purposes.
     * It allows you to run the plugin immediately out of
     * your integrated development environment (IDE).
     *
     * @param args whatever, it's ignored
     * @throws Exception
     */
    public static void main(String[] args) {
        final ImageJ ij = new ImageJ();
        ij.ui().showUI();
        //ImagePlus cyto1 = IJ.openImage("C:"+File.separator+"Users"+File.separator+"chaye"+File.separator+"OneDrive"+File.separator+"Documents"+File.separator+"Université"+File.separator+"BII_BIO-410"+File.separator+"Homework03"+File.separator+"cyto1.tif");
        //cyto1.show();
        //ImagePlus nucleus1 = IJ.openImage("C:"+File.separator+"Users"+File.separator+"chaye"+File.separator+"OneDrive"+File.separator+"Documents"+File.separator+"Université"+File.separator+"BII_BIO-410"+File.separator+"Homework03"+File.separator+"nucleus1.tif");
        //nucleus1.show();
        DetectBacteria correct = new DetectBacteria();
        correct.run();
    }
}
