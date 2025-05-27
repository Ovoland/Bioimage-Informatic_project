package ch.epfl.bio410.segmentation;

import ch.epfl.bio410.graph.PartitionedGraph;
import ch.epfl.bio410.graph.Spot;
import ch.epfl.bio410.graph.Spots;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.ChannelSplitter;
import ij.plugin.Duplicator;
import ij.plugin.frame.RoiManager;
import ij.process.ImageConverter;

import java.awt.*;
import java.util.Arrays;

import static com.sun.tools.corba.se.idl.toJavaPortable.Arguments.None;

public class LevelSetSegmentation {
    public static ImagePlus levelSetSegmentation(ImagePlus InputImg){
        ImagePlus imp = InputImg.duplicate();
        ImagePlus[] channels = ChannelSplitter.split(imp);
        ImagePlus bacteria = channels[0];
        ImagePlus segmented = segmentation(bacteria);
        return segmented;
    }


    private static ImagePlus segmentation(ImagePlus imp){
        IJ.run(imp, "Enhance Contrast...", "saturated=0.35 process_all");
        ImageConverter.setDoScaling(true);
        IJ.run(imp, "8-bit", "");

        //Future stacked image containing the segmented frame of the given image
        ImageStack segmentedStack = new ImageStack(imp.getWidth(), imp.getHeight());
        ImageStack processedStack = new ImageStack(imp.getWidth(), imp.getHeight());

        int NFrames = imp.getNFrames();
        for(int i = 1; i <= NFrames; i++){
            imp.setSlice(i);
            ImagePlus frame = new Duplicator().run(imp, 1,1,1,1,i,i);
            frame.show();
            double threshold = 45 - i*25/120;
            IJ.run(frame, "Find Maxima...", "prominence=" + threshold +" light output=[Point Selection]");
            IJ.run(frame, "Level Sets", "method=[Active Contours] use_fast_marching use_level_sets grey_value_threshold=5 distance_threshold=0.10 advection=2.20 propagation=1 curvature=1 grayscale=5 convergence=0.0050 region=outside");

            //Get the image produced by the level set segmentation
            String segmentationTitle = "Segmentation of " + frame.getTitle();
            String segmentationProcessTitle = "Segmentation progress of " + frame.getTitle();

            ImagePlus segmented = WindowManager.getImage(segmentationTitle);
            ImagePlus segmentedProgress = WindowManager.getImage(segmentationProcessTitle);

            //Remove noisy outliners
            IJ.run(segmented, "Despeckle", "");
            segmentedStack.addSlice(segmented.getProcessor());
            processedStack.addSlice(segmentedProgress.getProcessor());

            //Prevent the closing box to display
            frame.changes = false;
            segmented.changes = false;
            segmentedProgress.changes = false;


            frame.close();
            segmented.close();
            segmentedProgress.close();

        }
        ImagePlus segmentation = new ImagePlus("Processed Stack", segmentedStack);
        ImagePlus process = new ImagePlus("Process", processedStack);
        segmentation.show();
        process.show();

        return segmentation;
    }




}
