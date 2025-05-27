package ch.epfl.bio410.tracking;

import ch.epfl.bio410.cost.AbstractCost;
import ch.epfl.bio410.cost.DistanceAndIntensityCost;
import ch.epfl.bio410.graph.PartitionedGraph;
import ch.epfl.bio410.graph.Spot;
import ch.epfl.bio410.graph.Spots;
import ij.gui.GenericDialog;
import ij.plugin.GaussianBlur3D;
import ij.plugin.ImageCalculator;
import ij.process.ImageProcessor;
import net.imagej.ImageJ;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;
import ij.IJ;
import ij.ImagePlus;


/**
 */
@Plugin(type = Command.class, menuPath = "Plugins>BII>Tracking_Particles")
public class Tracking_Bright_Spots implements Command {

	private final double sigma = 6;  // Detection parameters, sigma of the DoG TODO : adapt it
	private final double threshold = 180;  // Detection parameters, threshold of the localmax TODO : adapt it
	private final double distmax = 20; 	// Cost parameters, maximum distance allowed to link spots together TODO : adapt it
	private final double costmax = 0.05;	// Cost parameters, maximum cost allowed to link spots togethers TODO : adapt it
	private final double lambda = 0.8; 	// Cost parameters, hyperparameter to balance cost function terms TODO : adapt it

	private static String inputFolder = "";

	@Override
	public void run() {


		// TODO question 6 - create a minimal GUI
		//Get help from this: https://github.com/SpimCat/postprocessing-utilities/blob/master/src/main/java/de/mpicbg/rhaase/spimcat/postprocessing/fijiplugins/projection/presentation/HalfStackProjectionOnFolderPlugin.java#L36-L55
		GenericDialog gd = new GenericDialog("Bright Spot tracking");

		gd.addDirectoryField("Image selection", inputFolder);

		gd.addNumericField("Sigma", 3, 0);
		gd.addNumericField("Threshold", 172, 0);
		gd.addNumericField("Distmax", 20, 0);
		gd.addNumericField("Costmax", 0.05, 3);
		gd.addNumericField("Lambda", 0.8, 2);


		//recover the values from the GUI and overshadow the global variables
		inputFolder = gd.getNextString();

		gd.showDialog();

		if (gd.wasCanceled()) {
			return;
		}

		double sigma = gd.getNextNumber();
		double threshold = gd.getNextNumber();
		double distmax = gd.getNextNumber();
		double costmax = gd.getNextNumber();
		double lambda = gd.getNextNumber();

		//Open the image based on the path given by the GUI
		//ImagePlus imp = IJ.getImage();
		ImagePlus imp = IJ.openImage(inputFolder);

		// Detection
		PartitionedGraph frames = detect(imp, sigma, threshold);
		frames.drawSpots(imp,5);

		// Create cost function TODO questions 2 & 5 - select one of cost function
		//AbstractCost cost = new SimpleDistanceCost(distmax);
		AbstractCost cost = new DistanceAndIntensityCost(imp, costmax, lambda);

		// Linking TODO questions 2 & 5 - select one of algorithm
		//PartitionedGraph trajectories = trackToFirstValidTrajectory(frames, cost);
		PartitionedGraph trajectories = trackToNearestTrajectory(frames, cost);
		trajectories.drawLines(imp);

		// TODO Bonus question - remove the comments
		double proximityDivision = 10; // Use to merge divided cells
		PartitionedGraph trajectoriesMerged = colorDivision(trajectories, proximityDivision);
		trajectoriesMerged.drawLines(imp);
	}

