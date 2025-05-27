package ch.epfl.bio410.graph;

/**
 * Class implementing a "Spot" object, with different attributes
 */
public class Spot {
	public int x;
	public int y;
	public int t;
	public double value = 0;

	/**
	 * Constructor of the class = mandatory method to build and initialize the "Spot" object
	 */
	public Spot(int x, int y, int t, double value) {
		this.x = x;
		this.y = y;
		this.t = t;
		this.value = value;
	}

	public double distance(Spot spot){
		return Math.sqrt(Math.pow(this.x - spot.x, 2) + Math.pow(this.y - spot.y, 2));
	}

	public double distanceU(Spot spot, double pixelWidth, double pixelHeight){
		return Math.sqrt(Math.pow((this.x - spot.x)*pixelWidth, 2) + Math.pow((this.y - spot.y)*pixelHeight, 2));
	}

	public double distanceX(Spot spot){
		return (this.x - spot.x);
	}
	public double distanceY(Spot spot){
		return (this.y - spot.y);
	}
}