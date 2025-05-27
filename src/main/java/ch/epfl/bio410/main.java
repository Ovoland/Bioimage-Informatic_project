package ch.epfl.bio410;

import ch.epfl.bio410.graph.PartitionedGraph;
import ch.epfl.bio410.ui.GUI;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.ChannelSplitter;
import ij.process.ImageConverter;
import net.imagej.ImageJ;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import java.util.Objects;

import static ch.epfl.bio410.measurments.LocalMotionMeasurement.localMotionMeasurement;
import static ch.epfl.bio410.measurments.MotionMeasurement.motionMeasurment;
import static ch.epfl.bio410.segmentation.GetCentroid.getCentroid;
import static ch.epfl.bio410.segmentation.LevelSetSegmentation.levelSetSegmentation;
import static ch.epfl.bio410.tracking.ReplisomeTracking.replisomeTracking;
import static ch.epfl.bio410.ui.GUI.UserSelection;
import static ch.epfl.bio410.ui.GUI.showGUI;


/**
 */
@Plugin(type = Command.class, menuPath = "Plugins>BII>Replisome_tracking")
public class main implements Command {

	private String[] methods = {"Level Set", "Skeleton Segmentation"};

	@Override
	public void run() {
		GUI.UserSelection  = showGUI(methods);
		//Open the image based on the path given by the GUI
		//ImagePlus imp = IJ.getImage(filePath);
		ImagePlus imp = IJ.openImage(UserSelection.inputImagePath);
		imp.show();

		ImagePlus segmented;
		if(UserSelection.loadSegmentation){
			segmented = IJ.openImage(UserSelection.segmentedPath);
		}else{
			if(Objects.equals(UserSelection.method, methods[0])) {
				// Level Set Segmentation
				segmented = levelSetSegmentation(imp);
			} else if(Objects.equals(UserSelection.method, methods[1])) {
				// Skeleton Segmentation
				IJ.run(imp, "Skeletonize (2D/3D)", "");
				ImageConverter.setDoScaling(true);
				segmented = imp;
			} else {
				IJ.error("Unknown segmentation method: " + UserSelection.method);
				return;
			}
		}

		segmented.show();


		PartitionedGraph centroids = getCentroid(segmented);

		// Replisome Detection
		PartitionedGraph replisomes = replisomeTracking(imp);
		motionMeasurment(imp, replisomes, UserSelection.deltaT);

		int[] replisomeToShow = {1};
		localMotionMeasurement(replisomes, centroids,imp,replisomeToShow);
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