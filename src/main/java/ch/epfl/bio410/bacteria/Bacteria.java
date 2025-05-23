package ch.epfl.bio410.bacteria;



public class Bacteria {
    public double xCentroid;
    public double yCentroid;

    //Bounding boxes attributes
    public double xBB;
    public double yBB;
    public double wBB;
    public double hBB;

    public Bacteria(double xCentroid, double yCentroid, double xBB, double yBB, double wBB, double hBB) {
        this.xCentroid = xCentroid;
        this.yCentroid = yCentroid;

        this.xBB = xBB;
        this.yBB = yBB;
        this.wBB = wBB;
        this.hBB = hBB;
    }
}

