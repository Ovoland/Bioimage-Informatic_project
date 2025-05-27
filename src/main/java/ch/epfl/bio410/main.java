package ch.epfl.bio410;

import ch.epfl.bio410.graph.PartitionedGraph;
import ch.epfl.bio410.segmentation.segmentBacteriaTrad;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.ChannelSplitter;
import ij.process.ImageConverter;
import net.imagej.ImageJ;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import static ch.epfl.bio410.measurments.MotionMeasurement.motionMeasurement;
import static ch.epfl.bio410.segmentation.GetCentroid.getCentroid;
import static ch.epfl.bio410.segmentation.LevelSetSegmentation.levelSetSegmentation;
import static ch.epfl.bio410.tracking.ReplisomeTracking.replisomeTracking;


/**
 */
@Plugin(type = Command.class, menuPath = "Plugins>BII>Replisome_tracking")
public class main implements Command {

	private String[] methods = {"Skeleton Segmentation", "Omnipose Segmentation", "Simple Thresholding"};

	@Override
	public void run() {
		//Open the image based on the path given by the GUI
		ImagePlus imp = IJ.openImage("data/Merged-2_light.tif");
		imp.show();

		String segmentationPath = "data/Segmented/segmented_light.tif";
		ImagePlus segmented;
		boolean loadSegmentation = true;
		if(loadSegmentation){
			segmented = IJ.openImage(segmentationPath);
		}else{
			segmented =  levelSetSegmentation(imp);
		}

		segmented.show();


		PartitionedGraph centroid = getCentroid(segmented);
		centroid.drawCentroid(imp,2);

		// Repliosome Detection
		//PartitionedGraph trajectories = replisomeTracking(imp);
		//motionMeasurement(imp, trajectories);


		/*
		//Bacteria Segmentation
		if (method.equals(methods[0])) {
			segmentBacteriaTrad.BacteriaSegmentation(imp);
		} else if (method.equals(methods[1])) {
			//Omnipose
		} else if (method.equals(methods[2])) {
			LevelSetSegmentation.BacteriaSegmentation(imp);
		}
		*/
	}


	/**
	 * This main function serves for development purposes.
	 * It allows you to run the plugin immediately out of
	 * your integrated development environment (IDE).
	 *
	 * @param args whatever, it's ignored
	 * @throws Exception
	 */
	public static void main(final String... args) throws Exception {
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();
	}
}