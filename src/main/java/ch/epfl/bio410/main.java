package ch.epfl.bio410;

import ch.epfl.bio410.graph.PartitionedGraph;
import ch.epfl.bio410.graph.Spot;
import ch.epfl.bio410.graph.Spots;
import ch.epfl.bio410.utils.segmentBacteria;
import ij.IJ;
import ij.ImagePlus;
import net.imagej.ImageJ;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import static ch.epfl.bio410.utils.motionMeasurement.measureMotion;
import static ch.epfl.bio410.utils.replisomeTracking.trackReplisome;

/**
 */
@Plugin(type = Command.class, menuPath = "Plugins>BII>Replisome_tracking")
public class main implements Command {

	@Override
	public void run() {
		
		//Open the image based on the path given by the GUI
		//ImagePlus imp = IJ.getImage();
		ImagePlus imp = IJ.openImage("data/light_merged2_late.tif");
		imp.show();
		// Detection
		PartitionedGraph trajectories = trackReplisome(imp);
		measureMotion(imp, trajectories);
		segmentBacteria.BacteriaSegmentation(imp);
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