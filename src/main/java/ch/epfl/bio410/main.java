package ch.epfl.bio410;

import ch.epfl.bio410.graph.PartitionedGraph;
import ch.epfl.bio410.graph.Spot;
import ch.epfl.bio410.graph.Spots;
import ch.epfl.bio410.utils.segmentBacteria;
import ch.epfl.bio410.utils.segmentBacteriaTrad;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import net.imagej.ImageJ;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import java.io.File;

import static ch.epfl.bio410.utils.motionMeasurement.measureMotion;
import static ch.epfl.bio410.utils.replisomeTracking.trackReplisome;

/**
 */
@Plugin(type = Command.class, menuPath = "Plugins>BII>Replisome_tracking")
public class main implements Command {

	private String[] methods = {"Skeleton Segmentation", "Omnipose Segmentation", "Simple Thresholding"};

	@Override
	public void run() {

		//GUI
		GenericDialog gui = new GenericDialog("Tracking Bright Spots");

		String defaultPath = "C:"+ File.separator+"Users";
		gui.addFileField("Choose File",defaultPath);

		gui.addChoice("Choose method of tracking",methods,methods[0]);
		gui.addMessage("**The Skeleton Segmentation is an approximated method**");
		gui.addMessage("**Only use the Simple Thresholding method if the bacterias are spaced out enough**");
		gui.showDialog();

		String filePath = gui.getNextString();
		String method = gui.getNextChoice();


		//Open the image based on the path given by the GUI
		ImagePlus imp = IJ.openImage(filePath);
		//ImagePlus imp = IJ.openImage("data/Merged-2_light.tif");
		imp.show();

		// Repliosome Detection
		PartitionedGraph trajectories = trackReplisome(imp.duplicate());
		measureMotion(imp.duplicate(), trajectories);


		/*
		//Bacteria Segmentation
		if (method.equals(methods[0])) {
			segmentBacteriaTrad.BacteriaSegmentation(imp);
		} else if (method.equals(methods[1])) {
			//Omnipose
		} else if (method.equals(methods[2])) {
			segmentBacteria.BacteriaSegmentation(imp);
		}
		*/
		segmentBacteriaTrad.BacteriaSegmentation(imp);
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