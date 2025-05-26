package ch.epfl.bio410.utils;

import ch.epfl.bio410.bacteria.Bacteria;
import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.ChannelSplitter;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;

import java.awt.*;
import java.util.Arrays;

public class segmentBacteriaTrad {
    public static void BacteriaSegmentation(ImagePlus img){
        ImagePlus[] channels = ChannelSplitter.split(img);
        ImagePlus bacteria = channels[0];
        skeleton(bacteria);
    }


    private static void skeleton(ImagePlus img){
        ImagePlus impThresholded = img.duplicate();
        ImagePlus imp = img.duplicate();
        impThresholded.show();

        //Segmenting bacterias
        IJ.run(impThresholded, "Enhance Contrast...", "saturated=0.35 process_all");
        IJ.run(impThresholded, "Make Binary", "calculate");
        Prefs.blackBackground = false;
        IJ.run(impThresholded, "Skeletonize", "stack");
        drawingBacteria(impThresholded);

        //Make measurements
        IJ.run("Set Measurements...", "area mean min centroid center perimeter bounding stack display redirect=None decimal=5");
        IJ.run(impThresholded, "Analyze Particles...", "display overlay add stack");
//clear exclude composite
        //get ROI manager
        //RoiManager rm = RoiManager.getRoiManager();
        getRoi(impThresholded, imp);
/*
        // get the open ResultsTable
        ResultsTable rt = ResultsTable.getResultsTable();

        //get the coordinate of the centroid
        double[] xCentroid = rt.getColumn("XM");
        double[] yCentroid = rt.getColumn("YM");

        //Get all the information of the bounding boxes
        double[] bx = rt.getColumn("BX");
        double[] by = rt.getColumn("BY");
        double[] width = rt.getColumn("Width");
        double[] height = rt.getColumn("Height");


        //Make the extraction frame by frame
        Bacteria[][] candidates ;
        for(int iParticule= 0; iParticule < rt.size(); ++iParticule){
            Bacteria bacteria = new Bacteria(xCentroid[iParticule],yCentroid[iParticule],bx[iParticule],by[iParticule],width[iParticule],height[iParticule]);
            //candidates[rt.getValue("Slice",iParticule)].pushBack(bacteria);
        }
        Roi[] roi = rm.getRoisAsArray();
        for (Roi r : roi) {
            rm.addRoi(r);
        }
        img.show();
*/



    }


    private static void drawingBacteria(ImagePlus img){
        ImagePlus impSkeleton = img.duplicate();
        impSkeleton.show();

        int nFrames = impSkeleton.getNFrames();
        int sizeX = impSkeleton.getWidth();
        int sizeY = impSkeleton.getHeight();
        int maxLength = 18;
        for (int t=0; t<nFrames; t++){
            impSkeleton.setPosition(1, 1, 1 + t);
            img.setPosition(1,1,1 + t);
            ImageProcessor ip = impSkeleton.getProcessor();
            ImageProcessor ipOut = img.getProcessor();
            IJ.log("Beginning Processing of frame "+(t+1));
            int counter = 0;
            for (int x = 0; x < sizeX; x++) {
                for (int y = 0; y < sizeY; y++) {
                    float pixelValue = ip.getPixelValue(x, y);
                    float neighborsValue = ip.getPixelValue(x - 1, y + 1) + ip.getPixelValue(x, y + 1) + ip.getPixelValue(x + 1, y + 1) + ip.getPixelValue(x - 1, y) + ip.getPixelValue(x + 1, y) + ip.getPixelValue(x - 1, y - 1) + ip.getPixelValue(x, y - 1) + ip.getPixelValue(x + 1, y - 1);
                    //System.out.println(pixelValue);
                    //System.out.println(neighborsValue);
                    if (pixelValue != 0 && neighborsValue == 255) {
                        counter++;
                        int x2 = x;
                        int y2 = y;
                        for (int i = 0; i < maxLength; i++) {
                            float[][] nextPixels = {{x2 - 1, x2, x2 + 1, x2 - 1, x2 + 1, x2 - 1, x2, x2 + 1},
                                    {y2 + 1, y2 + 1, y2 + 1, y2, y2, y2 - 1, y2 - 1, y2 - 1},
                                    {ip.getPixelValue(x2 - 1, y2 + 1), ip.getPixelValue(x2, y2 + 1), ip.getPixelValue(x2 + 1, y2 + 1), ip.getPixelValue(x2 - 1, y2), ip.getPixelValue(x2 + 1, y2), ip.getPixelValue(x2 - 1, y2 - 1), ip.getPixelValue(x2, y2 - 1), ip.getPixelValue(x2 + 1, y2 - 1)}};
                            for (int j = 0; j < nextPixels[2].length; j++) {
                                if (nextPixels[2][j] != 0) {
                                    ip.putPixelValue(x2, y2, 0);
                                    x2 = Math.round(nextPixels[0][j]);
                                    y2 = Math.round(nextPixels[1][j]);
                                    break;
                                }
                            }
                        }

                        ipOut.putPixelValue(x2, y2+1, 0);
                        ipOut.putPixelValue(x2-1, y2, 0);
                        ipOut.putPixelValue(x2, y2, 0);
                        ipOut.putPixelValue(x2+1, y2, 0);
                        ipOut.putPixelValue(x2, y2-1, 0);
                        x = 0;
                        y = 0;
                    }
                }
            }
            IJ.log(counter+" bacteria found");
        }
        impSkeleton.close();




    }


    private static void getRoi(ImagePlus img, ImagePlus img2){
        String titleImg2 = "Bacterias";
        img2.setTitle(titleImg2);
        img2.show();

        // get the RoiManager
        RoiManager rm = RoiManager.getRoiManager();
        Prefs.showAllSliceOnly = true;
        RoiManager.restoreCentered(false);
        Prefs.useNamesAsLabels = false;
        int nROI = rm.getCount(); // get the number of ROIs within the ROI Manager
        rm.runCommand(img,"Measure");
        ResultsTable rt = ResultsTable.getResultsTable();
        Double maxArea = 18.0 ;
        Double minArea = 0.05 ;


        IJ.selectWindow(titleImg2);
        for (int j = 0; j < nROI; j++) {
            rm.select(j); // select a specific roi
            Double area = rt.getColumn("Area")[j];
            if (area > maxArea | area < minArea) {
                rm.runCommand(img2,"Delete");
                rt.deleteRow(j);
                nROI = rm.getCount();
                j -= 1;
            }
            rm.runCommand("Update");
        }

        // deselect all the ROIs to be able to make a measurement on all ROIs
        rm.deselect();



    }


}
