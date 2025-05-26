package ch.epfl.bio410.segmentation;

import ch.epfl.bio410.bacteria.Bacteria;
import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.ChannelSplitter;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;

public class segmentBacteriaTrad {
    public static void BacteriaSegmentation(ImagePlus img){
        ImagePlus[] channels = ChannelSplitter.split(img);
        ImagePlus bacteria = channels[0];
        getROI(bacteria);
    }


    private static void getROI(ImagePlus img){
        ImagePlus impThresholded = img.duplicate();
        impThresholded.show();

        //Segmenting bacterias
        IJ.run(impThresholded, "Enhance Contrast...", "saturated=0.35 process_all");
        IJ.run(impThresholded, "Make Binary", "calculate");
        Prefs.blackBackground = false;
        IJ.run(impThresholded, "Skeletonize", "stack");
        drawingBacteria(impThresholded);

        //Make measurements
        IJ.run("Set Measurements...", "area mean min centroid center perimeter bounding stack display redirect=None decimal=5");
        IJ.run(impThresholded, "Analyze Particles...", "display clear exclude overlay add composite stack");

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




}
