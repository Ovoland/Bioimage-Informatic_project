package ch.epfl.bio410.graph;


import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Class implementing a "Spots" object. A "Spots" object is a list of "Spot" objects, with additional methods
 * to retrieve easily the first and the last the "Spot" of the list.
 */
public class Spots extends LinkedHashMap<Integer,Spot> {

    public Color color = Color.black;

    /**
     * Constructor of the class = mandatory method to build and initialize the "Spots" object
     */
    public Spots() {
        color = Color.getHSBColor((float) Math.random(), 1f, 1f);
        color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 120);
    }

    public void add(Spot spot) {
        this.put(spot.t, spot);
    }

    public Spot getSpot(int t) {
        return this.get(t);
    }

    public Spot last(){
        return this.get(getLastKey());
    }

    public Spot first(){
        return this.get(getFirstKey());
    }

    private Integer getFirstKey(){
        if(this.isEmpty()) return null;
        ArrayList<Integer> keys = new ArrayList<>(this.keySet());
        keys.sort(Comparator.naturalOrder());
        return keys.get(0);
    }

    private Integer getLastKey(){
        if(this.isEmpty()) return null;
        ArrayList<Integer> keys = new ArrayList<>(this.keySet());
        keys.sort(Comparator.naturalOrder());
        return keys.get(keys.size() -1);
    }


}
