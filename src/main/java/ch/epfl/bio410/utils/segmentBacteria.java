package ch.epfl.bio410.utils;

import ch.epfl.bio410.bacteria.Bacteria;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.ChannelSplitter;
import ij.plugin.frame.RoiManager;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.stream.IntStream;


public class segmentBacteria {
    public static void BacteriaSegmentation(ImagePlus img){
        ImagePlus imp = IJ.openImage("data/bacteria.tif");
        ImagePlus[] channels = ChannelSplitter.split(imp);
        ImagePlus bacteria = channels[0];
        getROI(bacteria);
    }


    private static void getROI(ImagePlus img){
        img.show();

        IJ.run(img, "Make Binary", "background=Light calculate black");
        int stackSize = img.getStackSize();
        IJ.run("Set Measurements...", "area mean min centroid center perimeter bounding stack display redirect=None decimal=5");
        IJ.run(img, "Analyze Particles...", "display clear exclude overlay add composite stack");

        //get ROI manager
        RoiManager rm = RoiManager.getRoiManager();

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




    }

}
