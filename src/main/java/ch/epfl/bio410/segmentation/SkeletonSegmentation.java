package ch.epfl.bio410.segmentation;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.measure.ResultsTable;
import ij.plugin.ChannelSplitter;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;

public class SkeletonSegmentation {
    public static void skeletonSegmentation(ImagePlus img, double bactLength){
        ImagePlus[] channels = ChannelSplitter.split(img);
        ImagePlus bacteria = channels[0];
        skeleton(bacteria, bactLength);
    }


    /*
    This function will process the given ImagePlus img to identify the skeletons of the bacterias as ROIs through an
    extremity sweep of the image once it is made binary and skeletonized. Then, it transfers the ROIs to the input img
     */
    private static void skeleton(ImagePlus img, double bactLength){
        ImagePlus impThresholded = img.duplicate();
        ImagePlus imp = img.duplicate();
        impThresholded.show();

        //Segmenting bacterias
        IJ.run(impThresholded, "Enhance Contrast...", "saturated=0.35 process_all");
        IJ.run(impThresholded, "Make Binary", "calculate");
        Prefs.blackBackground = false;
        IJ.run(impThresholded, "Skeletonize", "stack");
        drawingBacteria(impThresholded, bactLength);

        //Make measurements & delimiting ROIs
        IJ.run("Set Measurements...", "area mean min centroid center perimeter bounding stack display redirect=None decimal=5");
        IJ.run(impThresholded, "Analyze Particles...", "display clear exclude composite overlay add stack");

        //get ROI manager
        getRoi(impThresholded, imp);

    }


    /*
    This function will process a previously skeletonize image to identify bacteria of a maximum length in them. It
    operates by sweeping from the upper-left of the image to find extremities (only one neighbor of same pixel value).
    Once one is found, it follows it for a maximum length bactLength in pixel or until it finds no more pixel of same value. While
    following the branch, it suppresses the pixels to allow for more extremities (for bacteria too close to each other).
    This is done in a duplicate of the input ImagePlus img. In the original, only the end pixel of the branch found and
    its 8 neighbors are suppressed to create a space with the surrounding bacterias and prevent their grouping.
     */
    private static void drawingBacteria(ImagePlus img, double bactLength) {
        ImagePlus impSkeleton = img.duplicate();
        impSkeleton.show();

        int nFrames = impSkeleton.getNFrames();
        int sizeX = impSkeleton.getWidth();
        int sizeY = impSkeleton.getHeight();
        int maxLength = (int) bactLength;

        //Passing through frames
        for (int t = 0; t < nFrames; t++) {
            impSkeleton.setPosition(1, 1, 1 + t);
            img.setPosition(1, 1, 1 + t);
            ImageProcessor ip = impSkeleton.getProcessor();
            ImageProcessor ipOut = img.getProcessor();
            IJ.log("Beginning Processing of frame " + (t + 1));
            int counter = 0;

            //Passing through images
            for (int x = 0; x < sizeX; x++) {
                for (int y = 0; y < sizeY; y++) {
                    float pixelValue = ip.getPixelValue(x, y);
                    float neighborsValue = ip.getPixelValue(x - 1, y + 1) + ip.getPixelValue(x, y + 1) + ip.getPixelValue(x + 1, y + 1) + ip.getPixelValue(x - 1, y) + ip.getPixelValue(x + 1, y) + ip.getPixelValue(x - 1, y - 1) + ip.getPixelValue(x, y - 1) + ip.getPixelValue(x + 1, y - 1);

                    //Finding extremities
                    if (pixelValue != 0 && neighborsValue == 255) {
                        counter++;
                        int x2 = x;
                        int y2 = y;

                        //Following extremities
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

                        //Suppressing end of branch
                        ipOut.putPixelValue(x2, y2 + 1, 0);
                        ipOut.putPixelValue(x2 - 1, y2, 0);
                        ipOut.putPixelValue(x2, y2, 0);
                        ipOut.putPixelValue(x2 + 1, y2, 0);
                        ipOut.putPixelValue(x2, y2 - 1, 0);

                        //Resetting sweeping
                        x = 0;
                        y = 0;
                    }
                }
            }
            IJ.log(counter + " bacteria found");
        }
        impSkeleton.close();

    }


    /*
    This function transfers ROIs from a first ImagePlus img to a second ImagePlus img2 while filtering for ROI size to
    exclude over- or under-sized ROIs linked to non-bacterial particules in the image or reprouped bacteria which have
    been badly segmented by the previous method.
     */
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
