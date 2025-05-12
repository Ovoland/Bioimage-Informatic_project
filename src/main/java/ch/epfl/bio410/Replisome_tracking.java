package ch.epfl.bio410;

import ch.epfl.bio410.graph.PartitionedGraph;
import ij.IJ;
import ij.ImagePlus;
import net.imagej.ImageJ;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import static ch.epfl.bio410.utils.UtilsReplisomes.segmentReplisome;

/**
 */
@Plugin(type = Command.class, menuPath = "Plugins>BII>Replisome_tracking")
public class Replisome_tracking implements Command {

	@Override
	public void run() {
		
		//Open the image based on the path given by the GUI
		//ImagePlus imp = IJ.getImage();
		ImagePlus imp = IJ.openImage("data/light_Merged2.tif");
		imp.show();
		// Detection
		segmentReplisome(imp);

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