	/**
	 * This method allows to track single spots across frames.
	 * The algorithm is working by extending the current trajectories by
	 * appending the first valid spot of the next frame.
	 *
	 * @param frames Graph organized by partition of spots belonging to the same frame
	 * @param cost Cost function for the connection of spots
	 * @return Graph organized by partition of spots belonging to the same trajectory
	 */
	private PartitionedGraph trackToFirstValidTrajectory(PartitionedGraph frames, AbstractCost cost) {
		PartitionedGraph trajectories = new PartitionedGraph();
		for (Spots frame : frames) {
			for (Spot spot : frame) {
				Spots trajectory = trajectories.getPartitionOf(spot);
				if (trajectory == null) trajectory = trajectories.createPartition(spot);
				if (spot.equals(trajectory.last())) {
					int t0 = spot.t;
					for (int t=t0; t < frames.size() - 1; t++) {
						for(Spot next : frames.get(t+1)) {
							if (cost.validate(next, spot)) {
								IJ.log("#" + trajectories.size() + " spot " + next + " with a cost:" + cost.evaluate(next, spot));
								spot = next;
								trajectory.add(spot);
								break;
							}
						}
					}
				}
			}
		}
		return trajectories;
	}

	/**
	 * This method allows to track single spots across frames.
	 * The algorithm is working by extending the current trajectories by
	 * appending the nearest valid spot of the next frame.
	 *
	 * @param frames Graph organized by partition of spots belonging to the same frame
	 * @param cost Cost function for the connection of spots
	 * @return Graph organized by partition of spots belonging to the same trajectory
	 */
	private PartitionedGraph trackToNearestTrajectory(PartitionedGraph frames, AbstractCost cost) {
		PartitionedGraph trajectories = new PartitionedGraph();
		for (Spots frame : frames) {
			for (Spot spot : frame) {
				Spots trajectory = trajectories.getPartitionOf(spot);
				if (trajectory == null) trajectory = trajectories.createPartition(spot);
				if (spot.equals(trajectory.last())) {
					int t0 = spot.t;
					// TODO question 4 - add trajectory to the nearest spot of the next frame

					for (int t=t0; t < frames.size() - 1; t++) {
						//Let's keep track of the nearest Value and spot
						//Take the reference value of the first spot of the next frame
						double nearestValue = cost.evaluate(spot,frames.get(t+1).first());
						Spot nearestSpot = frames.get(t+1).first();
						//Determine if the next spot is probably missing
						boolean missingDot = true;

						for(Spot next : frames.get(t+1)) {
							double dist = cost.evaluate(spot, next);
							if(dist <= nearestValue && cost.validate(spot,next)){
								IJ.log("#" + trajectories.size() + " spot " + next + " with a cost:" + cost.evaluate(next, spot));
								nearestValue = dist;
								nearestSpot = next;
								//Indicates that at least one point has been found for the trajectory
								missingDot = false;
							}
						}
						//If no valid spot for the trajectory have been found, we don't add anything to the trajectory
						//If at least one valid spot have been found we add it to the trajectory
						if(!missingDot){
							trajectory.add(nearestSpot);
							spot = nearestSpot;
						}
					}
				}
			}
		}
		return trajectories;
	}

	/**
	 * TODO question 1 - fill the method description and input/output parameters
	 * This method allows bright spots detections on each frame of a given image
	 * The spot detection works by first enhancing spots of a given size using DoG filter
	 * then finding local maxima (spots) by applying a LocalMax operator
	 * The detected spots detected in each frame are then organised in a graph structure,
	 * where each trajectories contain a list of spot belonging to the same trajectory
	 *
	 * @param imp input image (with multiple frame) for which we want to detect the spots
	 * @param sigma standard deviation for Gaussian kernel used in the DoG filter. It impacts the smoothing and the size of the spot to be enhanced
	 * @param threshold value used for the LocalMax operator to indicate the min value for a local maxima
	 * @return Graph organized by partition of spots
	 */
	private PartitionedGraph detect(ImagePlus imp, double sigma, double threshold) {
		int nt = imp.getNFrames();
		new ImagePlus("DoG", dog(imp.getProcessor(), sigma)).show();
		PartitionedGraph graph = new PartitionedGraph();
		for (int t = 0; t < nt; t++) {
			imp.setPosition(1, 1, 1 + t);
			ImageProcessor ip = imp.getProcessor();
			ImageProcessor dog = dog(ip, sigma);
			Spots spots = localMax(dog, ip, t, threshold);
			IJ.log("Frame t:" + t + " #localmax:" + spots.size() );
			graph.add(spots);
		}
		return graph;
	}

