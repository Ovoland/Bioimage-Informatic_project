package ch.epfl.bio410.ui;

import ij.gui.GenericDialog;

import java.io.File;

public class GUI {

    public static UserSelection UserSelection;

    public static class UserSelection {
        public final String inputImagePath;
        public final boolean loadSegmentation;
        public final String segmentedPath;
        public final String method;
        public final double deltaT;

        public UserSelection(String inputImagePath,
                             boolean loadSegmentation,
                             String segmentedPath,
                             String method,
                             double deltaT)  // Added deltaT as a parameter
        {
            this.inputImagePath  = inputImagePath;
            this.loadSegmentation = loadSegmentation;
            this.segmentedPath   = segmentedPath;
            this.method          = method;
            this.deltaT         = deltaT;
        }
    }

    public static UserSelection showGUI(String[] methods){

		//GUI
		GenericDialog gui = new GenericDialog("Tracking Bright Spots");

		String defaultPath = "C:"+ File.separator+"Users";
        gui.addFileField("Choose the path of the image you want to process",defaultPath);
        gui.addNumericField("Indicate the time (sec) between two consecutive frame", 120,3 );

        gui.addCheckbox("Do you want to load a pre-segmented image?", false);
        gui.addMessage("Preload an already segmented image allow to skip the segmentation step and gain time");
        gui.addMessage("The segmented image must be a binary image of our original image");

        gui.showDialog();

        String inputImagePath = gui.getNextString();
        double delatT = gui.getNextNumber();
        boolean loadSegmentation = gui.getNextBoolean();

        if(loadSegmentation){
            GenericDialog gui2 = new GenericDialog("Tracking Bright Spots");

            gui2.addFileField("Choose the path of your segmented image",defaultPath);
            gui2.showDialog();

            String segmentedPath = gui2.getNextString();

            return new UserSelection(inputImagePath, loadSegmentation, segmentedPath, methods[0],delatT);

        }else{
            GenericDialog gui3 = new GenericDialog("Tracking Bright Spots");

            gui3.addChoice("Choose method for the segmentation",methods,methods[0]);

            gui3.addMessage("**We recommend using the Level Set method for the segmentation as it is the most accurate method**");

            //gui.addMessage("**The Skeleton Segmentation is an approximated method");
            //gui.addMessage("**Only use the Simple Thresholding method if the bacterias are spaced out enough**");

            gui3.showDialog();
            String method = gui3.getNextChoice();

            return new UserSelection(inputImagePath, loadSegmentation, "", method,delatT);
        }

    }
}
