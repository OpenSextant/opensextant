/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mitre.opensextant.desktop.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.mitre.opensextant.processing.Geocoding;

/**
 *
 * @author GBLACK
 */
public class JobStatistics {
    public static final int COORDINATE = 0;
    public static final int PLACE = 1;
    public static final int COUNTRY = 2;
    
    private int objCount = 0;
    private ArrayList<String>[] geos = new ArrayList[COUNTRY + 1];
    private HashMap[] geoHash = new HashMap[COUNTRY + 1];
    
    public void addGeo(Geocoding g, int arrayLoc) {
        String s = g.toString();
        if(s == null) return;
        int posCoord = s.indexOf(" @(");
        if(posCoord >= 0) s = s.substring(0, posCoord);
        if(geoHash[arrayLoc] == null) geoHash[arrayLoc] = new HashMap();
        
        if(geoHash[arrayLoc].containsKey(s))
        {
            int count = (int)geoHash[arrayLoc].get(s);
            geoHash[arrayLoc].put(s, ++count);
        }
        else
            geoHash[arrayLoc].put(s, 1);
    }
    
    public Set<String> getGeo(int arrayLoc) {
        return geoHash[arrayLoc].keySet();
    }
    
    public int getCount(int arrayLoc, String val){
        return (int)geoHash[arrayLoc].get(val);
    }
    
    public int getCount(int arrayLoc){
        if(geoHash[arrayLoc] == null) return 0;
        return geoHash[arrayLoc].size();
    }
    
    public void incrementObjCount() {
        objCount ++;
    }
    
    public int getObjCount() {
        return objCount;
    }
}