	/**
	 * TODO question 1 - fill the method description and input/output parameters
	 * This method applies the Difference of Gaussian (DoG) filter to the given image
	 * The DoG filter consists of subtracting 2 gaussian-blurred images with different sigma values (sigma1 and sigma 2)
	 * The second sigma value can be computer from the first one as sqrt(2)*sigma1
	 *
	 * @param ip input frame image (as an ImageProcessor) whose bright spots will be enhanced by DoG filter
	 * @param sigma standard deviation for Gaussian kernel used in the DoG filter. It impacts the smoothing and the size of the spot to be enhanced
	 * @return input frame image after applying the DoG filter
	 */
	private ImageProcessor dog(ImageProcessor ip, double sigma) {
		ImagePlus g1 = new ImagePlus("g1", ip.duplicate());
		ImagePlus g2 = new ImagePlus("g2", ip.duplicate());
		double sigma2 = (Math.sqrt(2) * sigma);
		GaussianBlur3D.blur(g1, sigma, sigma, 0);
		GaussianBlur3D.blur(g2, sigma2, sigma2, 0);
		ImagePlus dog = ImageCalculator.run(g1, g2, "Subtract create stack");
		return dog.getProcessor();
	}

	/**
	 * TODO question 1 - fill the method description and input/output parameters
	 * This method find local max (bright spot) on the given input image
	 * The algorithm works by first localizing candidate pixel in the original image which has a value above the threshold
	 * Then for each candidates, it verifies if the corresponding pixel in the DoG image has the highest value among its 8-connected neighbors
	 * If that's the case, the candidate is stored as a localMax
	 *
	 * @param dog input frame image (as an ImageProcessor) which has undergone a DoG filter. Used for peaks detection
	 * @param image input frame original image used to apply the intensity threshold
	 * @param t id of the frame of the given images
	 * @param threshold value indicate the min value of the detected peaks
	 * @return a list of all the local maximal values/spots
	 */
	public Spots localMax(ImageProcessor dog, ImageProcessor image, int t, double threshold) {
		Spots spots = new Spots();
		for (int x = 1; x < dog.getWidth() - 1; x++) {
			for (int y = 1; y < dog.getHeight() - 1; y++) {
				double valueImage = image.getPixelValue(x, y);
				if (valueImage >= threshold) {
					double v = dog.getPixelValue(x, y);
					double max = -1;
					for (int k = -1; k <= 1; k++)
						for (int l = -1; l <= 1; l++)
							max = Math.max(max, dog.getPixelValue(x + k, y + l));
					if (v == max) spots.add(new Spot(x, y, t, valueImage));
				}
			}
		}
		return spots;
	}

	/**
	 * This method changes the color such a way that the trajectory color
	 * of the mother and the two daughter cells are the same.
	 *
	 * @param input the partitioned graph where each partition contains the spots per trajectory
	 * @param proximityDivision threshold on the distance between spots above which
	 *                             the two spots are considered too far to have a parent link.
	 * @return the updated partitioned graph with the right colors
	 */
	public PartitionedGraph colorDivision(PartitionedGraph input, double proximityDivision) {
		PartitionedGraph out = new PartitionedGraph();
		for(Spots trajectory : input) {
			/*
				TODO Bonus question - add your code to assign the same color to the mother and daughter cells
			 */
			for(Spots nextTrajectory: input){
				if(trajectory != nextTrajectory){
					for (Spot spot : trajectory) {
						for (Spot nextSpot : nextTrajectory) {
							//Find at least one spot of another trajectory that is close enough
							if (spot.distance(nextSpot) < proximityDivision) {
								//Set similar color for close trajectories
								nextTrajectory.color = trajectory.color;
								break;
							}

						}
					}
				}
			}
			//Add each trajectory with the correct color
			out.add(trajectory);
		}
		return out;
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