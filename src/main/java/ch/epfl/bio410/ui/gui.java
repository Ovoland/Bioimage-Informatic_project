package ch.epfl.bio410.ui;

import ij.gui.GenericDialog;

import java.io.File;

public class gui {
    private String[] methods = {"Skeleton Segmentation", "Omnipose Segmentation", "Simple Thresholding"};

    static public void createGUI(){
        //GUI
        GenericDialog gui = new GenericDialog("Tracking Bright Spots");

        String defaultPath = "C:"+ File.separator+"Users";
        gui.addFileField("Choose File",defaultPath);

        //gui.addChoice("Choose method of tracking",methods,methods[0]);
        gui.addMessage("**The Skeleton Segmentation is an approximated method");
        gui.addMessage("**Only use the Simple Thresholding method if the bacterias are spaced out enough**");
        gui.showDialog();

        String filePath = gui.getNextString();
        String method = gui.getNextChoice();
    }
}
