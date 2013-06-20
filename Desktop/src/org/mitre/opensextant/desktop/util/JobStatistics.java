/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mitre.opensextant.desktop.util;

import gate.Document;
import java.util.ArrayList;
import org.mitre.giscore.events.Feature;
import org.mitre.opensextant.processing.Geocoding;
import org.mitre.opensextant.processing.OpenSextantSchema;
import org.mitre.opensextant.processing.output.result.IdentifierResult;

/**
 *
 * @author GBLACK
 */
public class JobStatistics {
    public static final int COORDINATE = 0;
    public static final int PLACE = 1;
    public static final int COUNTRY = 2;
    
    private int objCount = 0;
    private ArrayList<Geocoding>[] geos = new ArrayList[COUNTRY + 1];
    
    
    public void addGeo(Geocoding g, int arrayLoc) {
        ArrayList<Geocoding> x = geos[arrayLoc];
        if(x == null) x = new ArrayList<Geocoding>();
        x.add(g);
    }
    
    public ArrayList<Geocoding> getGeo(int arrayLoc) {
        return geos[arrayLoc];
    }
    
    public void incrementObjCount() {
        objCount ++;
    }

    
}
